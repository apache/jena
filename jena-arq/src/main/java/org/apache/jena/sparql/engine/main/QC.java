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

package org.apache.jena.sparql.engine.main;

import java.util.Iterator;

import org.apache.jena.graph.Triple;
import org.apache.jena.sparql.ARQConstants ;
import org.apache.jena.sparql.algebra.Op ;
import org.apache.jena.sparql.core.BasicPattern ;
import org.apache.jena.sparql.core.Substitute ;
import org.apache.jena.sparql.engine.ExecutionContext ;
import org.apache.jena.sparql.engine.QueryIterator ;
import org.apache.jena.sparql.engine.binding.Binding ;
import org.apache.jena.sparql.engine.iterator.QueryIterPlainWrapper;
import org.apache.jena.sparql.engine.iterator.QueryIterSingleton ;
import org.apache.jena.sparql.engine.main.solver.PatternMatchData;
import org.apache.jena.sparql.engine.main.solver.StageMatchTriple;
import org.apache.jena.sparql.util.Context ;

/** Library of operations related to query execution. */
public class QC
{
    public static OpExecutorFactory getFactory(Context context) {
        return (OpExecutorFactory)context.get(ARQConstants.sysOpExecutorFactory);
    }

    public static void setFactory(Context context, OpExecutorFactory factory) {
        context.set(ARQConstants.sysOpExecutorFactory, factory);
    }

    public static Op substitute(Op op, Binding binding) {
        return Substitute.substitute(op, binding);
    }

    public static QueryIterator execute(Op op, QueryIterator qIter, ExecutionContext execCxt) {
        return OpExecutor.execute(op, qIter, execCxt);
    }

    public static QueryIterator execute(Op op, Binding binding, ExecutionContext execCxt) {
        QueryIterator qIter = QueryIterSingleton.create(binding, execCxt);
        return OpExecutor.execute(op, qIter, execCxt);
    }

    /** Execute a BGP directly - no optimization. */
    public static QueryIterator executeDirect(BasicPattern pattern, QueryIterator input, ExecutionContext execCxt) {
        return PatternMatchData.execute(execCxt.getActiveGraph(), pattern, input, null, execCxt);
    }

    /**
     * Execute a triple pattern - top level variables only (no variables in RDF-sr embedded triples).
     * This is data access to asserted triples.
     */
    public static QueryIterator execute(QueryIterator input, Triple pattern, ExecutionContext execCxt) {
        Iterator<Binding> iter = StageMatchTriple.accessTriple(input, execCxt.getActiveGraph(), pattern, null, execCxt);
        return QueryIterPlainWrapper.create(iter, execCxt);
    }
}
