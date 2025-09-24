package com.example.connect.transforms;

import org.apache.kafka.common.config.ConfigDef;
import org.apache.kafka.connect.connector.ConnectRecord;
import org.apache.kafka.connect.data.*;
import org.apache.kafka.connect.transforms.Transformation;

import java.util.HashMap;
import java.util.Map;

public class UniversalDateTimeConverter<R extends ConnectRecord<R>> implements Transformation<R> {

    @Override
    public R apply(R record) {
        if (record.value() == null || !(record.value() instanceof Struct)) {
            return record;
        }

        Struct value = (Struct) record.value();
        Schema schema = value.schema();
        Map<String, Object> newValues = new HashMap<>();
        Map<String, Schema> newSchemas = new HashMap<>();

        // Convert all fields
        for (Field field : schema.fields()) {
            Object fieldValue = value.get(field);
            Schema fieldSchema = field.schema();
            String schemaName = fieldSchema.name();
            Object newValue = fieldValue;
            Schema newFieldSchema = fieldSchema;

            try {
                if (fieldValue != null) {
                    switch (schemaName) {
                        case "io.debezium.time.Date":
                        case "org.apache.kafka.connect.data.Date":
                            newValue = new java.sql.Timestamp(((Integer) fieldValue) * 86400000L);
                            newFieldSchema = Timestamp.builder().optional().build(); // works in old versions
                            break;

                        case "io.debezium.time.Timestamp":
                        case "org.apache.kafka.connect.data.Timestamp":
                            newValue = new java.sql.Timestamp((Long) fieldValue);
                            newFieldSchema = Timestamp.builder().optional().build();
                            break;

                        case "io.debezium.time.MicroTimestamp":
                            newValue = new java.sql.Timestamp(((Long) fieldValue) / 1000L);
                            newFieldSchema = Timestamp.builder().optional().build();
                            break;

                        case "io.debezium.time.NanoTimestamp":
                            newValue = new java.sql.Timestamp(((Long) fieldValue) / 1000000L);
                            newFieldSchema = Timestamp.builder().optional().build();
                            break;

                        default:
                            newFieldSchema = fieldSchema;
                    }
                } else {
                    newFieldSchema = fieldSchema;
                }
            } catch (Exception e) {
                newValue = fieldValue;
                newFieldSchema = fieldSchema;
            }

            newValues.put(field.name(), newValue);
            newSchemas.put(field.name(), newFieldSchema);
        }

        // Build new schema
        SchemaBuilder builder = SchemaBuilder.struct().name(schema.name());
        for (Field field : schema.fields()) {
            builder.field(field.name(), newSchemas.get(field.name()));
        }
        Schema updatedSchema = builder.build();

        // Build new struct
        Struct updatedValue = new Struct(updatedSchema);
        for (Field field : schema.fields()) {
            updatedValue.put(field.name(), newValues.get(field.name()));
        }

        return record.newRecord(
                record.topic(),
                record.kafkaPartition(),
                record.keySchema(),
                record.key(),
                updatedSchema,
                updatedValue,
                record.timestamp()
        );
    }

    @Override
    public ConfigDef config() {
        return new ConfigDef();
    }

    @Override
    public void close() {}

    @Override
    public void configure(Map<String, ?> configs) {}
}
