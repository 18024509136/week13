package com.geek.mymq;

import lombok.Data;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 消息队列代理类
 */
@Data
public class Broker {

    /**
     * 存放主题对应的队列对象
     */
    private final Map<String, Mq> topicMap = new ConcurrentHashMap<>(64);

    /**
     * 批量消费消息数量，默认为1条
     */
    private int pollMsgNum = 1;

    /**
     * 创建主题
     *
     * @param topic 主题名称
     */
    public void createTopic(String topic) {
        topicMap.putIfAbsent(topic, new Mq(topic));
    }

    /**
     * 获取主题对应的mq对象
     *
     * @param topic 主题名称
     * @return
     */
    private Mq getMq(String topic) {
        return this.topicMap.get(topic);
    }

    /**
     * 发送消息到mq对象
     *
     * @param topic   主题名称
     * @param message 消息数据
     * @return 发送是否成功，成功返回true，否则false
     */
    public boolean sendToMq(String topic, byte[] message) {
        Mq mq = this.getMq(topic);
        return mq.offer(message);
    }

    /**
     * 从mq对象获取消息数据
     *
     * @param topic         主题名称
     * @param consumerGroup 消费组名称
     * @return 消息数据集合
     */
    public List<byte[]> pollFromMq(String topic, String consumerGroup) {
        Mq mq = this.getMq(topic);
        return mq.poll(consumerGroup, this.pollMsgNum);
    }

    /**
     * 向mq对象发送ack消息
     *
     * @param topic         主题名称
     * @param consumerGroup 消费组名称
     * @param msgNum        单次消费的消息条数
     * @return true表示提交成功，否则为false
     */
    public boolean ackToMq(String topic, String consumerGroup, int msgNum) {
        try {
            Mq mq = this.getMq(topic);
            mq.setOffset(consumerGroup, msgNum);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 创建生产者
     *
     * @return
     */
    public Producer createProducer() {
        return new Producer(this);
    }

    /**
     * 创建消费者
     *
     * @param consumerGroup 消费组名称
     * @return
     */
    public Consumer createConsumer(String consumerGroup) {
        return new Consumer(this, consumerGroup);
    }
}
