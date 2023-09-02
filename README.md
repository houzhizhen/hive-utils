# hive 实用工具集

## 1. 编译
```bash
mvn clean package -DskipTests
```
把 target/hive-util-0.1.0.jar 拷贝到目标集群执行。

## 2. 执行方法
```bash
hive service jar hive-util-0.1.0.jar ${full-class-name} 
```
* 参数
以下示例设置 2 个参数
```bash
hive service jar hive-util-0.1.0.jar ${full-class-name} \
--hiveconf para1=value1 \
--hiveconf para2=value2 
```
* 指定参数文件
以下示例指定2个参数和一个配置文件。
```bash
hive service jar hive-util-0.1.0.jar ${full-class-name} \
--hiveconf para1=value1 \
--hiveconf para2=value2 \
/etc/tez/conf/tez-site.xml
```

* debug`
直接加上 --debug 就可以 debug，监听本地 8000 端口。
```bash
hive --debug service jar hive-util-0.1.0.jar ${full-class-name} \
--hiveconf para1=value1 \
--hiveconf para2=value2 \
/etc/tez/conf/tez-site.xml
```
## 3. bin 目录
bin 目录有各命令执行脚本

## 4. 支持命令列表
### 4.1 ## 生成表数据

可以为指定数据库中的数据表生成数据，数据表的类型必须是 textfile。生成文件到当前目录下。
### 4.2 生成表数据--示例
* 创建表
```sql
use default;
drop table if exists generate_table_data_test;
create table generate_table_data_test (
 c_boolean boolean,
 c_int int, 
 c_bigint bigint, 
 c_string string, 
 c_char char(20), 
 c_varchar varchar(200),
 c_float float,
 c_double double, 
 c_tinyint tinyint, 
 c_smallint smallint, 
 c_date date, 
 c_timestamp timestamp,
 c_decimal decimal(18,2)) stored as textfile;
```

* 生成数据到当前目录
  生成命令如下：
```bash
hive --service jar ./hive-util-0.1.0.jar com.baidu.hive.util.generator.TableDataGenerator \
 --hiveconf hive.generator.db-name=default \
 --hiveconf hive.generator.table-name=generate_table_data_test \
 --hiveconf hive.generator.file-name=generate_table_data_test.txt \
 --hiveconf hive.generator.size=100
```
参数说明：
hive.generator.db-name: 数据库名称
hive.generator.table-name: 表的名称
hive.generator.file-name: 数据文件的名称
hive.generator.size: 生成的记录条数

* 加载数据
  进入 hive 环境
```sql
use default;
load data local inpath './generate_table_data_test.txt' overwrite into table generate_table_data_test; 
```
其他说明:
字段类型并没有全覆盖，只覆盖了常见的类型。

### 4.2 监控
#### 4.2.1 Hive Server 监控
配置说明：
把 bin/hive-server-monitor.sh、bin/hive-server-jstack.sh、bin/hive-server-jmx.sh、bin/hive-server-histo.sh、hive-server-socket.sh 拷贝到 /home/hive/jmx 目录，把 jmx 目录的权限赋给 hive（`chown -R hive: /home/hive/jmx`）。

##### 4.2.1.1 Hive Server 监控配置
以 hive 账号配置定时任务。如每个小时的第30分钟监控，配置如下

```bash
crontab -e

30 * * * * sh /home/hive/jmx/hive-server-monitor.sh
```

hive-server-monitor.sh 执行以下 4 个脚本。
```
sh hive-server-jmx.sh
sh hive-server-jstack.sh
sh hive-server-histo.sh
sh hive-server-socket.sh
```
##### 4.2.1.2 hive-server-jmx.sh
打印 hive-server 的 jmx 信息到 logs 目录下的相应的文件中。

##### 4.2.1.3 hive-server-jstack.sh
打印 hive-server 的线程栈信息到 logs 目录下的相应的文件中。

##### 4.2.1.4 hive-server-histo.sh
打印 hive-server 的内存对象信息到 logs 目录下的相应的文件中。

##### 4.2.1.5 hive-server-socket.sh
打印 hive-server 的 socket 连接信息到 logs 目录下的相应的文件中。


#### 4.2.2 Hive Metastore 监控
配置说明：
把 bin/hive-metastore-monitor.sh、bin/hive-metastore-jstack.sh、bin/hive-metastore-histo.sh
、bin/hive-metastore-socket.sh 拷贝到 /home/hive/jmx 目录，把 jmx 目录的权限赋给 hive（`chown -R hive: /home/hive/jmx`）。

##### 4.2.2.1 Hive Server 监控配置
以 hive 账号配置定时任务。如每个小时的第30分钟监控，配置如下

```bash
crontab -e

30 * * * * sh /home/hive/jmx/hive-metastore-monitor.sh
```

hive-server-monitor.sh 执行以下 3 个脚本。
```
sh hive-metastore-jstack.sh
sh hive-metastore-histo.sh
sh hive-metastore-socket.sh
```

##### 4.2.2.2 hive-metastore-jstack.sh
打印 hive-metastore 的线程栈信息到 logs 目录下的相应的文件中。

##### 4.2.2.3 hive-metastore-histo.sh
打印 hive-metastore 的内存对象信息到 logs 目录下的相应的文件中。

##### 4.2.2.4 hive-metastore-socket.sh
打印 hive-metastore 的 socket 连接信息到 logs 目录下的相应的文件中。


### 4.3 Partition Generator

分区表对应的文件生成器，仅能在表目录里生成文件，添加分区后，并不能 select，因为文件内容都是二进制的`0`。
命令如下：
```bash
hive --service jar ./hive-util-0.1.0.jar com.baidu.hive.util.generator.PartitionGenerator \
     -hiveconf base-path=hdfs://localhost:9000/home/disk1/hive/hive-313/tp \
     -hiveconf part-names=dt,hour \
     -hiveconf sub-dirs=3,2 \
     -hiveconf files-in-partition=2 \
     -hiveconf file-size=1024
```
* 参数说明
base-path: 表的目录
part-names: 分区字段，中间用`,`分割。如示例中有2个分区字段，分别为`dt`和`hour`。
sub-dirs: 每层目录的数量，中间用`,`分割，和 part-names 的数量对应。如示例中 dt 这一层有3个目录, 分别为 dt=0, dt=1, dt=2。每个 dt 下，有2个 hour 子目录，分别为 hour=0,hour=1。
files-in-partition: 每个分区的文件数量。
file-size: 每个分区中文件的大小。

### 4.4 compile specified sql files
编译指定目录下的所有后缀为 '.sql' 的文件。
```
hive --service jar ./hive-util-0.1.0.jar com.baidu.hive.comiple.DriverCompile \
    --hiveconf database=tpcds_hdfs_orc_3 \
    --hiveconf path=/home/hive/hive-testbench/sample-queries-tpcds/ \
    --hiveconf iterators=1
```
* 参数说明
database: 数据库名. 如示例中先执行 use tpcds_hdfs_orc_3;
path: SQL 的所在目录。
iterators: 执行次数，如为2，则 directory 下的所有 '.sql' 文件执行 2 次。


### 4.5 get functions from sql
编译指定目录下的所有后缀为 '.sql' 的文件。
```
hive --service jar ./hive-util-0.1.0.jar com.baidu.hive.function.GetFunctionsFromSQL \
  --hiveconf path=/home/hive/hive-testbench/sample-queries-tpcds/ \
  --hiveconf suffix=sql
```
* 参数说明
path: SQL 的所在目录。
suffix: path 目录下文件的后缀。

输出出两列，第 1 列是函数名，第 2 列是函数所在的 SQL.


### 4.6 metastore connect test
Metastore 连接测试，打印所有的数据库。
```bash
hive --service jar ./hive-util-0.1.0.jar com.baidu.hive.metastore.MetaStoreConnectTest 
```

### 4.7 multi thread metastore connect test
多线程 metastore 连接测试，看 metastore 的多次连接是否能释放。
```bash
hive --service jar ./hive-util-0.1.0.jar com.baidu.hive.metastore.MultiThreadMetaStoreConnectTest \
     --hiveconf metastore.connection.count=20 \
     --hiveconf thread.count=2
```
* 参数说明
metastore.connection.count: 每个线程执行 metastore 连接的数量
thread.count: 线程的数量

metastore 连接执行以下操作：
```java
IMetaStoreClient metaStoreClient = MetaStoreUtil.createMetaStoreClient(hiveConf);
metaStoreClient.getAllDatabases();
metaStoreClient.close();
```
线程结束后，最后等待 1 小时。可以查看当前进程是否和 metastore 有多个未是否的 tpc 连接，或者 jvm 内有未释放的对象。

### 4.8 Execute statement parallel through jdbc with a single connection
创建一个 JDBC 连接，多线程同时使用这个连接，执行指定 SQL，每个线程执行指定次数。
```bash
hive --service jar ./hive-util-0.1.0.jar com.baidu.hive.jdbc.ParallelStatementTest \
 --hiveconf hiveUrl=jdbc:hive2://localhost:10000/default;principal=hive/_HOST@BAIDU.COM \
 --hiveconf userName=hive \
 --hiveconf parallelism=2 \
 --hiveconf times=10 \
 --hiveconf 'sql=select 1' 
```
* 参数说明
hiveUrl: HiveServer 的地址
userName: 连接 hiveserver 的用户名
parallelism: 线程的数量
times: 每个线程执行指定 SQL 的次数。
'sql=select 1'： 执行的SQL 内容，因为 SQL 有空格，所以整个参数用单引号括起来。

线程结束后，最后等待 1 小时。可以查看当前进程是否和 metastore 有多个未是否的 tpc 连接，或者 jvm 内有未释放的对象。

## 4.9 Execute statement parallel through jdbc with a connection for each thread

和 4.8 不同的是：本程序每个线程创建一个 JDBC 连接，执行指定 SQL，每个线程执行指定次数，每次执行之后 SLEEP 一段时间。
```bash
hive --service jar ./hive-util-0.1.0.jar com.baidu.hive.jdbc.MultiThreadStatementTest \
 --hiveconf hiveUrl=jdbc:hive2://localhost:10000/default \
 --hiveconf userName=hive \
 --hiveconf parallelism=2 \
 --hiveconf times=10 \
 --hiveconf 'sql=select 1' \
 --hiveconf sleepSeconds=10 \
 --hiveconf print-log-each-statement=true \
 --hiveconf create-connection-each-statement=true
```
* 参数说明
  hiveUrl: HiveServer 的地址
  userName: 连接 hiveserver 的用户名
  parallelism: 线程的数量
  times: 每个线程执行指定 SQL 的次数。
  'sql=select 1'： 执行的SQL 内容，因为 SQL 有空格，所以整个参数用单引号括起来。
  sleepSeconds: 每执行一次 SQL, sleep 多长时间。
  print-log-each-statement: 是否在每个SQL执行之后打印日志
  create-connection-each-statement: 是否在执行每个 SQL 时单独建立一个 jdbc 连接。如果否，则一个线程一个 jdbc 连接。
* 
## 4.10 multi thread connection at fixed period-test

本程序每隔一段时间，每个线程创建一个 JDBC 连接，执行指定 SQL，执行之后关闭连接。
用于测试以固定的速度和 hive-server建立会话，提交任务到 hive-server上，并且关闭会话。hive-server 的最大承受能力。是否执行速度越来越慢。
```bash
hive --service jar ./hive-util-0.1.0.jar com.baidu.hive.jdbc.MultiConnectionAtFixedPeriodTest \
 --hiveconf hiveUrl=jdbc:hive2://localhost:10000/default \
 --hiveconf userName=hive \
 --hiveconf parallelism=2 \
 --hiveconf 'sql=select 1' \
 --hiveconf intervalSeconds=1 
```
* 参数说明
  hiveUrl: HiveServer 的地址
  userName: 连接 hiveserver 的用户名
  intervalSeconds: 启动线程的周期。并每隔此周期，输出SQL的平均执行时间。
  parallelism: 每个周期启动的线程的数量
  'sql=select 1'： 执行的SQL 内容，因为 SQL 有空格，所以整个参数用单引号括起来。
  
## 4.11 MetaStore: 多线程创建并删除数据库测试

每个线程都分配一个序号，第1个为0。那么第1个线程就不断的执行 create database db0;drop database db0。
然后等待一段时间。在进入循环之前，如果数据库存在，则先删除。用于测试 metastore 的性能。
```bash
hive --service jar ./hive-util-0.1.0.jar com.baidu.hive.metastore.MetaStoreCreateAndDropDbTest \
     --hiveconf metastore.createAndDropDb.count=10 \
     --hiveconf sleep.seconds.between.operation=10 \
     --hiveconf thread.count=1
```
* 参数说明
  metastore.createAndDropDb.count: 每个线程执行 create database 和 drop database 的次数。
  sleep.seconds.between.operation: 每个线程执行 create database 和 drop database 后的等待时间。
  thread.count: 线程的个数。

## 4.12 monitor-delegation-token.sh
监控 delegation token 的数量，并且输出从数据库里查询一个 delegation token 的时间，看数据库速度是否正常。

## 4.13 Read All Tokens From MetaStore
```bash
hive --service jar hive-util-0.1.0.jar com.baidu.hive.security.token.ReadAllTokensFromStore
```
从 MetaStore 里读取所有的 Delegation Token 并且对内容进行解析，然后输出到 以 "hive-metastore-delegation-token-" 开头的本地文件，
文件名后面是生成的时间信息。

## 4.12 monitor-delegation-token.sh
监控 delegation token 的数量，并且输出从数据库里查询一个 delegation token 的时间，看数据库速度是否正常。

## 4.13 Read All Tokens From MetaStore
```bash
hive --service jar hive-util-0.1.0.jar com.baidu.hive.security.token.ReadAllTokensFromStore
```
从 MetaStore 里读取所有的 Delegation Token 并且对内容进行解析，然后输出到 以 "hive-metastore-delegation-token-" 开头的本地文件，
文件名后面是生成的时间信息。


## 4.14 ThreadLeakQueryLiftTimeHook 测试线程溢出分析
[jdk-endorsed](https://github.com/houzhizhen/jdk-endorsed)

hive-site.xml 配置
```xml
 <property>
    <name>hive.query.lifetime.hooks</name>
    <value>com.baidu.hive.ql.hooks.ThreadLeakQueryLiftTimeHook</value>
    <description>A comma separated list of hooks which implement QueryLifeTimeHook. These will be triggered before/after query compilation and before/after query execution, in the order specified.Implementations of QueryLifeTimeHookWithParseHooks can also be specified in this list. If they arespecified then they will be invoked in the same places as QueryLifeTimeHooks and will be invoked during pre and post query parsing</description>
  </property>
```

## 4.15 使用配置项的 RowFilter 

### 4.15.1 创建表
```sql
create table filter(c1 int, c2 int,c3 int);
insert into filter values(1,2,3);
insert into filter values(4,5,6);
```
### 4.15.2 hive-site.xml 配置
仅可以读取 c2 = 2 的记录。
```xml
  <property>
    <name>hive.security.authorization.manager</name>
    <value>com.baidu.hive.authorizer.RowFilterAuthorizerFactory</value>
  </property>
  <property>
    <name>rowfilter.db-name</name>
    <value>default</value>
  </property>
  <property>
    <name>rowfilter.table-name</name>
    <value>filter</value>
  </property>
  <property>
    <name>rowfilter.expression</name>
    <value>c2 = 2</value>
  </property>
```
### 4.15.3 测试
仅检索出 `c2 = 2` 的记录，符合预期。
```sql
hive> select * from filter;
OK
filter.c1 filter.c2 filter.c3
1 2 3
Time taken: 0.749 seconds, Fetched: 1 row(s)
```

## 4.16 HiveConfDiff
输出 Hive 变更的配置项。

```bash
hive --service jar hive-util-0.1.0.jar com.baidu.hive.conf.HiveConfDiff 
```

## 4.17 ConfDiff
比较两组文件值不一样的配置项。conf.default-files是默认值，可以有多个,中间用','分割。

```bash
hive --service jar hive-util-0.1.0.jar com.baidu.hive.conf.HiveConfDiff 
```

```bash
hive --service jar target/hive-util-0.1.0.jar com.baidu.hive.conf.ConfDiff  \
 --hiveconf conf.default-files=hdfs-default.xml   \
 --hiveconf conf.files=hdfs-site.xml
```
```bash
hive --service jar target/hive-util-0.1.0.jar com.baidu.hive.conf.ConfDiff  \
 --hiveconf conf.default-files=yarn-default.xml   \
 --hiveconf conf.files=yarn-site.xml
```

```bash
hive --service jar target/hive-util-0.1.0.jar com.baidu.hive.conf.ConfDiff  \
 --hiveconf conf.default-files=hive-default.xml,hivemetastore-default.xml,hiveserver2-default.xml \
 --hiveconf conf.files=hive-site.xml,hivemetastore-site.xml,hive-server2-site.xml
```

## 4.18 MultiThreadLongTimeTest
```bash
hive --service jar hive-util-0.1.0.jar com.baidu.hive.metastore.MultiThreadLongTimeTest  \
 --hiveconf thread.count=100 \
 --hiveconf metastore.api.exec.count.per-thread=1000000 \
 --hiveconf metastore.api.log.every.n-calls=1000
```

## 4.19 GenerateSchemaFromSql(Not completed)
```bash
cp input.sql target
cd target
hive --service jar hive-util-0.1.0.jar com.baidu.hive.util.generator.GenerateSchemaFromSql \
  --hiveconf hive.generator.sql.input.file-name=input.sql \
  --hiveconf hive.generator.sql.output.file-name=output.sql
--hiveconf hive.generator.sql.input.file-name=input.sql --hiveconf hive.generator.sql.output.file-name=output.sql

## 4.19 AddIn Persistent Function
```bash
hadoop fs -put hive-util-0.1.0.jar /user/hive/hive-util-0.1.0.jar;
```
hive 执行
```sql
create function add_int as 'com.baidu.hive.func.AddInt' using jar 'hdfs://bmr-master-8905dd3:8020//user/hive/hive-util-0.1.0.jar';
hive> desc function extended add_int;
OK
There is no documentation for function 'add_int'
Function class:com.baidu.hive.func.AddInt
Function type:PERSISTENT
Resource:hdfs://bmr-master-8905dd3:8020//user/hive/hive-util-0.1.0.jar
Time taken: 0.27 seconds, Fetched: 4 row(s)
hive> select add_int(1);
2
```

## 4.20 HiveMetastoreConfPrint
```bash
hive --service jar hive-util-0.1.0.jar com.baidu.hive.conf.HiveMetastoreConfPrint
```
## 4.21 PrintAstOfSql--把SQL 解析为抽象语法树，并打印输出
```bash
hive --service jar hive-util-0.1.0.jar com.baidu.hive.comiple.PrintAstOfSql \
--hiveconf 'hive.sql=select distinct(c1) from (select 1 c1)t'
```

## 4.21 CountApplications
```bash
hive --service jar hive-util-0.1.0.jar com.baidu.java.json.CountApplications /Users/houzhizhen/git/hive-utils/apps
```

## 4.22 CompareHistoFile
```bash
hive --service jar hive-util-0.1.0.jar \
com.baidu.java.jdk.CompareHistoFile \
/Users/houzhizhen/Documents/2023/yinshang/20230721/master2/20230721-141502-hive-server-histo-live.log \
/Users/houzhizhen/Documents/2023/yinshang/20230721/master2/20230721-151529-hive-server-histo-live.log
```
输出内容如下：
```bash
compare by size:
[C : 35471169376
[Ljava.lang.Object; : 958692040
org.apache.calcite.rex.RexCall : 892796784
com.google.common.collect.RegularImmutableList : 709257360
java.lang.String : 591558368
org.apache.calcite.rex.RexInputRef : 151330080
com.google.common.collect.SingletonImmutableList : 27737440
java.util.HashMap$Node : 15832320
java.util.Hashtable$Entry : 13398816
java.util.concurrent.ConcurrentHashMap$Node : 10901760
compare by ratio:
org.apache.calcite.rex.RexCall : 232500
org.apache.calcite.rex.RexInputRef : 29465
com.google.common.collect.RegularImmutableList : 452
org.codehaus.commons.compiler.Location : 256
org.apache.calcite.util.graph.DefaultEdge : 209
org.apache.calcite.plan.hep.HepRelVertex : 196
com.google.common.collect.SingletonImmutableList : 190
org.apache.hadoop.hive.ql.optimizer.calcite.reloperators.HiveFilter : 186
org.apache.calcite.runtime.FlatLists$Flat2List : 134
org.apache.calcite.plan.RelTraitSet$Cache : 131
```
分为两个部分，第1部分是compare by size:，是第2个文件中占用的字节数减第1个文件中文件的字节数，取前10名。

## DirectMoveTask
模拟以下SQL的最后一个阶段从分布式文件系统下载文件到本地。
```sql
insert overwrite local directory '/home/hive/out' select * from hzz1.t1;
```
执行名：
```bash
 hive --service jar hive-util-0.1.0.jar \
  com.baidu.hive.task.DirectMoveTask \
  --hiveconf hive.move.task.source.path=bos://spark-data-bd/houzhizhen/warehouse/hzz1.db/t1 \
  --hiveconf hive.move.task.dest.path=/home/hive/out
```