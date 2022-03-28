package com.baidu.hive.transaction.stream;

import org.apache.hadoop.hive.conf.HiveConf;
import org.apache.hive.streaming.HiveStreamingConnection;
import org.apache.hive.streaming.StreamingConnection;
import org.apache.hive.streaming.StreamingException;
import org.apache.hive.streaming.StrictDelimitedInputWriter;

import java.util.ArrayList;

public class TestStrictDelimitedInputWriter {

    public static void main(String []args) throws StreamingException {
        new TestStrictDelimitedInputWriter().test();
    }

    public void test() throws StreamingException {
        HiveConf hiveConf = new HiveConf();
        String dbName = "test";
        String tblName = "alerts";

// static partition values
        ArrayList<String> partitionVals = new ArrayList<String>(2);
        partitionVals.add("Asia");
        partitionVals.add("India");

// create delimited record writer whose schema exactly matches table schema
        StrictDelimitedInputWriter writer = StrictDelimitedInputWriter.newBuilder()
                                                                      .withFieldDelimiter(',')
                                                                      .build();
// create and open streaming connection (default.src table has to exist already)
        StreamingConnection connection = HiveStreamingConnection.newBuilder()
                                                                .withDatabase(dbName)
                                                                .withTable(tblName)
                                                                .withStaticPartitionValues(partitionVals)
                                                                .withAgentInfo("example-agent-1")
                                                                .withRecordWriter(writer)
                                                                .withHiveConf(hiveConf)
                                                                .connect();
// begin a transaction, write records and commit 1st transaction
        connection.beginTransaction();
        connection.write("1,val1".getBytes());
        connection.write("2,val2".getBytes());
        connection.commitTransaction();
// begin another transaction, write more records and commit 2nd transaction
        connection.beginTransaction();
        connection.write("3,val3".getBytes());
        connection.write("4,val4".getBytes());
        connection.commitTransaction();
// close the streaming connection
        connection.close();
    }
}
