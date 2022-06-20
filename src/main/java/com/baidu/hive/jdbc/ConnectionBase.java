package com.baidu.hive.jdbc;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public abstract class ConnectionBase {

    protected Connection connection;

    protected void createConnection() throws SQLException {
        createConnection("jdbc:hive2://localhost:2181/", "houzhizhen","houzhizhen");
    }

    protected void createConnection(String url, String userName, String password) throws SQLException {
        try {
            Class.forName("org.apache.hive.jdbc.HiveDriver");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }

        this.connection = DriverManager.getConnection(url, userName,password);
    }

    protected void closeConnection() throws SQLException {
        this.connection.close();
    }
}
