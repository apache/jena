/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.jena.geosparql.spatial.index.v2;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;

import org.apache.jena.geosparql.implementation.GeometryWrapper;
import org.apache.jena.geosparql.implementation.datatype.WKTDatatype;
import org.apache.jena.geosparql.implementation.jts.CustomGeometryFactory;
import org.apache.jena.geosparql.implementation.vocabulary.Geo;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.Polygon;

/**
 * Class to generate various geometry types for testing. The generated coordinate sequences are based on circles.
 * This class is also used in jena-benchmarks.
 */
public class GeometryGenerator {

    public enum GeometryType {
        POINT,
        LINESTRING,
        LINEARRING,
        POLYGON,
        MULTIPOINT,
        MULTILINESTRING,
        MULTIPOLYGON,
        GEOMETRYCOLLECTION;
    }

    public static Coordinate[] createCircle(GeometryFactory geometryFactory, Coordinate center, double radius, int numPoints, boolean closed) {
        Coordinate[] coords = new Coordinate[numPoints + (closed ? 1 : 0)]; // +1 to close the circle
        for (int i = 0; i < numPoints; i++) {
            double angle = 2 * Math.PI * i / numPoints;
            double x = center.x + radius * Math.cos(angle);
            double y = center.y + radius * Math.sin(angle);
            coords[i] = new Coordinate(x, y);
        }
        if (closed) {
            coords[numPoints] = coords[0];
        }
        return coords;
    }

    public static Geometry createGeom(GeometryType type, GeometryFactory f, Coordinate center, double radius, int numPoints) {
        Geometry result;
        switch(type) {
        case POINT:
            // Only return the point - (a circle of radius 0)
            result = f.createPoint(center); break;
        default:
            // Non-multi geometries.
            Coordinate[] c1 = createCircle(f,center, radius, numPoints, true);

            switch(type) {
            case MULTIPOINT: result = f.createMultiPointFromCoords(c1); break;
            case LINESTRING: result = f.createLineString(c1); break;
            case LINEARRING: result = f.createLinearRing(c1); break;
            case POLYGON: result = f.createPolygon(c1); break;
            default:
                // Multi geometries.
                Coordinate[] c2 = createCircle(f,center, radius * 0.5, (int)Math.max(3, numPoints * 0.8), true);

                switch(type) {
                case MULTILINESTRING: result = f.createMultiLineString(new LineString[]{f.createLineString(c1), f.createLineString(c2)}); break;
                case MULTIPOLYGON: result = f.createMultiPolygon(new Polygon[]{f.createPolygon(c1), f.createPolygon(c2)}); break;
                case GEOMETRYCOLLECTION: result = f.createGeometryCollection(new Geometry[] {f.createPoint(center), f.createLinearRing(c2)}); break;
                default:
                    throw new RuntimeException("Unsupported geometry type: " + type);
                }
            }
        }
        return result;
    }

    /**
     * Create a map that maps each geometry type to the specified amount.
     * For use with {@link #generateGraph(Graph, Envelope, Map)}.
     */
    public static Map<GeometryType, Number> createConfig(long amount) {
        // How many geometries to generate of each type.
        // Note that the index only stores their envelopes.
        Map<GeometryType, Number> config = new LinkedHashMap<>();
        config.put(GeometryType.POINT, amount);
        config.put(GeometryType.LINESTRING, amount);
        config.put(GeometryType.LINEARRING, amount);
        config.put(GeometryType.POLYGON, amount);
        config.put(GeometryType.MULTIPOINT, amount);
        config.put(GeometryType.MULTILINESTRING, amount);
        config.put(GeometryType.MULTIPOLYGON, amount);
        config.put(GeometryType.GEOMETRYCOLLECTION, amount);
        return config;
    }

    /**
     * Generate GeoSPARQL data with various geometry types in the given graph.
     *
     * @param graph The target graph.
     * @param envelope The allowed area for the positions of generated geometries (the shape may overlap).
     * @param config Map of geometry type to the number of geometries to generate.
     *
     * @return The number of generated geometries added to the graph.
     */
    public static long generateGraph(Graph graph, Envelope envelope, Map<GeometryType, Number> config) {
        GeometryFactory geometryFactory = CustomGeometryFactory.theInstance();

        float maxRadius = 1;
        int minNumPoints = 3;
        int maxNumPoints = 30;

        Random rand = new Random(0);

        float minX = (float)envelope.getMinX();
        float maxX = (float)envelope.getMaxX();
        float minY = (float)envelope.getMinY();
        float maxY = (float)envelope.getMaxY();

        float dX = maxX - minX;
        float dY = maxY - minY;
        int dNumPoints = maxNumPoints - minNumPoints;

        long generatedItemCount = 0;
        long nextFeatureId = 0;
        for (Entry<GeometryType, Number> e : config.entrySet()) {
            GeometryType geometryType = e.getKey();
            long count = e.getValue().longValue();
            for (long i = 0; i < count; ++i) {

                float x = minX + rand.nextFloat(dX);
                float y = minY + rand.nextFloat(dY);
                int numPoints = minNumPoints + (int)(rand.nextFloat(dNumPoints));

                Coordinate c = new Coordinate(x, y);
                Geometry g = GeometryGenerator.createGeom(geometryType, geometryFactory, c, maxRadius, numPoints);

                long featureId = nextFeatureId++;
                Node feature = NodeFactory.createURI("http://www.example.org/feature" + featureId);
                Node geometry = NodeFactory.createURI("http://www.example.org/geometry" + featureId);
                Node geom = new GeometryWrapper(g, WKTDatatype.URI).asNode();

                graph.add(feature, Geo.HAS_GEOMETRY_NODE, geometry);
                graph.add(geometry, Geo.AS_WKT_NODE, geom);

                ++generatedItemCount;
            }
        }
        return generatedItemCount;
    }
}
