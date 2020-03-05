# CPCDS Reference Server

This project is a reference FHIR server for the [Consumer-Directed Payer Data Exchange Implementation Guide](https://build.fhir.org/ig/HL7/carin-bb/toc.html) (also know as Carin Blue Button Implementation Guide). It is based on the [HAPI FHIR JPA Server](https://github.com/hapifhir/hapi-fhir-jpaserver-starter).

## Quickstart

The quickest way to get the server up and running is by pulling the built image from docker hub.

```bash
docker pull blangley/cpcds-server-ri
docker run -p 8080:8080 blangley/cpcds-server-ri
```

This will deploy the server to http://localhost:8080/fhir.

## Building Locally with Docker

To start the server simply build and run using docker. The container will automatically build and deploy using a tomcat server.

```bash
git clone https://github.com/carin-alliance/cpcds-server-ri.git
cd cpcds-server-ri
docker build -t cpcds-server-ri .
docker run -p 8080:8080 cpcds-server-ri
```

This will build a read only version of the server with the test data pre-loaded. The server will then be browesable at http://localhost:8080/ and the FHIR endpoint will be available at http://localhost:8080/fhir

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

This server is protected and requires users to authenticate before obtaining access to protected resources. The reference authorization server for this RI is the [CPCDS Auth Server](https://github.com/carin-alliance/cpcds-auth-server). Follow direction on the README to get the authorization server up and running. Instructions on how to obtain an access token can be found on the same README.

Once an access token is received it must be sent in the `Authorization` header using the correct `token_type` returned by the auth server. Using the default secret of "secret" an admin key valid until Jan 1, 2021 (for testing purposes) is:

```
eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJpc3MiOiJodHRwOi8vbG9jYWxob3N0OjgxODAiLCJleHAiOjE2MDk0NzcyMDAsImlhdCI6MTU4MzM0Njg1MywiY2xpZW50X2lkIjoiYWRtaW4ifQ.z1OVJO8HlFr87gtoPPGxSzHCf4O63LxYuE4PNzPRmxg
```

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

Note: The master branch of this repository sets up the server as read only. Uploading to the server will fail. To disable the read only interceptor switch to the `cpcds-write` branch.
