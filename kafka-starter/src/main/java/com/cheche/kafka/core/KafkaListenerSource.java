package com.cheche.kafka.core;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * @author fudy
 * @date 2023/3/2
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class KafkaListenerSource {

    /**
     * 是否批量拉取
     */
    private String type;

    /**
     * 并发消费线程数
     */
    private Integer concurrency;

    /**
     * 拉取超时时间
     */
    private Integer pollTimeout;

    /**
     * 未发现topic时不报错: 自动创建topic需要设置未false
     */
    private Boolean missingTopicsFatal;

    /**
     * offset确认方式 MANUAL
     */
    private String ackMode;

}
