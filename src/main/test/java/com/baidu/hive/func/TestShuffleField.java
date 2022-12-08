package com.baidu.hive.func;

import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Random;

public class TestShuffleField {

    @Test
    public void testFloat() {
        Random r = new Random(1);
        System.out.println(r.nextInt());
        System.out.println(r.nextInt());
        r.setSeed(1);
        System.out.println(r.nextInt());
        System.out.println(r.nextInt());
        float f = 2f;
        System.out.println(f);
        System.out.println(Float.valueOf("000.12"));
    }

    @Test
    public void testI() {
        char a = (char)Byte.parseByte("9");
        char b = '\t';
        System.out.println(a == b);
        System.out.println("a" + (char)Byte.parseByte("9")+"b");
    }

    @Test
    public void testAddResources() throws IOException {
        List<InputStream> inputStreams = loadResources("META-INF/MANIFEST.MF", null);
        System.out.println(inputStreams.size());
    }

    public static List<InputStream> loadResources(
            final String name, final ClassLoader classLoader) throws IOException {
        final List<InputStream> list = new ArrayList<InputStream>();
        final Enumeration<URL> systemResources =
                (classLoader == null ? ClassLoader.getSystemClassLoader() : classLoader)
                        .getResources(name);
        while (systemResources.hasMoreElements()) {
            list.add(systemResources.nextElement().openStream());
        }
        return list;
    }
}
