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

package org.apache.jena.sparql.engine.main.solver;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.Predicate;

import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.sparql.core.BasicPattern;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.sparql.engine.ExecutionContext;
import org.apache.jena.sparql.engine.QueryIterator;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.engine.iterator.Abortable;
import org.apache.jena.sparql.engine.iterator.QueryIterAbortable;

/**
 * Match a graph node + basic graph pattern.
 */
public class PatternMatchData {

    /**
     * Non-reordering execution of a triple pattern (basic graph pattern),
     * given an iterator of bindings as input.
     */
    public static QueryIterator execute(Graph graph, BasicPattern pattern,
                                        QueryIterator input, Predicate<Triple> filter,
                                        ExecutionContext execCxt)
    {
        List<Triple> triples = pattern.getList();

        Iterator<Binding> chain = input;
        List<Abortable> killList = new ArrayList<>();

        for ( Triple triple : triples ) {
            // Plain, no RDF-star
            //chain = StageMatchTriple.accessTriple(chain, graph, triple, filter, execCxt);

            // [Match] Missing filter.
            chain = SolverRX3.rdfStarTriple(chain, triple, execCxt);
            chain = SolverLib.makeAbortable(chain, killList);
        }

        // "input" will be closed by QueryIterAbortable but is otherwise unused.
        // "killList" will be aborted on timeout.
        return new QueryIterAbortable(chain, killList, input, execCxt);
    }

    /** Non-reordering execution of a quad pattern, a graph name and a basic graph pattern,
     *  given an iterator of bindings as input.
     *  <p>
     *  GraphNode is Node.ANY for execution over the union of named graphs.<br/>
     *  GraphNode is null for execution over the real default graph.
     */
    public static QueryIterator execute(DatasetGraph dsg, Node graphNode, BasicPattern pattern,
                                        QueryIterator input, Predicate<Quad> filter,
                                        ExecutionContext execCxt)
    {
        // Translate:
        //   graphNode may be Node.ANY, meaning we should make triples unique.
        //   graphNode may be null, meaning default graph
        if ( Quad.isUnionGraph(graphNode) )
            graphNode = Node.ANY;
        if ( Quad.isDefaultGraph(graphNode) )
            graphNode = null;

        List<Triple> triples = pattern.getList();
        boolean isDefaultGraph = (graphNode == null);
        boolean anyGraph = isDefaultGraph ? false : (Node.ANY.equals(graphNode));

        Iterator<Binding> chain = input;
        List<Abortable> killList = new ArrayList<>();

        for ( Triple triple : triples ) {
            // Plain - no RDF-star.
            //chain = StageMatchData.accessQuad(chain, graphNode, triple, filter, anyGraph, execCxt);

            // [Match] Missing filter.
            chain = SolverRX4.rdfStarQuad(chain, graphNode, triple, execCxt);
            chain = SolverLib.makeAbortable(chain, killList);
        }

        // "input" will be closed by QueryIterAbortable but is otherwise unused.
        // "killList" will be aborted on timeout.
        return new QueryIterAbortable(chain, killList, input, execCxt);
    }
}
