package com.cheche.kafka.core;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

/**
 * @author fudy
 * @date 2023/3/2
 */
@ConfigurationProperties(prefix = "cheche.kafka")
@Getter
@Setter
public class KafkaMultiSourceProperties {

    private String name;

    private List<KafkaSource> multiSource;

    public KafkaMultiSourceProperties() {
    }

    public KafkaMultiSourceProperties(String name, List<KafkaSource> kafkaSourceList){
        this.name = name;
        this.multiSource = kafkaSourceList;
    }
}
