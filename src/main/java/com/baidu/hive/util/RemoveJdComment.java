package com.baidu.hive.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class RemoveJdComment {

    public static void main(String[] args) throws IOException {
        args = new String[]{"/home/houzhizhen/dist/jdk8_301/URLClassPath.java",
                "/home/houzhizhen/git/hive-utils/src/main/resources/URLClassPath.java"};
        BufferedReader reader = new BufferedReader(new FileReader(args[0]));
        BufferedWriter writer = new BufferedWriter(new FileWriter(args[1]));
        String line = reader.readLine();
        while (line != null) {
            if (line.startsWith("/*")) {
                int endIndex = line.indexOf("*/");
                if (endIndex == -1) {
                    writer.write(line);
                    writer.write("\n");
                } else {
                    String t = line.substring(endIndex + 2);
                    writer.write(t);
                    writer.write("\n");
                }
            } else {
                writer.write(line);
                writer.write("\n");
            }
            line = reader.readLine();
        }
        writer.close();
        reader.close();
    }
}
