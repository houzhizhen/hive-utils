package jdk;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.WeakHashMap;

public class WeakHashMapTest {

    static class A {
        private int i;
        public A(int i) {
            this.i = i;
        }
        public int hashCode() {
            return Integer.hashCode(i);
        }

        @Override
        public boolean equals(Object o) {
            if (!(o instanceof A)) {
                return false;
            }
            return ((A) o).i == i;
        }
    }
    @Test
    public void testKeyNotReferenced() {
        WeakHashMap<A, Integer> weakHashMap = new WeakHashMap<A, Integer>();
        int LIMIT = 10000000;
        for (int i = 0; i < LIMIT; i++) {
            A a = new A(i);
            weakHashMap.put(a, i);
        }
        int nullCount = 0;
        for (int i = 0; i < LIMIT; i++) {
            A a = new A(i);
            Integer b = weakHashMap.get(a);
            if (b == null) {
                nullCount++;
            }
        }
        System.out.println("weakHashMap.size:" + weakHashMap.size() + ", nullCount:" + nullCount);
    }

    @Test
    public void testKeyReferenced() {
        WeakHashMap<A, Integer> weakHashMap = new WeakHashMap<A, Integer>();
        int LIMIT = 10000000;
        List<A> keys = new ArrayList<>(LIMIT);
        for (int i = 0; i < LIMIT; i++) {
            A a = new A(i);
            keys.add(a);
            weakHashMap.put(a, i);
        }
        int nullCount = 0;
        for (int i = 0; i < LIMIT; i++) {
            A a = new A(i);
            Integer b = weakHashMap.get(a);
            if (b == null) {
                nullCount++;
            }
        }
        System.out.println("weakHashMap.size:" + weakHashMap.size() + ", nullCount:" + nullCount);
    }
}
