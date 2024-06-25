package org.apache.jena.sparql.engine.join;

public class QueryExecResultData {
    protected String queryString;
    protected String originalOpString;
    protected String optimizedOpString;
    protected long resultCount;

    public QueryExecResultData(String queryString, String originalOpString, String optimizedOpString, long resultCount) {
        super();
        this.queryString = queryString;
        this.originalOpString = originalOpString;
        this.optimizedOpString = optimizedOpString;
        this.resultCount = resultCount;
    }

    public String getQueryString() {
        return queryString;
    }

    public String getOriginalOpString() {
        return originalOpString;
    }

    public String getOptimizedOpString() {
        return optimizedOpString;
    }

    public long getResultCount() {
        return resultCount;
    }

    @Override
    public String toString() {
        return String.join("\n",
                "Query:", queryString,
                "Original op:", originalOpString,
                "Optimized op:", optimizedOpString,
                "Result count:", Long.toString(resultCount));
    }
}
