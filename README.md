# CPCDS Reference Server

This project is a reference FHIR server for the [Consumer-Directed Payer Data Exchange Implementation Guide](https://build.fhir.org/ig/HL7/carin-bb/toc.html) (also know as Carin Blue Button Implementation Guide). It is based on the [HAPI FHIR JPA Server](https://github.com/hapifhir/hapi-fhir-jpaserver-starter) (HAPI 4.1.0).

The server is hosted live at https://cpcds-server.lantanagroup.com/fhir.
For more information on connecting visit the [Connectathon Wiki](https://github.com/carin-alliance/cpcds-server-ri/wiki/Connectathon-README)
For more information on the AWS server visit the [wiki](https://github.com/carin-alliance/cpcds-server-ri/wiki/AWS-Reference-Implementation).

## Quickstart 

The quickest way to get the server up and running is by pulling the built image from docker hub.

```bash
docker pull lantanagroup/cpcds-server-ri
docker run -p 8080:8080 -e SERVER_ADDRESS=http://localhost:8080/fhir lantanagroup/cpcds-server-ri
```

This will deploy the server to http://localhost:8080/fhir

Note: A docker-compose file is also included which can be configured for your use case. To run using compose use `docker-compose up`.

## Building Locally with Docker

The docker image can also be built locally before running. The container will automatically build and deploy using a tomcat server. You may need to configure some settings in `docker-compose.yml` before running.

```bash
git clone https://github.com/carin-alliance/cpcds-server-ri.git
cd cpcds-server-ri
./build-docker-image.sh
docker-compose up
```

Alternatively you can build and run using normal docker commands:

```bash
docker build -t cpcds-server-ri .
docker run -p 8080:8080 -e SERVER_ADDRESS=http://localhost:8080/fhir cpcds-server-ri
```

This will build a read only version of the server with the test data pre-loaded. The server will then be browesable at http://localhost:8080/cpcds-server and the FHIR endpoint will be available at http://localhost:8080/fhir

## Manual Build and Run

Clone the repo and build the server:

```bash
git clone https://github.com/carin-alliance/cpcds-server-ri.git
cd cpcds-server-ri
export SERVER_ADDRESS=http://localhost:8080/fhir
mvn dependency:resolve
mvn jetty:run
```

The server will then be browseable at http://localhost:8080/ and the FHIR endpoint will be available at http://localhost:8080/fhir/

Note: this has only been tested with Java 8, if you are using a different version of Java and experience issues try switching to Java 8.
Note: a common error is about `.m2/repositories/com/h2database`. This is most likely due to running the server with a different version of Java. If you encounter this issue verify you are using Java 8, delete the `h2database` folder and run the server again.

## GET Requests

This server is protected and requires users to authenticate before obtaining access to protected resources. The authorization server is hosted at the `/oauth` endpoint. The Capability Statement for this server is at the `/fhir/metadata` endpoint. The smart configuration file is at `fhir/.well-known/smart-configuration`. Even with an access token, only the resources marked as must support in the IG will be available.

Once an access token is received it must be sent in the `Authorization` header using the correct `token_type` returned by the auth server. Learn more about the server authorization from the [wiki](https://github.com/carin-alliance/cpcds-server-ri/wiki)

## Configuration

Beyond on the normal HAPI configuration found in `src/main/resources/hapi.properties`, this server has a few other configurations.

| ENV            | Required | Description                                                                                           |
| -------------- | -------- | ----------------------------------------------------------------------------------------------------- |
| SERVER_ADDRESS | Yes      | The base url for this server. For localhost this should be `http://localhost:8080/fhir`               |
| ADMIN_TOKEN    | No       | The value of the admin token to bypass authorization. If unset no admin token can be used.            |

## Uploading Test Data

The master branch of this repository sets up the server as read only. Uploading to the server will fail. To disable the read only interceptor switch to the `cpcds-write` branch. The database is read from `target/database/h2.mv.db`. A copy of the database with the test data preloaded can be found in the `/data` directory. Copying this into `target/database` will allow the server to have a read only copy of the loaded database.

## Security

Since this code base serves as the reference implementation for the Carin BB IG there are multiple places where potential security vulnerabilities were intentionally made to allow testing developers to debug their code. With these vulnerabilities in place, testing and debugging connections is substantially easier. If this code is to be used for a production enviornment care must be taken to ensure all vulnerabilities are fixed. Below is a list of _some_ of the identified issues. It is your responsibility to make sure all vulnerabilities, including those not listed below, are fixed in a production enviornment.

1. Logger statements print secrets - in places such as `User.java` and `Client.java` the logger displays the hashed password and client secret. Caution should be used any time a secret value is logged. Care should be taken to protect the log files from malicious users.
2. Debug endpoint - the debug endpoint provides public access to the Users and Client table which provides hashed passwords and client secrets. This endpoint also provides public access to the log file. The debug endpoint should be removed for a production enviornment.
3. Managing keys - the RSA keys used to sign and validate the JWT tokens are hard coded in `App.java`. Your implementation must change these keys and ensure they are stored in a secure location. Consider having rotating keys.

This may not be an exhaustive list. The developers of this reference implementations are not responsible for any vulnerabilities in the code base. All use of this repository comes with the understanding this reference implementation is used for testing connections only.


## Questions and Contributions
Questions about the project can be asked in the [CARIN BlueButton stream on the FHIR Zulip Chat](https://chat.fhir.org/#narrow/stream/204607-CARIN-IG-for-Blue-Button.C2.AE).

This project welcomes Pull Requests. Any issues identified with the RI should be submitted via the [GitHub issue tracker](https://github.com/carin-alliance/cpcds-server-ri/issues).

As of October 1, 2022, The Lantana Consulting Group is responsible for the management and maintenance of this Reference Implementation.
In addition to posting on FHIR Zulip Chat channel mentioned above you can contact [Corey Spears](mailto:corey.spears@lantanagroup.com) for questions or requests.
