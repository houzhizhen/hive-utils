package com.baidu.hive.comiple;

import org.junit.Test;

public class TestDriverCompile {

    @Test
    public void test() {
        DriverCompile.main(new String[] {
            "--database", "aaa",
            "--directory", "/home/houzhizhen/git/hive-testbench/sample-queries-tpcds"
        });
    }
}
