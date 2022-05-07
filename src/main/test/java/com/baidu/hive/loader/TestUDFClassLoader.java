package com.baidu.hive.loader;

import org.apache.hadoop.hive.conf.HiveConf;
import org.apache.hadoop.hive.ql.exec.UDFClassLoader;
import org.apache.hadoop.hive.ql.session.SessionState;
import org.junit.Assert;
import org.junit.Test;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.TimeUnit;

public class TestUDFClassLoader {

    @Test
    public void test1() throws ClassNotFoundException, MalformedURLException, InterruptedException {

        HiveConf hiveConf = new HiveConf();
        SessionState ss = new SessionState(hiveConf, "mockUser");
        SessionState.start(ss);
        SessionState.setCurrentSessionState(ss);

        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        /*
         * find a jar, which must not dependent by this project, or else
         * it will be loaded by system class loader.
         */
        URL[] urls = new URL[]{new URL("file:///home/houzhizhen/dist/upload.jar")};

        for (int i = 0; i < 10000; i++) {
            UDFClassLoader udfClassLoader = new UDFClassLoader(urls, loader);
            // What ever class in the jar

            Thread.currentThread().setContextClassLoader(udfClassLoader);
            SessionState.get().getConf().setClassLoader(udfClassLoader);
            Class s = SessionState.get().getConf().getClassByNameOrNull("UploadClient");
            Assert.assertEquals(s.getName(), "UploadClient");
        }
        Class s = SessionState.get().getConf().getClassByNameOrNull("UploadClient");
        System.out.println("load class finished");
        TimeUnit.MINUTES.sleep(5);

    }
}
