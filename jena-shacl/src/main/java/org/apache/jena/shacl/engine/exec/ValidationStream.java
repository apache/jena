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

package org.apache.jena.shacl.engine.exec;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.riot.system.StreamRDF;
import org.apache.jena.riot.system.StreamRDFWrapper;
import org.apache.jena.shacl.engine.ValidationContext;
import org.apache.jena.shacl.lib.ShLib;
import org.apache.jena.shacl.validation.ReportItem;
import org.apache.jena.sparql.path.P_Link;

public class ValidationStream extends StreamRDFWrapper {
    private ValidationContext vCxt;
    private Collection<TripleValidator> validators;
    private final Set<Node> predicates = new HashSet<>();

    public ValidationStream(ValidationContext vCxt, Collection<TripleValidator> validators, StreamRDF dest) {
        super(dest);
        this.vCxt = vCxt;
        this.validators = validators;
        validators.stream()
            .filter(v->ShLib.isImmediate(v.getTarget()))
            .forEach(v->{
                if ( v.getPath() != null ) {
                    // sh:path.
                    P_Link pl = (P_Link)(v.getPath());
                    Node p = pl.getNode();
                    predicates.add(p);
                    // Later: More complex path : end predicate.
                } else {
                    // sh:targetObjectsOf
                    predicates.add(v.getTarget().getObject());
                }
            });


//            .map(v->v.getTarget())
//            .forEach(t->predicates.add(t.getObject()));

        predicates.forEach(p->System.out.println("  ValidationStream predicate: <"+p.getURI()+">"));
    }

    @Override
    public void start() { super.start(); }

    @Override
    public void triple(Triple triple) {
        if ( !predicates.contains(triple.getPredicate()) )
            return;
        for ( TripleValidator validator : validators ) {
            ReportItem item = validator.validate(vCxt, triple);
            if (item != null )
                vCxt.reportEntry(item, validator, triple);
        }
    }

//    @Override
//    public void quad(Quad quad) {}
//
//    @Override
//    public void base(String base) {}
//
//    @Override
//    public void prefix(String prefix, String iri) {}

    @Override
    public void finish() { super.finish(); }
}
