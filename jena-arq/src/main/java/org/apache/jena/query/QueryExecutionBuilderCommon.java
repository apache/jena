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

package org.apache.jena.query;

import java.util.concurrent.TimeUnit;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.sparql.util.Context;
import org.apache.jena.sparql.util.Symbol;

/**
 * Common elements of query execution building.
 */
public interface QueryExecutionBuilderCommon {

    public QueryExecutionBuilderCommon query(Query query);

    public QueryExecutionBuilderCommon query(String queryString);

    public QueryExecutionBuilderCommon query(String queryString, Syntax syntax);

    public QueryExecutionBuilderCommon set(Symbol symbol, Object value);

    public QueryExecutionBuilderCommon set(Symbol symbol, boolean value);

    public QueryExecutionBuilderCommon context(Context context);

    /** Prefer {@link #substitution(QuerySolution)} which substitutes variables for values in the the query before execution. */
    public QueryExecutionBuilderCommon initialBinding(QuerySolution querySolution);

    public QueryExecutionBuilderCommon substitution(QuerySolution querySolution);

    public QueryExecutionBuilderCommon substitution(String varName, RDFNode value);

    public QueryExecutionBuilderCommon timeout(long value, TimeUnit timeUnit);

    public default QueryExecutionBuilderCommon timeout(long value) { return timeout(value, TimeUnit.MILLISECONDS); }

    public QueryExecution build();

    public default ResultSet select() { return build().execSelect(); }

    public default Model construct() { return build().execConstruct(); }

    public default Model describe() { return build().execDescribe(); }

    public default boolean ask() { return build().execAsk(); }
}

