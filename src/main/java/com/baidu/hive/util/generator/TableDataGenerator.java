package com.baidu.hive.util.generator;

import com.baidu.hive.driver.DriverBase;
import com.baidu.hive.util.HiveTestUtils;
import com.baidu.hive.util.log.LogUtil;
import com.baidu.hive.util.rand.RandomUtil;
import com.google.common.base.Preconditions;
import org.apache.hadoop.hive.conf.HiveConf;
import org.apache.hadoop.hive.metastore.api.FieldSchema;
import org.apache.hadoop.hive.metastore.api.StorageDescriptor;
import org.apache.hadoop.hive.ql.metadata.Hive;
import org.apache.hadoop.hive.ql.metadata.HiveException;
import org.apache.hadoop.hive.ql.metadata.Table;
import org.apache.hadoop.hive.serde.serdeConstants;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Map;

/**
 * Generate the data for table with textfile format.
 * Not generate data for the partition columns.
 */
public class TableDataGenerator extends DriverBase {

    public static final String FIELD_DELIMITER_KEY = "field.delim";
    private final String dbName;
    private final String tbName;
    private final String fileName;
    private final long size;

    public TableDataGenerator(String dbName, String tbName, String fileName, long size) {
        Preconditions.checkNotNull(dbName, "The dbName can't be null");
        Preconditions.checkNotNull(tbName, "The tbName can't be null");
        this.dbName = dbName;
        this.tbName = tbName;
        this.fileName = fileName;
        this.size = size;
    }

    public void generate() throws HiveException, IOException {
        this.createSession();
        Hive hive = Hive.get();
        Table table = hive.getTable(dbName, tbName);

        this.generateFile(table);
        this.closeSession();
    }

    private void generateFile(Table table) throws IOException {
        List<FieldSchema> fieldSchemas = table.getCols();
        StorageDescriptor storageDescriptor = table.getSd();
        Map<String, String> sdParameters = table.getTTable().getSd().getSerdeInfo().getParameters();
        String delimiter = "\001";
        if (sdParameters.containsKey(FIELD_DELIMITER_KEY)) {
            delimiter = sdParameters.get(FIELD_DELIMITER_KEY);
        }
        if (!"org.apache.hadoop.mapred.TextInputFormat".equals(storageDescriptor.getInputFormat())) {
            LogUtil.log("Table " + table.getTableName() + " is not textfile");
            System.exit(1);
        }
        PrintWriter printWriter = new PrintWriter(new BufferedWriter(new FileWriter(this.fileName)));
        for (long i = 0; i < this.size; i++) {
            for (int col = 0; col < fieldSchemas.size(); col++) {
                FieldSchema schema = fieldSchemas.get(col);
                if (col != 0) {
                    printWriter.print(delimiter);
                }
                if (serdeConstants.STRING_TYPE_NAME.equals(schema.getType())) {
                    printWriter.print(RandomUtil.randomString(10));
                } else if (serdeConstants.DOUBLE_TYPE_NAME.equals(schema.getType())) {
                    printWriter.print(RandomUtil.randomDouble());
                } else if (schema.getType().startsWith(serdeConstants.DECIMAL_TYPE_NAME)){
                    String suffix = schema.getType().substring(serdeConstants.DECIMAL_TYPE_NAME.length());
                    suffix = suffix.substring(1, suffix.length() - 1);
                    String[] suffixArray = suffix.split(",");
                    int scale = Integer.parseInt(suffixArray[0].trim());
                    int precision =Integer.parseInt(suffixArray[1].trim());;
                    printWriter.print(RandomUtil.randomDecimal(scale, precision));
                } else {
                    String msg = "Cannot random for field " + schema.getName() + " with type " + schema.getType();
                    throw new RuntimeException(msg);
                }


            }
            printWriter.println();
        }
        printWriter.close();
    }

    public static void main(String [] args) throws HiveException, IOException {

        HiveConf conf = new HiveConf();
        HiveTestUtils.addResource(conf, args);

        String dbName = conf.get("hive.generator.db-name");
        String tbName = conf.get("hive.generator.table-name");
        String fileName = conf.get("hive.generator.file-name");
        long size = conf.getLong("hive.generator.size", 100L);
        LogUtil.log("TableDataGenerate parameters ",
                    "dbName:" + dbName,
                    "tbName:" + tbName,
                    "fileName:" + fileName,
                    "size:" + size);
        TableDataGenerator generator = new TableDataGenerator(dbName, tbName, fileName, size);
        generator.generate();
    }
}
