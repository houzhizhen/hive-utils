package com.baidu.hive.metastore;

import com.baidu.hive.util.log.LogUtil;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hive.conf.HiveConf;
import org.apache.hadoop.hive.metastore.IMetaStoreClient;
import org.apache.hadoop.hive.metastore.api.Database;
import org.apache.hadoop.hive.metastore.api.MetaException;
import org.apache.hadoop.hive.metastore.api.PrincipalType;
import org.apache.hadoop.hive.metastore.conf.MetastoreConf;
import org.apache.hadoop.hive.ql.metadata.Table;
import org.apache.thrift.TException;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

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

    private Configuration conf;
    private IMetaStoreClient client;

    @Before
    public void init() throws MetaException {
        this.conf = MetastoreConf.newMetastoreConf();;
        // Disable txn
        this.conf.set(HiveConf.ConfVars.HIVE_TXN_MANAGER.varname,
                "org.apache.hadoop.hive.ql.lockmgr.DummyTxnManager");
        MetastoreConf.setBoolVar(conf, MetastoreConf.ConfVars.HIVE_SUPPORT_CONCURRENCY,
                HiveConf.ConfVars.HIVE_SUPPORT_CONCURRENCY.defaultBoolVal);
        LogUtil.logParameter(conf, HiveConf.ConfVars.HIVE_TXN_MANAGER.varname);
        LogUtil.logParameter(conf, HiveConf.ConfVars.HIVE_SUPPORT_CONCURRENCY.varname);
        this.catalogLocation = this.conf.get(CATALOG_LOCATION_KEY, CATALOG_LOCATION_DEFAULT);
        this.client = MetaStoreUtil.createMetaStoreClient(this.conf);
    }
//
//    public static void main(String[] args) throws TException {
//        HiveConf hiveConf = new HiveConf();
//        HiveTestUtils.addResource(hiveConf, args);
//        // HiveTestUtils.printHiveConfByKeyOrder(hiveConf);
//
//        MetaStoreApiTest test = new MetaStoreApiTest(hiveConf);
//        test.init();
//        // test.testDatabase();
//        test.testTable();
//        test.close();
//    }

    @After
    public void close() {
        this.client.close();
        LogUtil.log("MetaStoreApiTest closed");
    }

    @Test
    public void testDatabase() throws TException {
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
