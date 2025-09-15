**DB2 to PostgreSQL CDC Setup using Debezium and Kafka Connect**

**🧾 Purpose![](Aspose.Words.70bb372b-1300-4475-adb4-af05b637af90.001.png)**

This project sets up Change Data Capture (CDC) from an on-prem DB2 database to a PostgreSQL target using Debezium connectors on Kafka Connect. The pipeline streams changes via Kafka topics and sinks them into the PostgreSQL database.

**🛠️ Infrastructure Overview![](Aspose.Words.70bb372b-1300-4475-adb4-af05b637af90.002.png)**

- **Kafka Cluster**: Strimzi Kafka deployed on GKE
- **Kafka Connect**: Debezium-based Connect cluster running as StatefulSet
- **DB2 Source Connector**: Captures changes from DB2 tables and streams to Kafka topics
- **Postgres Sink Connector**: Reads Kafka topics and writes to PostgreSQL
- **Schema History Topic**: Kafka topic for Debezium schema tracking
- **Namespace/Schema Management**: PostgreSQL schema permissions handled for auto-create

**📦 Tools Used![](Aspose.Words.70bb372b-1300-4475-adb4-af05b637af90.003.png)**

- **Kafka / Strimzi**: Messaging backbone
- **Debezium**: CDC from DB2
- **PostgreSQL**: Sink database
- **Kubernetes (GKE)**: Orchestrates Kafka Connect and connectors
- **kubectl**: For management and debugging
- **Terraform (optional)**: For GKE and Strimzi deployments

**🚀 Deployment Instructions![](Aspose.Words.70bb372b-1300-4475-adb4-af05b637af90.004.png)**

**Prerequisites**

- Kubernetes cluster with  kubectl access![](Aspose.Words.70bb372b-1300-4475-adb4-af05b637af90.005.png)
- Kafka cluster deployed (Strimzi)
- DB2 source database accessible via hostname/port
- PostgreSQL target database with proper schema permissions

**Steps**

1. Deploy Kafka Connect StatefulSet on GKE (or your cluster).
1. Apply DB2 Source connector YAML: 

kubectl apply -f db2-source.yaml![](Aspose.Words.70bb372b-1300-4475-adb4-af05b637af90.006.png)

3. Apply PostgreSQL Sink connector YAML: kubectl apply -f postgres-sink.yaml![](Aspose.Words.70bb372b-1300-4475-adb4-af05b637af90.007.png)
3. Validate connectors: 

kubectl exec -n data-dev -it my-connect-connect-0 -- curl -s http:// localhost:8083/connectors/db2-source/status![](Aspose.Words.70bb372b-1300-4475-adb4-af05b637af90.008.png)

kubectl exec -n data-dev -it my-connect-connect-0 -- curl -s http:// localhost:8083/connectors/postgres-sink/status

5. Test data consumption from Kafka: 

kafka-console-consumer --bootstrap-server <kafka-bootstrap> --topic db2.<schema>.<table>![](Aspose.Words.70bb372b-1300-4475-adb4-af05b637af90.009.png) --from-beginning --max-messages 1

**✅ Connector Configuration Notes![](Aspose.Words.70bb372b-1300-4475-adb4-af05b637af90.010.png)**

**DB2 Source Connector**

- database.hostname : DB2 hostname or proxy![](Aspose.Words.70bb372b-1300-4475-adb4-af05b637af90.011.png)
- database.port : DB2 port
- database.user /  password : DB2 credentials
- database.dbname : Database name
- table.include.list : List of tables to capture
- topic.prefix : Kafka topic prefix ![](Aspose.Words.70bb372b-1300-4475-adb4-af05b637af90.012.png)
- key.converter /  value.converter :  JsonConverter with schema enabled
- Debezium unwrap transform enabled for clean payloads

**PostgreSQL Sink Connector**

- topics : Kafka topic(s) to sink![](Aspose.Words.70bb372b-1300-4475-adb4-af05b637af90.013.png)
- table.name.format : Include schema and table in format  schema."TABLE\_NAME"![](Aspose.Words.70bb372b-1300-4475-adb4-af05b637af90.014.png)
- connection.url : JDBC connection to PostgreSQL
- connection.user /  password : PostgreSQL credentials
- auto.create : true (if table may not exist)
- auto.evolve : true (for schema evolution)
- insert.mode : upsert
- pk.mode : record\_value,  pk.fields : primary key(s) from payload

**Important:** Always include the schema in  table.name.format instead of a separate  schema.name ![](Aspose.Words.70bb372b-1300-4475-adb4-af05b637af90.015.png)![](Aspose.Words.70bb372b-1300-4475-adb4-af05b637af90.016.png)property.

**🧹 Cleanup![](Aspose.Words.70bb372b-1300-4475-adb4-af05b637af90.017.png)**

To remove connectors and Kafka topics:

kubectl delete -f db2-source.yaml kubectl![](Aspose.Words.70bb372b-1300-4475-adb4-af05b637af90.018.png) delete -f postgres-sink.yaml

To clean PostgreSQL tables: 

DROP TABLE IF EXISTS aadhar."CRA\_KYC\_MST";![](Aspose.Words.70bb372b-1300-4475-adb4-af05b637af90.019.png)

**🔄 Troubleshooting Tips![](Aspose.Words.70bb372b-1300-4475-adb4-af05b637af90.020.png)**

- Check connector status via REST API
- Inspect logs of Kafka Connect pods for serialization or table mapping errors![](Aspose.Words.70bb372b-1300-4475-adb4-af05b637af90.021.png)
- Ensure PostgreSQL user has  USAGE and  CREATE privileges on target schema
- Verify Kafka topic payloads using  kafka-console-consumer
- Double quotes around table names are required if uppercase letters exist![](Aspose.Words.70bb372b-1300-4475-adb4-af05b637af90.022.png)
- Remove unsupported properties like  schema.name from sink connector

**📚 References![](Aspose.Words.70bb372b-1300-4475-adb4-af05b637af90.023.png)**

- [Debezium DB2 Connector](https://debezium.io/documentation/reference/connectors/db2.html)
- [Kafka Connect JDBC Sink](https://docs.confluent.io/kafka-connect-jdbc/current/sink-connector/index.html)
- [Strimzi Kafka on Kubernetes](https://strimzi.io/)
- PostgreSQL schema management documentation
3
