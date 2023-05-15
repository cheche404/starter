package com.cheche.kafka.core;

import lombok.Getter;
import org.springframework.util.Assert;

/**
 * @author fudy
 * @date 2023/3/2
 */
@Getter
public class KafkaMessage<K, V> {
    /**
     * topic
     */
    private String topic;

    /**
     * key
     */
    private K key;

    /**
     * 数据
     */
    private V value;

    /**
     * 时间
     */
    private Long serial;

    KafkaMessage(String topic, K key, V value) {
        Assert.notNull(topic, "Message's topic can't be null");
        Assert.notNull(value, "Message's data can't be null");
        this.topic = topic;
        this.key = key;
        this.value = value;
        this.serial = System.currentTimeMillis();
    }

    @Override
    public String toString() {
        return String.format("{topic:%s, key:%s, data:%s, serial:%s}", this.topic, String.valueOf(this.key), String.valueOf(this.value), this.serial);
    }
}
