package com.baidu.hive.schueuler.fuse;

import org.apache.hadoop.conf.Configuration;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FuseOptimizer {

    private final QueryExecutor queryExecutor;
    private Map<String, QueryJob> jobMap;
    private Map<QueryData, List<QueryJob>> queryData2Jobs;
    // QueryData only corresponding size of List<QueryJob> is bigger than 1,put it  ReadWriteInfo to ReadWriteInfo.
    private Map<QueryData, ReadWriteInfo> queryDataReadWriteInfoMap;

    public FuseOptimizer(QueryExecutor queryExecutor, Configuration conf) {
        this.queryExecutor = queryExecutor;
    }

    public void prepareBatch() {
        this.jobMap = new HashMap<>();
        this.queryData2Jobs = new HashMap<>();
        this.queryDataReadWriteInfoMap = new HashMap<>();
    }

    public void addJob(List<String> parentJobId, String jobId, String query, Object environment) {
        QueryJob job = new QueryJob(parentJobId, jobId, query, environment);
        this.jobMap.put(job.getJobId(), job);
    }

    public void executeBatch() {
        generateQueryInputsAndOutputs();
        generateQueryData2Jobs();
        generateQueryDataReadWriteInfoMap();
        rewriteQuery();
        executeRewrittenQuery();
    }

    /**
     * Topology the jobMap, and execute the jobs.
     * For each QueryJob, after execute, get Inputs. for each Inputs get ReadWriteInfo from queryDataReadWriteInfoMap.
     *
     */
    private void executeRewrittenQuery() {
        // queryExecutor.run();
    }

    /**
     * Rewrite query using queryDataUsageInfoMap and queryDataReadWriteInfoMap.
     * Put then rewritten query in QueryJob, the query may be written multiple times.
     * If the data is first read, we may generate a new sync job data first,
     * so we generate a QueryJob object and add the new sync job to the parentList of all jobs that read the data .
     *  each QueryData in queryDataUsageInfoMap, add a deleting cache job,
     *  and put all the List<QueryJob> as parent of new deleting cache job.
     *
     */
    private void rewriteQuery() {
    }


    /**
     * Generate queryDataUsageInfoMap using queryData2Jobs.
     * Only generate for queryData used more than 1 times.
     */
    public void generateQueryDataReadWriteInfoMap() {
        for(Map.Entry<QueryData, List<QueryJob>> entry : this.queryData2Jobs.entrySet()) {
            QueryData queryData = entry.getKey();
            List<QueryJob> jobs = entry.getValue();
            // Used more than 1 times.
            if (jobs.size() == 1) {
                continue;
            }
            // need hasWriter;
            boolean hasWriter = false;
            for (QueryJob job : jobs) {
                if (job.getOutputs().contains(queryData)) {
                    hasWriter = true;
                }
            }
            boolean needSync = true;
            if (hasWriter) {
                // will not generate sync job from storage cluster
            } else {
                // will generate sync job from storage cluster
                //
            }
            // Assume writer job must be executed before reader job,
            // but will not be checked here.
        }
    }

    public void finishBatch() {

    }
    /**
     * Populate queryData2Jobs, use the inputs and outputs in all QueryJob.
     */
    private void generateQueryData2Jobs() {

    }

    /**
     * Generate inputs and outputs info for all query job.
     * If the following conditions meet, does not put in inputs or outputs,
     * and will not consider optimization.
     * 1. The input or output is not all partition columns specified,
     * 2. The inputs or outputs concerns more than one partitions, and may be one of partitions
     * may be used again, the others not.
     */
    private void generateQueryInputsAndOutputs() {
    }

    public void close() {
        // Do nothing
    }
}
