package com.cheche.consul.service;

import com.cheche.consul.domain.RegisterServiceModel;
import com.orbitz.consul.model.health.Service;
import com.orbitz.consul.model.health.ServiceHealth;

import java.util.List;
import java.util.Map;

/**
 * consul service interface
 *
 * @author fudy
 * @date 2023/3/2
 */
public interface ConsulService {

  /**
   * service register
   *
   * @param registerServiceModel registry service model
   */
  void serviceRegister(RegisterServiceModel registerServiceModel);

  /**
   * getHealthyServiceInstances
   *
   * @param serviceName service name
   * @return instances
   */
  List<ServiceHealth> getHealthyServiceInstances(String serviceName);

  /**
   * getAllService
   *
   * @return instances
   */
  Map<String, Service> getAllService();

}
