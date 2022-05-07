package com.baidu.hive.compare;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class CompareDirectories {

    public static void main(String[] args) {
        if (args.length != 2) {
            // exit("You must specify two parameters.");
            args = new String[] {"/home/ali/hive/temp/org", "/usr/local/hive/temp/org"};
        }
        File file1 = new File(args[0]);
        File file2 = new File(args[1]);
        if (!file1.exists()) {
            exit("File " + file1 + " does not exist");
        }
        if (!file1.exists()) {
            exit("File " + file2 + " does not exist");
        }
        compare(file1, file2);
    }

    private static void compare(File file1, File file2) {
        if (file1.isFile() && file2.isFile()) {
            if (file1.length() != file2.length()) {
//                System.out.println("file1: " + file1.getAbsolutePath() + ", length:" + file1.length() +
//                                   ", file2: " + file2.getAbsolutePath() + ", length:" + file2.length());
                System.out.println("cp '" + file1.getAbsolutePath() + "' '" + file2.getAbsolutePath() +"'");

            }
        }


        if (file1.isDirectory() && file2.isDirectory()) {
            Set<String> fileNameSet1 = subFiles(file1);
            Set<String> fileNameSet2 = subFiles(file2);
            for (String subFileName1 : fileNameSet1) {
                if (fileNameSet2.contains(subFileName1)) {
                    compare(new File(file1, subFileName1), new File(file2, subFileName1));
                } else {
                    // System.out.println("FileSet-1 contains " + new File(file1, subFileName1).getAbsolutePath());
                    System.out.println("cp '" + new File(file1, subFileName1).getAbsolutePath() + "' '" + new File(file2, subFileName1)+ "'");
                }

            }
        }
    }

    public static Set<String> subFiles(File file) {
        Set<String> fileSet = new HashSet<>();
        File[] subFiles = file.listFiles();
        for (File subFile : subFiles) {
            fileSet.add(subFile.getName());
        }
        return fileSet;
    }

    public static void exit(String message) {
        System.out.println(message);
        System.exit(1);
    }
}
