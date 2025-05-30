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

import java.lang.invoke.MethodHandles;
import java.util.Iterator;

import org.apache.jena.atlas.iterator.Iter;
import org.apache.jena.atlas.iterator.IteratorCloseable;
import org.apache.jena.geosparql.implementation.GeometryWrapper;
import org.apache.jena.geosparql.implementation.vocabulary.Geo;
import org.apache.jena.geosparql.implementation.vocabulary.SpatialExtension;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.system.G;
import org.locationtech.jts.geom.Envelope;
import org.opengis.geometry.MismatchedDimensionException;
import org.opengis.referencing.operation.TransformException;
import org.opengis.util.FactoryException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SpatialIndexFindUtils {
    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    /**
     * Find Spatial Index Items from all graphs in Dataset.<br>
     *
     * @param datasetGraph
     * @param srsURI
     * @return SpatialIndexItems found.
     */
    public static IteratorCloseable<SpatialIndexItem> findSpatialIndexItems(DatasetGraph datasetGraph, String srsURI) {
        Graph defaultGraph = datasetGraph.getDefaultGraph();
        IteratorCloseable<SpatialIndexItem> itemsIter = findSpatialIndexItems(defaultGraph, srsURI);
        try {
            //Named Models
            Iterator<Node> graphNodeIt = datasetGraph.listGraphNodes();
            Iterator<SpatialIndexItem> namedGraphItemsIt = Iter.iter(graphNodeIt).flatMap(graphNode -> {
                Graph namedGraph = datasetGraph.getGraph(graphNode);
                IteratorCloseable<SpatialIndexItem> graphItems = findSpatialIndexItems(namedGraph, srsURI);
                return graphItems;
            });
            itemsIter = Iter.iter(itemsIter).append(namedGraphItemsIt);
        } catch(Throwable t) {
            t.addSuppressed(new RuntimeException("Failure during findSpatialIndexItems.", t));
            Iter.close(itemsIter);
            throw t;
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
    public static final IteratorCloseable<SpatialIndexItem> findSpatialIndexItems(Graph graph, String srsURI) {
        IteratorCloseable<SpatialIndexItem> result;
        // Only add one set of statements as a converted dataset will duplicate the same info.
        if (graph.contains(null, Geo.HAS_GEOMETRY_NODE, null)) {
            // LOGGER.info("Feature-hasGeometry-Geometry statements found.");
            // if (graph.contains(null, SpatialExtension.GEO_LAT_NODE, null)) {
            //     LOGGER.warn("Lat/Lon Geo predicates also found but will not be added to index.");
            // }
            result = findGeometryIndexItems(graph, srsURI);
        } else if (graph.contains(null, SpatialExtension.GEO_LAT_NODE, null)) {
            // LOGGER.info("Geo predicate statements found.");
            result = findGeoPredicateIndexItems(graph, srsURI);
        } else {
            result = Iter.empty();
        }
        return result;
    }

    /** Print out log messages for what type of spatial data is found in the given graph. */
    public static final void checkSpatialIndexItems(Graph graph) {
        // Only add one set of statements as a converted dataset will duplicate the same info.
        if (graph.contains(null, Geo.HAS_GEOMETRY_NODE, null)) {
            LOGGER.info("Feature-hasGeometry-Geometry statements found.");
            if (graph.contains(null, SpatialExtension.GEO_LAT_NODE, null)) {
                LOGGER.warn("Lat/Lon Geo predicates also found but will not be added to index.");
            }
        } else if (graph.contains(null, SpatialExtension.GEO_LAT_NODE, null)) {
            LOGGER.info("Geo predicate statements found.");
        }
    }

    /**
     *
     * @param graph
     * @param srsURI
     * @return SpatialIndexItem items prepared for adding to SpatialIndex.
     */
    public static IteratorCloseable<SpatialIndexItem> findGeometryIndexItems(Graph graph, String srsURI) {
        Iterator<Triple> stmtIter = graph.find(null, Geo.HAS_GEOMETRY_NODE, null);
        IteratorCloseable<SpatialIndexItem> result = Iter.iter(stmtIter).flatMap(stmt -> {
            Node feature = stmt.getSubject();
            Node geometry = stmt.getObject();

            Iterator<Node> nodeIter = G.iterSP(graph, geometry, Geo.HAS_SERIALIZATION_NODE);

            // XXX If there is a super-property then the concrete serializations are not tried.
            try {
                if (!nodeIter.hasNext()) {
                    Iter.close(nodeIter);

                    Iterator<Node> wktNodeIter = G.iterSP(graph, geometry, Geo.AS_WKT_NODE);
                    nodeIter = wktNodeIter;

                    Iterator<Node> gmlNodeIter = G.iterSP(graph, geometry, Geo.AS_GML_NODE);
                    nodeIter = Iter.append(wktNodeIter, gmlNodeIter);
                }
            } catch (Throwable t) {
                t.addSuppressed(new RuntimeException("Error encountered.", t));
                Iter.close(nodeIter);
                throw t;
            }

            Iterator<SpatialIndexItem> itemIter = Iter.map(nodeIter, geometryNode -> {
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
     * @param graph
     * @param srsURI
     * @return Geo predicate objects prepared for adding to SpatialIndex.
     */
    public static IteratorCloseable<SpatialIndexItem> findGeoPredicateIndexItems(Graph graph, String srsURI) {
        // Warn about multiple lat/lon combinations only at most once per graph.
        boolean enableWarnings = false;
        boolean[] loggedMultipleLatLons = { false };
        Iterator<Triple> latIt = graph.find(Node.ANY, SpatialExtension.GEO_LAT_NODE, Node.ANY);
        IteratorCloseable<SpatialIndexItem> result = Iter.iter(latIt).flatMap(triple -> {
            Node feature = triple.getSubject();
            Node lat = triple.getObject();

            // Create the cross-product between lats and lons.
            Iterator<Node> lons = G.iterSP(graph, feature, SpatialExtension.GEO_LON_NODE);

            // On malformed data this can cause lots of log output. Perhaps it's better to keep validation separate from indexing.
            int[] lonCounter = {0};
            Iterator<SpatialIndexItem> r = Iter.iter(lons).map(lon -> {
                if (enableWarnings) {
                    if (lonCounter[0] == 1) {
                        if (!loggedMultipleLatLons[0]) {
                            LOGGER.warn("Geo predicates: multiple longitudes detected on feature " + feature + ". Further warnings will be omitted.");
                            loggedMultipleLatLons[0] = true;
                        }
                    }
                    ++lonCounter[0];
                }
                GeometryWrapper geometryWrapper = ConvertLatLon.toGeometryWrapper(lat, lon);
                SpatialIndexItem item = makeSpatialIndexItem(feature, geometryWrapper, srsURI);
                return item;
            });
            return r;
        });
        return result;
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
