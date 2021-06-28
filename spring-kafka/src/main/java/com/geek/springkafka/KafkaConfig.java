package com.geek.springkafka;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.listener.ConsumerAwareListenerErrorHandler;

import java.util.List;

@Configuration
public class KafkaConfig {

    /**
     * 批量消息处理异常处理器
     * 注意要在@KafkaListener注解中指定使用哪个errorHandler
     *
     * @return
     */
    @Bean(name = "errorHandler")
    public ConsumerAwareListenerErrorHandler errorHandler() {
        return (message, e, consumer) -> {
            System.out.println("捕获到消息处理发生的异常：" + e.getMessage());
            // 对于非批次处理的KafkaListener，这里的message只能强转为单个ConsumerRecord对象了
            List<ConsumerRecord<String, String>> records = (List<ConsumerRecord<String, String>>) (message.getPayload());
            records.iterator().forEachRemaining(record -> {
                System.out.println("处理异常的消息有：" + record.value());
            });
            return null;
        };
    }
}
