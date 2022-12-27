package jdk;

import org.apache.hadoop.hive.ql.exec.FunctionRegistry;
import org.apache.hadoop.hive.ql.udf.generic.GenericUDF;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.ServiceLoader;

public class TestSystemLoader {
    private static final Logger LOG = LoggerFactory.getLogger(TestSystemLoader.class);

    @Test
    public void test() {
        Map<String, Class<? extends GenericUDF>> map = loadEdapUDF();
        System.out.println(map.keySet());
    }

    private static Map<String, Class<? extends GenericUDF>> loadEdapUDF() {
        Map<String, Class<? extends GenericUDF>> classMap = new HashMap<>();

        ServiceLoader<GenericUDF> loadedUDF = ServiceLoader.load(GenericUDF.class);
        Iterator<GenericUDF> udfIterator = loadedUDF.iterator();

        while (udfIterator.hasNext()) {
            Class<? extends GenericUDF> clazz = udfIterator.next().getClass();
            Field udfNameField = null;

            // UDF_NAME 是静态方法
            try {
                udfNameField = clazz.getDeclaredField("UDF_NAME");
            } catch (NoSuchFieldException e) {
                LOG.warn(clazz.getName() + " not UDF_NAME filed." );
                continue;
            }

            udfNameField.setAccessible(true);

            if (udfNameField != null) {
                try {
                    classMap.put(String.valueOf(udfNameField.get(null)), clazz);
                } catch (IllegalAccessException e) {
                    LOG.warn("illegal access " + clazz.getName() +" UDF_NAME field value." );
                }
            }
        }
        return classMap;
    }
}
