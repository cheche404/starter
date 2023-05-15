package com.cheche.kafka.consul.domain;

import com.orbitz.consul.model.agent.ImmutableRegCheck;

/**
 * registry service model
 *
 * @author fudy
 * @date 2023/3/2
 */
public class RegisterServiceModel {

  /**
   * id
   */
  private String id;

  /**
   * name
   */
  private String name;

  /**
   * tags
   */
  private String[] tags;

  /**
   * address
   */
  private String address;

  /**
   * port
   */
  private Integer port;

  /**
   * element
   */
  private ImmutableRegCheck check;

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String[] getTags() {
    return tags;
  }

  public void setTags(String[] tags) {
    this.tags = tags;
  }

  public String getAddress() {
    return address;
  }

  public void setAddress(String address) {
    this.address = address;
  }

  public Integer getPort() {
    return port;
  }

  public void setPort(Integer port) {
    this.port = port;
  }

  public ImmutableRegCheck getCheck() {
    return check;
  }

  public void setCheck(ImmutableRegCheck check) {
    this.check = check;
  }

  /**
   * build ImmutableRegCheck
   *
   * @param checkUrl check url
   * @param interval interval time
   * @return ImmutableRegCheck
   */
  public static ImmutableRegCheck buildImmutableRegCheck(String checkUrl, String interval) {
    return ImmutableRegCheck.builder().http(checkUrl).interval(interval).build();
  }
}
