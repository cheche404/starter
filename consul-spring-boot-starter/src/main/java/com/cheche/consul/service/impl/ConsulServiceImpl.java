package com.cheche.consul.service.impl;

import com.cheche.consul.service.ConsulService;
import com.cheche.consul.domain.RegisterServiceModel;
import com.orbitz.consul.AgentClient;
import com.orbitz.consul.HealthClient;
import com.orbitz.consul.model.agent.ImmutableRegistration;
import com.orbitz.consul.model.health.ServiceHealth;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * consul service
 *
 * @author fudy
 * @date 2023/3/2
 */
@Service
public class ConsulServiceImpl implements ConsulService {

  private final AgentClient agentClient;
  private final HealthClient healthClient;

  public ConsulServiceImpl(AgentClient agentClient, HealthClient healthClient) {
    this.agentClient = agentClient;
    this.healthClient = healthClient;
  }

  @Override
  public void serviceRegister(RegisterServiceModel registerServiceModel) {
    ImmutableRegistration.Builder builder = ImmutableRegistration.builder();
    builder.id(registerServiceModel.getId())
      .name(registerServiceModel.getName())
      .addTags(registerServiceModel.getTags())
      .address(registerServiceModel.getAddress())
      .port(registerServiceModel.getPort())
      .addChecks(registerServiceModel.getCheck());
    agentClient.register(builder.build());
  }

  @Override
  public List<ServiceHealth> getHealthyServiceInstances(String serviceName) {
    return healthClient.getHealthyServiceInstances(serviceName).getResponse();
  }

  @Override
  public Map<String, com.orbitz.consul.model.health.Service> getAllService() {
    return agentClient.getServices();
  }
}
