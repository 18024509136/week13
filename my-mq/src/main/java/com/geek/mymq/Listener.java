package com.geek.mymq;

import java.util.List;

/**
 * 监听接口
 */
public interface Listener {

    /**
     * 消费者监听回调方法
     *
     * @param messages
     */
    void onMessage(List<Message> messages);
}
