package jdk;

import org.junit.Test;

class A {
    void doSomething() {
        System.out.println("In a");
    }
}
class B extends A {
    void doSomething() {
        System.out.println("In B");
    }
    void doSomethingElse() {
        super.doSomething();
    }
}

public class ExtendTest {

    @Test
    public void test() {
        B b = new B();
        b.doSomethingElse();
    }
}
