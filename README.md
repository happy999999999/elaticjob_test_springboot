<center>Elastic-Job-Lite技术分享</center>

## 一、简介

Elastic-Job是一个开源分布式调度解决方案，由两个相互独立的子项目Elastic-Job-Lite和Elastic-Job-Cloud组成。

Elastic-Job-Lite定位为轻量级无中心化解决方案，使用 jar 的形式提供分布式任务的协调服务。

![img](https://raw.githubusercontent.com/happy999999999/images/main/elaticjob_test_springboot/1.png)

---

## 二、快速入门

1、引入MAVEN

```xml
<dependency>
            <groupId>org.apache.shardingsphere.elasticjob</groupId>
            <artifactId>elasticjob-lite-spring-boot-starter</artifactId>
            <version>3.0.0</version>
</dependency>
```

2、作业开发

```java
public class SecondJob implements SimpleJob {
    @Override
    public void execute(ShardingContext shardingContext) {
        //do something
        log.info(this.getClass().getName()+",this context is:"+ shardingContext
                +", now time is :"+new Date(System.currentTimeMillis()));
        //通过区分当前分片来执行不同的任务
        switch (shardingContext.getShardingItem()) {
            case 0:
                // do something by sharding item 0
                break;
            case 1:
                // do something by sharding item 1
                break;
            case 2:
                // do something by sharding item 2
                break;
            // case n: ...
        }
    }
}
```

3、作业配置

```yaml
elasticjob:
  regCenter:
    serverLists: 192.168.20.176:2181 #zk地址
    namespace: elasticjob-lite-springboot #命名空间
  jobs:
    secondJob:
      elasticJobClass: test.job.MyJob #任务执行类
      cron: 0/5 * * * * ? #定时任务
      shardingTotalCount: 4 #分片总数
      shardingItemParameters: 0=Beijing,1=Shanghai,2=Guangzhou,3=QINGDAO #每个分片参数，格式：分片序号=分片参数
```

----

## 三、功能特性

### 1、分布式调度协调：用ZK实现注册中心。

注册中心在定义的命名空间下，创建作业名称节点，用于区分不同作业，所以作业一旦创建则不能修改作业名称，如果修改名称将视为新的作业。 作业名称节点下又包含5个数据子节点，分别是 config, instances, sharding, servers 和 leader。

补充：当作业一旦创建，后期修改只能通过运维平台修改，或重命名作业名称以创建产生新的作业使用。

---

### 2、错过执行作业重触发与失效转移

#### 错过执行作业重触发

错过任务重执行功能可以使逾期未执行的作业在之前作业执行完成之后立即执行。 举例说明，若作业以每小时为间隔执行，每次执行耗时 30 分钟。如下如图所示。

![img](https://raw.githubusercontent.com/happy999999999/images/main/elaticjob_test_springboot/2.png)

图中表示作业分别于 12:00，13:00 和 14:00 执行。图中显示的当前时间点为 13:00 的作业执行中。

如果 12：00 开始执行的作业在 13:10 才执行完毕，那么本该由 13:00 触发的作业则错过了触发时间，需要等待至 14:00 的下次作业触发。 如下如图所示。

![img](https://raw.githubusercontent.com/happy999999999/images/main/elaticjob_test_springboot/3.png)

在开启错过任务重执行功能之后，ElasticJob 将会在上次作业执行完毕后，立刻触发执行错过的作业。如下图所示。

![img](https://raw.githubusercontent.com/happy999999999/images/main/elaticjob_test_springboot/4.png)

在 13：00 和 14:00 之间错过的作业将会重新执行。

##### 适用场景

在一次运行耗时较长且间隔较长的作业场景，错过任务重执行是提升作业运行实时性的有效手段； 对于未见得关注单次作业的实时性的短间隔的作业来说，开启错过任务重执行并无必要。

##### 开启方式

1、通过标记任务属性misfire: true来开启。

2、通过运维平台勾选支持错过重执行。

#### 失效转移

失效转移是当前执行作业的临时补偿执行机制，在下次作业运行时，会通过重分片对当前作业分配进行调整。 举例说明，若作业以每小时为间隔执行，每次执行耗时 30 分钟。如下如图所示。

![img](https://raw.githubusercontent.com/happy999999999/images/main/elaticjob_test_springboot/5.png)

图中表示作业分别于 12:00，13:00 和 14:00 执行。图中显示的当前时间点为 13:00 的作业执行中。

如果作业的其中一个分片服务器在 13:10 的时候宕机，那么剩余的 20 分钟应该处理的业务未得到执行，并且需要在 14:00 时才能再次开始执行下一次作业。 也就是说，在不开启失效转移的情况下，位于该分片的作业有 50 分钟空档期。如下如图所示。

![img](https://raw.githubusercontent.com/happy999999999/images/main/elaticjob_test_springboot/6.png)

在开启失效转移功能之后，ElasticJob 的其他服务器能够在感知到宕机的作业服务器之后，补偿执行该分片作业。如下图所示。

![img](https://raw.githubusercontent.com/happy999999999/images/main/elaticjob_test_springboot/7.png)

在资源充足的情况下，作业仍然能够在 13:30 完成执行。

##### 适用场景

开启失效转移功能，ElasticJob 会监控作业每一分片的执行状态，并将其写入注册中心，供其他节点感知。

在一次运行耗时较长且间隔较长的作业场景，失效转移是提升作业运行实时性的有效手段； 对于间隔较短的作业，会产生大量与注册中心的网络通信，对集群的性能产生影响。 而且间隔较短的作业并未见得关注单次作业的实时性，可以通过下次作业执行的重分片使所有的分片正确执行，因此不建议短间隔作业开启失效转移。

另外需要注意的是，作业本身的幂等性，是保证失效转移正确性的前提。

##### 开启方式

1、通过标记任务属性failover: true来开启。

2、通过运维平台勾选支持自动失效转移。

---

### 3、任务分片

作业运行起来后，服务器、实例、线程、分片的关系图：

![img](https://raw.githubusercontent.com/happy999999999/images/main/elaticjob_test_springboot/8.png)

创建作业时，需要设置这个作业有多少个分片来执行，如果作业A在机器1上获得了两个分片，那么这两个分片实际上是两个线程（注意是一台机器上获得两个分片，每台机器上装一个elastic-job 服务的情况下），这两个线程共用的是同一个class实例，也就是说这两个分片 会共享 此class实例的成员变量。分片1修改了实例的成员变量，就会被分片2读到，从而影响分片2的作业逻辑。

配置方式：

```yaml
shardingTotalCount: 3 #分片总数
shardingItemParameters: 0=Beijing,1=Shanghai,2=Guangzhou #每个分片参数，格式：分片序号=分片参数
```

#### 任务分配方式

ElasticJob 中任务分片项的概念，使得任务可以在分布式的环境下运行，每台任务服务器只运行分配给该服务器的分片。 随着服务器的增加或宕机，ElasticJob 会近乎实时的感知服务器数量的变更，从而重新为分布式的任务服务器分配更加合理的任务分片项，使得任务可以随着资源的增加而提升效率。

任务的分布式执行，需要将一个任务拆分为多个独立的任务项，然后由分布式的服务器分别执行某一个或几个分片项。

举例说明，如果作业分为 4 片，用两台服务器执行，则每个服务器分到 2 片，分别负责作业的 50% 的负载，如下图所示。

![img](https://raw.githubusercontent.com/happy999999999/images/main/elaticjob_test_springboot/9.png)

#### 资源最大限度利用

ElasticJob 提供最灵活的方式，最大限度的提高执行作业的吞吐量。 当新增加作业服务器时，ElasticJob 会通过注册中心的临时节点的变化感知到新服务器的存在，并在下次任务调度的时候重新分片，新的服务器会承载一部分作业分片，如下图所示。

![img](https://raw.githubusercontent.com/happy999999999/images/main/elaticjob_test_springboot/10.png)

将分片项设置为大于服务器的数量，最好是大于服务器倍数的数量，作业将会合理的利用分布式资源，动态的分配分片项。

例如：3 台服务器，分成 10 片，则分片项分配结果为服务器 A = 0,1,2,9；服务器 B = 3,4,5；服务器 C = 6,7,8。 如果服务器 C 崩溃，则分片项分配结果为服务器 A = 0,1,2,3,4; 服务器 B = 5,6,7,8,9。 在不丢失分片项的情况下，最大限度的利用现有资源提高吞吐量。

#### 高可用

当作业服务器在运行中宕机时，注册中心同样会通过临时节点感知，并将在下次运行时将分片转移至仍存活的服务器，以达到作业高可用的效果。 本次由于服务器宕机而未执行完的作业，则可以通过失效转移的方式继续执行。如下图所示。

![img](https://raw.githubusercontent.com/happy999999999/images/main/elaticjob_test_springboot/11.png)

将分片总数设置为 1，并使用多于 1 台的服务器执行作业，作业将会以 1 主 n 从的方式执行。 一旦执行作业的服务器宕机，等待执行的服务器将会在下次作业启动时替补执行。开启失效转移功能效果更好，如果本次作业在执行过程中宕机，备机会立即替补执行。

#### 分片策略

##### 1、平均分片策略（默认）

类型：AVG_ALLOCATION

根据分片项平均分片。

如果作业服务器数量与分片总数无法整除，多余的分片将会顺序的分配至每一个作业服务器。

举例说明：

1. 如果 3 台作业服务器且分片总数为9，则分片结果为：1=[0,1,2], 2=[3,4,5], 3=[6,7,8]；
2. 如果 3 台作业服务器且分片总数为8，则分片结果为：1=[0,1,6], 2=[2,3,7], 3=[4,5]；
3. 如果 3 台作业服务器且分片总数为10，则分片结果为：1=[0,1,2,9], 2=[3,4,5], 3=[6,7,8]。

##### 2、奇偶分片策略

类型：ODEVITY

根据作业名称哈希值的奇偶数决定按照作业服务器 IP 升序或是降序的方式分片。

如果作业名称哈希值是偶数，则按照 IP 地址进行升序分片； 如果作业名称哈希值是奇数，则按照 IP 地址进行降序分片。 可用于让服务器负载在多个作业共同运行时分配的更加均匀。

举例说明：

1. 如果 3 台作业服务器，分片总数为2且作业名称的哈希值为偶数，则分片结果为：1 = [0], 2 = [1], 3 = []；
2. 如果 3 台作业服务器，分片总数为2且作业名称的哈希值为奇数，则分片结果为：3 = [0], 2 = [1], 1 = []。

##### 3、轮询分片策略

类型：ROUND_ROBIN

根据作业名称轮询分片。

#### 作业线程池处理策略

##### 1、CPU 资源策略

类型：CPU。根据 CPU 核数 * 2 创建作业处理线程池。

##### 2、单线程策略

类型：SINGLE_THREAD。使用单线程处理作业。

----

### 4、作业类型（Simple、DataFlow、Script）

#### SimpleJob：简单实现，执行execute()方法。需实现 SimpleJob 接口。

作业实现：

```java
public class MyJob implements SimpleJob {

    @Override
    public void execute(ShardingContext shardingContext) {
        log.info(this.getClass().getName() + ",this context is:" + shardingContext 
                + ", now time is :" + new Date(System.currentTimeMillis()));
    }
}
```

任务配置：

```yaml
elasticjob:
  regCenter:
    serverLists: 192.168.20.176:2181 #zk地址
    namespace: elasticjob-lite-springboot #命名空间
  jobs:
    myJob:
      elasticJobClass: test.job.MyJob #任务执行类
      cron: 0/5 * * * * ? #定时任务
      shardingTotalCount: 4 #分片总数
      shardingItemParameters: 0=Beijing,1=Shanghai,2=Guangzhou,3=QINGDAO #每个分片参数，格式：分片序号=分片参数
      misfire: true #错过执行作业重触发
      failover: true #自动失效转移
```

#### DataFlowJob：用于处理数据流，必须实现 fetchData()和processData()的方法，fetchData()用来获取数据，processData()用来处理获取到的数据。

作业实现：

```java
public class ThirdJob implements DataflowJob {
    @Override
    public List fetchData(ShardingContext shardingContext) {
        log.info("sharding item = {},start fetch data",shardingContext.getShardingItem());
        return Arrays.asList("jack","mack","dasi");
    }

    @Override
    public void processData(ShardingContext shardingContext, List list) {
        log.info("sharding item = {},list={}",shardingContext.getShardingItem(),list.toString());
    }
}
```

任务配置：与SimpleJob相同

```yaml
elasticjob:
  regCenter:
    serverLists: 192.168.20.176:2181 #zk地址
    namespace: elasticjob-lite-springboot #命名空间
  jobs:
    thirdJob:
      elasticJobClass: test.job.ThirdJob
      cron: 0/5 * * * * ?
      shardingTotalCount: 2
      shardingItemParameters: 0=Beijing,1=Shanghai
```

#### ScriptJob：Script 类型作业意为脚本类型作业，支持 shell，python，perl 等所有类型脚本。

任务配置：与SimpleJob、DataFlowJob配置不同

```yaml
elasticjob:
  regCenter:
    serverLists: 192.168.20.176:2181 #zk地址
    namespace: elasticjob-lite-springboot #命名空间
  jobs:
    fourthJobIsScriptJob:
      elasticJobType: SCRIPT #任务类型为脚本
      cron: 0/10 * * * * ? #定时任务
      shardingTotalCount: 3 #分片总数
      props:
        script.command.line: "echo SCRIPT Job: " #脚本执行代码
```

---

### 5、Elatic-Job-Lite运维平台

测试环境地址：http://192.168.9.55:8088/

账号：root	密码：root

#### 配置

1、进入首页，通过添加按钮自己项目的注册中心。

![img](https://raw.githubusercontent.com/happy999999999/images/main/elaticjob_test_springboot/12.png)

2、添加

注册中心名称：自定义。	注册中心地址：zk地址。	命名空间：同配置文件中的命名空间。

![img](https://raw.githubusercontent.com/happy999999999/images/main/elaticjob_test_springboot/13.png)

3、连接注册中心后，即可在作业操作中的作业维度和服务器维度进行控制。

#### 作业维度

在作业维度中，可以看到当前配置中心的所有作业情况以及对各个作业进行控制。

![img](https://raw.githubusercontent.com/happy999999999/images/main/elaticjob_test_springboot/14.png)

可修改的参数：

![img](https://raw.githubusercontent.com/happy999999999/images/main/elaticjob_test_springboot/15.png)

#### 服务器维度

服务器维度可以控制各个服务器的状态以及控制每个服务器下每个任务的状态。

![img](https://raw.githubusercontent.com/happy999999999/images/main/elaticjob_test_springboot/16.png)

![img](https://raw.githubusercontent.com/happy999999999/images/main/elaticjob_test_springboot/17.png)

----

引用：

测试代码地址：https://github.com/happy999999999/elaticjob_test_springboot

elaticjob代码地址：https://github.com/apache/shardingsphere-elasticjob

elaticjob说明文档：https://shardingsphere.apache.org/elasticjob/current/cn/overview/