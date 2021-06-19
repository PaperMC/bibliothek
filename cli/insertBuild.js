const fs = require("fs");
const gitlog = require("gitlog").default;
const {MongoClient} = require("mongodb");
const path = require("path");
const yargs = require("yargs");

const argv = yargs
  .option("projectName", requiredOptionOf("string"))
  .option("projectFriendlyName", requiredOptionOf("string"))
  .option("versionGroupName", requiredOptionOf("string"))
  .option("versionName", requiredOptionOf("string"))
  .option("buildNumber", requiredOptionOf("number"))
  .option("repositoryPath", requiredOptionOf("string"))
  .option("storagePath", requiredOptionOf("string"))
  .option("download", requiredOptionOf("string"))
  .help()
  .alias("help", "h")
  .version(false)
  .argv;

const projectName = argv.projectName;
const projectFriendlyName = argv.projectFriendlyName;
const versionGroupName = argv.versionGroupName;
const versionName = argv.versionName;
const buildNumber = argv.buildNumber;
const repositoryPath = argv.repositoryPath;
const storagePath = argv.storagePath;
// type:path:hash:name
let downloads = argv.download;

if(typeof downloads === "string") {
  const tempDownloads = downloads;
  downloads = [tempDownloads];
}

// Validate downloads
let foundPrimary = false;
for(let download of downloads) {
  const info = download.split(":");
  if(info.length === 3) {
    if(foundPrimary === true) {
      console.log("Too many primary files.");
      return;
    } else {
      foundPrimary = true;
    }
  }
}

// ----------------------------------------------------------------------------------------------------

const downloadsPath = path.join(
  storagePath,
  projectName,
  versionName,
  buildNumber.toString()
);

if(!fs.existsSync(downloadsPath)) {
  fs.mkdirSync(downloadsPath, {
    recursive: true
  });
}

for(let download of downloads) {
  const info = download.split(":");
  if(info.length === 3) {
    const downloadPath = path.join(
      downloadsPath,
      projectName + "-" + versionName + "-" + buildNumber + ".jar"
    );
    fs.copyFileSync(info[1], downloadPath);
  } else if(info.length === 4) {
    const downloadPath = path.join(
      downloadsPath,
      info[3]
    );
    fs.copyFileSync(info[1], downloadPath);
  }
}

const client = new MongoClient("mongodb://localhost:27017", {
  useUnifiedTopology: true
});

async function run() {
  try {
    await client.connect();
    const database = client.db("library"); // "library" instead of "bibliothek" is intentional here
    const project = await database.collection("projects").findOneAndUpdate(
      {"name": projectName},
      {
        $setOnInsert: {
          "name": projectName,
          "friendlyName": projectFriendlyName
        }
      },
      {
        new: true,
        returnDocument: "after",
        upsert: true
      }
    );
    const versionGroup = await database.collection("version_groups").findOneAndUpdate(
      {
        "project": project.value._id,
        "name": versionGroupName
      },
      {
        $setOnInsert: {
          "project": project.value._id,
          "name": versionGroupName
        }
      },
      {
        new: true,
        returnDocument: "after",
        upsert: true
      }
    );
    const version = await database.collection("versions").findOneAndUpdate(
      {
        "project": project.value._id,
        "name": versionName
      },
      {
        $setOnInsert: {
          "project": project.value._id,
          "group": versionGroup.value._id,
          "name": versionName
        }
      },
      {
        new: true,
        returnDocument: "after",
        upsert: true
      }
    );
    const oldBuild = await database.collection("builds").findOne({
      "project": project.value._id,
      "version": version.value._id
    }, {sort: {_id: -1}});
    let changes = [];
    const lastBuild = oldBuild && oldBuild.changes.length ? oldBuild.changes.slice(0, 1)[0].commit : "HEAD^1";
    const commits = gitlog({
      repo: repositoryPath,
      fields: ["hash", "subject", "rawBody"],
      branch: lastBuild + "...HEAD"
    });
    commits.forEach(function (commit) {
      changes.push({
        "commit": commit.hash,
        "summary": commit.subject,
        "message": commit.rawBody
      });
    });
    const buildDownloads = {};
    for(let download of downloads) {
      const info = download.split(":");
      if(info.length === 3) {
        buildDownloads[info[0].replace(".", ":")] = {
          "name": projectName + "-" + versionName + "-" + buildNumber + ".jar",
          "sha256": info[2]
        };
      } else if(info.length === 4) {
        buildDownloads[info[0].replace(".", ":")] = {
          "name": info[3],
          "sha256": info[2]
        };
      }
    }
    const build = await database.collection("builds").insertOne({
      "project": project.value._id,
      "version": version.value._id,
      "number": buildNumber,
      "time": new Date(),
      "changes": changes,
      "downloads": buildDownloads
    });
    console.log("Inserted build " + buildNumber + " for project " + project.value.name + " (" + project.value._id + ") version " + version.value.name + " (" + version.value._id + "): " + build.insertedId);
  } finally {
    await client.close();
  }
}

run().catch(console.dir);

function requiredOptionOf(type) {
  return {
    type: type,
    required: true
  };
}
