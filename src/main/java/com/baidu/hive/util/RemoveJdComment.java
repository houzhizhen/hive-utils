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
//        args = new String[]{"/home/houzhizhen/Downloads/ali-SemanticAnalyzer.java",
//        "/home/houzhizhen/Downloads/c-ali-SemanticAnalyzer.java"};
        BufferedReader reader = new BufferedReader(new FileReader(args[0]));
        BufferedWriter writer = new BufferedWriter(new FileWriter(args[1]));
        String line = reader.readLine();
        while (line != null && line.length() > 12) {
            if (line.length() > 12 && line.startsWith("/*")) {
                // Remove leading 12 characters.
                writer.write(line.substring(12));
                writer.write("\n");
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
