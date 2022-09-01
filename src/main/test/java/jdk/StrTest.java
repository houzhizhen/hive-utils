package jdk;

public class StrTest {

    public static void main(String[] args) {
        String s = new String(new byte[]{ 0x54});
        System.out.println(s);
    }
}
