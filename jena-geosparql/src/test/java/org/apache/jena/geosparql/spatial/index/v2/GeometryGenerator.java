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

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.Polygon;

/** Class to generate various geometry types for testing. The generated coordinate sequences are based on circles. */
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
}
