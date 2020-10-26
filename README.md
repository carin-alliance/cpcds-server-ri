# CPCDS Reference Server

This is the write branch of the C4BB (CPCDS) Server RI. It allows data to be uploaded to the server.

## Manually Running

Clone the repo and build the server:

```bash
git clone https://github.com/carin-alliance/cpcds-server-ri.git
cd cpcds-server-ri
mvn dependency:resolve
mvn jetty:run
```

The server will then be browesable at http://localhost:8080/cpcds-server and the FHIR endpoint will be available at http://localhost:8080/cpcds-server/fhir

Note: This has only been tested using Java 8. Later version may not be supported.

## Uploading Test Data

The Ruby script `upload.rb` uploads test data into the FHIR server. The easiest way to run is with [Bundler](https://bundler.io/). To install Bundler

```bash
sudo gem install bundler
```

Then execute the upload script

```bash
bundle install
bundle exec ruby upload.rb {FHIR Server}
```

By default the upload script will use http://localhost:8080/cpcds-server/fhir as the FHIR server. To upload to a different endpoint provide the full URL as the first argument.

To clear the database delete the directory `target/database` and rerun the server.

Note: The upload script will only upload Patient, Claim, and ExplanationOfBenefit resources.

## Steps

1. Usually, you will want to remove all the data from the server with `rm -rf target` so that the server starts with a blank slate. Skip this step if you want to retain the existing data.
1. Start the server with `mvn jetty:run`. Note you will need to use JDK8 for this.
1. In a separate terminal, run `bundle exec ruby upload.rb` to upload data to the server. The resources from `CPCDS_patient_data` and the example resources from the IG will be uploaded.
1. Once the upload has completed, use `CTRL+c` to stop the server.
1. Now it is necessary to copy this data into the master branch. Change to the master branch: `git checkout master`.
1. Remove the old database `rm -f data/h2.mv.db`.
1. Move the new database to the correct location `mv target/database/h2.mv.db data/h2.mv.db`.
1. Stage the new database with `git add data`.
1. Commit the database with `git commit -m 'update data'`.
1. Push up your changes with `git push`.
1. Now the AWS instance needs to be updated. Build the docker image `docker build -t blangley/cpcds-server-ri .`.
1. Push the new image to dockerhub with `docker push blangley/cpcds-server-ri`.
1. Now connect to the AWS instance `ssh -i {pem file} ubuntu@ec2-18-217-72-168.us-east-2.compute.amazonaws.com `.
1. Stop the running image ``.
1. Pull the newly created image `sudo docker pull blangley/cpcds-server-ri`.
1. Run the new docker image `sudo docker run -d -p 8080:8080 blangley/cpcds-server-ri`.
