package com.geek.mymq;

import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@SpringBootApplication
public class MyMqApplication {

    public static void main(String[] args) {
        SpringApplication.run(MyMqApplication.class, args);
    }

    @Bean
    ApplicationRunner run() {
        return args -> {
            /**
             * 消费主题
             */
            final String topic = "test1.topic";

            /**
             * 消费组名称前缀
             */
            final String consumerGroupPrefix = "consumerGp_";

            Broker broker = new Broker();
            broker.setPollMsgNum(2);

            broker.createTopic(topic);

            Producer producer = broker.createProducer();
            producer.setMsgSerializer(new MsgSerializer());

            // 开启生产者线程生产消息
            new Thread(() -> {
                for (int i = 0; i < 10; i++) {
                    Order order = new Order(String.valueOf(i), i);
                    producer.send(topic, new Message(order));
                    try {
                        Thread.sleep(300);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }).start();

            ExecutorService executorService = Executors.newFixedThreadPool(2);
            // 模拟两个不同消费组来消费同一个主题
            for (int i = 1; i <= 2; i++) {
                final int suffix = i;
                executorService.execute(() -> {
                    String consumerGroupName = consumerGroupPrefix + suffix;

                    Consumer consumer = broker.createConsumer(consumerGroupName);
                    consumer.setMsgDeserializer(new MsgDeserializer());

                    // 创建监听者消费消息
                    consumer.setListener(new AbstractListener(topic) {
                        @Override
                        public void onMessage(List<Message> messages) {
                            messages.forEach(msg -> {
                                Order order = (Order) (msg.getBody());
                                System.out.println(consumerGroupName + "消费订单消息，订单号为：" + order.getOrderNo());
                            });
                            consumer.ack(topic, consumerGroupName, messages.size());
                        }
                    });

                    /**
                     * 轮询拉取主题消息
                     */
                    while (true) {
                        consumer.poll();
                        try {
                            Thread.sleep(500);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        };
    }
}
