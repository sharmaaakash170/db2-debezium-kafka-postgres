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

public class LowercaseKeyFields<R extends ConnectRecord<R>> implements Transformation<R> {

    @Override
    public R apply(R record) {
        if (record.key() == null) {
            return record;
        }

        Object key = record.key();

        // Case 1: Key is a Struct
        if (key instanceof Struct) {
            Struct struct = (Struct) key;

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
                newSchema,           // ✅ lowercase key schema
                newStruct,           // ✅ lowercase key
                record.valueSchema(),
                record.value(),
                record.timestamp()
            );
        }

        // Case 2: Key is a Map
        else if (key instanceof Map) {
            Map<?, ?> originalMap = (Map<?, ?>) key;
            Map<String, Object> lowercaseMap = new HashMap<>();
            for (Map.Entry<?, ?> entry : originalMap.entrySet()) {
                lowercaseMap.put(entry.getKey().toString().toLowerCase(), entry.getValue());
            }

            return record.newRecord(
                record.topic(),
                record.kafkaPartition(),
                record.keySchema(),
                lowercaseMap,       // ✅ lowercase map keys
                record.valueSchema(),
                record.value(),
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
