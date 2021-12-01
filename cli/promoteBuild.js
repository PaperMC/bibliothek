const yargs = require("yargs");
const {MongoClient} = require("mongodb");
const ObjectId = require("mongodb").ObjectId;

const argv = yargs
  .option("build", optionOf("string"))
  .option("promoted", optionOf("boolean"))
  .help()
  .alias("help", "h")
  .version(false)
  .argv;

const client = new MongoClient("mongodb://localhost:27017", {
  useUnifiedTopology: true
});

async function run() {
  try {
    await client.connect();
    const database = client.db("library");

    const build = await database.collection("builds").findOne({
      _id: ObjectId(argv.build)
    });

    if (!build) {
      console.log(`Build not found with ID ${argv.build}`);
      return;
    }

    await database.collection("builds").updateOne(
      {
        _id: ObjectId(argv.build)
      },
      {
        $set: {
          promoted: argv.promoted === true
        }
      }
    );

    console.log(`Build ${argv.build} set promoted to ${argv.promoted}`);
  } finally {
    await client.close();
  }
}

run().catch(console.dir);

function optionOf(type) {
  return {
    type,
    required: true
  };
}
