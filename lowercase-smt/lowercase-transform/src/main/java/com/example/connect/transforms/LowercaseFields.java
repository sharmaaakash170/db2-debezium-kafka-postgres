package com.example.connect.transforms;

import org.apache.kafka.connect.connector.ConnectRecord;
import org.apache.kafka.connect.data.Field;
import org.apache.kafka.connect.data.Schema;
import org.apache.kafka.connect.data.SchemaBuilder;
import org.apache.kafka.connect.data.Struct;
import org.apache.kafka.connect.transforms.Transformation;
import org.apache.kafka.common.config.ConfigDef;

import java.util.HashMap;
import java.util.Map;

public class LowercaseFields<R extends ConnectRecord<R>> implements Transformation<R> {

    @Override
    public R apply(R record) {
        if (record.value() == null) {
            return record;
        }

        Object value = record.value();

        // Case 1: Value is a Struct
        if (value instanceof Struct) {
            Struct struct = (Struct) value;

            // Build new schema with lowercase fields
            SchemaBuilder builder = SchemaBuilder.struct();
            for (Field field : struct.schema().fields()) {
                builder.field(field.name().toLowerCase(), field.schema());
            }
            Schema newSchema = builder.build();

            // Build new struct with lowercase field names
            Struct newStruct = new Struct(newSchema);
            for (Field field : struct.schema().fields()) {
                newStruct.put(field.name().toLowerCase(), struct.get(field));
            }

            return record.newRecord(
                record.topic(),
                record.kafkaPartition(),
                record.keySchema(),
                record.key(),
                newSchema,
                newStruct,
                record.timestamp()
            );

        } 
        // Case 2: Value is a Map (after unwrap)
        else if (value instanceof Map) {
            Map<?, ?> originalMap = (Map<?, ?>) value;
            Map<String, Object> lowercaseMap = new HashMap<>();
            for (Map.Entry<?, ?> entry : originalMap.entrySet()) {
                lowercaseMap.put(entry.getKey().toString().toLowerCase(), entry.getValue());
            }

            return record.newRecord(
                record.topic(),
                record.kafkaPartition(),
                record.keySchema(),
                record.key(),
                record.valueSchema(),
                lowercaseMap,
                record.timestamp()
            );
        } 
        // Other types: leave unchanged
        else {
            return record;
        }
    }

    @Override
    public void configure(Map<String, ?> configs) { }

    @Override
    public void close() { }

    @Override
    public ConfigDef config() {
        return new ConfigDef();
    }
}
