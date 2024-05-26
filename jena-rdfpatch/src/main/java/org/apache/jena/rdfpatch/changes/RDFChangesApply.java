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
import org.apache.jena.query.ReadWrite;
import org.apache.jena.rdfpatch.RDFChanges;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.Quad;

/** Apply changes to a {@link DatasetGraph} */
public class RDFChangesApply implements RDFChanges {

    private DatasetGraph dsg;

    public RDFChangesApply(DatasetGraph dsg) {
        this.dsg = dsg;
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
        if ( g == null )
            g = Quad.defaultGraphNodeGenerated;
        dsg.add(g, s, p, o);
    }

    @Override
    public void delete(Node g, Node s, Node p, Node o) {
        if ( g == null )
            g = Quad.defaultGraphNodeGenerated;
        dsg.delete(g, s, p, o);
    }

    @Override
    public void addPrefix(Node gn, String prefix, String uriStr) {
        Graph g = ( gn == null ) ? dsg.getDefaultGraph() : dsg.getGraph(gn);
        g.getPrefixMapping().setNsPrefix(prefix, uriStr);
    }

    @Override
    public void deletePrefix(Node gn, String prefix) {
        Graph g = ( gn == null ) ? dsg.getDefaultGraph() : dsg.getGraph(gn);
        g.getPrefixMapping().removeNsPrefix(prefix);
    }

    @Override
    public void txnBegin() {
        dsg.begin(ReadWrite.WRITE);
    }

    @Override
    public void txnCommit() {
        dsg.commit();
    }

    @Override
    public void txnAbort() {
        dsg.abort();
    }
}
