package com.example.connect.transforms;

import org.apache.kafka.connect.connector.ConnectRecord;
import org.apache.kafka.connect.transforms.Transformation;
import org.apache.kafka.common.config.ConfigDef;

import java.util.Map;

public class LowercaseTopic<R extends ConnectRecord<R>> implements Transformation<R> {

    @Override
    public R apply(R record) {
        // Lowercase the topic name
        String lowerTopic = record.topic().toLowerCase();

        return record.newRecord(
                lowerTopic,                  // new topic
                record.kafkaPartition(),
                record.keySchema(),
                record.key(),
                record.valueSchema(),
                record.value(),
                record.timestamp()
        );
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
