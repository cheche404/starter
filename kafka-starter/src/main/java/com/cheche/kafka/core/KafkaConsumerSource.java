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
public class KafkaConsumerSource {

    /**
     * 分组id
     */
    private String groupId;

    /**
     * earliest ：在偏移量无效的情况下，消费者将从起始位置读取分区的记录
     * 当各分区下有已提交的offset时，从提交的offset开始消费；无提交的offset时，从头开始消费
     */
    private String autoOffsetReset = "latest";

    /**
     * 是否自动提交偏移量，默认值是true,为了避免出现重复数据和数据丢失，可以把它设置为false,然后手动提交偏移量
     */
    private boolean enableAutoCommit = false;

    /**
     * 注意该值不要改得太大，如果Poll太多数据，而不能在下次Poll之前消费完，则会触发一次负载均衡，产生卡顿。
     */
    private Integer maxPollRecords = 500;

    /**
     * 自动提交的时间间隔 在spring boot 2.X 版本中这里采用的是值的类型为Duration 需要符合特定的格式，如1S,1M,2H,5D
     */
    private String autoCommitInterval;

    /**
     * #如果在这个时间内没有收到心跳，该消费者会被踢出组并触发{组再平衡 rebalance}
     * 两次Poll之间的最大允许间隔。
     * 消费者超过该值没有返回心跳，服务端判断消费者处于非存活状态，服务端将消费者从Consumer Group移除并触发Rebalance，默认30s。 单位毫秒
     */
    private Integer sessionTimeoutMs = 10000;

    /**
     * 拉取线程数
     */
    private int concurrency;

    /**
     * 每次fetch请求时，server应该返回的最小字节数。如果没有足够的数据返回，请求会等待，直到足够的数据才会返回。默认 1
     */
    private int fetchMinSize = 1;

    /**
     * Fetch请求发给broker后，在broker中可能会被阻塞的（当topic中records的总size小于fetch.min.bytes时），
     * 此时这个fetch请求耗时就会比较长。这个配置就是来配置最多等待response多久。
     */
    private int fetchMaxWait = 500;
}
