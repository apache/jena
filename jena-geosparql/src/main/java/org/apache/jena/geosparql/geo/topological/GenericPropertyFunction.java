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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import org.apache.jena.geosparql.geof.topological.GenericFilterFunction;
import org.apache.jena.geosparql.implementation.GeometryWrapper;
import org.apache.jena.geosparql.implementation.index.QueryRewriteIndex;
import org.apache.jena.geosparql.implementation.vocabulary.Geo;
import org.apache.jena.geosparql.implementation.vocabulary.SpatialExtension;
import org.apache.jena.geosparql.spatial.SpatialIndex;
import org.apache.jena.geosparql.spatial.SpatialIndexException;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.ExecutionContext;
import org.apache.jena.sparql.engine.QueryIterator;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.engine.binding.BindingFactory;
import org.apache.jena.sparql.engine.iterator.QueryIterConcat;
import org.apache.jena.sparql.engine.iterator.QueryIterNullIterator;
import org.apache.jena.sparql.engine.iterator.QueryIterSingleton;
import org.apache.jena.sparql.expr.ExprEvalException;
import org.apache.jena.sparql.pfunction.PFuncSimple;
import org.apache.jena.sparql.util.FmtUtils;
import org.apache.jena.util.iterator.ExtendedIterator;
import org.apache.jena.vocabulary.RDF;
import org.locationtech.jts.geom.Envelope;
import org.opengis.geometry.MismatchedDimensionException;
import org.opengis.referencing.operation.TransformException;
import org.opengis.util.FactoryException;

/**
 *
 *
 *
 */
public abstract class GenericPropertyFunction extends PFuncSimple {

    private final GenericFilterFunction filterFunction;

    public GenericPropertyFunction(GenericFilterFunction filterFunction) {
        this.filterFunction = filterFunction;
    }

    @Override
    public QueryIterator execEvaluated(Binding binding, Node subject, Node predicate, Node object, ExecutionContext execCxt) {
        // optionally accept bound literals for simpler usage

//        if (object.isLiteral()) {
//            //These Property Functions do not accept literals as objects so exit quickly.
//            return QueryIterNullIterator.create(execCxt);
//        }

        if (subject.isConcrete() && object.isConcrete()) {
            //Both are bound.
            return bothBound(binding, subject, predicate, object, execCxt);
        } else if (subject.isVariable() && object.isVariable()) {
            //Both are unbound.
            return bothUnbound(binding, subject, predicate, object, execCxt);
        } else {
            //One bound and one unbound.
            return oneBound(binding, subject, predicate, object, execCxt);
        }

    }

    private QueryIterator bothBound(Binding binding, Node subject, Node predicate, Node object, ExecutionContext execCxt) {

        Graph graph = execCxt.getActiveGraph();
        QueryRewriteIndex queryRewriteIndex = QueryRewriteIndex.retrieve(execCxt);

        Boolean isPositiveResult = queryRewrite(graph, subject, predicate, object, queryRewriteIndex);
        if (isPositiveResult) {
            //Filter function test succeded so retain binding.
            return QueryIterSingleton.create(binding, execCxt);
        } else {
            //Filter function test failed so null result.
            return QueryIterNullIterator.create(execCxt);
        }

    }

    private QueryIterator bothUnbound(Binding binding, Node subject, Node predicate, Node object, ExecutionContext execCxt) {

        QueryIterConcat queryIterConcat = new QueryIterConcat(execCxt);
        Var subjectVar = Var.alloc(subject.getName());

        Graph graph = execCxt.getActiveGraph();

        //Search for both Features and Geometry in the Graph. Reliant upon consistent usage of SpatialObject (which is base class of Feature and Geometry) if present.
        ExtendedIterator<Triple> subjectTriples;
        if (graph.contains(null, RDF.type.asNode(), Geo.SPATIAL_OBJECT_NODE)) {
            subjectTriples = graph.find(null, RDF.type.asNode(), Geo.SPATIAL_OBJECT_NODE);
        } else if (graph.contains(null, RDF.type.asNode(), Geo.FEATURE_NODE) || graph.contains(null, RDF.type.asNode(), Geo.GEOMETRY_NODE)) {
            ExtendedIterator<Triple> featureTriples = graph.find(null, RDF.type.asNode(), Geo.FEATURE_NODE);
            ExtendedIterator<Triple> geometryTriples = graph.find(null, RDF.type.asNode(), Geo.GEOMETRY_NODE);
            subjectTriples = featureTriples.andThen(geometryTriples);
        } else {
            //Check for Geo Predicate Features in the Graph if no GeometryLiterals found.
            subjectTriples = graph.find(null, SpatialExtension.GEO_LAT_NODE, null);
        }

        //Bind all the Spatial Objects or Geo Predicates once as the subject and search for corresponding Objects.
        while (subjectTriples.hasNext()) {
            Triple subjectTriple = subjectTriples.next();
            Node boundSubject = subjectTriple.getSubject();
            Binding subjectBind = BindingFactory.binding(binding, subjectVar, boundSubject);
            QueryIterator queryIter = oneBound(subjectBind, boundSubject, predicate, object, execCxt);
            queryIterConcat.add(queryIter);
        }

        return queryIterConcat;
    }

    private QueryIterator oneBound(Binding binding, Node subject, Node predicate, Node object, ExecutionContext execCxt) {

        Graph graph = execCxt.getActiveGraph();
        Node boundNode;
        Node unboundNode;
        Boolean isSubjectBound;
        if (subject.isConcrete()) {
            //Subject is bound, object is unbound.
            boundNode = subject;
            unboundNode = object;
            isSubjectBound = true;
        } else {
            //Object is bound, subject is unbound.
            boundNode = object;
            unboundNode = subject;
            isSubjectBound = false;
        }

        if (!(boundNode.isLiteral() || graph.contains(boundNode, RDF.type.asNode(), Geo.SPATIAL_OBJECT_NODE) || graph.contains(boundNode, RDF.type.asNode(), Geo.FEATURE_NODE) || graph.contains(boundNode, RDF.type.asNode(), Geo.GEOMETRY_NODE))) {
            if (!graph.contains(boundNode, SpatialExtension.GEO_LAT_NODE, null)) {
                //Bound node is not a Feature or a Geometry or has Geo predicates so exit.
                return QueryIterNullIterator.create(execCxt);
            }
        }

        boolean isSpatialIndex = SpatialIndex.isDefined(execCxt);
        QueryIterConcat queryIterConcat;
        if (!isSpatialIndex || filterFunction.isDisjoint() || filterFunction.isDisconnected()) {
            //Disjointed so retrieve all cases.
            queryIterConcat = findAll(graph, boundNode, unboundNode, binding, isSubjectBound, predicate, execCxt);
        } else {
            //Only retrieve those in the spatial index which are within same bounding box.
            queryIterConcat = findIndex(graph, boundNode, unboundNode, binding, isSubjectBound, predicate, execCxt);
        }

        return queryIterConcat;
    }

    private QueryIterConcat findAll(Graph graph, Node boundNode, Node unboundNode, Binding binding, boolean isSubjectBound, Node predicate, ExecutionContext execCxt) {

        //Prepare the results.
        Var unboundVar = Var.alloc(unboundNode.getName());
        QueryIterConcat queryIterConcat = new QueryIterConcat(execCxt);

        //Search for both Features and Geometry in the Graph. Reliant upon consistent usage of SpatialObject (which is base class of Feature and Geometry) if present.
        ExtendedIterator<Triple> spatialTriples;
        if (graph.contains(null, RDF.type.asNode(), Geo.SPATIAL_OBJECT_NODE)) {
            spatialTriples = graph.find(null, RDF.type.asNode(), Geo.SPATIAL_OBJECT_NODE);
        } else if (graph.contains(null, RDF.type.asNode(), Geo.FEATURE_NODE) || graph.contains(null, RDF.type.asNode(), Geo.GEOMETRY_NODE)) {
            ExtendedIterator<Triple> featureTriples = graph.find(null, RDF.type.asNode(), Geo.FEATURE_NODE);
            ExtendedIterator<Triple> geometryTriples = graph.find(null, RDF.type.asNode(), Geo.GEOMETRY_NODE);
            spatialTriples = featureTriples.andThen(geometryTriples);
        } else {
            //Check for Geo Predicate Features in the Graph if no GeometryLiterals found.
            spatialTriples = graph.find(null, SpatialExtension.GEO_LAT_NODE, null);
        }

        while (spatialTriples.hasNext()) {
            Triple spatialTriple = spatialTriples.next();
            Node spatialNode = spatialTriple.getSubject();
            Binding newBind = BindingFactory.binding(binding, unboundVar, spatialNode);
            QueryIterator queryIter;
            if (isSubjectBound) {
                queryIter = bothBound(newBind, boundNode, predicate, spatialNode, execCxt);
            } else {
                queryIter = bothBound(newBind, spatialNode, predicate, boundNode, execCxt);
            }
            queryIterConcat.add(queryIter);
        }

        return queryIterConcat;
    }

    private QueryIterConcat findIndex(Graph graph, Node boundNode, Node unboundNode, Binding binding, boolean isSubjectBound, Node predicate, ExecutionContext execCxt) throws ExprEvalException {

        try {
            //Prepare for results.
            Var unboundVar = Var.alloc(unboundNode.getName());
            QueryIterConcat queryIterConcat = new QueryIterConcat(execCxt);

            //Find the asserted triples.
            List<Node> assertedNodes = !isSubjectBound || !boundNode.isLiteral() ? findAsserted(graph, boundNode, isSubjectBound, predicate) : Collections.emptyList();
            for (Node node : assertedNodes) {
                Binding newBind = BindingFactory.binding(binding, unboundVar, node);
                QueryIterator queryIter = QueryIterSingleton.create(newBind, execCxt);
                queryIterConcat.add(queryIter);
            }

            //Find the GeometryLiteral of the Bound Node.
            SpatialObjectGeometryLiteral boundGeometryLiteral = SpatialObjectGeometryLiteral.retrieve(graph, boundNode);
            if (!boundGeometryLiteral.isValid()) {
                //Bound Node is not a Feature or a Geometry or there is no GeometryLiteral so exit.
                return queryIterConcat;
            }

            Node geometryLiteral = boundGeometryLiteral.getGeometryLiteral();

            //Perform the search of the Spatial Index of the Dataset.
            SpatialIndex spatialIndex = SpatialIndex.retrieve(execCxt);
            GeometryWrapper geom = GeometryWrapper.extract(geometryLiteral);
            GeometryWrapper transformedGeom = geom.transform(spatialIndex.getSrsInfo());
            Envelope searchEnvelope = transformedGeom.getEnvelope();
            HashSet<Resource> features = spatialIndex.query(searchEnvelope);

            //Check each of the Features that match the search.
            for (Resource feature : features) {
                Node featureNode = feature.asNode();

                //Ensure not already an asserted node.
                if (!assertedNodes.contains(featureNode)) {

                    Binding newBind = BindingFactory.binding(binding, unboundVar, featureNode);
                    QueryIterator queryIter;
                    if (isSubjectBound) {
                        queryIter = bothBound(newBind, boundNode, predicate, featureNode, execCxt);
                    } else {
                        queryIter = bothBound(newBind, featureNode, predicate, boundNode, execCxt);
                    }
                    queryIterConcat.add(queryIter);
                }

                //Also test all Geometry of the Features. All, some or one Geometry may have matched.
                ExtendedIterator<Triple> featureGeometryTriples = graph.find(feature.asNode(), Geo.HAS_GEOMETRY_NODE, null);
                while (featureGeometryTriples.hasNext()) {
                    Triple unboundTriple = featureGeometryTriples.next();
                    Node geomNode = unboundTriple.getObject();

                    //Ensure not already an asserted node.
                    if (!assertedNodes.contains(geomNode)) {
                        Binding newBind = BindingFactory.binding(binding, unboundVar, geomNode);
                        QueryIterator queryIter;
                        if (isSubjectBound) {
                            queryIter = bothBound(newBind, boundNode, predicate, geomNode, execCxt);
                        } else {
                            queryIter = bothBound(newBind, geomNode, predicate, boundNode, execCxt);
                        }
                        queryIterConcat.add(queryIter);
                    }
                }
            }

            return queryIterConcat;
        } catch (MismatchedDimensionException | TransformException | FactoryException | SpatialIndexException ex) {
            throw new ExprEvalException(ex.getMessage() + ": " + FmtUtils.stringForNode(boundNode) + ", " + FmtUtils.stringForNode(unboundNode) + ", " + FmtUtils.stringForNode(predicate), ex);
        }
    }

    private List<Node> findAsserted(Graph graph, Node boundNode, boolean isSubjectBound, Node predicate) {
        List<Node> assertedNodes = new ArrayList<>();
        if (isSubjectBound) {
            ExtendedIterator<Triple> assertedTriples = graph.find(boundNode, predicate, null);
            while (assertedTriples.hasNext()) {
                Node assertedNode = assertedTriples.next().getObject();
                assertedNodes.add(assertedNode);
            }
        } else {
            ExtendedIterator<Triple> assertedTriples = graph.find(null, predicate, boundNode);
            while (assertedTriples.hasNext()) {
                Node assertedNode = assertedTriples.next().getSubject();
                assertedNodes.add(assertedNode);
            }
        }
        return assertedNodes;
    }

    protected final Boolean queryRewrite(Graph graph, Node subject, Node predicate, Node object, QueryRewriteIndex queryRewriteIndex) {

        if (graph.contains(subject, predicate, object)) {
            //The graph contains the asserted triple, return the current binding.
            return true;
        }

        //If query re-writing is disabled then exit - already checked that graph does not contain the asserted relation.
        if (!queryRewriteIndex.isIndexActive()) {
            return false;
        }

        //Begin Query Re-write by finding the literals of the Feature or Geometry.
        SpatialObjectGeometryLiteral subjectSpatialLiteral = SpatialObjectGeometryLiteral.retrieve(graph, subject);
        if (!subjectSpatialLiteral.isValid()) {
            //Subject is not a Feature or a Geometry or there is no GeometryLiteral so exit.
            return false;
        }

        SpatialObjectGeometryLiteral objectSpatialLiteral = SpatialObjectGeometryLiteral.retrieve(graph, object);
        if (!objectSpatialLiteral.isValid()) {
            //Object is not a Feature or a Geometry or there is no GeometryLiteral so exit.
            return false;
        }

        //Check the QueryRewriteIndex for the result.
        Boolean isPositive = queryRewriteIndex.test(subjectSpatialLiteral.getGeometryLiteral(), predicate, objectSpatialLiteral.getGeometryLiteral(), this);
        return isPositive;
    }

    public Boolean testFilterFunction(Node subjectGeometryLiteral, Node objectGeometryLiteral) {
        return filterFunction.exec(subjectGeometryLiteral, objectGeometryLiteral);
    }

}
