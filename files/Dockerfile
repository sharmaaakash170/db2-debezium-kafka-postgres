FROM quay.io/strimzi/kafka:0.47.0-kafka-4.0.0
USER root:root

RUN mkdir -p /opt/kafka/plugins/kafka-connect-jdbc
RUN mkdir -p /opt/kafka/plugins/debezium-connector-db2

COPY kafka-connect-jdbc-10.8.4.jar /opt/kafka/plugins/kafka-connect-jdbc/
COPY postgresql-42.7.4.jar /opt/kafka/plugins/kafka-connect-jdbc/
COPY plugin.properties /opt/kafka/plugins/kafka-connect-jdbc/

COPY debezium-connector-db2/* /opt/kafka/plugins/debezium-connector-db2/

COPY log4j.properties /opt/kafka/config/log4j.properties

RUN chown -R 1001:0 /opt/kafka/plugins \
    && chmod -R 755 /opt/kafka/plugins

USER 1001
