## 作业1 ##
搭建一个 3 节点 Kafka 集群，测试功能和性能；实现 spring kafka 下对 kafka 集群的操作，将代码提交到 github。  
### 3节点kafka集群搭建与性能测试 ###
- 在本地5台虚拟机bigdata01~bigdata05上搭建kafka集群。由于是大数据集群，出于资源复用考虑，kafka注册用的zookeeper复用了大数据环境的zookeeper
- 部署文档请参考有道云笔记链接 https://note.youdao.com/s/RAjsWNCy
- 功能与性能测试，单台节点配置为4核12G
(1)创建测试主题 kafka-topics.sh --zookeeper bigdata03:2181 --create --topic test1 --partitions 3 --replication-factor 2  
(2)生产性能测试结果：  
kafka-producer-perf-test.sh --topic test1 --num-records 100000 --record-size 1000 --throughput 200000 --producer-props bootstrap.servers=bigdata03:9092  
73649 records sent, 14729.8 records/sec (14.05 MB/sec), 1228.9 ms avg latency, 2109.0 ms max latency.  
100000 records sent, 16531.658125 records/sec (15.77 MB/sec), 1240.89 ms avg latency, 2109.00 ms max latency, 1214 ms 50th, 1940 ms 95th, 2062 ms 99th, 2100 ms 99.9th.  
(3)消费性能测试结果：  
kafka-consumer-perf-test.sh --bootstrap-server bigdata03:9092 --topic test1 --fetch-size 1048576 --messages 100000 --threads 3
start.time, end.time, data.consumed.in.MB, MB.sec, data.consumed.in.nMsg, nMsg.sec, rebalance.time.ms, fetch.time.ms, fetch.MB.sec, fetch.nMsg.sec
2021-06-24 12:01:23:520, 2021-06-24 12:01:25:455, 95.3674, 49.2855, 100000, 51679.5866, 1624507284329, -1624507282394, -0.0000, -0.0001  

### spring kafka 下对 kafka 集群的操作 ###
这里专注于spring kafka的生产和消费操作，具体注意的事项在代码注释中有说明  
- 配置文件请参考application.yml，包含producer、consumer、listener的配置，注意其中的transaction-id-prefix、listener.type、listener.concurrency对生产和消费模式有较大影响
- com.geek.springkafka.KafkaController负责生产消息，simpleSend()在同步模式下发送消息，callbackSend()在异步模式下发送消息，transactionalSend()在事务模式下发送消息
- com.geek.springkafka.CustomizePartitioner为自定义分区器，根据消息的哈希取余定位到对应的分区
- com.geek.springkafka.KafkaConfig中自定义了1个消费异常处理器ConsumerAwareListenerErrorHandler
- com.geek.springkafka.KafkaSimpleConsumer是普通的单条消息消费处理器
- com.geek.springkafka.KafkaBatchConsumer是批量消息消费处理器
- com.geek.springkafka.KafkaSpecificConsumer是可以指定消费组ID、消费的主题、消费的分区、消费offset的消费处理器

## 作业2 ##
思考和设计自定义 MQ 第二个版本或第三个版本，写代码实现其中至少一个功能点，把设计思路和实现代码，提交到 GitHub。   
- com.geek.mymq.Mq为队列实际操作和管理类，维护消息的入队和出队，也维护不同消费组的消费offset，其中使用了LinkedList来模拟队列
- com.geek.mymq.Broker为代理者，负责创建主题、生产者和消费者，同时作为代理来传递消息入队列，拉取消息出队列以及提交ack到队列，来避免生产者和消费者直接和队列交互，降低耦合度
- com.geek.mymq.Producer为生产者，负责发送序列化后的消息数据到broker，其中序列化类为com.geek.mymq.MsgSerializer。  
将序列化、加密、压缩等操作（这里是实现了序列化）放到生产端进行，可以极大地简化broker端的逻辑和降低broker的压力
- com.geek.mymq.Consumer为消费者，但只负责消费主题的调度，比如推或拉模式（这里选择了拉模式），需要拿到哪个主题的消息，以及对原始消息进行解密、解压缩、反序列化的操作（这里是实现了发序列化），  
不关心具体的消费业务逻辑。这里的反序列化类为com.geek.mymq.MsgDeserializer
- 具体的消息消费逻辑就放到了com.geek.mymq.AbstractListener的实现当中，是作为com.geek.mymq.Consumer的poll()方法一个回调函数
- 抽象的消息实体为com.geek.mymq.Message，分消息头和消息体。这里消息体模拟了一个com.geek.mymq.Order实体类
- com.geek.mymq.MyMqApplication的run()为整个项目的测试方法，模拟了2个不同的消费组对主题test1.topic进行同时消费
