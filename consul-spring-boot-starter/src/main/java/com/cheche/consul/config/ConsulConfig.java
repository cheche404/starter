package com.cheche.consul.config;

import com.cheche.consul.properties.ConsulProperties;
import com.google.common.net.HostAndPort;
import com.orbitz.consul.AgentClient;
import com.orbitz.consul.Consul;
import com.orbitz.consul.HealthClient;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * consul configuration
 *
 * @author fudy
 * @date 2023/3/2
 */
@Configuration
@EnableConfigurationProperties(ConsulProperties.class)
public class ConsulConfig {

  @Bean
  public Consul consul(ConsulProperties consulProperties) {
    return Consul.builder()
      .withHostAndPort(HostAndPort.fromParts(consulProperties.getHost(), consulProperties.getPort()))
      .withPing(false)
      .build();
  }

  @Bean
  public AgentClient agentClient(Consul consul) {
    return consul.agentClient();
  }

  @Bean
  public HealthClient healthClient(Consul consul) {
    return consul.healthClient();
  }

}
