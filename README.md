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
### 4.1 数据生成器
可以为指定数据库中的数据表生成数据。详细信息见 [数据生成器.md](bin/table-data-generator.md)

### 4.2 Hive Server监控
配置说明：
把 bin/hive-server-monitor.sh、bin/hive-server-jstack.sh、bin/hive-server-jmx.sh、bin/hive-server-histo.sh 拷贝到 /home/hive/jmx 目录，把 jmx 目录的权限赋给 hive（`chown -R hive: /home/hive/jmx`）。

#### 4.2.1 Hive Server 监控配置
以 hive 账号配置定时任务。如每个小时的第30分钟监控，配置如下

```bash
crontab -e

30 * * * * sh /home/hive/jmx/hive-server-monitor.sh
```

hive-server-monitor.sh 执行以下 3 个脚本。
```
sh hive-server-jmx.sh
sh hive-server-jstack.sh
sh hive-server-histo.sh
```
#### 4.2.2 hive-server-jmx.sh
打印 hive-server 的 jmx 信息到本目录的文件中。

#### 4.2.3 hive-server-jstack.sh
打印 hive-server 的线程栈信息到本目录的文件中。

#### 4.2.3 hive-server-histo.sh
打印 hive-server 的内存对象信息到本目录的文件中。


### 4.3 PartitionGenerator

分区表对应的文件生成器，仅能在表目录里生成文件，添加分区后，并不能 select，因为文件内容都是二进制的`0`。
命令如下：
参考 partition-generator.sh
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

### 4.4 driver-compile.sh 
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


### 4.5 get-functions-from-sql.sh
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


### 4.6 metastore-connect-test.sh
Metastore 连接测试，打印所有的数据库。
```
hive --service jar ./hive-util-0.1.0.jar com.baidu.hive.metastore.MetaStoreConnectTest 
```

### 4.7 multi-thread-metastore-connect-test.sh
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

### 4.8 parallel-jdbc-statement.sh
多线程同时连接 hive server，执行指定 SQL。
```bash
hive --service jar ./hive-util-0.1.0.jar com.baidu.hive.jdbc.ParallelStatementTest \
 --hiveconf hiveUrl=jdbc:hive2://localhost:10000/default;principal=hive/_HOST@BAIDU.COM \
 --hiveconf userName=hive \
 --hiveconf parallelism=2 \
 --hiveconf times=10 \
 --hiveconf 'sql=select 1' \

```
* 参数说明
hiveUrl: HiveServer 的地址
userName: 连接 hiveserver 的用户名
parallelism: 线程的数量
times: 每个线程执行指定 SQL 的次数。
'sql=select 1'： 执行的SQL 内容，因为 SQL 有空格，所以整个参数用单引号括起来。

线程结束后，最后等待 1 小时。可以查看当前进程是否和 metastore 有多个未是否的 tpc 连接，或者 jvm 内有未释放的对象。
