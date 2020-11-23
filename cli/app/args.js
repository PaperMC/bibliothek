
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

module.exports = {
  projectName: argv.projectName,
  projectFriendlyName: argv.projectFriendlyName,
  versionGroupName: argv.versionGroupName,
  versionName: argv.versionName,
  buildNumber: argv.buildNumber,
  repositoryPath: argv.repositoryPath,
  storagePath: argv.storagePath,
  downloads: argv.download
};

function requiredOptionOf(type) {
  return {
    type: type,
    required: true
  };
}
