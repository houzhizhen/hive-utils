package com.baidu.hive.jdbc;

import com.baidu.hive.util.log.LogUtil;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.concurrent.TimeUnit;

public class GetColumnNames extends ConnectionBase {

    public static void main(String[] args) throws Exception {
        GetColumnNames getColumnNames = new GetColumnNames();
        for (int i = 0; i < 1; i++) {
            LogUtil.log("time=" + i);
            getColumnNames.createConnection();
            getColumnNames.getColumnNames("hive", "default", "t1", "%");
            getColumnNames.closeConnection();
        }
        // TimeUnit.SECONDS.sleep(3600);
    }

    public void getColumnNames(String catalog, String dbName, String tableName, String pattern) throws SQLException, ClassNotFoundException {
        ResultSet rs = this.connection.getMetaData().getColumns(catalog, dbName, tableName, pattern);
        ResultSetMetaData rsMeta = rs.getMetaData();
        int columnCount = rsMeta.getColumnCount();
//        for (int i = 1; i <= columnCount; i++) {
//            System.out.printf("%20s", rsMeta.getColumnName(i));
//            if (i != columnCount) {
//                System.out.printf(" ");
//            }
//        }
//        System.out.print("\n");
        while(rs.next()) {
            for (int i = 1; i <= columnCount; i++) {
                //System.out.printf("%20s", rs.getString(i));
                if (i != columnCount) {
                    //System.out.print(" ");
                }
            }
            //System.out.print("\n");
        }
        rs.close();
    }
}
