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
package org.apache.jena.geosparql.geo.topological;

import org.apache.jena.datatypes.DatatypeFormatException;
import org.apache.jena.geosparql.implementation.GeometryWrapper;
import org.apache.jena.geosparql.implementation.vocabulary.Geo;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.riot.other.G;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.ExecutionContext;
import org.apache.jena.sparql.engine.QueryIterator;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.engine.binding.BindingFactory;
import org.apache.jena.sparql.engine.iterator.QueryIterConcat;
import org.apache.jena.sparql.engine.iterator.QueryIterNullIterator;
import org.apache.jena.sparql.engine.iterator.QueryIterSingleton;
import org.apache.jena.sparql.expr.ExprEvalException;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.pfunction.PFuncSimple;
import org.apache.jena.util.iterator.ExtendedIterator;
import org.apache.jena.vocabulary.RDF;

/**
 *
 *
 */
public abstract class GenericGeometryPropertyFunction extends PFuncSimple {

    protected abstract NodeValue applyPredicate(GeometryWrapper geometryWrapper);

    @Override
    public QueryIterator execEvaluated(Binding binding, Node subject, Node predicate, Node object, ExecutionContext execCxt) {

        if (subject.isConcrete() && object.isLiteral()) {
            //Both are bound.
            return bothBound(binding, subject, predicate, object, execCxt);
        } else if (subject.isVariable() && object.isVariable()) {
            //Both are unbound.
            return bothUnbound(binding, subject, predicate, object, execCxt);
        } else if (subject.isConcrete() && object.isVariable()) {
            //Subject bound and object unbound.
            return objectUnbound(binding, subject, predicate, object, execCxt);
        } else {
            //Subject unbound and object bound.
            return subjectUnbound(binding, subject, predicate, object, execCxt);
        }

    }

    protected Node getGeometryLiteral(Node subject, Node predicate, Graph graph) throws ExprEvalException {

        try {
            //Check for the asserted value and return if found.
            if (graph.contains(subject, predicate, null))
                return G.getSP(graph, subject, predicate);

            //Check that the Geometry has a serialisation to use.
            Node geomLiteral = G.getSP(graph, subject, Geo.HAS_SERIALIZATION_NODE);

            // If hasSerialization not found then check asWKT and asGML.
            if (geomLiteral == null) {
                geomLiteral = G.getSP(graph, subject, Geo.AS_WKT_NODE);
                if (geomLiteral == null)
                    geomLiteral = G.getSP(graph, subject, Geo.AS_GML_NODE);
            }

            if (geomLiteral != null) {
                GeometryWrapper geometryWrapper = GeometryWrapper.extract(geomLiteral);
                NodeValue predicateResult = applyPredicate(geometryWrapper);
                return predicateResult.asNode();
            }

            return null;

        } catch (DatatypeFormatException ex) {
            throw new ExprEvalException(ex.getMessage(), ex);
        }
    }

    private QueryIterator bothBound(Binding binding, Node subject, Node predicate, Node object, ExecutionContext execCxt) {

        //Check that the subject and object binding are valid for the predicate.
        Graph graph = execCxt.getActiveGraph();
        Node geometryLiteral = getGeometryLiteral(subject, predicate, graph);

        if (geometryLiteral != null) {
            if (object.matches(geometryLiteral)) {
                return QueryIterSingleton.create(binding, execCxt);
            }
        }

        return QueryIterNullIterator.create(execCxt);
    }

    private QueryIterator subjectUnbound(Binding binding, Node subject, Node predicate, Node object, ExecutionContext execCxt) {
        QueryIterConcat queryIterConcat = new QueryIterConcat(execCxt);

        Graph graph = execCxt.getActiveGraph();

        ExtendedIterator<Triple> subjectTriples = graph.find(null, RDF.type.asNode(), Geo.GEOMETRY_NODE);

        Var subjectVar = Var.alloc(subject.getName());
        while (subjectTriples.hasNext()) {
            Triple subjectTriple = subjectTriples.next();
            Binding newBind = BindingFactory.binding(binding, subjectVar, subjectTriple.getSubject());
            QueryIterator queryIter = bothBound(newBind, subjectTriple.getSubject(), predicate, object, execCxt);
            queryIterConcat.add(queryIter);
        }

        return queryIterConcat;
    }

    private QueryIterator objectUnbound(Binding binding, Node subject, Node predicate, Node object, ExecutionContext execCxt) {

        Graph graph = execCxt.getActiveGraph();
        Node geometryLiteral = getGeometryLiteral(subject, predicate, graph);

        if (geometryLiteral != null) {
            return QueryIterSingleton.create(binding, Var.alloc(object.getName()), geometryLiteral, execCxt);
        } else {
            return QueryIterNullIterator.create(execCxt);
        }
    }

    private QueryIterator bothUnbound(Binding binding, Node subject, Node predicate, Node object, ExecutionContext execCxt) {
        QueryIterConcat queryIterConcat = new QueryIterConcat(execCxt);

        Graph graph = execCxt.getActiveGraph();

        ExtendedIterator<Triple> subjectTriples = graph.find(null, RDF.type.asNode(), Geo.GEOMETRY_NODE);

        Var subjectVar = Var.alloc(subject.getName());
        while (subjectTriples.hasNext()) {
            Triple subjectTriple = subjectTriples.next();
            Binding newBind = BindingFactory.binding(binding, subjectVar, subjectTriple.getSubject());
            QueryIterator queryIter = objectUnbound(newBind, subjectTriple.getSubject(), predicate, object, execCxt);
            queryIterConcat.add(queryIter);
        }

        return queryIterConcat;
    }

}
