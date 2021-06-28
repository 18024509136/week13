package com.geek.springkafka;

import org.apache.kafka.clients.producer.Partitioner;
import org.apache.kafka.common.Cluster;

import java.util.Map;

/**
 * 自定义分区器
 */
public class CustomizePartitioner implements Partitioner {
    @Override
    public int partition(String topic, Object key, byte[] keyBytes, Object value, byte[] valueBytes, Cluster cluster) {
        String message = (String) value;
        int partitionCount = cluster.partitionCountForTopic(topic);
        int index = message.hashCode() % partitionCount;
        System.out.println("消息定位到分区" + index);
        return index;
    }

    @Override
    public void close() {

    }

    @Override
    public void configure(Map<String, ?> configs) {

    }
}
