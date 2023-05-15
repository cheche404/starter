package com.cheche.kafka.configuration;

import com.cheche.kafka.core.KafkaListenerSource;
import com.cheche.kafka.core.KafkaMultiSourceProperties;
import com.cheche.kafka.core.KafkaSource;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.support.serializer.JsonSerializer;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

/**
 * @author fudy
 * @date 2023/3/2
 */
@Slf4j
@Component(value = "kafkaMultiSourceRegister")
public class KafkaMultiSourceRegister implements InitializingBean {

    @Resource
    private KafkaMultiSourceProperties kafkaMultiSourceProperties;

    @Resource
    private DefaultListableBeanFactory beanFactory;

    @Value("${cheche.kafka.enabled:false}")
    private Boolean kafkaEnabled;

    /**
     * 消费者配置
     */
    private Map<String, Object> consumerConfig(KafkaSource kafkaSource) {
        Map<String, Object> props = new HashMap<>(16);
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaSource.getBootstrapServers());
        props.put(ConsumerConfig.GROUP_ID_CONFIG, kafkaSource.getGroupId());
        props.put(ConsumerConfig.CLIENT_ID_CONFIG, kafkaSource.getClientId());
        props.put(ConsumerConfig.HEARTBEAT_INTERVAL_MS_CONFIG, kafkaSource.getHeartbeatInterval());
        props.put(ConsumerConfig.FETCH_MIN_BYTES_CONFIG, kafkaSource.getConsumer().getFetchMinSize());
        props.put(ConsumerConfig.FETCH_MAX_WAIT_MS_CONFIG, kafkaSource.getConsumer().getFetchMaxWait());
        props.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, kafkaSource.getConsumer().getMaxPollRecords());
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, kafkaSource.getConsumer().isEnableAutoCommit());
        props.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, kafkaSource.getConsumer().getSessionTimeoutMs());
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, kafkaSource.getConsumer().getAutoOffsetReset());
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        return props;
    }


    /**
     * 生产者配置
     */
    private Map<String, Object> producerConfig(KafkaSource kafkaSource) {
        Map<String, Object> props = new HashMap<>(16);
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaSource.getBootstrapServers());
        props.put(ProducerConfig.RETRIES_CONFIG, kafkaSource.getProducer().getRetries());
        props.put(ProducerConfig.ACKS_CONFIG, kafkaSource.getProducer().getAcks());
        props.put(ProducerConfig.BATCH_SIZE_CONFIG, kafkaSource.getProducer().getBatchSize());
        props.put(ProducerConfig.BUFFER_MEMORY_CONFIG, kafkaSource.getProducer().getBufferMemory());

        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        props.put(ProducerConfig.CLIENT_ID_CONFIG, kafkaSource.getClientId());

        //压缩格式
        props.put(ProducerConfig.COMPRESSION_TYPE_CONFIG, kafkaSource.getProducer().getCompressionType());
        props.put(ProducerConfig.LINGER_MS_CONFIG, kafkaSource.getProducer().getLinger());
        return props;
    }


    /**
     * 自动注入
     * @throws Exception 异常
     */
    @Override
    public void afterPropertiesSet() {

        if(CollectionUtils.isEmpty(kafkaMultiSourceProperties.getMultiSource())){
            return;
        }

        kafkaMultiSourceProperties.getMultiSource().forEach(kafkaSource -> {
            //自定义消费者工厂
            DefaultKafkaConsumerFactory<Object, Object> consumerFactory = new DefaultKafkaConsumerFactory<>(consumerConfig(kafkaSource));
            //自定义生产者工厂
            DefaultKafkaProducerFactory<Object, Object> producerFactory = new DefaultKafkaProducerFactory<>(producerConfig(kafkaSource));


            ConcurrentKafkaListenerContainerFactory<String, String> factory = new ConcurrentKafkaListenerContainerFactory<>();
            factory.setConsumerFactory(consumerFactory);
            factory.getContainerProperties().setPollTimeout(15000);

            if (!kafkaSource.getConsumer().isEnableAutoCommit()) {
                //配置手动提交offset
                factory.getContainerProperties().setAckMode((ContainerProperties.AckMode.MANUAL));
            }

            //批量消费模式设置
            if(kafkaSource.getListener().getType().equalsIgnoreCase(KafkaProperties.Listener.Type.BATCH.name())){
                KafkaListenerSource listener = kafkaSource.getListener();
                //设置为批量消费，每个批次数量在Kafka配置参数中设置ConsumerConfig.MAX_POLL_RECORDS_CONFIG
                factory.setBatchListener(true);
                factory.setConcurrency(listener.getConcurrency());
                factory.getContainerProperties().setPollTimeout(listener.getPollTimeout());
                factory.getContainerProperties().setMissingTopicsFatal(listener.getMissingTopicsFatal());
                log.info("====== kafka多实例批量消费kafka设置： 拉取数：{}, 线程数：{}", kafkaSource.getConsumer().getMaxPollRecords(), listener.getConcurrency());
            }

            //注册bean实例
            beanFactory.registerSingleton(kafkaSource.getClientId() + "-ConsumerFactory", consumerFactory);
            beanFactory.registerSingleton(kafkaSource.getClientId() + "-ProducerFactory", producerFactory);
            beanFactory.registerSingleton(kafkaSource.getClientId() + "-KafkaListenerContainerFactory", factory);

            log.info("====== kafka多实例bean：beanName:" + kafkaSource.getClientId() + "-ConsumerFactory");
            log.info("====== kafka多实例bean：beanName:" + kafkaSource.getClientId() + "-ProducerFactory");
            log.info("====== kafka多实例bean：beanName:" + kafkaSource.getClientId() + "-KafkaListenerContainerFactory");
        });

        log.info("====== kafka 多消费者初始化完成...");
        if(!kafkaEnabled){
            log.info("====== kafka starter被禁用，目前不能使用kafka组件");
        }
    }
}