package com.cheche.kafka.service;

import com.cheche.kafka.core.KafkaRequest;
import org.springframework.kafka.KafkaException;
import org.springframework.kafka.support.SendResult;
import org.springframework.util.concurrent.ListenableFuture;

/**
 * @author fudy
 * @date 2023/3/2
 */
public interface IKafkaService<K, V> {

    /**
     * 接收Kafka请求
     * @param request   请求
     * @return
     * @throws KafkaException
     */
    ListenableFuture<SendResult<K, V>> handle(KafkaRequest<K, V> request) throws KafkaException;

}
