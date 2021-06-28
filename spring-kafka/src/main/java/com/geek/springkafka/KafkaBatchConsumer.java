package com.geek.springkafka;

import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 批量消费者，注意设置fetch-min-size、fetch-max-wait、listener.type=batch
 */
@Component
public class KafkaBatchConsumer {

    @KafkaListener(groupId = "test-consumer2", topics = "test1", errorHandler = "errorHandler")
    public void onMessage(List<ConsumerRecord<String, String>> records, Consumer consumer) {
        System.out.println(Thread.currentThread().getName() + "接收到的消息数量为：" + records.size());
        records.forEach(record -> {
            System.out.println("消费" + record.topic() + "主题的分区" + record.partition() + "的消息：" + record.value());
        });

        // 手动同步提交，要将enable-auto-commit设置为false
        consumer.commitSync();
        // 模拟处理出现异常，检测异常处理器是否生效
        // throw new RuntimeException("处理消息出现异常");
    }
}
