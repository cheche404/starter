package com.cheche.kafka.core;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.kafka.support.SendResult;
import org.springframework.util.concurrent.FailureCallback;
import org.springframework.util.concurrent.SuccessCallback;

/**
 * @author fudy
 * @date 2023/3/2
 */
@Getter
public class KafkaRequest<K, V> {
    /**
     * 消息
     */
    private KafkaMessage<K, V> message;

    /**
     * 发送成功回调
     */
    private SuccessCallback<SendResult<K, V>> successCallback;

    /**
     * 发送失败回调
     */
    private FailureCallback failureCallback;

    private KafkaRequest(String topic, K key, V value, SuccessCallback<SendResult<K, V>> successCallback, FailureCallback failureCallback) {
        this.message = new KafkaMessage<>(topic, key, value);
        this.successCallback = successCallback;
        this.failureCallback = failureCallback;
    }

    public static <K, V> Builder<K, V> builder() {
        return new Builder<>();
    }

    @Setter
    @Accessors(chain = true)
    public static class Builder<K, V> {
        /**
         * topic
         */
        private String topic;

        /**
         * key
         */
        private K key;

        /**
         * 消息内容
         */
        private V value;

        /**
         * 发送成功回调
         */
        private SuccessCallback<SendResult<K, V>> successCallback;

        /**
         * 发送失败回调
         */
        private FailureCallback failureCallback;

        public KafkaRequest<K, V> build() {
            return new KafkaRequest<>(this.topic, this.key, this.value, this.successCallback, this.failureCallback);
        }
    }
}
