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

package org.apache.jena.rdflink;

import java.util.function.Consumer;

import org.apache.jena.graph.Graph;
import org.apache.jena.query.Query;
import org.apache.jena.sparql.core.Transactional;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.exec.QueryExec;
import org.apache.jena.sparql.exec.RowSet;

/** SPARQL Query Operations on a connection.
 *
 * @see RDFLink
 */
public interface LinkSparqlQuery extends Transactional, AutoCloseable
{
    /**
     * Execute a SELECT query and process the RowSet with the handler code.
     * @param query
     * @param rowSetAction
     */
    public void queryRowSet(String query, Consumer<RowSet> rowSetAction);

    /**
     * Execute a SELECT query and process the RowSet with the handler code.
     * @param query
     * @param rowSetAction
     */
    public void queryRowSet(Query query, Consumer<RowSet> rowSetAction);

    /**
     * Execute a SELECT query and process the rows of the results with the handler code.
     * @param query
     * @param rowAction
     */
    public void querySelect(String query, Consumer<Binding> rowAction);

    /**
     * Execute a SELECT query and process the rows of the results with the handler code.
     * @param query
     * @param rowAction
     */
    public void querySelect(Query query, Consumer<Binding> rowAction);

    /** Execute a CONSTRUCT query and return as a Model */
    public Graph queryConstruct(String query);

    /** Execute a CONSTRUCT query and return as a Model */
    public Graph queryConstruct(Query query);

    /** Execute a DESCRIBE query and return as a Model */
    public Graph queryDescribe(String query);

    /** Execute a DESCRIBE query and return as a Model */
    public Graph queryDescribe(Query query);

    /** Execute a ASK query and return a boolean */
    public boolean queryAsk(String query);

    /** Execute a ASK query and return a boolean */
    public boolean queryAsk(Query query);

    /** Setup a SPARQL query execution.
     *
     *  See also {@link #querySelect(Query, Consumer)}, {@link #queryConstruct(Query)},
     *  {@link #queryDescribe(Query)}, {@link #queryAsk(Query)}
     *  for ways to execute queries for of a specific form.
     *
     * @param query
     * @return QExec
     */
    public QueryExec query(Query query);

    /** Setup a SPARQL query execution.
     *
     *  See also {@link #querySelect(String, Consumer)}, {@link #queryConstruct(String)},
     *  {@link #queryDescribe(String)}, {@link #queryAsk(String)}
     *  for ways to execute queries for of a specific form.
     *
     * @param queryString
     * @return QExec
     */
    public QueryExec query(String queryString);

    /** Close this connection.  Use with try-resource. */
    @Override public void close();
}

