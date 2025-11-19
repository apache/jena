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

package org.apache.jena.geosparql.implementation.access;

import java.lang.invoke.MethodHandles;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.jena.atlas.iterator.Iter;
import org.apache.jena.datatypes.DatatypeFormatException;
import org.apache.jena.geosparql.implementation.GeometryWrapper;
import org.apache.jena.geosparql.implementation.vocabulary.Geo;
import org.apache.jena.geosparql.implementation.vocabulary.SpatialExtension;
import org.apache.jena.geosparql.spatial.ConvertLatLon;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.system.G;
import org.apache.jena.system.RDFDataException;
import org.apache.jena.util.iterator.ExtendedIterator;
import org.apache.jena.util.iterator.WrappedIterator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Central place for accessing Wgs84 point geometries in a {@link Graph}.
 */
public class AccessWGS84 {
    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    /** True iff the graph contains wgs84:{lat, long} triples.
     *  True does not imply that there are resources that have both lat AND long properties. */
    public static boolean containsGeoLiteralProperties(Graph graph) {
        return containsGeoLiteralProperties(graph, null);
    }

    /** True iff the node has wgs84:{lat, long} triples.
     *  True does not imply that both lat AND long are present on the node. */
    public static boolean hasGeoLiteralProperties(Graph graph, Node feature) {
        Objects.requireNonNull(feature);
        return containsGeoLiteralProperties(graph, feature);
    }

    /** For each matching resource, build triples of format 's geo:hasGeometry geometryLiteral'. */
    // XXX geo:hasSerialization might seem a better choice but the original jena-geosparql implementation used geo:hasGeometry.
    public static ExtendedIterator<Triple> findGeoLiteralsAsTriples(AtomicBoolean cancel, Graph graph, Node s) {
        return findGeoLiteralsAsTriples(cancel, graph, s, Geo.HAS_GEOMETRY_NODE);
    }

    /**
     * For each matching resource and its geometries, create triples of format 's p geometryLiteral'
     *
     * @param graph
     * @param s The match subject. May be null.
     * @param p The predicate to use for creating triples. Can be chosen freely but must not be null.
     * @return Iterator of created triples (not obtained from the graph directly).
     */
    public static ExtendedIterator<Triple> findGeoLiteralsAsTriples(AtomicBoolean cancel, Graph graph, Node s, Node p) {
        return findGeoLiterals(cancel, graph, s).mapWith(e -> Triple.create(e.getKey(), p, e.getValue().asNode()));
    }

    /**
     * For each matching resource, build geometry literals from the cartesian product of the WGS84 lat/long properties.
     * Resources must have both properties, lat and long, to be matched by this method.
     */
    public static ExtendedIterator<Entry<Node, GeometryWrapper>> findGeoLiterals(AtomicBoolean cancel, Graph graph, Node s) {
        // Warn about multiple lat/lon combinations only at most once per graph.
        boolean enableWarnings = false;
        boolean[] loggedMultipleLatLons = { false };
        ExtendedIterator<Triple> latIt = G.find(cancel, graph, s, SpatialExtension.GEO_LAT_NODE, Node.ANY);
        ExtendedIterator<Entry<Node, GeometryWrapper>> result = WrappedIterator.create(Iter.iter(latIt).flatMap(triple -> {
            Node feature = triple.getSubject();
            Node lat = triple.getObject();

            // Create the cross-product between lats and lons.
            ExtendedIterator<Node> lons = G.iterSP(graph, feature, SpatialExtension.GEO_LON_NODE);

            // On malformed data this can cause lots of log output. Perhaps it's better to keep validation separate from indexing.
            int[] lonCounter = {0};
            ExtendedIterator<Entry<Node, GeometryWrapper>> r = lons.mapWith(lon -> {
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
                return Map.entry(feature, geometryWrapper);
            });
            return r;
        }));
        return result;
    }

    /**
     * Read lat/lon values for the given subject. Null if there are no such properties.
     * Throws {@link DatatypeFormatException} when detecting incorrect use of these properties.
     */
    public static Node getGeoLiteral(Graph graph, Node s) {
        Node lat = null;
        try {
            lat = G.getZeroOrOneSP(graph, s, SpatialExtension.GEO_LAT_NODE);
        } catch (RDFDataException ex) {
            throw new DatatypeFormatException(s + " has more than one geo:lat property.");
        }

        Node lon = null;
        try {
            lon = G.getZeroOrOneSP(graph, s, SpatialExtension.GEO_LON_NODE);
        } catch ( RDFDataException ex) {
            throw new DatatypeFormatException(s + " has more than one geo:lon property.");
        }

        // Both null -> return null.
        if (lat == null && lon == null) {
            return null;
        }

        if (lat == null) {
            throw new DatatypeFormatException(s + " has a geo:lon property but is missing geo:lat.");
        }
        if (lon == null) {
            throw new DatatypeFormatException(s + " has a geo:lat property but is missing geo:lon.");
        }
        Node geometryLiteral = ConvertLatLon.toNode(lat, lon);
        return geometryLiteral;
    }

    private static boolean containsGeoLiteralProperties(Graph graph, Node s) {
        boolean result =
            graph.contains(s, SpatialExtension.GEO_LAT_NODE, null) ||
            graph.contains(s, SpatialExtension.GEO_LON_NODE, null);
        return result;
    }
}
