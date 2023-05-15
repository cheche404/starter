package com.cheche.kafka.annotation;

import org.springframework.context.annotation.DependsOn;
import org.springframework.core.annotation.AliasFor;
import org.springframework.stereotype.Component;

import java.lang.annotation.*;

/**
 * @author fudy
 * @date 2023/3/2
 */
@Documented
@Component
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@DependsOn(value = "kafkaMultiSourceRegister")
public @interface KafkaService {

    @AliasFor(annotation = Component.class)
    String value() default "";
}
