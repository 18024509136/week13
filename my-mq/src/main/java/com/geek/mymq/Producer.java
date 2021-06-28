package com.geek.mymq;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;

/**
 * 生产者
 */
@Data
@AllArgsConstructor
public class Producer implements Serializable {

    /**
     * 代理对象
     */
    private Broker broker;

    /**
     * 消息序列化器
     */
    private MsgSerializer msgSerializer;

    public Producer(Broker broker) {
        this.broker = broker;
    }

    public void setMsgSerializer(MsgSerializer msgSerializer) {
        this.msgSerializer = msgSerializer;
    }

    public boolean send(String topic, Message message) {
        byte[] msgBytes = msgSerializer.serialize(message);
        return broker.sendToMq(topic, msgBytes);
    }
}
