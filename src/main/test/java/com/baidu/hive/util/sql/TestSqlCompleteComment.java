package com.baidu.hive.util.sql;

import org.junit.Test;

import java.io.IOException;

import static com.baidu.hive.util.sql.SqlCompleteComment.correctLineWithComment;

public class TestSqlCompleteComment {

    @Test
    public void test() throws IOException {
        String str = "AFS";
        System.out.println(str + ":" + correctLineWithComment(str));
        str = "COMMENT '";
        System.out.println(str + ":" + correctLineWithComment(str));
        str = "COMMENT '',";
        System.out.println(str + ":" + correctLineWithComment(str));
        str = "COMMENT 'AAA";
        System.out.println(str + ":" + correctLineWithComment(str));
        str = "'COMMENT'='aa'";
        System.out.println(str + ":" + correctLineWithComment(str));
    }
}
