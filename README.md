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