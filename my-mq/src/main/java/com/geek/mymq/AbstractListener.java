package com.geek.mymq;

import lombok.Data;

/**
 * 抽象监听器
 */
@Data
public abstract class AbstractListener implements Listener {

    public AbstractListener(String topic) {
        this.topic = topic;
    }

    /**
     * 监听的主题名称
     */
    protected String topic;
}
