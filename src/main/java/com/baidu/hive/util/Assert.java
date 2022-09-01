package com.baidu.hive.util;

public class Assert {

    public static void assertTrue(boolean value) {
        if (!value) {
            throw new AssertionError();
        }
    }

    public static void assertEquals(Object o1, Object o2) {
        if (o1 == o2) {
            return;
        }
        String message = "o1:" + (o1 == null ? "null" : o1.toString()) + ", o2:" + (o2 == null ? "null" : o2.toString());
        if (o1 == null || o2 == null || !o1.equals(o2)) {
            throw new RuntimeException(message);
        }
    }

    public static void assertNull(Object o) {
        if (o != null) {
            throw new IllegalArgumentException("parameter " + o + " is not null");
        }
    }
}
