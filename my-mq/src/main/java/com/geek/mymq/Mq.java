package com.geek.mymq;

import lombok.Data;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 队列管理对象
 */
@Data
public class Mq {

    private String topic;

    /**
     * 用LinkedList模拟队列
     */
    private List<byte[]> queue = new LinkedList<>();

    /**
     * 存储每个消费组的offset
     */
    private Map<String, AtomicInteger> consumerOffsetMap = new ConcurrentHashMap<>(10);

    /**
     * 首次获取offset锁
     */
    private final Lock firstGetLock = new ReentrantLock();

    public Mq(String topic) {
        this.topic = topic;
    }

    /**
     * 设置某个消费组的偏移量
     *
     * @param consumerGroup 消费组名称
     * @param delta         偏移量的增量
     */
    public void setOffset(String consumerGroup, int delta) {
        if (delta <= 0 || delta > queue.size()) {
            throw new RuntimeException("超出队列的上界");
        }

        AtomicInteger currentOffset = getOffset(consumerGroup);
        int returnNewOffset = currentOffset.addAndGet(delta);

        // 每个消费组都有独立的offset计数器对象，将该对象作为锁，可以隔离消费组间的锁，提高并发度
        synchronized (currentOffset) {
            // 允许设置后的偏移量与队列实际最大下标+1的值一致
            if (returnNewOffset > queue.size()) {
                // 如果offset越界，则该消费组的offset设置将进行回滚操作
                int oldOffset = returnNewOffset - delta;
                currentOffset.set(oldOffset);
                throw new RuntimeException("超出队列的上界");
            }
        }
    }

    /**
     * 获取某个消费组的偏移量
     *
     * @param consumerGroup 消费组名称
     * @return
     */
    private AtomicInteger getOffset(String consumerGroup) {
        AtomicInteger currentOffset = consumerOffsetMap.get(consumerGroup);
        // 当消费组的offet计数器为空时，则初始化并放入map中存储
        if (currentOffset == null) {
            // 大部分情况不会进入该逻辑，消费组间锁力度很低
            firstGetLock.lock();
            try {
                if (currentOffset == null) {
                    currentOffset = new AtomicInteger(0);
                    consumerOffsetMap.put(consumerGroup, currentOffset);
                }
            } finally {
                firstGetLock.unlock();
            }
        }
        return currentOffset;
    }

    /**
     * 新增消息
     *
     * @param message 消息字节数据
     * @return
     */
    public boolean offer(byte[] message) {
        return queue.add(message);
    }

    /**
     * 拉消息
     *
     * @param consumerGroup 消费组名称
     * @param msgNum        批量拉取数量
     * @return 消息字节数据集合，一个byte[]代表一条消息
     */
    public List<byte[]> poll(String consumerGroup, int msgNum) {
        AtomicInteger offset = this.getOffset(consumerGroup);
        // 消费组偏移计数器为空或队列为空则返回null
        if (offset == null || queue.isEmpty()) {
            return null;
        }
        int offsetValue = offset.intValue();
        int queueSize = queue.size();
        // 循环获取次数
        int delta = msgNum;
        // 如果偏移量超过队列最大下标，返回null
        if (offsetValue > queueSize - 1) {
            return null;
            // 如果偏移量刚刚达到队列最大下标
        } else if (offsetValue == queueSize - 1) {
            delta = 0;
            // 如果偏移量+拉取数量超过队列最大下标，则取到队列最大下标为止
        } else if (offsetValue + msgNum > queueSize - 1) {
            delta = queueSize - offsetValue - 1;
        }

        List<byte[]> msgList = new ArrayList<>();
        for (int i = 0; i < delta + 1; i++) {
            byte[] msg = queue.get(offsetValue++);
            msgList.add(msg);
        }

        return msgList;
    }


}
