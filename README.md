# DB2 to PostgreSQL CDC Setup using Debezium and Kafka Connect

## 🧾 Purpose

This project sets up Change Data Capture (CDC) from an on-prem DB2 database to a PostgreSQL target using Debezium connectors on Kafka Connect. The pipeline streams changes via Kafka topics and sinks them into the PostgreSQL database.

## 🛠️ Infrastructure Overview

- **Kafka Cluster**: Strimzi Kafka deployed on GKE
- **Kafka Connect**: Debezium-based Connect cluster running as StatefulSet
- **DB2 Source Connector**: Captures changes from DB2 tables and streams to Kafka topics
- **Postgres Sink Connector**: Reads Kafka topics and writes to PostgreSQL
- **Schema History Topic**: Kafka topic for Debezium schema tracking
- **Namespace/Schema Management**: PostgreSQL schema permissions handled for auto-create

## 📦 Tools Used

- **Kafka / Strimzi**: Messaging backbone
- **Debezium**: CDC from DB2
- **PostgreSQL**: Sink database
- **Kubernetes (GKE)**: Orchestrates Kafka Connect and connectors
- **kubectl**: For management and debugging
- **Terraform (optional)**: For GKE and Strimzi deployments

## 🚀 Deployment Instructions

### Prerequisites

- Kubernetes cluster with `kubectl` access
- Kafka cluster deployed (Strimzi)
- DB2 source database accessible via hostname/port
- PostgreSQL target database with proper schema permissions

### Steps

1. Deploy Kafka Connect StatefulSet on GKE (or your cluster).
2. Apply DB2 Source connector YAML:
   ```bash
   kubectl apply -f db2-source.yaml
   ```
3. Apply PostgreSQL Sink connector YAML:
   ```bash
   kubectl apply -f postgres-sink.yaml
   ```
4. Validate connectors:
   ```bash
   kubectl exec -n data-dev -it my-connect-connect-0 -- curl -s http://localhost:8083/connectors/db2-source/status
   kubectl exec -n data-dev -it my-connect-connect-0 -- curl -s http://localhost:8083/connectors/postgres-sink/status
   ```
5. Test data consumption from Kafka:
   ```bash
   kafka-console-consumer --bootstrap-server <kafka-bootstrap> --topic db2.<schema>.<table> --from-beginning --max-messages 1
   ```

## ✅ Connector Configuration Notes

### DB2 Source Connector

- `database.hostname`: DB2 hostname or proxy
- `database.port`: DB2 port
- `database.user` / `password`: DB2 credentials
- `database.dbname`: Database name
- `table.include.list`: List of tables to capture
- `topic.prefix`: Kafka topic prefix
- `key.converter` / `value.converter`: `JsonConverter` with schema enabled
- Debezium unwrap transform enabled for clean payloads

### PostgreSQL Sink Connector

- `topics`: Kafka topic(s) to sink
- `table.name.format`: Include schema and table in format `schema."TABLE_NAME"`
- `connection.url`: JDBC connection to PostgreSQL
- `connection.user` / `password`: PostgreSQL credentials
- `auto.create`: true (if table may not exist)
- `auto.evolve`: true (for schema evolution)
- `insert.mode`: upsert
- `pk.mode`: record_value, `pk.fields`: primary key(s) from payload

**Important:** Always include the schema in `table.name.format` instead of a separate `schema.name` property.

## 🧹 Cleanup

To remove connectors and Kafka topics:

```bash
kubectl delete -f db2-source.yaml
kubectl delete -f postgres-sink.yaml
```

To clean PostgreSQL tables:
```sql
DROP TABLE IF EXISTS aadhar."CRA_KYC_MST";
```

## 🔄 Troubleshooting Tips

- Check connector status via REST API
- Inspect logs of Kafka Connect pods for serialization or table mapping errors
- Ensure PostgreSQL user has `USAGE` and `CREATE` privileges on target schema
- Verify Kafka topic payloads using `kafka-console-consumer`
- Double quotes around table names are required if uppercase letters exist
- Remove unsupported properties like `schema.name` from sink connector

## 📚 References

- [Debezium DB2 Connector](https://debezium.io/documentation/reference/connectors/db2.html)
- [Kafka Connect JDBC Sink](https://docs.confluent.io/kafka-connect-jdbc/current/sink-connector/index.html)
- [Strimzi Kafka on Kubernetes](https://strimzi.io/)
- PostgreSQL schema management documentation