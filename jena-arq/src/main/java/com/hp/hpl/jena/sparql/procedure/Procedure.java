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

package com.hp.hpl.jena.sparql.procedure ;
import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.sparql.engine.ExecutionContext ;
import com.hp.hpl.jena.sparql.engine.QueryIterator ;
import com.hp.hpl.jena.sparql.expr.ExprList ;
import com.hp.hpl.jena.sparql.util.PrintSerializable ;

public interface Procedure extends PrintSerializable
{
    /**
     * Called during query plan construction immediately after the construction
     * of the property function instance.
     * 
     * @param procId
     *            The procedure identifier (usually a URI)
     * @param args
     *            The argument list (unevaluated expressions)
     * @param execCxt
     *            Execution context
     */
    public void build(Node procId, ExprList args, ExecutionContext execCxt) ;

    /**
     * Call the procedure, with an input iterator of bindings.
     * Implementations can inherit from the convenience form {@link ProcEval}
     * which calls repeated for each binding. 
     * 
     * @param input
     *            QueryIterator from the previous stage
     * @param execCxt
     *            The execution context
     * @return QueryIterator
     */
    public QueryIterator proc(QueryIterator input, ExecutionContext execCxt) ;
}
