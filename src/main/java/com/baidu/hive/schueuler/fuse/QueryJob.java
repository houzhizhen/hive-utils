package com.baidu.hive.schueuler.fuse;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public class QueryJob {

    private List<String> parentJobIds;
    private final String jobId;
    private final String query;
    private final Object environment;
    private Set<QueryData> inputs;
    private Set<QueryData> outputs;
    private String rewrittenQuery;
    public boolean isFuseOptimizerGenerated;

    public QueryJob(List<String> parentJobIds, String jobId, String query, Object environment) {
        this.parentJobIds = parentJobIds;
        this.jobId = jobId;
        this.query = query;
        this.environment = environment;
    }

    public String getRewrittenQuery() {
        return rewrittenQuery;
    }

    public void setRewrittenQuery(String rewrittenQuery) {
        this.rewrittenQuery = rewrittenQuery;
    }

    public Set<QueryData> getInputs() {
        return new HashSet<>(this.inputs);
    }

    public void setInputs(Set<QueryData> inputs) {
        this.inputs = new HashSet<>(inputs);
    }

    public Set<QueryData> getOutputs() {
        return new HashSet<>(this.outputs);
    }

    public void setOutputs(Set<QueryData> outputs) {
        this.outputs = new HashSet<>(outputs);
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        QueryJob queryJob = (QueryJob) o;
        return jobId.equals(queryJob.jobId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(jobId);
    }

    public List<String> getParentJobIds() {
        return new ArrayList<>(this.parentJobIds);
    }
    public void setParentJobIds(List<String> parentJobIds) {
        this.parentJobIds = new ArrayList<>(parentJobIds);
    }
    public String getJobId() {
        return jobId;
    }

    public String getQuery() {
        return query;
    }

    public Object getEnvironment() {
        return environment;
    }

    public boolean isFuseOptimizerGenerated() {
        return isFuseOptimizerGenerated;
    }

    public void setFuseOptimizerGenerated(boolean fuseOptimizerGenerated) {
        isFuseOptimizerGenerated = fuseOptimizerGenerated;
    }
}
