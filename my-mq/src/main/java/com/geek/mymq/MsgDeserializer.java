package com.geek.mymq;

import lombok.SneakyThrows;

/**
 * 消息反序列化器
 */
public class MsgDeserializer {

    /**
     * 反序列化
     *
     * @param messageBytes 消息数据
     * @return
     */
    @SneakyThrows
    public Message deserialize(byte[] messageBytes) {
        Message message = SerializationUtil.deserializer(messageBytes, Message.class);
        return message;
    }
}
