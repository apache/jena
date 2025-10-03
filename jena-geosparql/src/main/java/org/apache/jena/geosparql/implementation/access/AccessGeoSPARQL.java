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

import java.util.Iterator;
import java.util.Objects;

import org.apache.jena.atlas.iterator.Iter;
import org.apache.jena.geosparql.implementation.vocabulary.Geo;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.system.G;
import org.apache.jena.util.iterator.ExtendedIterator;

/**
 * Central place for accessing GeoSparql spatial objects in a {@link Graph}.
 *
 * Note: Using the "GeoLiterals" methods on RDF data that do not conform to the GeoSparql
 * specification will return whatever values are present -
 * regardless of whether those values are valid literals.
 */
public class AccessGeoSPARQL {
    public static boolean isPredicateOfFeature(Node n) {
        return n.equals(Geo.HAS_GEOMETRY_NODE) || n.equals(Geo.HAS_DEFAULT_GEOMETRY_NODE);
    }

    public static boolean isPredicateOfGeoResource(Node n) {
        return n.equals(Geo.AS_WKT_NODE) || n.equals(Geo.AS_GML_NODE) || n.equals(Geo.HAS_SERIALIZATION_NODE);
    }

    public static boolean isTripleOfFeature(Triple t) {
        return isPredicateOfFeature(t.getPredicate());
    }

    public static boolean isTripleOfGeoResource(Triple t) {
        return isPredicateOfGeoResource(t.getPredicate());
    }

    /** True iff the graph contains geometry literals. */
    public static boolean containsGeoLiterals(Graph graph) {
        return containsGeoLiterals(graph, null);
    }

    /** True iff the node has geometry literals. Arguments must not be null. */
    public static boolean hasGeoLiterals(Graph graph, Node geometry) {
        Objects.requireNonNull(geometry);
        return containsGeoLiterals(graph, geometry);
    }

    /** True if the node has a geometry or default geometry. Arguments must not be null. */
    public static boolean hasGeoResources(Graph graph, Node feature) {
        Objects.requireNonNull(feature);
        boolean result =
            graph.contains(feature, Geo.HAS_DEFAULT_GEOMETRY_NODE, null) ||
            graph.contains(feature, Geo.HAS_GEOMETRY_NODE, null);
        return result;
    }

    /**
     * True if the node is a geosparql spatial object by the present (geometry-related) properties.
     * A mere "SpatialObject" type does not count.
     * Arguments must not be null. Wgs84 does not count
     */
    public static boolean isSpatialObjectByProperties(Graph graph, Node featureOrGeometry) {
        return hasGeoLiterals(graph, featureOrGeometry) || hasGeoResources(graph, featureOrGeometry);
    }

    /**
     * Find all triples with geo:hasDefaultGeometry and geo:hasGeometry predicates.
     * If a feature has a default geometry, then this method will omit all its (non-default) geometries.
     */
    public static ExtendedIterator<Triple> findSpecificGeoResources(Graph graph) {
        // List resources that have a default geometry followed by those that
        // only have a non-default one.
        ExtendedIterator<Triple> result = graph.find(null, Geo.HAS_DEFAULT_GEOMETRY_NODE, null);
        try {
            boolean hasDefaultGeometry = result.hasNext();
            ExtendedIterator<Triple> it = graph.find(null, Geo.HAS_GEOMETRY_NODE, null);

            // No default geometry -> no need to filter.
            result = hasDefaultGeometry
                ? result.andThen(it.filterDrop(t -> G.hasProperty(graph, t.getSubject(), Geo.HAS_DEFAULT_GEOMETRY_NODE)))
                : result.andThen(it);
        } catch (RuntimeException t) {
            result.close();
            throw new RuntimeException(t);
        }
        return result;
    }

    public static ExtendedIterator<Triple> findDefaultGeoResources(Graph graph) {
        return graph.find(null, Geo.HAS_DEFAULT_GEOMETRY_NODE, null);
    }

    public static ExtendedIterator<Triple> findSpecificGeoResources(Graph graph, Node feature) {
        Objects.requireNonNull(feature);
        ExtendedIterator<Triple> result = graph.find(feature, Geo.HAS_DEFAULT_GEOMETRY_NODE, null);
        try {
            if (!result.hasNext()) {
                result.close();
            }
            result = graph.find(feature, Geo.HAS_GEOMETRY_NODE, null);
        } catch (RuntimeException t) {
            result.close();
            throw new RuntimeException(t);
        }
        return result;
    }

    /**
     * Resolve a feature to its set of specific geometries via the following chain:
     * <pre>
     *   feature -&gt; (geo:hasDefaultGeometry, geo:hasGeometry) -&gt;
     *     ({geo:asWKT, geo:asGML}, geo:hasSerialization) -&gt; geo-literal.
     * </pre>
     *
     * If a geo:hasDefaultGeometry does not lead to a valid geo-literal there is no backtracking to geo:hasGeometry.
     */
    public static Iterator<Triple> findSpecificGeoLiteralsByFeature(Graph graph, Node feature) {
        return Iter.flatMap(findSpecificGeoResources(graph, feature),
            t -> findSpecificGeoLiterals(graph, t.getObject()));
    }

    /**
     * Iterate all triples of geometry resources with their most specific serialization form.
     * The specific properties geo:asWKT and geo:asGML take precedence over the more general geo:hasSerialization.
     * This means if a resource has wkt and/or gml then all geo:hasSerialization triples will be omitted for it.
     */
    public static ExtendedIterator<Triple> findSpecificGeoLiterals(Graph graph) {
        ExtendedIterator<Triple> result = graph.find(null, Geo.AS_WKT_NODE, null);
        try {
            result = result.andThen(graph.find(null, Geo.AS_GML_NODE, null));
            // If there is no specific serialization property use the general one.
            if (!result.hasNext()) {
                result.close();
                result = graph.find(null, Geo.HAS_SERIALIZATION_NODE, null);
            } else {
                // Append more general serializations for those resources that lack a specific one.
                ExtendedIterator<Triple> it = graph.find(null, Geo.HAS_SERIALIZATION_NODE, null).filterDrop(t ->
                    G.hasProperty(graph, t.getSubject(), Geo.AS_WKT_NODE) ||
                    G.hasProperty(graph, t.getSubject(), Geo.AS_GML_NODE));
                result = result.andThen(it);
            }
        } catch (RuntimeException t) {
            result.close();
            throw new RuntimeException(t);
        }
        return result;
    }

    /**
     * Iterate a given geometry resource's most specific geometry literals.
     * The geometry resource must not be null.
     * A specific serialization (WKT, GML) takes precedence over the more general hasSerialization property.
     */
    public static ExtendedIterator<Triple> findSpecificGeoLiterals(Graph graph, Node geometry) {
        Objects.requireNonNull(geometry);
        ExtendedIterator<Triple> result = graph.find(geometry, Geo.AS_WKT_NODE, null);
        try {
            result = result.andThen(graph.find(geometry, Geo.AS_GML_NODE, null));
            if (!result.hasNext()) {
                result.close();
                // Fallback to the more generic property.
                result = graph.find(geometry, Geo.HAS_SERIALIZATION_NODE, null);
            }
        } catch (RuntimeException t) {
            result.close();
            throw new RuntimeException(t);
        }
        return result;
    }

    public static Node getGeoLiteral(Graph graph, Node geometry) {
        Triple t = getGeoLiteralTriple(graph, geometry);
        Node n = (t == null) ? null : t.getObject();
        return n;
    }

    public static Triple getGeoLiteralTriple(Graph graph, Node geometry) {
        Objects.requireNonNull(geometry);

        // Find the geometry literal of the geometry resource.
        Triple t;
        if ((t = getTripleSP(graph, geometry, Geo.HAS_SERIALIZATION_NODE)) != null) {
            return t;
        }

        // If hasSerialization not found then check asWKT.
        if ((t = getTripleSP(graph, geometry, Geo.AS_WKT_NODE)) != null) {
            return t;
        }

        // If asWKT not found then check asGML.
        if ((t = getTripleSP(graph, geometry, Geo.AS_GML_NODE)) != null) {
            return t;
        }

        return null;
    }

    private static Triple getTripleSP(Graph graph, Node s, Node p) {
        Node o = G.getSP(graph, s, p);
        Triple t = (o == null) ? null : Triple.create(s, p, o);
        return t;
    }

    /** Shared code to test whether a node or graph has serialization properties. */
    private static boolean containsGeoLiterals(Graph graph, Node node) {
        boolean result =
            graph.contains(node, Geo.HAS_SERIALIZATION_NODE, null) ||
            graph.contains(node, Geo.AS_WKT_NODE, null) ||
            graph.contains(node, Geo.AS_GML_NODE, null);
        return result;
    }
}
