## kafka多消费者starter
kafka-starter支持多kafka数据源订阅和发布，仅需简单的配置即可启用，同时也支持原生的spring-kafka配置，
弥补了spring-kafka不兼容一些底层的参数缺陷

### 使用教程：

引入依赖包
```xml
<!--kafka封装starter-->
    <dependency>
        <groupId>com.cheche</groupId>
        <artifactId>kafka-starter</artifactId>
        <version>1.0.0-SNAPSHOT</version>
    </dependency>
```

### 1、多数据源kafka配置

cheche.kafka.enabled=true

```yaml
cheche:
# kafka消费配置
  kafka:
    enabled: true
    name: multi-consumers
    topic:
      real-data: cx_data_topics_1
    group:
      real-data: cx_data_group
    multiSource:
      - client-id: source1
        # 172.17.8.33:9092,172.17.8.34:9092,172.17.8.35:9092
        bootstrap-servers: 172.24.8.10:9092,172.24.8.11:9092,172.24.8.12:9092
        # producer生产者配置序列化
        producer:
          # 应答级别:多少个分区副本备份完成时向生产者发送ack确认(可选0、1、all/-1)
          acks: 1
          retries: 1
          key-serializer: org.apache.kafka.common.serialization.StringSerializer
          value-serializer: org.springframework.kafka.support.serializer.JsonSerializer
          # 生产者每个批次最多放多少条记录
          batch-size: 16384
          # 生产者一端总的可用发送缓存区大小，此处设置为32M
          buffer-memory: 33554432
          compressionType: lz4
          # 自定义分区器
          # spring.kafka.producer.properties.partitioner.class=com.felix.kafka.producer.CustomizePartitioner
          properties:
            # 当生产端积累的消息达到batch-size或接收到消息linger.ms后,生产者就会将消息提交给kafka
            linger:
              ms: 10
        # consumer消费者配置序列化
        consumer:
          key-deserializer: org.apache.kafka.common.serialization.StringDeserializer
          value-deserializer: org.apache.kafka.common.serialization.StringDeserializer
          group-id: defaultGroupId
          auto-offset-reset: earliest
          enable-auto-commit: false
          max-poll-records: 2000
          #服务器应以字节为单位返回获取请求的最小数据量，默认值为1，对应的kafka的参数为fetch.min.bytes。
          ##fetch-min-size: 20000000
          #fetch-max-wait: 50
        listener:
          type: batch
          concurrency: 6
          poll-timeout: 2000
          #自动创建topic
          missing-topics-fatal: false
          ack-mode: MANUAL
        properties:
          max:
            pool:
              interval:
                #0.10.1.0版本后新增的，这个参数需要根据实际业务处理时间进行设置，一旦Consumer处理不过来，就会被踢出
                ms: 600000
```

#### 消费者使用方式

首先在项目中创建一个KafkaConsumerManager，
必须添加注解@DependsOn("kafkaMultiSourceRegister")  //多消费源必须先于当前bean初始化完成，否则加载不到KafkaListenerContainerFactory的bean 2022-09-22
其次指定containerFactory，名称根据你的配置文件中的client-id决定：
格式：clientId-KafkaListenerContainerFactory
```java

@Slf4j
@ConditionalOnProperty(prefix = "cheche.kafka", name = "enabled", havingValue = "true")    //增加启用开关 2022-09-22
@Component
@DependsOn("kafkaMultiSourceRegister")  //多消费源必须先于当前bean初始化完成，否则加载不到KafkaListenerContainerFactory的bean 2022-09-22
public class KafkaConsumerManager {

    @Resource
    private ApplicationContext applicationContext;
    @Resource
    private EventHandlerPool eventHandlerPool;


    //组消费(批量拉取)模式, 设备上下线事件消息监听
    @KafkaListener(groupId = "group_cheche_point_event", topics = "#{'${cheche.kafka.topic.cheche_point_event:cheche_point_event}'.split(',')}",
            clientIdPrefix = "point_event", containerFactory = "consumer1-KafkaListenerContainerFactory")
    public void listenPointEvent(List<String> list, Acknowledgment ack) {

        log.info("================ 批量拉取数量：{}", list.size());
        applicationContext.publishEvent(new KafkaMsgEvent(list, EventTypeEnum.POINT_EVENT));

        //手动提交offset
        ack.acknowledge();
    }


    //组消费(批量拉取)模式, 设备天书告警
    @KafkaListener(groupId = "group_cheche_alarm_msg", topics = "#{'${cheche.kafka.topic.cheche_alarm_msg:cheche_alarm_msg}'.split(',')}",
            clientIdPrefix = "alarm_msg", containerFactory = "consumer1-KafkaListenerContainerFactory")
    public void listen(List<String> list, Acknowledgment ack) {

        //log.info("================ 批量拉取数量：{}", list.toString());
        applicationContext.publishEvent(new KafkaMsgEvent(list, EventTypeEnum.ALARM_EVENT));

        //手动提交offset
        ack.acknowledge();
    }


    @Async("rd-ThreadPool")  //异步操作，指定线程池
    @EventListener
    public void onApplicationEvent(KafkaMsgEvent event) {

        //异步线程批量对比
        dispatchMsgToExecute(event.getSource(), event.getMsgType());

        //方案一：
        //sendToRedis(event.getSource().toString());
    }


    /**
     * @desc 消息分发器
     * @param msgs
     * @param typeEnum
     */
    public void dispatchMsgToExecute(Object msgs, EventTypeEnum typeEnum){

        //创建处理器
        AbstractMsgHandler<?> handler = eventHandlerPool.getHandler(typeEnum.name());
        //序列化数据格式
        //优化遍历List<String>逐个转换JSONObject, 改为批量转换List<String> --》 List<JSONObject> 性能提升12倍以上
        //List<JSONObject> list = JSONObject.parseObject(event.getSource().toString(), List.class);
        List processMsgs = handler.processMsg(msgs.toString());
        //处理数据
        handler.handleMsg(processMsgs);
    }



    public static void main(String[] args) {
        String name = EventTypeEnum.ALARM_EVENT.name();
        System.out.println(name);
    }
}
```

#### 生产者使用方式

在自身项目中创建ProducerConfig,
必须添加注解@DependsOn("kafkaMultiSourceRegister")  //多消费源必须先于当前bean初始化完成，否则加载不到KafkaListenerContainerFactory的bean 2022-09-22
```java
@Configuration
public class ProducerConfig {

    /**
     * @desc 不指定factory的bean名称，会使用默认的producer，需要配置spring.kafka.xxxx
     * @param produceFactory
     * @return
     */
    @Bean("cheche-streaming")
    @DependsOn("kafkaMultiSourceRegister")
    public KafkaTemplate<String, Object> kafkaTemplate(@Qualifier("source1-ProducerFactory") ProducerFactory<String, Object> produceFactory) {
        return new KafkaTemplate<>(produceFactory);
    }
}
```


### 2、原生kafka配置

只需在原生基础上，加上enabled属性即可启用，详细内容见KafkaConsumerConfiguration.java类，KafkaProcedureConfiguration.java

```yaml
spring:
    kafka:
        #bootstrap-servers: cttq-cdh-c01-d001:9092,cttq-cdh-c01-d002:9092,cttq-cdh-c01-d003:9092,cttq-cdh-c01-d004:9092,cttq-cdh-c01-d005:9092,cttq-cdh-c01-d006:9092,cttq-cdh-c01-d007:9092,cttq-cdh-c01-d008:9092,cttq-cdh-c01-d009:9092
        bootstrap-servers: 172.24.8.10:9092,172.24.8.11:9092,172.24.8.12:9092
        #172.17.5.101:9092,172.17.5.102:9092,172.17.5.103:9092,172.17.5.104:9092,172.17.5.105:9092,172.17.5.106:9092,172.17.5.107:9092,172.17.5.108:9092,172.17.5.109:9092
        #producer生产者配置序列化
        producer:
          enabled: true
          key-serializer: org.apache.kafka.common.serialization.StringSerializer
          value-serializer: org.apache.kafka.common.serialization.StringSerializer
          batch-size: 16384 #生产者每个批次最多放多少条记录
          buffer-memory: 33554432 #生产者一端总的可用发送缓存区大小，此处设置为32M
        #consumer消费者配置序列化
        consumer:
          enabled: true
          key-deserializer: org.apache.kafka.common.serialization.StringDeserializer
          value-deserializer: org.apache.kafka.common.serialization.StringDeserializer
          group-id: defaultGroupId
          auto-offset-reset: earliest
          #auto-offset-reset: latest
          enable-auto-commit: false
          max-poll-records: 1500
          #服务器应以字节为单位返回获取请求的最小数据量，默认值为1，对应的kafka的参数为fetch.min.bytes。
          ##fetch-min-size: 20000000
          #fetch-max-wait: 50
        listener:
          type: batch
          concurrency: 8
          poll-timeout: 2000
          missing-topics-fatal: false
          ack-mode: MANUAL
        properties:
          max:
            pool:
              interval:
                ms: 600000 #0.10.1.0版本后新增的，这个参数需要根据实际业务处理时间进行设置，一旦Consumer处理不过来，就会被踢出Consumer Group
```

#### 消费者使用方式：

不需要指定containerFactory，因为默认是spring自身初始化的factory
```java
//组消费(批量拉取)模式, 设备天书告警
    @KafkaListener(groupId = "group_cheche_alarm_msg", topics = "#{'${cheche.kafka.topic.cheche_alarm_msg:cheche_alarm_msg}'.split(',')}",
            clientIdPrefix = "alarm_msg")
    public void listen(List<String> list, Acknowledgment ack) {

        //log.info("================ 批量拉取数量：{}", list.toString());
        applicationContext.publishEvent(new KafkaMsgEvent(list, EventTypeEnum.ALARM_EVENT));

        //手动提交offset
        ack.acknowledge();
    }
```


#### 生产者使用方式：
```java
@Resource
private KafkaTemplate<String, Object> kafkaTemplate;
```

