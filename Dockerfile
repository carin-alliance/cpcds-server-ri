FROM maven:3.6.1-jdk-8 AS build
COPY pom.xml /usr/src/app/pom.xml
COPY src /usr/src/app/src
RUN mvn -f /usr/src/app/pom.xml clean package

FROM jetty:9-jre8-alpine
COPY --from=build /usr/src/app/target/cpcds-server.war /var/lib/jetty/webapps/cpcds-server.war
COPY --from=build /usr/src/app/target /var/lib/jetty/target 
ADD ./data /var/lib/jetty/target/
COPY src /var/lib/jetty/src
USER root
RUN chown -R jetty:jetty /var/lib/jetty/target
USER jetty:jetty
EXPOSE 8080