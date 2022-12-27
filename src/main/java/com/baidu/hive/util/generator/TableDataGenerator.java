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
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * Generate the data for table with textfile format.
 * Not generate data for the partition columns.
 */
public class TableDataGenerator extends DriverBase {

    public static final String FIELD_DELIMITER_KEY = "field.delim";
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");
    private static final int DATE_RANGE = 1000 * 60 * 60 * 24 * 3650;
    private static final SimpleDateFormat TIMESTAMP_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private static final int TIMESTAMP_RANGE = 1000 * 60 * 60 * 24 * 3650;
    private final String dbName;
    private final String tbName;
    private final String fileName;
    private final long size;

    public TableDataGenerator(HiveConf conf, String dbName,
                              String tbName, String fileName,
                              long size) {
        super(conf);
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
        // generate the fields metadata
        String[] colTypes = new String[fieldSchemas.size()];
        int[] precisions = new int[fieldSchemas.size()];
        int[] scales = new int[fieldSchemas.size()];
        for (int col = 0; col < fieldSchemas.size(); col++) {
            FieldSchema schema = fieldSchemas.get(col);
            String colType = schema.getType();
            int leftBraceIndex = colType.indexOf('(');
            if (leftBraceIndex == -1) {
                colTypes[col] = colType.trim();
                continue;
            }
            colTypes[col] = colType.substring(0, leftBraceIndex);
            int rightBraceIndex = colType.indexOf(')');
            String precisionAndScale = colType.substring(leftBraceIndex + 1, rightBraceIndex).trim();
            int commaIndex = precisionAndScale.indexOf(",");
            if (commaIndex == -1) {
                precisions[col] = Integer.parseInt(precisionAndScale);
            } else {
                String[] array = precisionAndScale.split(",");
                precisions[col] = Integer.parseInt(array[0].trim());
                scales[col] = Integer.parseInt(array[0].trim());
            }
        }

        // generate data
        Random random = new Random(1L);
        PrintWriter printWriter = new PrintWriter(new BufferedWriter(new FileWriter(this.fileName)));
        for (long i = 0; i < this.size; i++) {
            for (int col = 0; col < colTypes.length; col++) {
                String colType = colTypes[col];
                int precision = precisions[col];
                int scale = scales[col];
                if (col != 0) {
                    printWriter.print(delimiter);
                }
                if (serdeConstants.BOOLEAN_TYPE_NAME.equals(colType)) {
                    printWriter.print(random.nextBoolean());
                } else if (serdeConstants.INT_TYPE_NAME.equals(colType)) {
                    printWriter.print(random.nextInt());
                } else if (serdeConstants.BIGINT_TYPE_NAME.equals(colType)) {
                    printWriter.print(random.nextLong());
                } else if (serdeConstants.STRING_TYPE_NAME.equals(colType)) {
                    printWriter.print(RandomUtil.randomString(10));
                } else if (serdeConstants.CHAR_TYPE_NAME.equals(colType)) {
                    printWriter.print(RandomUtil.randomString(Math.min(precision, 10)));
                } else if (serdeConstants.VARCHAR_TYPE_NAME.equals(colType)) {
                    printWriter.print(RandomUtil.randomString(Math.min(precision, 10)));
                } else if (serdeConstants.FLOAT_TYPE_NAME.equals(colType)) {
                    printWriter.print(random.nextFloat());
                } else if (serdeConstants.DOUBLE_TYPE_NAME.equals(colType)) {
                    printWriter.print(random.nextDouble());
                } else if (serdeConstants.TINYINT_TYPE_NAME.equals(colType)) {
                    printWriter.print((byte)random.nextInt(Byte.MAX_VALUE));
                } else if (serdeConstants.SMALLINT_TYPE_NAME.equals(colType)) {
                    printWriter.print(random.nextInt(Short.MAX_VALUE));
                } else if (serdeConstants.DATE_TYPE_NAME.equals(colType)) {
                    Date date = new Date(System.currentTimeMillis() - random.nextInt(DATE_RANGE));
                    printWriter.print(DATE_FORMAT.format(date));
                } else if (serdeConstants.TIMESTAMP_TYPE_NAME.equals(colType)) {
                    Date date = new Date(System.currentTimeMillis() - random.nextInt(TIMESTAMP_RANGE));
                    printWriter.print(TIMESTAMP_FORMAT.format(date));
                } else if (serdeConstants.DECIMAL_TYPE_NAME.equals(colType)){
                    printWriter.print(RandomUtil.randomDecimal(precision, scale));
                } else {
                    String msg = "Cannot random for field " + fieldSchemas.get(col).getName() + " with type " + colTypes[col];
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
        TableDataGenerator generator = new TableDataGenerator(conf, dbName,
                                                              tbName, fileName, size);
        generator.generate();
        System.exit(0);
    }
}
