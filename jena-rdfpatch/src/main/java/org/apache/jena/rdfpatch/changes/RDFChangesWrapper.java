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

import org.apache.jena.graph.Node;
import org.apache.jena.rdfpatch.RDFChanges;

/** Wrapper for {@link RDFChanges} */
public class RDFChangesWrapper implements RDFChanges {

    private RDFChanges other;
    protected RDFChanges get() { return other ; }

    public RDFChangesWrapper(RDFChanges other) {
        this.other = other;
    }

    @Override
    public void start() {
        get().start();
    }

    @Override
    public void finish() {
        get().finish();
    }

    @Override
    public void header(String field, Node value) {
        get().header(field, value);
    }

    @Override
    public void add(Node g, Node s, Node p, Node o) {
        get().add(g, s, p, o);
    }

    @Override
    public void delete(Node g, Node s, Node p, Node o) {
        get().delete(g, s, p, o);
    }

    @Override
    public void addPrefix(Node gn, String prefix, String uriStr) {
        get().addPrefix(gn, prefix, uriStr);
    }

    @Override
    public void deletePrefix(Node gn, String prefix) {
        get().deletePrefix(gn, prefix);
    }

    @Override
    public void txnBegin() {
        get().txnBegin();
    }

    @Override
    public void txnCommit() {
        get().txnCommit();
    }

    @Override
    public void txnAbort() {
        get().txnAbort();
    }

    @Override
    public void segment() {
        get().segment();
    }
}
