# CPCDS Reference Server

This project is a reference FHIR server for the [Consumer-Directed Payer Data Exchange Implementation Guide](https://build.fhir.org/ig/HL7/carin-bb/toc.html) (also know as Carin Blue Button Implementation Guide). It is based on the [HAPI FHIR JPA Server](https://github.com/hapifhir/hapi-fhir-jpaserver-starter).

## Quickstart

To start the server simply build and run using docker. The container will automatically build and deploy using a tomcat server.

```bash
git clone https://github.com/carin-alliance/cpcds-server-ri.git
cd cpcds-server-ri
docker build -t cpcds-server-ri .
docker run -p 8080:8080 cpcds-server-ri
```

The server will then be browesable at http://localhost:8080/ and the FHIR endpoint will be available at http://localhost:8080/fhir

## Manually Running

To start the reference server without using docker you must install [Tomcat](http://tomcat.apache.org/) and [Maven](https://maven.apache.org/).
Clone the repo and build the server:

```bash
git clone https://github.com/carin-alliance/cpcds-server-ri.git
cd cpcds-server-ri
mvn dependency:resolve
mvn install -DskipTests
```

This will create a web archive (`.war`) in the `target` directory. Move the `war` file into the `webapps` directory of your tomcat server. Finally run the tomcat server.
