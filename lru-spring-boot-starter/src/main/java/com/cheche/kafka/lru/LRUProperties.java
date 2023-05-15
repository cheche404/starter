package com.cheche.kafka.lru;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author cheche
 * @date 2023/2/8
 */
@ConfigurationProperties(prefix = "lru")
public class LRUProperties {
  private Integer capacity;

  public Integer getCapacity() {
    return capacity;
  }

  public void setCapacity(Integer capacity) {
    this.capacity = capacity;
  }
}
