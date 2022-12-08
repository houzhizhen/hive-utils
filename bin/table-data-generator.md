## 生成表数据

可以为指定数据库中的数据表生成数据，数据表的类型必须是 textfile。生成文件到当前目录下。
### 生成表数据--示例
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
