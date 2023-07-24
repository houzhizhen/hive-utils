package com.baidu.java.json;

import org.apache.commons.lang.mutable.MutableInt;
import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.type.TypeFactory;
import org.codehaus.jackson.type.JavaType;

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

public class CountApplications {

    private static final JavaType MAP_TYPE = TypeFactory.fromClass(Map.class);
    private static final JavaType LIST_TYPE = TypeFactory.fromClass(List.class);

    private final JsonFactory jsonFactory = new JsonFactory();
    private final ObjectMapper objectMapper = new ObjectMapper(jsonFactory);
    private Map<String, MutableInt> countMap = new HashMap<>();
    private final String filePath;
    public CountApplications(String filePath) {
        this.filePath = filePath;
    }

    public void countApplications() {
        File file = new File(filePath);
        try {
            BufferedReader reader = new BufferedReader(new FileReader(file));
            String line;
            while((line = reader.readLine())!= null) {
                processLine(line);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        printCount();
    }

    private void printCount() {
        List<Map.Entry<String, MutableInt>> nlist = new ArrayList<>(countMap.entrySet());
        nlist.sort(Map.Entry.comparingByValue(Comparator.reverseOrder()));
        nlist.forEach(System.out::println);
    }

    private void processLine(String line) throws IOException {
        List<Map<String, String>> obj = (List<Map<String, String>>) objectMapper.readValue(line, LIST_TYPE);
        for (Map<String, String> map : obj) {
            String name = map.get("name");
            MutableInt count = this.countMap.get(name);
            if (count == null) {
                countMap.put(name, new MutableInt(1));
            } else {
                count.increment();
            }
        }
    }
    public static void main(String[] args) {
        if (args == null || args.length ==0) {
            args = new String[]{"/Users/houzhizhen/git/hive-utils/apps"};
        }
        CountApplications count = new CountApplications(args[0]);
        count.countApplications();
    }
}
