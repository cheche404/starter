package com.cheche.lru;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author cheche
 * @date 2023/2/8
 */
@Configuration
@EnableConfigurationProperties(LRUProperties.class)
public class LRUAutoConfiguration {

  @Autowired
  LRUProperties properties;

  @Bean
  @ConditionalOnMissingBean
  public LRUService lruService() {
    return new LRUService(properties);
  }
}
