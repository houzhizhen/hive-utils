package com.baidu.hive.util;

import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IOUtils;

import java.io.*;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

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

    public static String[] getSQLsFroPath(FileSystem fs, Path path) {
        BufferedReader bufferReader = null;
        try {
            bufferReader = new BufferedReader(new InputStreamReader(fs.open(path)));
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
    public static List<Path> getFiles(FileSystem fs, Path path) throws IOException {
        List<Path> result = new ArrayList<>();

        Queue<Path> queue = new LinkedList<>();
        queue.add(path);
        while (! queue.isEmpty()) {
            Path currentDir = queue.remove();
            FileStatus[] statuses = fs.listStatus(currentDir);
            for (FileStatus status : statuses) {
                if (status.isDirectory()) {
                    queue.add(status.getPath());
                } else {
                    result.add(status.getPath());
                }
            }
        }
        return result;
    }

    public static boolean filterOutSql(String sql) {
        sql = sql.trim();
        if(! sql.contains("select")) {
            return true;
        }
        return false;
    }
}
