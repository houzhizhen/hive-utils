package jdk;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RemoveMap {

    public static void main(String[] args) {
        List<String> remove = new ArrayList<>();
        Map<String, String> map = new HashMap();
        for (int i = 0; i < 100; i++) {
            map.put(i+"" , (i+1)+"");
        }
        for (Map.Entry<String, String> entry : map.entrySet()) {
            System.out.println("Removing" + entry.getKey());
            remove.add(entry.getKey());
        }
        for (String key : remove) {
            map.remove(key);
        }
        System.out.println("Remaining\n" + map);
    }
}
