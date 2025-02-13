package com.baidu.hive.metastore.listener;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hive.metastore.TransactionalMetaStoreEventListener;
import org.apache.hadoop.hive.metastore.api.MetaException;
import org.apache.hadoop.hive.metastore.events.AddPartitionEvent;

/**
 <property>
 <name>hive.metastore.transactional.event.listeners</name>
 <value>com.baidu.hive.metastore.listener.ThrowExceptionTransactionListener</value>
 </property>
 */
public class ThrowExceptionTransactionListener extends TransactionalMetaStoreEventListener {
    public ThrowExceptionTransactionListener(Configuration config) {
        super(config);
    }
    public void onAddPartition(AddPartitionEvent partitionEvent) throws MetaException {
        throw new MetaException("add partition failed in ThrowExceptionTransactionListener");
    }
}
