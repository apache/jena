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
package org.apache.jena.geosparql.spatial;

import java.util.Iterator;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.jena.atlas.iterator.Iter;
import org.apache.jena.atlas.iterator.IteratorCloseable;
import org.apache.jena.geosparql.implementation.GeometryWrapper;
import org.apache.jena.geosparql.implementation.access.AccessGeoSPARQL;
import org.apache.jena.geosparql.implementation.access.AccessWGS84;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.sparql.core.DatasetGraph;
import org.locationtech.jts.geom.Envelope;
import org.opengis.geometry.MismatchedDimensionException;
import org.opengis.referencing.operation.TransformException;
import org.opengis.util.FactoryException;

public class SpatialIndexFindUtils {
    /**
     * Find Spatial Index Items from all graphs in Dataset.<br>
     *
     * @param datasetGraph
     * @param srsURI
     * @return SpatialIndexItems found.
     */
    public static IteratorCloseable<SpatialIndexItem> findIndexItems(AtomicBoolean cancel, DatasetGraph datasetGraph, String srsURI) {
        Graph defaultGraph = datasetGraph.getDefaultGraph();
        IteratorCloseable<SpatialIndexItem> itemsIter = findIndexItems(cancel, defaultGraph, srsURI);
        try {
            //Named Models
            Iterator<Node> graphNodeIt = datasetGraph.listGraphNodes();
            Iterator<SpatialIndexItem> namedGraphItemsIt = Iter.iter(graphNodeIt).flatMap(graphNode -> {
                Graph namedGraph = datasetGraph.getGraph(graphNode);
                IteratorCloseable<SpatialIndexItem> graphItems = findIndexItems(cancel, namedGraph, srsURI);
                return graphItems;
            });
            itemsIter = Iter.iter(itemsIter).append(namedGraphItemsIt);
        } catch(Throwable t) {
            Iter.close(itemsIter);
            throw new RuntimeException(t);
        }
        return itemsIter;
    }

    /**
     * Find items from the Model transformed to the SRS URI.
     *
     * @param graph
     * @param srsURI
     * @return Items found in the Model in the SRS URI.
     */
    public static final IteratorCloseable<SpatialIndexItem> findIndexItems(AtomicBoolean cancel, Graph graph, String srsURI) {
        IteratorCloseable<SpatialIndexItem> result;
        // Only add one set of statements as a converted dataset will duplicate the same info.
        if (AccessGeoSPARQL.containsGeoLiterals(graph)) {
            result = findIndexItemsGeoSparql(cancel, graph, srsURI);
        } else if (AccessWGS84.containsGeoLiteralProperties(graph)) {
            result = findIndexItemsWgs84(cancel, graph, srsURI);
        } else {
            result = Iter.empty();
        }
        return result;
    }

    /**
     *
     * @param graph
     * @param srsURI
     * @return SpatialIndexItem items prepared for adding to SpatialIndex.
     */
    public static IteratorCloseable<SpatialIndexItem> findIndexItemsGeoSparql(AtomicBoolean cancel, Graph graph, String srsURI) {
        Iterator<Triple> stmtIter = AccessGeoSPARQL.findSpecificGeoResources(cancel, graph);
        IteratorCloseable<SpatialIndexItem> result = Iter.iter(stmtIter).flatMap(stmt -> {
            Node feature = stmt.getSubject();
            Node geometry = stmt.getObject();
            Iterator<Triple> serializationIter = AccessGeoSPARQL.findSpecificGeoLiterals(cancel, graph, geometry);
            Iterator<SpatialIndexItem> itemIter = Iter.map(serializationIter, triple -> {
                Node geometryNode = triple.getObject();
                GeometryWrapper geometryWrapper = GeometryWrapper.extract(geometryNode);
                SpatialIndexItem item = makeSpatialIndexItem(feature, geometryWrapper, srsURI);
                return item;
            });
            return itemIter;
        });
        return result;
    }

    /**
     *
     *
     * @param graph
     * @param srsURI
     * @return Geo predicate objects prepared for adding to SpatialIndex.
     */
    public static IteratorCloseable<SpatialIndexItem> findIndexItemsWgs84(AtomicBoolean cancel, Graph graph, String srsURI) {
        return Iter.iter(AccessWGS84.findGeoLiterals(cancel, graph, null)).map(e -> {
            Node feature = e.getKey();
            GeometryWrapper geometryWrapper = e.getValue();
            SpatialIndexItem item = makeSpatialIndexItem(feature, geometryWrapper, srsURI);
            return item;
        });
    }

    public static SpatialIndexItem makeSpatialIndexItem(Node feature, GeometryWrapper geometryWrapper, String srsURI) {
        // Ensure all entries in the target SRS URI.
        GeometryWrapper transformedGeometryWrapper = unsafeConvert(geometryWrapper, srsURI);
        Envelope envelope = transformedGeometryWrapper.getEnvelope();
        SpatialIndexItem item = new SpatialIndexItem(envelope, feature);
        return item;
    }

    public static GeometryWrapper unsafeConvert(GeometryWrapper geometryWrapper, String srsURI) {
        GeometryWrapper result;
        try {
            result = geometryWrapper.convertSRS(srsURI);
        } catch (MismatchedDimensionException | FactoryException | TransformException e) {
            throw new RuntimeException(e);
        }
        return result;
    }
}
