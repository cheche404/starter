package com.cheche.kafka.configuration;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.producer.Producer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextClosedEvent;

import java.util.Map;
import java.util.Objects;

/**
 * @author fudy
 * @date 2023/3/2
 */
@Slf4j
public class KafkaCloser implements ApplicationListener<ContextClosedEvent> {

    @Autowired(required = false)
    private Map<String, Producer<?, ?>> procedures;

    @Autowired(required = false)
    private Map<String, Consumer<?, ?>> consumers;


    @Override
    public void onApplicationEvent(ContextClosedEvent contextClosedEvent) {
        if (!Objects.isNull(procedures)) {
            procedures.values().forEach(Producer::close);
        }

        if (!Objects.isNull(consumers)) {
            consumers.values().forEach(Consumer::close);
        }

        log.info("kafka 多消费者关闭成功========");
    }
}
