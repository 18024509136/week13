package com.geek.mymq;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 消费者
 */
@Data
@AllArgsConstructor
public class Consumer {

    /**
     * 代理对象
     */
    private Broker broker;

    /**
     * 消费组名称
     */
    private String groupName;

    /**
     * 消息反序列化器
     */
    private MsgDeserializer msgDeserializer;

    /**
     * 监听回调对象
     */
    private AbstractListener listener;

    public Consumer(Broker broker, String groupName) {
        this.broker = broker;
        this.groupName = groupName;
    }

    /**
     * 拉取消息
     */
    public void poll() {
        List<byte[]> originMsgs = broker.pollFromMq(listener.getTopic(), this.groupName);
        if (originMsgs == null) {
            return;
        }

        List<Message> messageList = originMsgs.parallelStream().map(origingMsg -> {
            Message message = msgDeserializer.deserialize(origingMsg);
            return message;
        }).collect(Collectors.toList());

        listener.onMessage(messageList);
    }

    /**
     * 向broker发送ack
     *
     * @param topic     主题名称
     * @param groupName 消费组名称
     * @param msgNum    单次消费的消息条数
     */
    public void ack(String topic, String groupName, int msgNum) {
        broker.ackToMq(topic, groupName, msgNum);
    }
}
