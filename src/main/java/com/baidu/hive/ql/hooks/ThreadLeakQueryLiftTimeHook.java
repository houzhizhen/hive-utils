package com.baidu.hive.ql.hooks;

import org.apache.hadoop.hive.ql.hooks.QueryLifeTimeHook;
import org.apache.hadoop.hive.ql.hooks.QueryLifeTimeHookContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ThreadLeakQueryLiftTimeHook implements QueryLifeTimeHook {

    private static final Logger LOG = LoggerFactory.getLogger(ThreadLeakQueryLiftTimeHook.class);

    @Override
    public void beforeCompile(QueryLifeTimeHookContext queryLifeTimeHookContext) {
        ExecutorService es = Executors.newFixedThreadPool(4);
        es.submit(() -> LOG.info("ThreadLeakQueryLiftTimeHook thread pool leak"));
    }

    @Override
    public void afterCompile(QueryLifeTimeHookContext queryLifeTimeHookContext, boolean b) {

    }

    @Override
    public void beforeExecution(QueryLifeTimeHookContext queryLifeTimeHookContext) {

    }

    @Override
    public void afterExecution(QueryLifeTimeHookContext queryLifeTimeHookContext, boolean b) {

    }
}
