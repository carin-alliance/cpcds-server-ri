# CPCDS Reference Server

This project is a reference FHIR server for the [Consumer-Directed Payer Data Exchange Implementation Guide](https://build.fhir.org/ig/HL7/carin-bb/toc.html) (also know as Carin Blue Button Implementation Guide). It is based on the [HAPI FHIR JPA Server](https://github.com/hapifhir/hapi-fhir-jpaserver-starter).

## Quickstart

To start the server simply build and run using docker. The container will automatically build and deploy using a tomcat server.

```bash
git clone https://github.com/carin-alliance/cpcds-server-ri.git
cd cpcds-server-ri
docker build -t cpcds-server .
docker run -p 8080:8080 cpcds-server
```

This will build a read only version of the server with the test data pre-loaded. The server will then be browesable at http://localhost:8080/ and the FHIR endpoint will be available at http://localhost:8080/fhir

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

## GET Requests

The server uses JWT tokens to authenticate the user on the server. At this point the payload is insecure (will be changing). The tokens are signed using HMAC SHA256 with "secret" as the secret. For production versions the secret should be 64 bits and obviously kept secret. The server expects the `iss` claim to be `cpcds-server-ri`. The `patient.id` is encoded in the `patient` claim as a string. The JWT tokens for a few test patients is provided below.

| Patient ID | JWT token                                                                                                                                                         |
| ---------- | :---------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| 1          | eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJjcGNkcy1zZXJ2ZXItcmkiLCJwYXRpZW50IjoiMSIsImlhdCI6MTU4MjA1NDEzNX0.soTbe6tuu0pkNUkmLZJ24dLH9KvSunLkWxul07mV4a0      |
| 689        | eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJjcGNkcy1zZXJ2ZXItcmkiLCJwYXRpZW50IjoiNjg5IiwiaWF0IjoxNTgyMDU0MTM1fQ.6Xgy_d5MBi316cSEmqmJJY_s065uClmcjnNePnQkZuk   |
| admin      | eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJjcGNkcy1zZXJ2ZXItcmkiLCJwYXRpZW50IjoiYWRtaW4iLCJpYXQiOjE1ODIwNTQxMzV9.nUZKr9WUMXPG2v3iDSbz03fcZJm41nUHIiNmL80PJh0 |

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
