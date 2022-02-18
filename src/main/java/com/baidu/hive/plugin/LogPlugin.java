package com.baidu.hive.plugin;

import org.apache.hadoop.hive.ql.hooks.QueryLifeTimeHook;
import org.apache.hadoop.hive.ql.hooks.QueryLifeTimeHookContext;
import org.apache.hadoop.hive.ql.session.SessionState;

import java.io.*;
import java.util.HashSet;
import java.util.Set;

public class LogPlugin implements QueryLifeTimeHook {

    private Set<SessionState> sessionStateSet = new HashSet<>();

    @Override
    public void beforeCompile(QueryLifeTimeHookContext queryLifeTimeHookContext) {

        SessionState sessionState = SessionState.get();
        if (sessionStateSet.contains(sessionState)) {
            return;
        }
        sessionStateSet.add(sessionState);
        String fileName = queryLifeTimeHookContext.getHiveConf().get("hive.print.out.file.name",
                                                                     "print.out_" + sessionState.getSessionId());
        try {
            sessionState.out = new PrintStream(new BufferedOutputStream(new FileOutputStream(fileName)));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
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
