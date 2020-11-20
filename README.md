# CPCDS Reference Server

This project is a reference FHIR server for the [Consumer-Directed Payer Data Exchange Implementation Guide](https://build.fhir.org/ig/HL7/carin-bb/toc.html) (also know as Carin Blue Button Implementation Guide). It is based on the [HAPI FHIR JPA Server](https://github.com/hapifhir/hapi-fhir-jpaserver-starter).

The server is hosted live at http://cpcds-ri.org/cpcds-server/fhir

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

The server will then be browseable at http://localhost:8080/cpcds-server and the FHIR endpoint will be available at http://localhost:8080/cpcds-server/fhir

Note: This has only been tested using Java 8. Later version may not be supported.

## GET Requests

This server is protected and requires users to authenticate before obtaining access to protected resources. The reference authorization server for this RI is the [CPCDS Auth Server](https://github.com/carin-alliance/cpcds-auth-server). Follow direction on the README to get the authorization server up and running. Instructions on how to obtain an access token can be found on the same README.

Once an access token is received it must be sent in the `Authorization` header using the correct `token_type` returned by the auth server.

## Configuration

If the address of CPCDS server or authorization server change they must be updated in `src/main/resources/hapi.properties` by modifying the `server_address` and `auth_server_address` properties respectivelty.

## Uploading Test Data

The master branch of this repository sets up the server as read only. Uploading to the server will fail. To disable the read only interceptor switch to the `cpcds-write` branch. The database is read from `target/database/h2.mv.db`. A copy of the database with the test data preloaded can be found in the `/data` directory. Copying this into `target/database` will allow the server to have a read only copy of the loaded database.
