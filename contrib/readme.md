# contrib guide

* use the provided docker-compose file to setup a local mongo db
```bash
docker-compose up -d
```
* use the provided BibliothekApplication run config to start the application
* connect to the db using intellij (`mongodb://localhost:27017`)
* prepare the cli
```bash
(cd ../cli && yarn)
```
* seed the db by running the cli
```bash
node ../cli/insertBuild.js --projectName paper --projectFriendlyName Paper --versionGroupName 1.20 --versionName 1.20 --buildNumber 1 --repositoryPath ../ --storagePath ../work --download application:empty.jar:e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855
```
* test the app
```bash
curl http://localhost:8080/v2/projects/paper/versions/1.20/builds/1
```
