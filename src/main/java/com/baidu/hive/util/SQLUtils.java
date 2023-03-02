package com.baidu.hive.util;

import org.apache.hadoop.io.IOUtils;

import java.io.*;

public class SQLUtils {

    public static String[] getSQLsFromFile(File file) {
        BufferedReader bufferReader = null;
        try {
            bufferReader = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
            return getSQLFromReader(bufferReader).split(";");
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            IOUtils.closeStream(bufferReader);
        }
    }

    public static String getSQLFromReader(BufferedReader r) throws IOException {
        String line;
        StringBuilder qsb = new StringBuilder();

        while ((line = r.readLine()) != null) {
            // Skipping through comments
            if (! line.startsWith("--")) {
                qsb.append(line).append("\n");
            }
        }
        return qsb.toString();
    }
}
