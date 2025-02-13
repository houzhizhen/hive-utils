package com.baidu.hive.util.sql;

import com.baidu.hive.util.HiveTestUtils;
import org.apache.hadoop.hive.conf.HiveConf;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class SqlCompleteComment {
    private final static int COMMENT_LENGTH = "COMMENT".length();




    public static void main(String[] args) throws IOException {
        HiveConf conf = new HiveConf();
        HiveTestUtils.addResource(conf, args);
        File input = new File(conf.get("input"));
        File output = new File(conf.get("output"));
        if (! input.exists()) {
            System.out.println("input " + input + " not exists");
        }
        BufferedWriter writer = new BufferedWriter(new FileWriter(output));
        BufferedReader reader = new BufferedReader(new FileReader(input));
        String s;
        while((s = reader.readLine()) != null) {
            writer.write(correctLineWithComment(s));
            writer.write('\n');
        }
    }

    protected static String correctLineWithComment(String s) {
        int  index = s.indexOf("COMMENT");
        if (index == -1) {
            return s;
        } else if (index > 0 && s.length() >  + COMMENT_LENGTH + 1
                && s.charAt(index -1 ) == '\'' &&
                s.charAt(index + COMMENT_LENGTH) == '\'' ) {

            // TBLPROPERTIES (
            //  'COMMENT'='aa',
            String part2 = s.substring(index + "COMMENT".length() + 1);
            String newPart2 = correctPart2(part2);
            return s.substring(0, index + "COMMENT".length() + 1) + newPart2;
        }else {
            String part2 = s.substring(index + "COMMENT".length());
            String newPart2 = correctPart2(part2);
            return s.substring(0, index + "COMMENT".length()) + newPart2;
        }
    }

    private static String correctPart2(String part2) {
        if (part2 == null) {
            return ";";
        }
        int count = 0;
        for (int i = 0; i < part2.length(); i++) {
            if (part2.charAt(i) == '\'') {
                if (i > 0 && part2.charAt(i-1) == '\\') {
                    continue;
                }
                count++;
            }
        }
        if (count == 2) {
            return part2;
        } else if (count == 1) {
            return part2 + "',";
        } else {
            throw new RuntimeException(part2 + " has ' count:" + count);
        }
    }
}
