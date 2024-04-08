/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.jena.sparql.engine.benchmark;

public class QueryTaskResult {
    protected String queryString;
    protected String originalOpString;
    protected String optimizedOpString;
    protected long resultSetSize;

    public QueryTaskResult(String queryString, String originalOpString, String optimizedOpString, long resultSetSize) {
        super();
        this.queryString = queryString;
        this.originalOpString = originalOpString;
        this.optimizedOpString = optimizedOpString;
        this.resultSetSize = resultSetSize;
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

    public long getResultSetSize() {
        return resultSetSize;
    }

    @Override
    public String toString() {
        return String.join("\n",
                "Query:", queryString,
                "Original op:", originalOpString,
                "Optimized op:", optimizedOpString,
                "Result count:", Long.toString(resultSetSize));
    }
}
