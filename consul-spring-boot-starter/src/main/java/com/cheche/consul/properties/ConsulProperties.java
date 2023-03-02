package com.cheche.consul.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * consul properties
 *
 * @author fudy
 * @date 2023/3/2
 */
@ConfigurationProperties(prefix = "cheche.consul")
public class ConsulProperties {

  /**
   * host
   */
  private String host;

  /**
   * port
   */
  private Integer port;

  public String getHost() {
    return host;
  }

  public void setHost(String host) {
    this.host = host;
  }

  public Integer getPort() {
    return port;
  }

  public void setPort(Integer port) {
    this.port = port;
  }
}
