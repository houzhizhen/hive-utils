package com.baidu.java.jdk;

import org.apache.tez.common.Preconditions;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CompareHistoFile {

    final private File histoFile1;
    final private File histoFile2;

    public CompareHistoFile(File histoFile1, File histoFile2) {
        Preconditions.checkArgument(histoFile1.exists(), "Histogram file does not exist: %s", histoFile1.getAbsolutePath());
        Preconditions.checkArgument(histoFile2.exists(), "Histogram file does not exist: %s", histoFile2.getAbsolutePath());
        this.histoFile1 = histoFile1;
        this.histoFile2 = histoFile2;
    }


    private void compare() {
        Map<String, Long> map1 = readHistoFile(histoFile1);
        Map<String, Long> map2 = readHistoFile(histoFile2);
        System.out.println("compare by size:");
        compareBySize(map1, map2);
        System.out.println("compare by ratio:");
        compareByRatio(map1, map2);
    }

    private void compareByRatio(Map<String, Long> map1, Map<String, Long> map2) {
        Map<String, Long> diffMap = new HashMap<>();
        for (Map.Entry<String, Long>  entry : map1.entrySet()) {
            String key = entry.getKey();
            Long value1 = entry.getValue();
            Long value2 = map2.get(key);
            if (value2 != null) {
                diffMap.put(key, value2 / value1);
            }
        }
        printTop(diffMap);
    }

    private void compareBySize(Map<String, Long> map1, Map<String, Long> map2) {
        Map<String, Long> diffMap = new HashMap<>();
        for (Map.Entry<String, Long>  entry : map1.entrySet()) {
            String key = entry.getKey();
            Long value1 = entry.getValue();
            Long value2 = map2.get(key);
            if (value2 != null) {
                diffMap.put(key, value2 - value1);
            }
        }
        printTop(diffMap);
    }

    private void printTop(Map<String, Long> map) {
        List<Map.Entry<String, Long>> nlist = new ArrayList<>(map.entrySet());
        nlist.sort(Map.Entry.comparingByValue(Comparator.reverseOrder()));
        for (int i = 0; i < 50; i++) {
            System.out.println(nlist.get(i).getKey() + " : " + nlist.get(i).getValue());
        }
    }

    private Map<String, Long> readHistoFile(File histoFile1) {
        Map<String, Long> map = new HashMap<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(histoFile1))) {
            // Skip three lines
            for (int i = 0; i < 3; i++) {
                reader.readLine();
            }
            String line;
            while((line = reader.readLine())!= null) {
                String[] array = line.trim().split("\\s+");
                if (array.length != 4) {
                    continue;
                }
                map.put(array[3], Long.parseLong(array[2]));
            }
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return map;
    }

    public static void main(String[] args) {
        if (args.length != 2) {
            System.out.println("Usage: java CompareHistoFile <filename> <filename>");
            System.exit(-1);
        }

        File histoFile1 = new File(args[0]);
        File histoFile2 = new File(args[1]);
        CompareHistoFile compare = new CompareHistoFile(histoFile1, histoFile2);
        compare.compare();
    }
}
