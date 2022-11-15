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

import java.util.ArrayList;
import java.util.List;

import org.apache.jena.graph.Node;
import org.apache.jena.rdfpatch.RDFChanges;

/**
 *  An {@link RDFChanges} that replicates the stream of changes to N other {@link RDFChanges} streams.
 */
public class RDFChangesN implements RDFChanges
{
    /** Create a 2-way {@code RDFChangesN} */
    public static RDFChanges multi(RDFChanges sc1, RDFChanges sc2) {
        if ( sc1 == null )
            return sc2;
        if ( sc2 == null )
            return sc1;
        if ( sc1 instanceof RDFChangesN ) {
            ((RDFChangesN)sc1).add(sc2);
            return sc1;
        } else {
            return new RDFChangesN(sc1, sc2) ;
        }
    }

    private final List<RDFChanges> changes = new ArrayList<>();
    public RDFChangesN(RDFChanges... changes) {
        for ( RDFChanges sc : changes ) {
            add(sc);
        }
    }

    public RDFChangesN(List<RDFChanges> changes) {
        for ( RDFChanges sc : changes ) {
            add(sc);
        }
    }

    private void add(RDFChanges sc) {
        changes.add(sc);
    }

    @Override
    public void start() {
        changes.forEach(RDFChanges::start);
    }

    @Override
    public void finish() {
        changes.forEach(RDFChanges::finish);
    }

    @Override
    public void header(String field, Node value) {
        changes.forEach(c->c.header(field, value));
    }

    @Override
    public void add(Node g, Node s, Node p, Node o) {
        changes.forEach(sc->sc.add(g,s,p,o));
    }

    @Override
    public void delete(Node g, Node s, Node p, Node o) {
        changes.forEach(sc->sc.delete(g,s,p,o));
    }

    @Override
    public void addPrefix(Node graph, String prefix, String uriStr) {
        changes.forEach(sc->sc.addPrefix(graph, prefix, uriStr));
    }

    @Override
    public void deletePrefix(Node graph, String prefix) {
        changes.forEach(sc->sc.deletePrefix(graph, prefix));
    }

    @Override
    public void txnBegin() {
        changes.forEach(sc->sc.txnBegin());
    }

    @Override
    public void txnCommit() {
        changes.forEach(RDFChanges::txnCommit);
    }

    @Override
    public void txnAbort() {
        changes.forEach(RDFChanges::txnAbort);
    }

    @Override
    public void segment() {
        changes.forEach(RDFChanges::segment);
    }
}
