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

package org.apache.jena.update;

import org.apache.jena.query.QuerySolution;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.sparql.util.Context;
import org.apache.jena.sparql.util.Symbol;

public interface UpdateExecutionBuilder {

    /** Append the updates in an {@link UpdateRequest} to the {@link UpdateRequest} being built. */
    public UpdateExecutionBuilder update(UpdateRequest updateRequest);

    /** Add the {@link Update} to the {@link UpdateRequest} being built. */
    public UpdateExecutionBuilder update(Update update);

    /** Parse and update operations to the {@link UpdateRequest} being built. */
    public UpdateExecutionBuilder update(String updateRequestString);

    public UpdateExecutionBuilder set(Symbol symbol, Object value);

    public UpdateExecutionBuilder set(Symbol symbol, boolean value);

    public UpdateExecutionBuilder context(Context context);

    public UpdateExecutionBuilder substitution(QuerySolution querySolution);

    public UpdateExecutionBuilder substitution(String varName, RDFNode value);

    public UpdateExecution build();

    /** Build and execute */
    public default void execute() {
        build().execute();
    }
}
