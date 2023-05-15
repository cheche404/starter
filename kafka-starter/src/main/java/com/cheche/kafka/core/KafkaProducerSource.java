package com.cheche.kafka.core;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.Map;

/**
 * @author fudy
 * @date 2023/3/2
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class KafkaProducerSource {

    /**
     * 设置重试次数
     */
    private int retries = 3;

    /**
     * 达到batchSize大小的时候会发送消息
     */
    private int batchSize = 16384;

    /**
     * 延时时间，延时时间到达之后计算批量发送的大小没达到也发送消息
     */
    private int linger = 10;

    /**
     * 缓冲区的值
     */
    private int bufferMemory = 33554432;

    /**
     * 创建生产者配置map，ProducerConfig中的可配置属性比spring boot自动配置要多
     * 压缩算法
     */
    private String compressionType = "none";

    /**
     * 应答级别:多少个分区副本备份完成时向生产者发送ack确认(可选0、1、all/-1)
     */
    private String acks = "1";

    /**
     * 多余配置
     */
    private Map<String, Object> properties;
}
