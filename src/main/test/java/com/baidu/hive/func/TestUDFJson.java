package com.baidu.hive.func;

import com.baidu.hive.util.log.LogUtil;
import org.apache.hadoop.io.Text;
import org.junit.Test;

public class TestUDFJson {

    @Test
    public void test1() {
        long timeBegin = System.currentTimeMillis();
        UDFJson udf = new UDFJson();

        long length = 0;
        for (int i = 0; i < 10000000; i++) {
            String data = "{\n" +
                    " \"store\":\n" +
                    "        {\n" +
                    "         \"fruit\":[{\"weight\":8,\"type\":\"apple\"}, {\"weight\":9,\"type\":\"pear\"}],  \n" +
                    "         \"bicycle\":{\"price\":19.95,\"color\":\"red\"}\n" +
                    "         }, \n" +
                    " \"email\":\"amy@only_for_json_udf_test.net\", \n" +
                    " \"owner\":\"amy" + i +
                    "\"}\n";
           Object v = udf.evaluate(data, "$.owner");
            // System.out.println(v);
           // length += ((Text)v).getLength();
        }
        long timeSpent = System.currentTimeMillis() - timeBegin;
        LogUtil.log("timeSpent:" + timeSpent);

    }
}
