package com.baidu.hive.jdbc;

import java.sql.*;
import java.util.Properties;

public class ConnectionWithProps {

    private static String url = "jdbc:hive2://localhost:10000/default";
    public static void main(String[] args) throws SQLException {
        try {
            Class.forName("org.apache.hive.jdbc.HiveDriver");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }

        Properties props = getProps();

        Connection connection = DriverManager.getConnection(url, props);
        PreparedStatement pstat = connection.prepareStatement("show databases");
        ResultSet rs = pstat.executeQuery();
        int columnCount = rs.getMetaData().getColumnCount();
        while (rs.next()) {
            for (int i = 1; i <= columnCount; i++) {
                System.out.print(rs.getString(i) + " ");
            }
            System.out.println();
        }
        rs.close();
        pstat.close();
        connection.close();
    }

    private static Properties getProps() {
        Properties props = new Properties();
       // =none, =, =default, , =, =
        props.put("hiveconf:mapreduce.job.queuename", "default");
        props.put("hiveconf:edap.session.token.key", "*");
        props.put("user", "jiaqi.ma");
        props.put("hiveconf:hive.fetch.task.conversion", "none");
        //props.put("hiveconf:hive.metastore.client.class", "com.baidubce.edap.catalog.metastore.EdapCatalogHiveClient");
        props.put("hiveconf:edap.endpoint", "http://edap.bj.baidubce.com");
        props.put("hiveconf:tez.queue.name", "default");
        props.put("hiveconf:edap.secret.key", "*");
        props.put("hiveconf:edap.access.key", "541671ac9c9211ef82a649b0ad09bfc2");
        return props;
    }

    protected void createConnection(String url, String userName, String password) throws SQLException {

    }
}
