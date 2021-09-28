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

package org.apache.jena.sparql.exec;

import org.apache.jena.graph.Node;
import org.apache.jena.query.ARQ;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.util.Context;
import org.apache.jena.sparql.util.Symbol;
import org.apache.jena.update.Update;
import org.apache.jena.update.UpdateRequest;

public interface UpdateExecBuilder {

    /** Set the update. */
    public UpdateExecBuilder update(UpdateRequest request);

    /** Set the update. */
    public UpdateExecBuilder update(Update update);

    /** Set the update. */
    public UpdateExecBuilder update(String updateString);

    /** Set a context entry. */
    public UpdateExecBuilder set(Symbol symbol, Object value);

    /** Set a context entry. */
    public UpdateExecBuilder set(Symbol symbol, boolean value);

    /**
     * Set the context. if not set, defaults to the system context
     * ({@link ARQ#getContext}).
     */
    public UpdateExecBuilder context(Context context);

    /** Provide a set of (Var, Node) for substitution in the query when QueryExec is built. */
    public UpdateExecBuilder substitution(Binding binding);

    /** Provide a (Var, Node) for substitution in the query when QueryExec is built. */
    public UpdateExecBuilder substitution(Var var, Node value);

    /** Provide a (Var name, Node) for substitution in the query when QueryExec is built. */
    public default UpdateExecBuilder substitution(String var, Node value) {
        return substitution(Var.alloc(var), value);
    }

    public UpdateExec build();

    /** Build and execute. */
    public default void execute() {
        build().execute();
    }
}
