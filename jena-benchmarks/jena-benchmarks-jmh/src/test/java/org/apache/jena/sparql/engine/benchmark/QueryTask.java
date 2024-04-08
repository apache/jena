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

/** Base class for a task that executes a (SELECT) query and can optionally validate the result set size. */
public abstract class QueryTask {
    protected final String queryString;
    protected final long expectedResultSetSize;
    protected final boolean skipExecution;
    protected final boolean skipValidation;

    public QueryTask(String queryString, long expectedResultSetSize, boolean skipExecution, boolean skipValidation) {
        super();
        this.queryString = queryString;
        this.expectedResultSetSize = expectedResultSetSize;
        this.skipExecution = skipExecution;
        this.skipValidation = skipValidation;
    }

    public String getQueryString() {
        return queryString;
    }

    public long getExpectedResultSetSize() {
        return expectedResultSetSize;
    }

    public boolean skipExecution() {
        return skipExecution;
    }

    public boolean skipValidation() {
        return skipValidation;
    }

    public abstract QueryTaskResult exec();
}
