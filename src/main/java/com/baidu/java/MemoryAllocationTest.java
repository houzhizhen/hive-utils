package com.baidu.java;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class MemoryAllocationTest {

    private static int SIZE = 1024 * 1024 * 1024;
    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println("You must specify parameter maxTimes.");
            System.exit(1);
        }
        int maxTimes = Integer.parseInt(args[0]);
        long sum = 0;
        List<byte[]> memoryList = new ArrayList();
        int size = SIZE;
        for (int i = 0; i < maxTimes; i++) {
            byte[] b = new byte[size];
            for (int j = 0; j < b.length; j++) {
                b[j] = (byte)j;
            }
            sum += size;
            System.out.println("Total allocated memory " + sum);
            memoryList.add(b);
            try {
                TimeUnit.SECONDS.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        size = SIZE / 10;
        for (int i = 0; i < 10; i++) {
            byte[] b = new byte[size / 10];
            for (int j = 0; j < b.length; j++) {
                b[j] = (byte)j;
            }
            sum += size;
            System.out.println("Total allocated memory " + sum);
            memoryList.add(b);
            try {
                TimeUnit.SECONDS.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
