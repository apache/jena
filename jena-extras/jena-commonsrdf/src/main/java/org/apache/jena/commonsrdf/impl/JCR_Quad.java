/**
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

package org.apache.jena.commonsrdf.impl;

import java.util.Objects;
import java.util.Optional;

import org.apache.commons.rdf.api.BlankNodeOrIRI;
import org.apache.commons.rdf.api.IRI;
import org.apache.commons.rdf.api.Quad;
import org.apache.commons.rdf.api.RDFTerm;
import org.apache.jena.commonsrdf.JenaCommonsRDF;
import org.apache.jena.graph.Node;

public class JCR_Quad implements Quad, JenaQuad {
    private final Optional<BlankNodeOrIRI> graphName;
    private final BlankNodeOrIRI subject;
    private final IRI predicate;
    private final RDFTerm object;
    private org.apache.jena.sparql.core.Quad quad = null;

//    /*package*/ JCR_Quad(BlankNodeOrIRI graphName, BlankNodeOrIRI subject, IRI predicate, RDFTerm object) {
//        this(Optional.of(graphName), subject, predicate, object);
//    }

    /*package*/ JCR_Quad(Optional<BlankNodeOrIRI> graphName, BlankNodeOrIRI subject, IRI predicate, RDFTerm object) {
        this.graphName = graphName;
        this.subject = subject;
        this.predicate = predicate;
        this.object = object;
    }

    /*package*/ JCR_Quad(org.apache.jena.sparql.core.Quad quad) {
        Node gn = quad.getGraph();
        this.graphName = graphName(gn);
        this.subject = (BlankNodeOrIRI)JCR_Factory.fromJena(quad.getSubject());
        this.predicate = (IRI)JCR_Factory.fromJena(quad.getPredicate());
        this.object = JCR_Factory.fromJena(quad.getObject());
        this.quad = quad;
    }


    private static Optional<BlankNodeOrIRI> graphName(Node gn) {
        return (gn == null || org.apache.jena.sparql.core.Quad.isDefaultGraph(gn))
        ? Optional.empty()
        : Optional.of((BlankNodeOrIRI)JCR_Factory.fromJena(gn));
    }

    private static Node graphName(Optional<BlankNodeOrIRI> graphName) {
        if ( ! graphName.isPresent() )
            return org.apache.jena.sparql.core.Quad.defaultGraphIRI;
        return  JenaCommonsRDF.toJena(graphName.get());
    }

    @Override
    public org.apache.jena.sparql.core.Quad getQuad() {
        if ( quad == null )
            quad = org.apache.jena.sparql.core.Quad.create(graphName(graphName), JenaCommonsRDF.toJena(subject), JenaCommonsRDF.toJena(predicate), JenaCommonsRDF.toJena(object));
        return quad;
    }

    @Override
    public Optional<BlankNodeOrIRI> getGraphName() {
        return graphName;
    }

    @Override
    public BlankNodeOrIRI getSubject() {
        return subject;
    }

    @Override
    public IRI getPredicate() {
        return predicate;
    }

    @Override
    public RDFTerm getObject() {
        return object;
    }

    @Override
    public int hashCode() {
        return Objects.hash(getSubject(), getPredicate(), getObject());
    }

    @Override
    public boolean equals(Object other) {
        if ( other == this ) return true;
        if ( other == null ) return false;
        if ( ! ( other instanceof Quad ) ) return false;
        Quad quad = (Quad)other;
        return
            getGraphName().equals(quad.getGraphName()) &&
            getSubject().equals(quad.getSubject()) &&
            getPredicate().equals(quad.getPredicate()) &&
            getObject().equals(quad.getObject());
    }

    @Override
    public String toString() {
        return getGraphName()+" "+getSubject()+" "+getPredicate()+" "+getObject()+" .";
    }
}

