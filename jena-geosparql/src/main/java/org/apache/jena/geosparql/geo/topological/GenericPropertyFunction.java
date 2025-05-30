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

import java.util.Collection;
import java.util.List;

import org.apache.jena.atlas.iterator.Iter;
import org.apache.jena.geosparql.geof.topological.GenericFilterFunction;
import org.apache.jena.geosparql.implementation.GeometryWrapper;
import org.apache.jena.geosparql.implementation.index.QueryRewriteIndex;
import org.apache.jena.geosparql.implementation.vocabulary.Geo;
import org.apache.jena.geosparql.implementation.vocabulary.SpatialExtension;
import org.apache.jena.geosparql.spatial.SpatialIndex;
import org.apache.jena.geosparql.spatial.SpatialIndexException;
import org.apache.jena.geosparql.spatial.index.v2.SpatialIndexLib;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.ExecutionContext;
import org.apache.jena.sparql.engine.QueryIterator;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.engine.binding.BindingFactory;
import org.apache.jena.sparql.engine.iterator.QueryIter;
import org.apache.jena.sparql.engine.iterator.QueryIterConcat;
import org.apache.jena.sparql.engine.iterator.QueryIterNullIterator;
import org.apache.jena.sparql.engine.iterator.QueryIterPlainWrapper;
import org.apache.jena.sparql.engine.iterator.QueryIterSingleton;
import org.apache.jena.sparql.expr.ExprEvalException;
import org.apache.jena.sparql.pfunction.PFuncSimple;
import org.apache.jena.sparql.util.FmtUtils;
import org.apache.jena.system.G;
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

    private QueryIterator bothBound(Binding binding, boolean isSubjectBound, Node subject, Node predicate, Node object, ExecutionContext execCxt) {
        QueryIterator iter = isSubjectBound
                ? bothBound(binding, subject, predicate, object, execCxt)
                : bothBound(binding, object, predicate, subject, execCxt);
        return iter;
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
        Var subjectVar = Var.alloc(subject.getName());

        Graph graph = execCxt.getActiveGraph();

        //Search for both Features and Geometry in the Graph. Reliant upon consistent usage of SpatialObject (which is base class of Feature and Geometry) if present.
        ExtendedIterator<Triple> spatialTriples = findSpatialTriples(graph);
        ExtendedIterator<Binding> iterator = spatialTriples
            .mapWith(Triple::getSubject)
            .mapWith(node -> BindingFactory.binding(binding, subjectVar, node));

        QueryIter queryIter = QueryIter.flatMap(
            QueryIterPlainWrapper.create(iterator, execCxt),
            b -> oneBound(b, b.get(subjectVar), predicate, object, execCxt),
            execCxt
        );
        return queryIter;
    }

    private QueryIterator oneBound(Binding binding, Node subject, Node predicate, Node object, ExecutionContext execCxt) {

        Graph graph = execCxt.getActiveGraph();
        Node boundNode;
        Node unboundNode;
        boolean isSubjectBound;
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

        if (!(boundNode.isLiteral() ||
                graph.contains(boundNode, RDF.type.asNode(), Geo.SPATIAL_OBJECT_NODE) ||
                graph.contains(boundNode, RDF.type.asNode(), Geo.FEATURE_NODE) ||
                graph.contains(boundNode, RDF.type.asNode(), Geo.GEOMETRY_NODE))) {
            if (!graph.contains(boundNode, SpatialExtension.GEO_LAT_NODE, null)) {
                //Bound node is not a Feature or a Geometry or has Geo predicates so exit.
                return QueryIterNullIterator.create(execCxt);
            }
        }

        boolean isSpatialIndex = SpatialIndexLib.isDefined(execCxt);
        QueryIterator result;
        if (!isSpatialIndex || filterFunction.isDisjoint() || filterFunction.isDisconnected()) {
            //Disjointed so retrieve all cases.
            result = findAll(graph, boundNode, unboundNode, binding, isSubjectBound, predicate, execCxt);
        } else {
            //Only retrieve those in the spatial index which are within same bounding box.
            result = findIndex(graph, boundNode, unboundNode, binding, isSubjectBound, predicate, execCxt);
        }
        return result;
    }

    private QueryIterator findAll(Graph graph, Node boundNode, Node unboundNode, Binding binding, boolean isSubjectBound, Node predicate, ExecutionContext execCxt) {

        //Prepare the results.
        Var unboundVar = Var.alloc(unboundNode.getName());

        //Search for both Features and Geometry in the Graph. Reliant upon consistent usage of SpatialObject (which is base class of Feature and Geometry) if present.
        ExtendedIterator<Triple> spatialTriples = findSpatialTriples(graph);

        ExtendedIterator<Binding> iterator = spatialTriples
            .mapWith(Triple::getSubject)
            .mapWith(node -> BindingFactory.binding(binding, unboundVar, node));

        return QueryIter.flatMap(
            QueryIterPlainWrapper.create(iterator, execCxt),
            b -> {
                Node spatialNode = b.get(unboundVar);
                QueryIterator iter = bothBound(b, isSubjectBound, boundNode, predicate, spatialNode, execCxt);
                return iter;
            },
            execCxt);
    }

    private static ExtendedIterator<Triple> findSpatialTriples(Graph graph) {
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
        return spatialTriples;
    }

    private QueryIterator findIndex(Graph graph, Node boundNode, Node unboundNode, Binding binding, boolean isSubjectBound, Node predicate, ExecutionContext execCxt) throws ExprEvalException {
        try {
            //Prepare for results.
            Var unboundVar = Var.alloc(unboundNode);

            //Find the asserted triples.
            Collection<Node> assertedNodes = !isSubjectBound || !boundNode.isLiteral()
                ? findAsserted(graph, boundNode, isSubjectBound, predicate)
                : List.of();

            QueryIterator assertedNodesIter = QueryIterPlainWrapper.create(
                Iter.map(assertedNodes.iterator(), node -> BindingFactory.binding(binding, unboundVar, node)),
                execCxt);

            //Find the GeometryLiteral of the Bound Node.
            SpatialObjectGeometryLiteral boundGeometryLiteral = SpatialObjectGeometryLiteral.retrieve(graph, boundNode);
            if (!boundGeometryLiteral.isValid()) {
                //Bound Node is not a Feature or a Geometry or there is no GeometryLiteral so exit.
                return assertedNodesIter;
            }

            QueryIterConcat queryIterConcat = new QueryIterConcat(execCxt);
            queryIterConcat.add(assertedNodesIter);

            Node geometryLiteral = boundGeometryLiteral.getGeometryLiteral();

            // Perform the search of the Spatial Index of the Dataset.
            SpatialIndex spatialIndex = SpatialIndexLib.retrieve(execCxt);
            GeometryWrapper geom = GeometryWrapper.extract(geometryLiteral);
            GeometryWrapper transformedGeom = geom.transform(spatialIndex.getSrsInfo());

            Envelope searchEnvelope = transformedGeom.getEnvelope();
            Node graphName = SpatialIndexLib.unwrapGraphName(graph);
            Collection<Node> features = spatialIndex.query(searchEnvelope, graphName);

            // Check each of the Features that match the search.
            QueryIterator featuresIter = QueryIterPlainWrapper.create(
                    Iter.map(features.iterator(), feature -> BindingFactory.binding(binding, unboundVar, feature)),
                    execCxt);

            QueryIterator queryIterator = QueryIter.flatMap(featuresIter,
                featureBinding -> {
                    return findByFeature(graph, binding, featureBinding,
                            isSubjectBound, boundNode, predicate, unboundVar,
                            execCxt, assertedNodes);
                },
                execCxt);
            queryIterConcat.add(queryIterator);

            return queryIterConcat;
        } catch (MismatchedDimensionException | TransformException | FactoryException | SpatialIndexException ex) {
            throw new ExprEvalException(ex.getMessage() + ": " + FmtUtils.stringForNode(boundNode) + ", " + FmtUtils.stringForNode(unboundNode) + ", " + FmtUtils.stringForNode(predicate), ex);
        }
    }

    private QueryIterator findByFeature(Graph graph, Binding binding, Binding featureBinding,
            boolean isSubjectBound, Node boundNode, Node predicate, Var unboundVar,
            ExecutionContext execCxt, Collection<Node> assertedNodes) {

        Node featureNode = featureBinding.get(unboundVar);
        QueryIterConcat featureIterConcat = new QueryIterConcat(execCxt);

        // Check Features directly if not already asserted
        if (!assertedNodes.contains(featureNode)) {
            QueryIterator tmpIter = bothBound(featureBinding, isSubjectBound, boundNode, predicate, featureNode, execCxt);
            featureIterConcat.add(tmpIter);
        }

        // Also test all Geometry of the Features. All, some or one Geometry may have matched.
        ExtendedIterator<Node> featureGeometries = G.iterSP(graph, featureNode, Geo.HAS_GEOMETRY_NODE);
        QueryIterator geometriesQueryIterator = QueryIterPlainWrapper.create(
            Iter.map(
                Iter.filter( // omit asserted
                    featureGeometries,
                    geometry -> !assertedNodes.contains(geometry)
                ),
                geometryNode -> BindingFactory.binding(binding, unboundVar, geometryNode)),
            execCxt);

        geometriesQueryIterator = QueryIter.flatMap(
            geometriesQueryIterator,
            b2 -> {
                Node geomNode = b2.get(unboundVar);
                return bothBound(b2, isSubjectBound, boundNode, predicate, geomNode, execCxt);
            },
            execCxt);

        featureIterConcat.add(geometriesQueryIterator);
        return featureIterConcat;
    }

    private List<Node> findAsserted(Graph graph, Node boundNode, boolean isSubjectBound, Node predicate) {
        List<Node> assertedNodes = isSubjectBound
            ? G.listSP(graph, boundNode, predicate)
            : G.listPO(graph, predicate, boundNode);
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

