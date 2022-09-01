package com.baidu.hive.metastore;

import com.baidu.hive.util.Assert;
import com.baidu.hive.util.HiveTestUtils;
import com.baidu.hive.util.log.LogUtil;
import org.apache.hadoop.hive.conf.HiveConf;
import org.apache.hadoop.hive.metastore.IMetaStoreClient;
import org.apache.hadoop.hive.metastore.api.Database;
import org.apache.hadoop.hive.metastore.api.MetaException;
import org.apache.hadoop.hive.metastore.api.PrincipalType;
import org.apache.hadoop.hive.ql.metadata.Table;
import org.apache.thrift.TException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Test MetaStore api
 */
public class MetaStoreApiTest {

    private static String CATALOG_LOCATION_KEY="hive.catalog.location";
    private static String CATALOG_LOCATION_DEFAULT = "hdfs://localhost:9000/user/hive/warehouse";

    private String catalogLocation;

    private HiveConf hiveConf;
    private IMetaStoreClient client;

    public MetaStoreApiTest(HiveConf hiveConf) {
        this.hiveConf = hiveConf;
        // Disable txn
        this.hiveConf.set(HiveConf.ConfVars.HIVE_TXN_MANAGER.varname, "org.apache.hadoop.hive.ql.lockmgr.DummyTxnManager");
        this.hiveConf.setBoolVar(HiveConf.ConfVars.HIVE_SUPPORT_CONCURRENCY, HiveConf.ConfVars.HIVE_SUPPORT_CONCURRENCY.defaultBoolVal);
        LogUtil.logParameter(hiveConf, HiveConf.ConfVars.HIVE_TXN_MANAGER.varname);
        LogUtil.logParameter(hiveConf, HiveConf.ConfVars.HIVE_SUPPORT_CONCURRENCY.varname);
        this.catalogLocation = this.hiveConf.get(CATALOG_LOCATION_KEY, CATALOG_LOCATION_DEFAULT);
    }

    public static void main(String[] args) throws TException {
        HiveConf hiveConf = new HiveConf();
        HiveTestUtils.addResource(hiveConf, args);
        // HiveTestUtils.printHiveConfByKeyOrder(hiveConf);

        MetaStoreApiTest test = new MetaStoreApiTest(hiveConf);
        test.init();
        // test.testDatabase();
        test.testTable();
        test.close();
    }

    public void init() throws MetaException {
        this.client = MetaStoreUtil.createMetaStoreClient(this.hiveConf);
        LogUtil.log("MetaStoreApiTest init");
    }

    public void close() {
        this.client.close();
        LogUtil.log("MetaStoreApiTest closed");
    }

    private void testDatabase() throws TException {
        String[] dbArray = new String[] {"meta_test11", "meta_test12"};

        for (int i = 0; i < dbArray.length; i++) {
            dropDatabase("meta_test11", true);
            dropDatabase("meta_test12", true);
        }

        for (int i = 0; i < dbArray.length; i++) {
            createDatabase(dbArray[i]);
        }

        for (int i = 0; i < dbArray.length; i++) {
            dropDatabase(dbArray[i], false);
        }

        for (int i = 0; i < dbArray.length; i++) {
            createDatabase(dbArray[i], generateParameters());
        }
        showDatabases(dbArray);
        for (int i = 0; i < dbArray.length; i++) {
            dropDatabase(dbArray[i], false);
        }
    }

    private void testTable() {
        // createTableInDefaultDatabase();
    }

    /**
     * args: [Table(tableName:t2,
     * dbName:default, owner:houzhizhen,
     * createTime:1661331824, lastAccessTime:0,
     * retention:0,
     * sd:StorageDescriptor(cols:[FieldSchema(name:c1, type:string, comment:null)],
     * location:null,
     * inputFormat:org.apache.hadoop.hive.ql.io.orc.OrcInputFormat,
     * outputFormat:org.apache.hadoop.hive.ql.io.orc.OrcOutputFormat,
     * compressed:false,
     * numBuckets:-1,
     * serdeInfo:SerDeInfo(name:null, serializationLib:org.apache.hadoop.hive.ql.io.orc.OrcSerde,
     * parameters:{serialization.format=1}),
     * bucketCols:[], sortCols:[], parameters:{},
     * skewedInfo:SkewedInfo(skewedColNames:[], skewedColValues:[], skewedColValueLocationMaps:{}),
     * storedAsSubDirectories:false),
     * partitionKeys:[],
     * parameters:{totalSize=0, numRows=0, rawDataSize=0,
     * COLUMN_STATS_ACCURATE={"BASIC_STATS":"true","COLUMN_STATS":{"c1":"true"}}, numFiles=0, bucketing_version=2}, viewOriginalText:null, viewExpandedText:null, tableType:MANAGED_TABLE, privileges:PrincipalPrivilegeSet(userPrivileges:{houzhizhen=[PrivilegeGrantInfo(privilege:INSERT, createTime:-1, grantor:houzhizhen, grantorType:USER, grantOption:true), PrivilegeGrantInfo(privilege:SELECT, createTime:-1, grantor:houzhizhen, grantorType:USER, grantOption:true), PrivilegeGrantInfo(privilege:UPDATE, createTime:-1, grantor:houzhizhen, grantorType:USER, grantOption:true), PrivilegeGrantInfo(privilege:DELETE, createTime:-1, grantor:houzhizhen, grantorType:USER, grantOption:true)]}, groupPrivileges:null, rolePrivileges:null), temporary:false, catName:hive, ownerType:USER)
     */
    private void createTableInDefaultDatabase(String dbName, String tbName) {
        Table table = new Table();
        table.setDbName(dbName);
        table.setTableName(tbName);
        //client.createTable(table);
    }

    private void createDatabase(String dbName) throws TException {
        String location = this.catalogLocation + "/" + dbName + ".db";
        String ownerName = "houzhizhen";
        Database database = new Database();
        database.setName(dbName);
        database.setDescription(null);
        database.setLocationUri(location);
        database.setParameters(null);
        database.setOwnerName(ownerName);
        database.setOwnerType(PrincipalType.USER);
        this.client.createDatabase(database);

        Database db = this.client.getDatabase(dbName);

        Assert.assertEquals(dbName, db.getName());
        Assert.assertNull(db.getDescription());
        Assert.assertEquals(location, db.getLocationUri());
        Assert.assertTrue(db.getParameters().isEmpty());
        Assert.assertEquals(location, db.getLocationUri());
        Assert.assertEquals(ownerName, db.getOwnerName());
        Assert.assertEquals(PrincipalType.USER, db.getOwnerType());
    }

    private void createDatabase(String dbName, Map<String, String> parameters) throws TException {
        String location = this.catalogLocation + "/" + dbName + ".db";
        String ownerName = "houzhizhen";
        Database database = new Database();
        database.setName(dbName);
        String desc = generateDescription();
        database.setDescription(desc);
        database.setLocationUri(location);
        database.setParameters(parameters);
        database.setOwnerName(ownerName);
        database.setOwnerType(PrincipalType.USER);
        this.client.createDatabase(database);

        Database db = this.client.getDatabase(dbName);

        Assert.assertEquals(dbName, db.getName());
        Assert.assertEquals(desc, db.getDescription());
        Assert.assertEquals(location, db.getLocationUri());
        Assert.assertEquals(parameters, db.getParameters());
        Assert.assertEquals(location, db.getLocationUri());
        Assert.assertEquals(ownerName, db.getOwnerName());
        Assert.assertEquals(PrincipalType.USER, db.getOwnerType());

        Assert.assertEquals(desc, db.getDescription());
        Assert.assertEquals(location, db.getLocationUri());
    }

    private void dropDatabase(String dbName, boolean ifExists) {
        try {
            this.client.dropDatabase(dbName);
        } catch (TException e) {
            if (!ifExists) {
                throw new RuntimeException(e);
            }
        }
    }

    private void showDatabases(String[] dbArray) throws TException {
       List<String> dbs = this.client.getAllDatabases();
       for (int i = 0; i < dbArray.length; i++) {
           Assert.assertTrue(dbs.contains(dbArray[i]));
       }
    }

    private Map<String, String> generateParameters() {
        Map<String, String> parameters = new HashMap<>(10);
        for (int i = 1; i < 5; i++) {
            parameters.put("key" + i, String.valueOf(i));
        }
        return parameters;
    }

    private String generateDescription() {
        return "fixed description";
    }
}
