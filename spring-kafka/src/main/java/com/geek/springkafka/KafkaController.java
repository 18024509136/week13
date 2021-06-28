package com.geek.springkafka;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@RequestMapping("/kafka")
@RestController
public class KafkaController {

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    /**
     * 普通发送消息
     *
     * @param message
     * @return
     */
    @GetMapping("/normal")
    public String simpleSend(String message) {
        ListenableFuture<SendResult<String, String>> future = kafkaTemplate.send("test1", message);
        try {
            SendResult<String, String> sendResult = future.get(10000, TimeUnit.SECONDS);
            // 消息发送到的topic
            String topic = sendResult.getRecordMetadata().topic();
            // 消息发送到的分区
            int partition = sendResult.getRecordMetadata().partition();
            // 消息在分区内的offset
            long offset = sendResult.getRecordMetadata().offset();
            return "发送消息成功" + topic + "-" + partition + "-" + offset;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "消息发送失败";
    }

    /**
     * 异步带回调发送
     *
     * @param message
     * @return
     */
    @GetMapping("/callback")
    public String callbackSend(String message) {
        kafkaTemplate.send("test1", message).addCallback(success -> {
            String topic = success.getRecordMetadata().topic();
            int partition = success.getRecordMetadata().partition();
            long offset = success.getRecordMetadata().offset();
            System.out.println("发送消息成功:" + topic + "-" + partition + "-" + offset);
        }, failure -> {
            System.out.println("发送消息失败:" + failure.getMessage());
        });
        return "已异步发送消息";
    }

    /**
     * 事务性发送
     * acks: all以及配置transaction-id-prefix 方可生效
     *
     * @param message
     * @return
     */
    @GetMapping("/transactional")
    public String transactionalSend(String message) {
        String result = "消息发送失败";
        try {
            result = kafkaTemplate.executeInTransaction(operations -> {
                // 注意executeInTransaction内部不要自己吞了异常，否则事务管理器无法捕捉异常并回滚
                operations.send("test1", message);
                int i = 1 / 0;
                return "发送消息成功";
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }
}
