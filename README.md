# CPCDS Reference Server

This project is a reference FHIR server for the [Consumer-Directed Payer Data Exchange Implementation Guide](https://build.fhir.org/ig/HL7/carin-bb/toc.html) (also know as Carin Blue Button Implementation Guide). It is based on the [HAPI FHIR JPA Server](https://github.com/hapifhir/hapi-fhir-jpaserver-starter).

## Quickstart

The quickest way to get the server up and running is by pulling the built image from docker hub.

```bash
docker pull blangley/cpcds-server-ri
docker run -p 8080:8080 blangley/cpcds-server-ri
```

This will deploy the server to http://localhost:8080/cpcds-server/fhir.

## Building Locally with Docker

To start the server simply build and run using docker. The container will automatically build and deploy using a tomcat server.

```bash
git clone https://github.com/carin-alliance/cpcds-server-ri.git
cd cpcds-server-ri
docker build -t cpcds-server-ri .
docker run -p 8080:8080 cpcds-server-ri
```

This will build a read only version of the server with the test data pre-loaded. The server will then be browesable at http://localhost:8080/cpcds-server and the FHIR endpoint will be available at http://localhost:8080/cpcds-server/fhir

## Manual Build and Run

Clone the repo and build the server:

```bash
git clone https://github.com/carin-alliance/cpcds-server-ri.git
cd cpcds-server-ri
mvn dependency:resolve
mvn jetty:run
```

The server will then be browesable at http://localhost:8080/cpcds-server and the FHIR endpoint will be available at http://localhost:8080/cpcds-server/fhir

Note: This has only been tested using Java 8. Later version may not be supported.

## GET Requests

This server is protected and requires users to authenticate before obtaining access to protected resources. This RI uses an Okta OAuth2 Server. The root address of the server is https://dev-610235.okta.com/oauth2/aus41l4rzyXcafD534x6/ and following the OAuth protocol a list of the endpoints can be found at https://dev-610235.okta.com/oauth2/aus41l4rzyXcafD534x6/.well-known/oauth-authorization-server.

To authenticate with the server use the dummy patient `synthea.patient@example.com` with password `Password1`. The patientId for Synthea Patient is `1`.

The server will then return a token which can be used in the Authorization header (type Bearer token) to make GET requests.

Note: For testing purposes hard coded values of the code*challenge and code_verifier are `qjrzSW9gMiUgpUvqgEPE4*-8swvyCtfOVvg55o5S_es`and`M25iVXpKU3puUjFaYWg3T1NDTDQtcW1ROUY5YXlwalNoc0hhakxifmZHag` respectively.

The admin of the authorization server is [blangley@mitre.org](mailto:blangley@mitre.org).

## Configuration

If the address of CPCDS server or authorization server change they must be updated in `src/main/resources/hapi.properties` by modifying the `server_address` and `auth_server_address` properties respectivelty.

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

Note: The master branch of this repository sets up the server as read only. Uploading to the server will fail. To disable the read only interceptor switch to the `cpcds-write` branch. The database is read from `target/database/h2.mv.db`. A copy of the database with the test data preloaded can be found in the `/data` directory. Copying this into `target/database` will allow the server to have a read only copy of the loaded database.
