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

package org.apache.jena.rdfconnection;

import java.util.function.Consumer;

import org.apache.jena.query.*;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.sparql.core.Transactional;

/** SPARQL Query Operations on a connection.
 *
 * @see RDFConnection
 * @see RDFConnectionFactory
 */
public interface SparqlQueryConnection extends Transactional, AutoCloseable
{
    /**
     * Execute a SELECT query and process the ResultSet with the handler code.
     * @param query
     * @param resultSetAction
     */
    public void queryResultSet(String query, Consumer<ResultSet> resultSetAction);

    /**
     * Execute a SELECT query and process the ResultSet with the handler code.
     * @param query
     * @param resultSetAction
     */
    public void queryResultSet(Query query, Consumer<ResultSet> resultSetAction);

    /**
     * Execute a SELECT query and process the rows of the results with the handler code.
     * @param query
     * @param rowAction
     */
    public void querySelect(String query, Consumer<QuerySolution> rowAction);

    /**
     * Execute a SELECT query and process the rows of the results with the handler code.
     * @param query
     * @param rowAction
     */
    public void querySelect(Query query, Consumer<QuerySolution> rowAction);

    /** Execute a CONSTRUCT query and return as a Model */
    public Model queryConstruct(String query);

    /** Execute a CONSTRUCT query and return as a Model */
    public Model queryConstruct(Query query);

    /** Execute a DESCRIBE query and return as a Model */
    public Model queryDescribe(String query);

    /** Execute a DESCRIBE query and return as a Model */
    public Model queryDescribe(Query query);

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
     * @return QueryExecution
     */
    public QueryExecution query(Query query);

    /** Setup a SPARQL query execution.
     *
     *  See also {@link #querySelect(String, Consumer)}, {@link #queryConstruct(String)},
     *  {@link #queryDescribe(String)}, {@link #queryAsk(String)}
     *  for ways to execute queries for of a specific form.
     *
     * @param queryString
     * @return QueryExecution
     */
    public QueryExecution query(String queryString);

    /**
     * Return a execution builder initialized with the RDFConnection setup.
     *
     * @return QueryExecutionBuilderCommon
     */
    public QueryExecutionBuilderCommon newQuery();

    /** Close this connection.  Use with try-resource. */
    @Override public void close();
}

