package com.cheche.kafka.configuration;

import com.cheche.kafka.core.KafkaMultiSourceProperties;
import com.cheche.kafka.service.IKafkaService;
import com.cheche.kafka.service.impl.KafkaService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.kafka.annotation.EnableKafka;

/**
 * @author fudy
 * @date 2023/3/2
 */
@EnableKafka
@Import({KafkaMultiSourceRegister.class, KafkaConsumerConfiguration.class,
        KafkaProcedureConfiguration.class})
@Configuration(proxyBeanMethods = false)
@ConditionalOnWebApplication
@EnableConfigurationProperties(KafkaMultiSourceProperties.class)
public class KafkaStarterAutoConfiguration {

    @Bean
    public IKafkaService kafkaService() {
        return new KafkaService();
    }

    @Bean
    public KafkaCloser kafkaCloser() {
        return new KafkaCloser();
    }

}
