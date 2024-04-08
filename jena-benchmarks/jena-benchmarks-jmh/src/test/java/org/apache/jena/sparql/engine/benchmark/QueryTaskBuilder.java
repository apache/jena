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

public abstract class QueryTaskBuilder {
    protected String queryString;
    protected long expectedResultSetSize;
    protected boolean skipValidation;
    protected boolean skipExecution;

    public QueryTaskBuilder query(String queryString) {
        this.queryString = queryString;
        return this;
    }

    /** For select queries: Set the expected result set size. */
    public QueryTaskBuilder expectedResultSetSize(long expectedResultSetSize) {
        this.expectedResultSetSize = expectedResultSetSize;
        return this;
    }

    public QueryTaskBuilder skipExecution(boolean skipExecution) {
        this.skipExecution = skipExecution;
        return this;
    }

    public QueryTaskBuilder skipValidation(boolean skipValidation) {
        this.skipValidation = skipValidation;
        return this;
    }

    public abstract QueryTask build();
}
