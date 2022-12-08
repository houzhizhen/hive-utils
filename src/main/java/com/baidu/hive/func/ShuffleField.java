package com.baidu.hive.func;

import org.apache.hadoop.hive.ql.exec.MapredContext;
import org.apache.hadoop.hive.ql.exec.UDFArgumentException;
import org.apache.hadoop.hive.ql.metadata.HiveException;
import org.apache.hadoop.hive.ql.udf.generic.GenericUDF;
import org.apache.hadoop.hive.serde2.objectinspector.ObjectInspector;

public class ShuffleField extends GenericUDF {

    final public static String SHUFFLE_SEED_KEY = "hive.shuffle.field.seed";

    private transient int seed;

    @Override
    public ObjectInspector initialize(ObjectInspector[] objectInspectors) throws UDFArgumentException {
        if (objectInspectors.length != 1) {

        }
        return null;
    }

    @Override
    public void configure(MapredContext context) {
       this.seed = context.getJobConf().getInt(SHUFFLE_SEED_KEY, 0);
       if (this.seed == 0) {
           throw new RuntimeException("Must config " + SHUFFLE_SEED_KEY);
       }
    }

    @Override
    public Object evaluate(DeferredObject[] deferredObjects) throws HiveException {
        return null;
    }

    @Override
    public String getDisplayString(String[] strings) {
        return null;
    }
}
