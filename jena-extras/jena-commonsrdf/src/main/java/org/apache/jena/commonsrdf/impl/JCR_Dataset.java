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

package org.apache.jena.commonsrdf.impl;

import static org.apache.jena.commonsrdf.JenaCommonsRDF.fromJena;
import static org.apache.jena.commonsrdf.JenaCommonsRDF.*;

import java.util.Iterator;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;

import org.apache.commons.rdf.api.*;
import org.apache.jena.atlas.iterator.Iter;
import org.apache.jena.commonsrdf.JenaCommonsRDF;
import org.apache.jena.graph.Node;
import org.apache.jena.sparql.core.DatasetGraph;

public class JCR_Dataset implements Dataset, JenaDataset {

    private DatasetGraph dataset;

    /*package*/ JCR_Dataset(org.apache.jena.sparql.core.DatasetGraph datasetGraph) {
        this.dataset = datasetGraph;
    }

    @Override
    public org.apache.jena.sparql.core.DatasetGraph getDataset() {
        return dataset;
    }

    @Override
    public void add(Quad quad) { dataset.add(toJena(quad)); }

    @Override
    public void add(BlankNodeOrIRI graphName, BlankNodeOrIRI subject, IRI predicate, RDFTerm object) {
        if ( graphName == null )
            dataset.add(org.apache.jena.sparql.core.Quad.defaultGraphNodeGenerated, toJena(subject), toJena(predicate), toJena(object));
        else
            dataset.add(toJena(graphName), toJena(subject), toJena(predicate), toJena(object));
    }

    @Override
    public boolean contains(Quad quad) {
        return dataset.contains(toJena(quad));
    }

    @Override
    public boolean contains(Optional<BlankNodeOrIRI> graphName, BlankNodeOrIRI subject, IRI predicate, RDFTerm object) {
        return dataset.contains(toJenaAny(graphName), toJenaAny(subject), toJenaAny(predicate), toJenaAny(object));
    }

    @Override
    public Graph getGraph() {
        org.apache.jena.graph.Graph graph = dataset.getDefaultGraph();
        return fromJena(graph);
    }

    @Override
    public Optional<Graph> getGraph(BlankNodeOrIRI graphName) {
        if ( graphName == null )
            return Optional.of(fromJena(dataset.getDefaultGraph()));
        org.apache.jena.graph.Node node = toJena(graphName);
        org.apache.jena.graph.Graph graph = dataset.getGraph(node);
        if ( graph == null )
            return Optional.empty();
        return Optional.of(fromJena(graph));
    }

    @Override
    public Stream<BlankNodeOrIRI> getGraphNames() {
        Iterator<org.apache.jena.graph.Node> iter = dataset.listGraphNodes();
        Function<Node, BlankNodeOrIRI> mapper = n-> n.isBlank() ? new JCR_BlankNode(n) : new JCR_IRI(n);
        return Iter.asStream(iter).map(mapper);
    }

    @Override
    public void remove(Quad quad) { dataset.delete(toJena(quad)); }

    @Override
    public void remove(Optional<BlankNodeOrIRI> graphName, BlankNodeOrIRI subject, IRI predicate, RDFTerm object) {
        dataset.deleteAny(toJenaAny(graphName), toJenaAny(subject), toJenaAny(predicate), toJenaAny(object));
    }

    @Override
    public void clear() {
        dataset.clear();
    }

    @Override
    public long size() {
        return Iter.count(dataset.find());
    }

    @Override
    public Stream<? extends Quad> stream() {
        return Iter.asStream(dataset.find()).map(q->fromJena(q));
    }

    @Override
    public Stream<? extends Quad> stream(Optional<BlankNodeOrIRI> graphName, BlankNodeOrIRI subject, IRI predicate, RDFTerm object) {
        return Iter.asStream(dataset.find(
            toJenaAny(graphName), toJena(subject), toJena(predicate), toJena(object)
        )).map(q->fromJena(q));
    }

    private Node ny(RDFTerm term) {
        if ( term == null )
            return Node.ANY;
        return JenaCommonsRDF.toJena(term);
    }


}
