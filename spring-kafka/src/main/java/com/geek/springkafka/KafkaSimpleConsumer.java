package com.geek.springkafka;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * 普通消费者
 * listener.type=batch注释掉后，去掉@Component前的注释方可正常运行
 */
//@Component
public class KafkaSimpleConsumer {

    @KafkaListener(topics = {"test1"})
    public void onMessage(ConsumerRecord<String, String> record) {
        System.out.println("消费" + record.topic() + "主题的分区" + record.partition() + "的消息：" + record.value());
    }
}
