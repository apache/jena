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
package org.apache.jena.geosparql.spatial.property_functions;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import org.apache.commons.collections4.iterators.IteratorChain;
import org.apache.jena.datatypes.DatatypeFormatException;
import org.apache.jena.geosparql.implementation.GeometryWrapper;
import org.apache.jena.geosparql.implementation.SRSInfo;
import org.apache.jena.geosparql.implementation.vocabulary.Geo;
import org.apache.jena.geosparql.implementation.vocabulary.SpatialExtension;
import org.apache.jena.geosparql.spatial.ConvertLatLon;
import org.apache.jena.geosparql.spatial.SearchEnvelope;
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
import org.apache.jena.sparql.engine.iterator.QueryIterConcat;
import org.apache.jena.sparql.engine.iterator.QueryIterNullIterator;
import org.apache.jena.sparql.engine.iterator.QueryIterSingleton;
import org.apache.jena.sparql.expr.ExprEvalException;
import org.apache.jena.sparql.pfunction.PFuncSimpleAndList;
import org.apache.jena.sparql.pfunction.PropFuncArg;
import org.apache.jena.sparql.util.FmtUtils;

/**
 *
 *
 */
public abstract class GenericSpatialPropertyFunction extends PFuncSimpleAndList {

    public static final int DEFAULT_LIMIT = -1;

    private SpatialIndex spatialIndex;
    private SpatialArguments spatialArguments;

    @Override
    public final QueryIterator execEvaluated(Binding binding, Node subject, Node predicate, PropFuncArg object, ExecutionContext execCxt) {
        try {
            spatialIndex = SpatialIndex.retrieve(execCxt);
            spatialArguments = extractObjectArguments(predicate, object, spatialIndex.getSrsInfo());
            return search(binding, execCxt, subject, spatialArguments.limit);
        } catch (SpatialIndexException ex) {
            throw new ExprEvalException(ex.getMessage(), ex);
        }
    }

    /**
     *
     *
     * @param predicate
     * @param object
     * @param indexSRSInfo
     * @return Spatial arguments extracted from the object according to the
     * predicate.
     */
    protected abstract SpatialArguments extractObjectArguments(Node predicate, PropFuncArg object, SRSInfo indexSRSInfo);

    private QueryIterator search(Binding binding, ExecutionContext execCxt, Node subject, int limit) {

        //Subject is bound
        if (subject.isURI() || subject.isBlank()) {
            boolean isMatched = checkBound(execCxt, subject);
            if (isMatched) {
                return QueryIterSingleton.create(binding, execCxt);
            } else {
                return QueryIterNullIterator.create(execCxt);
            }
        }

        if (subject.isVariable()) {
            return checkUnbound(binding, execCxt, subject, limit);
        } else {
            //Subject is not a variable (and not a URI or Blank - tested earlier).
            throw new ExprEvalException("Not a URI, Blank or variable: " + FmtUtils.stringForNode(subject));
        }
    }

    private boolean checkBound(ExecutionContext execCxt, Node subject) {

        try {
            Graph graph = execCxt.getActiveGraph();

            IteratorChain<Triple> spatialTriples = new IteratorChain<>();

            //Check for Geometry and so GeometryLiterals.
            if (graph.contains(subject, Geo.HAS_GEOMETRY_NODE, null)) {
                //A Feature can have many geometries so add each of them. The check Geo.HAS_DEFAULT_GEOMETRY_NODE will only return one but requires the data to have these present.
                Iterator<Triple> geometryTriples = graph.find(subject, Geo.HAS_GEOMETRY_NODE, null);
                while (geometryTriples.hasNext()) {
                    Node geometry = geometryTriples.next().getObject();
                    spatialTriples.addIterator(graph.find(geometry, Geo.HAS_SERIALIZATION_NODE, null));
                }
            } else {
                //Check for Geo predicates against the feature when no geometry literals found.
                if (graph.contains(subject, SpatialExtension.GEO_LAT_NODE, null) && graph.contains(subject, SpatialExtension.GEO_LON_NODE, null)) {
                    Node lat = graph.find(subject, SpatialExtension.GEO_LAT_NODE, null).next().getObject();
                    Node lon = graph.find(subject, SpatialExtension.GEO_LON_NODE, null).next().getObject();
                    Node latLonGeometryLiteral = ConvertLatLon.toNode(lat, lon);
                    Triple triple = new Triple(subject, Geo.HAS_GEOMETRY_NODE, latLonGeometryLiteral);
                    spatialTriples.addIterator(Arrays.asList(triple).iterator());
                }
            }

            //Check through each Geometry and stop if one is accepted.
            boolean isMatched = false;
            while (spatialTriples.hasNext()) {

                Triple triple = spatialTriples.next();
                Node geometryLiteral = triple.getObject();
                GeometryWrapper targetGeometryWrapper = GeometryWrapper.extract(geometryLiteral);
                isMatched = checkSecondFilter(spatialArguments, targetGeometryWrapper);
                if (isMatched) {
                    //Stop checking when match is true.
                    break;
                }
            }

            return isMatched;
        } catch (DatatypeFormatException ex) {
            throw new ExprEvalException(ex.getMessage(), ex);
        }
    }

    /**
     * Closer check of relation of target GeometryWrapper.
     *
     * @param spatialArguments
     * @param targetGeometryWrapper
     * @return Result of second filter.
     */
    protected abstract boolean checkSecondFilter(SpatialArguments spatialArguments, GeometryWrapper targetGeometryWrapper);

    /**
     * Unbound values being retrieved may require a closer check.<br>
     * SpatialIndex uses bounding box to retrieve objects which may be over
     * generous or produce some false positives. Seconds
     *
     * @return Whether a second filter check is required.
     */
    protected abstract boolean requireSecondFilter();

    private QueryIterator checkUnbound(Binding binding, ExecutionContext execCxt, Node subject, int limit) {

        QueryIterConcat queryIterConcat = new QueryIterConcat(execCxt);
        if (limit < 0) {
            limit = Integer.MAX_VALUE;
        }

        //Find all Features in the spatial index which are within the rough search envelope.
        SearchEnvelope searchEnvelope = spatialArguments.searchEnvelope;
        HashSet<Resource> features = searchEnvelope.check(spatialIndex);

        Var subjectVar = Var.alloc(subject.getName());
        int count = 0;
        for (Resource feature : features) {

            boolean isMatched;

            if (requireSecondFilter()) {
                //Check all the GeometryLiterals of the Feature in a fine-grained test.
                isMatched = checkBound(execCxt, feature.asNode());
            } else {
                //Second filter is not required so accept the case.
                isMatched = true;
            }

            if (isMatched) {
                count++; //Exit on limit of zero.
                if (count > limit) {
                    break;
                }
                QueryIterator queryIter = QueryIterSingleton.create(binding, subjectVar, feature.asNode(), execCxt);
                queryIterConcat.add(queryIter);
            }
        }
        return queryIterConcat;
    }

}
