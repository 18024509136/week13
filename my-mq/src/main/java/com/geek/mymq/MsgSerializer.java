package com.geek.mymq;

import lombok.SneakyThrows;

/**
 * 消息序列化器
 */
public class MsgSerializer {

    /**
     * 序列化
     *
     * @param object 待序列化对象
     * @return
     */
    @SneakyThrows
    public byte[] serialize(Object object) {
        return SerializationUtil.serializer(object);
    }
}
