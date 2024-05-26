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

package org.apache.jena.rdfpatch.system;

import org.apache.jena.graph.*;
import org.apache.jena.rdfpatch.RDFChanges;
import org.apache.jena.shared.PrefixMapping;
import org.apache.jena.sparql.graph.GraphWrapper;

/**
 * Connect a {@link Graph} with {@link RDFChanges}. All operations on the {@link Graph}
 * that cause changes have the change sent to the {@link RDFChanges}.
 * <p>
 * Use {@link GraphRealChanges} to get a graph that logs only changes that have a real
 * effect - that makes the changes log reversible (play delete for each add) to undo a
 * sequence of changes.
 * <p>
 * The graph name is settable; it is arbitrary, or null, and not deduced from the graph.
 * <p>
 * If the graph is a graph from a dataset and the same graph name is to be used, then
 * instead of this class, get a graph from {@link DatasetGraphChanges}.
 *
 * @see DatasetGraphChanges
 * @see RDFChanges
 */
public class GraphChanges extends GraphWrapper /*implements GraphWithPerform*/ // Or WrappedGraph
{
    private final RDFChanges changes;
    protected final Node graphName;
    private final PrefixMapping prefixMapping;
    private final TransactionHandler transactionHandler;

    /**
     * Send changes to a graph to a {@link RDFChanges} with null for the graph name.
     */
    public GraphChanges(Graph graph, RDFChanges changes) {
        this(graph, null, changes);
    }

    /**
     * Send changes to a graph to a {@link RDFChanges} with the specified graph name.
     * The graph name may be null for "no name".
     */
    public GraphChanges(Graph graph, Node graphName, RDFChanges changes) {
        super(graph);
        this.graphName = graphName;
        this.changes = changes;
        this.prefixMapping = new PrefixMappingChanges(graph, graphName, changes);
        this.transactionHandler = new TransactionHandlerMonitor(graph.getTransactionHandler(), changes);
    }

    @Override
    public void add(Triple t) {
        changes.add(graphName,  t.getSubject(), t.getPredicate(), t.getObject());
        super.add(t);
    }

    @Override
    public void delete(Triple t) {
        changes.delete(graphName,  t.getSubject(), t.getPredicate(), t.getObject());
        super.delete(t);
    }

//    @Override public void performAdd( Triple t )    { add(t); }
//    @Override public void performDelete( Triple t ) { delete(t); }

    @Override
    public void clear() {
        remove(Node.ANY, Node.ANY, Node.ANY);
    }

    @Override
    public PrefixMapping getPrefixMapping() {
        return prefixMapping;
    }

    @Override
    public void remove(Node s, Node p, Node o) {
        // Convert to calls to delete.
        GraphUtil.remove(this, s, p, o);
    }

    @Override
    public TransactionHandler getTransactionHandler() {
        return transactionHandler;
    }
}
