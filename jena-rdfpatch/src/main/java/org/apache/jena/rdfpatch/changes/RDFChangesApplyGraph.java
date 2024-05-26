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

package org.apache.jena.rdfpatch.changes;

import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.rdfpatch.PatchException;
import org.apache.jena.rdfpatch.RDFChanges;
import org.apache.jena.sparql.util.FmtUtils;

/** Apply changes to a {@link Graph} */
public class RDFChangesApplyGraph implements RDFChanges {

    private Graph graph;

    public RDFChangesApplyGraph(Graph graph) {
        this.graph = graph;
    }

    private static class QuadDataException extends PatchException {
        QuadDataException(String msg) { super(msg) ; }
    }

    @Override
    public void start() {}

    @Override
    public void finish() {}

    @Override
    public void segment() {}

    @Override
    public void header(String field, Node value) {}

    @Override
    public void add(Node g, Node s, Node p, Node o) {
        if ( g != null )
            throw new QuadDataException("Attempt to add quad data to a graph: g="+FmtUtils.stringForNode(g));
        graph.add(Triple.create(s, p, o));
    }

    @Override
    public void delete(Node g, Node s, Node p, Node o) {
        if ( g != null )
            throw new QuadDataException("Attempt to delete quad data to a graph: g="+FmtUtils.stringForNode(g));
        graph.delete(Triple.create(s, p, o));
    }

    @Override
    public void addPrefix(Node gn, String prefix, String uriStr) {
        graph.getPrefixMapping().setNsPrefix(prefix, uriStr);
    }

    @Override
    public void deletePrefix(Node gn, String prefix) {
        graph.getPrefixMapping().removeNsPrefix(prefix);
    }

    @Override
    public void txnBegin() {
        graph.getTransactionHandler().begin();
    }

    @Override
    public void txnCommit() {
        graph.getTransactionHandler().commit();
    }

    @Override
    public void txnAbort() {
        graph.getTransactionHandler().abort();
    }
}
