package com.geek.springkafka;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.PartitionOffset;
import org.springframework.kafka.annotation.TopicPartition;
import org.springframework.stereotype.Component;

/**
 * 可以指定消费组ID、消费的主题、消费的分区、消费offset的消费者
 * listener.type=batch注释掉后，去掉@Component前的注释方可正常运行
 */
//@Component
public class KafkaSpecificConsumer {

    /**
     * partitions和partitionOffsets配置的分区不能有重复
     * 监听多个主题的时候，就配置多个TopicPartition
     * partitions可配置多个
     * PartitionOffset可以配置多个
     * topics属性不能和topicPartitions属性共存
     *
     * @param record
     */
    @KafkaListener(groupId = "test-consumer2",
            topicPartitions = {
                    @TopicPartition(
                            topic = "test1",
                            partitions = {"0"},
                            partitionOffsets = {@PartitionOffset(partition = "1", initialOffset = "33270")}
                    )
            })
    public void onMessage(ConsumerRecord<String, String> record) {
        System.out.println("topic:" + record.topic() + "|partition:" + record.partition() + "|offset:" + record.offset() + "|value:" + record.value());
    }
}
