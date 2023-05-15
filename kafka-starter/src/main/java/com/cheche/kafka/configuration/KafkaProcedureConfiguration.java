package com.cheche.kafka.configuration;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * @author fudy
 * @date 2023/3/2
 */
@Slf4j
@ConditionalOnProperty(prefix = "spring.kafka.producer", name = "enabled", havingValue = "true")
public class KafkaProcedureConfiguration {

    @Value("${spring.kafka.bootstrap-servers:'localhost:9092'}")
    private String bootStrapServers;

    /**
     * 重试失败次数
     */
    @Value("${spring.kafka.producer.retries: 1}")
    private int procedureRetries;

    /**
     * broker收到消息后的确认机制  -1/all 0 1
     */
    @Value("${spring.kafka.producer.acks: 1}")
    private String producerAcks;

    /**
     * 批量处理大小 默认值16384
     */
    @Value("${spring.kafka.producer.batch-size: 16384}")
    private int procedureBatchSize;

    /**
     * 缓存等待发送broker内存字节 默认值33554432
     */
    @Value("${spring.kafka.producer.buffer-memory: 33554432}")
    private int procedureBufferMemory;

    /**
     * 发出请求传递给broker,用于服务端日志
     */
    @Value("${spring.kafka.producer.client-id: default}")
    private String procedureClientId;

    /**
     * 等待时间发送 如果为0 batchSize 不起作用
     */
    @Value("${spring.kafka.producer.properties.linger.ms: 500}")
    private int producerLinger;

    /**
     * 事务线程池前缀
     */
    @Value("${spring.kafka.producer.transaction-id-prefix: iip}")
    private String transPrefixId;

    @Value("${spring.kafka.producer.compression-type: lz4}")
    private String compressionType;

    @Value("${spring.kafka.producer.properties.linger.ms: 10}")
    private Integer lingerMs;

    /**
     * KafkaTemplate
     */
    @Bean
    public KafkaTemplate<String, Object> kafkaTemplate(ProducerFactory<String, Object> produceFactory) {
        return new KafkaTemplate<>(produceFactory);
    }

    /**
     * 工厂
     */
    @Bean("defaultProducerFactory")
    public ProducerFactory<String, Object> producerFactory() {
        DefaultKafkaProducerFactory<String, Object> factory = new DefaultKafkaProducerFactory<>(producerConfig());
        //factory.transactionCapable();
        //factory.setTransactionIdPrefix(transPrefixId);
        log.info("====== spring默认kafkaProducerFactory设置 压缩格式：{}, linger ms：{}", compressionType, lingerMs);
        return factory;
    }

    /**
     * 事务服务
     */
    /*@Bean
    public KafkaTransactionManager<String, Object> transactionManager(ProducerFactory<String, Object> produceFactory) {
        return new KafkaTransactionManager<>(produceFactory);
    }*/

    /**
     * 生产者配置
     */
    private Map<String, Object> producerConfig() {
        Map<String, Object> props = new HashMap<>(16);
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootStrapServers);
        props.put(ProducerConfig.RETRIES_CONFIG, procedureRetries);
        props.put(ProducerConfig.ACKS_CONFIG, producerAcks);
        props.put(ProducerConfig.BATCH_SIZE_CONFIG, procedureBatchSize);
        props.put(ProducerConfig.BUFFER_MEMORY_CONFIG, procedureBufferMemory);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.CLIENT_ID_CONFIG, procedureClientId);

        //压缩格式
        props.put(ProducerConfig.COMPRESSION_TYPE_CONFIG, compressionType);
        props.put(ProducerConfig.LINGER_MS_CONFIG, producerLinger);
        return props;
    }

}
