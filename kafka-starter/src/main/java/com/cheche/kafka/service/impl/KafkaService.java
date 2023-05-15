package com.cheche.kafka.service.impl;

import com.cheche.kafka.core.KafkaMessage;
import com.cheche.kafka.core.KafkaRequest;
import com.cheche.kafka.service.IKafkaService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.KafkaException;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.util.concurrent.ListenableFuture;

import java.util.Objects;

/**
 * @author fudy
 * @date 2023/3/2
 */
@Slf4j
public class KafkaService<K, V> implements IKafkaService<K, V> {


    @Autowired
    private KafkaTemplate<K, V> kafkaTemplate;

    /**
     * 发送消息
     *
     * @param request kafka请求
     * @return 结果
     */
    @Override
    public ListenableFuture<SendResult<K, V>> handle(KafkaRequest<K, V> request) throws KafkaException {

        if (Objects.isNull(request)) {
            if (log.isErrorEnabled()) {
                log.error("Kafka request is empty");
            }
            throw new KafkaException("Kafka请求为空");
        }

        KafkaMessage<K, V> kafkaMessage = request.getMessage();

        String topic = kafkaMessage.getTopic();

        ListenableFuture<SendResult<K, V>> send = kafkaTemplate.send(topic, kafkaMessage.getKey(),
                kafkaMessage.getValue());
        send.addCallback((result) -> {
            if (log.isInfoEnabled()) {
                log.info(String.format("Message %s send successfully", kafkaMessage.toString()));
            }

            if (!Objects.isNull(request.getSuccessCallback())) {
                request.getSuccessCallback().onSuccess(result);
            }
        }, (t) -> {
            if (log.isErrorEnabled()) {
                log.error(String.format("Failed to sent message %s, %s", kafkaMessage.toString(), t.getMessage()), t);
            }

            if (!Objects.isNull(request.getFailureCallback())) {
                request.getFailureCallback().onFailure(t);
            }
        });
        return send;
    }
}
