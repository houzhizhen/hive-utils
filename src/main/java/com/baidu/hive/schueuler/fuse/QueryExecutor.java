package com.baidu.hive.schueuler.fuse;

public interface QueryExecutor {

    /**
     * Run query as jobId, and the environment, query may be not same as @link{FuseOptimizer#addJob}
     */
    void run(String jobId, String sql, Object environment);

    /**
     *
     * @param jobId
     */
    void jobFinished(String jobId, FinishState state);
}
