package com.cheche.kafka.core;

import lombok.*;
import lombok.experimental.Accessors;

/**
 * @author fudy
 * @date 2023/3/2
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class KafkaSource {

    /**
     * bootstrapServers： 默认 localhost:9092
     */
    private String bootstrapServers = "localhost:9092";

    /**
     * 用于服务端日志
     */
    private String clientId = "default";

    /**
     * 消费者配置
     */
    private KafkaConsumerSource consumer;

    /**
     * 生产者配置
     */
    private KafkaProducerSource producer;

    /**
     * kafka消费监听器配置
     */
    private KafkaListenerSource listener;

    /**
     * 标识消费者的消费组
     */
    private String groupId = "default";

    /**
     * 心跳与消费者协调的间隔时间
     */
    private int heartbeatInterval = 3000;
}
