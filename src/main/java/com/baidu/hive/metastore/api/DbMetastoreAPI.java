package com.baidu.hive.metastore.api;

import org.apache.hadoop.hive.conf.HiveConf;
import org.apache.hadoop.hive.metastore.api.Database;
import org.apache.hadoop.hive.metastore.api.PrincipalType;
import org.apache.thrift.TException;

import java.util.Map;

public class DbMetastoreAPI extends MetastoreAPIBase {

    public DbMetastoreAPI(HiveConf hiveConf) {
        super(hiveConf);
    }

    public Database createDatabase(String dbName) throws TException {
        String location = this.catalogLocation + "/" + dbName + ".db";
        log("creating database location " + location);
        String ownerName = "hive";
        Database database = new Database();
        database.setName(dbName);
        database.setDescription(null);
        database.setLocationUri(location);
        database.setParameters(null);
        database.setOwnerName(ownerName);
        database.setOwnerType(PrincipalType.USER);
        this.client.createDatabase(database);

        return this.client.getDatabase(dbName);
    }

    public Database createDatabase(String dbName, Map<String, String> parameters) throws TException {
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

        return this.client.getDatabase(dbName);
    }

    public void dropDatabase(String dbName, boolean ignoreUnknownDb) {
        log("Dropping database " + dbName);
        try {
            this.client.dropDatabase(dbName, true, ignoreUnknownDb, true);
        } catch (TException e) {
            if (!ignoreUnknownDb) {
                throw new RuntimeException(e);
            }
        }
    }

    public void init() {
        super.init();
    }
}
