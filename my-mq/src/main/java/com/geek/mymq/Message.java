package com.geek.mymq;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * 消息对象
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Message {

    public Message(Object body) {
        this.body = body;
    }

    /**
     * 消息头
     */
    private Map<String, Object> header;

    /**
     * 消息体
     */
    private Object body;
}
