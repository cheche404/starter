package com.cheche.kafka.configuration;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Primary;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.config.KafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer;
import org.springframework.kafka.listener.ContainerProperties;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

/**
 * @author fudy
 * @date 2023/3/2
 */
@Slf4j
@ConditionalOnProperty(prefix = "spring.kafka.consumer", name = "enabled", havingValue = "true")
public class KafkaConsumerConfiguration {


    @Value("${spring.kafka.bootstrap-servers:'localhost:9092'}")
    private String bootstrapServers;

    /**
     * 用于服务端日志
     */
    @Value("${spring.kafka.consumer.client-id: default}")
    private String consumerClientId;

    /**
     * 标识消费者的消费组
     */
    @Value("${spring.kafka.consumer.group-id: default}")
    private String consumerGroupId;

    /**
     * 心跳与消费者协调的间隔时间
     */
    @Value("${spring.kafka.consumer.heartbeat-interval: 3000}")
    private int consumerHeartInterval;

    /**
     * 每次fetch请求时，server应该返回的最小字节数。如果没有足够的数据返回，请求会等待，直到足够的数据才会返回。默认 1
     */
    @Value("${spring.kafka.consumer.fetch-min-size: 1}")
    private int consumerFetchMinSize;

    /**
     * Fetch请求发给broker后，在broker中可能会被阻塞的（当topic中records的总size小于fetch.min.bytes时），此时这个fetch请求耗时就会比较长。这个配置就是来配置consumer最多等待response多久。
     */
    @Value("${spring.kafka.consumer.fetch-max-wait: 500}")
    private int consumerFetchMaxWait;

    /**
     * 需要在session.timeout.ms这个时间内处理完的条数 默认500
     */
    @Value("${spring.kafka,consumer.max-poll-records: 500}")
    private int consumerMaxPollRecords;

    /**
     * 自动同步offset 默认true
     */
    @Value("${spring.kafka.consumer.enable-auto-commit: false}")
    private boolean consumerEnableAutoCommit;

    /**
     * 会话的超时限制
     */
    @Value("${spring.kafka.consumer.properties.session.timeout.ms: 10000}")
    private int consumerSessionTimeoutMs;

    /**
     * 没有初始化的offset 消费 ealiest latest none 默认latest
     */
    @Value("${spring.kafka.consumer.auto-offset-reset: earliest}")
    private String consumerAutoOffsetReset;

    @Value("${spring.kafka.listener.ack-mode: MANUAL_IMMEDIATE}")
    private ContainerProperties.AckMode ackMode;

    @Value("${spring.kafka.listener.concurrency: 1}")
    private Integer concurrency;

    @Resource
    private KafkaProperties kafkaProperties;



    /**
     * spring-kafka默认的消费者工厂配置，允许配置多个consumerFactory，需要指定要使用哪个工厂，不指定则使用当前kafka默认的
     */
    @Primary
    @Bean
    @DependsOn("kafkaMultiSourceRegister")
    public ConsumerFactory<?, ?> kafkaConsumerFactory() {
        return new DefaultKafkaConsumerFactory<>(consumerConfig());
    }

    @Primary
    @Bean
    public KafkaListenerContainerFactory<ConcurrentMessageListenerContainer<String, String>> kafkaListenerContainerFactory(ConsumerFactory<String, String> consumerFactory) {
        ConcurrentKafkaListenerContainerFactory<String, String> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory);
        factory.getContainerProperties().setPollTimeout(kafkaProperties.getListener().getPollTimeout() != null ?
                kafkaProperties.getListener().getPollTimeout().getSeconds() * 1000 : 5000);

        //配置手动提交offset
        factory.getContainerProperties().setAckMode(ackMode);

        //批量消费模式设置
        if(kafkaProperties.getListener().getType().equals(KafkaProperties.Listener.Type.BATCH)){

            //设置为批量消费，每个批次数量在Kafka配置参数中设置ConsumerConfig.MAX_POLL_RECORDS_CONFIG
            factory.setBatchListener(true);
            factory.setConcurrency(concurrency);
            factory.getContainerProperties().setMissingTopicsFatal(kafkaProperties.getListener().isMissingTopicsFatal());
        }
        log.info("====== spring默认kafkaConsumerFactory设置 批量消费kafka拉取数：{}, 线程数：{}", kafkaProperties.getConsumer().getMaxPollRecords(), concurrency);
        return factory;
    }

    /**
     * 消费者配置
     */
    private Map<String, Object> consumerConfig() {
        Map<String, Object> props = new HashMap<>(12);
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, consumerGroupId);
        props.put(ConsumerConfig.CLIENT_ID_CONFIG, consumerClientId);
        props.put(ConsumerConfig.HEARTBEAT_INTERVAL_MS_CONFIG, consumerHeartInterval);
        props.put(ConsumerConfig.FETCH_MIN_BYTES_CONFIG, consumerFetchMinSize);
        props.put(ConsumerConfig.FETCH_MAX_WAIT_MS_CONFIG, consumerFetchMaxWait);
        props.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, consumerMaxPollRecords);
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, consumerEnableAutoCommit);
        props.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, consumerSessionTimeoutMs);
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, consumerAutoOffsetReset);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        return props;
    }
}
