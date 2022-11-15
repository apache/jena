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

package org.apache.jena.rdfpatch;

import org.apache.jena.graph.Triple;
import org.apache.jena.rdfpatch.changes.RDFChangesCollector;
import org.apache.jena.riot.system.StreamRDF;
import org.apache.jena.sparql.core.Quad;

// DRY with StreamPatch.

/** An {@link StreamRDF} that converts to  patch format. */
public class RDF2Patch implements StreamRDF {
    RDFChangesCollector x = new RDFChangesCollector();
    private final RDFChanges changes;

//    public RDFPatch getRDFPatch() { return x.getRDFPatch(); }

    public RDF2Patch(RDFChanges changes) {
        this.changes = changes;
    }

    @Override
    public void start() {
        changes.txnBegin();
    }

    @Override
    public void triple(Triple triple) {
        changes.add(null, triple.getSubject(), triple.getPredicate(), triple.getObject());
    }

    @Override
    public void quad(Quad quad) {
        changes.add(quad.getGraph(), quad.getSubject(), quad.getPredicate(), quad.getObject());
    }

    @Override
    public void base(String base) {}

    @Override
    public void prefix(String prefix, String iri) {
        changes.addPrefix(null, prefix, iri);
    }

    @Override
    public void finish() {
        changes.txnCommit();
    }
}
