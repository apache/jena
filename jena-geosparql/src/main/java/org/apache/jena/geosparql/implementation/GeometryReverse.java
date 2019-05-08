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
package org.apache.jena.geosparql.implementation;

import org.apache.jena.geosparql.implementation.registry.SRSRegistry;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryCollection;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.LinearRing;
import org.locationtech.jts.geom.Polygon;

/**
 *
 *
 */
public class GeometryReverse {

    /**
     * Checks the spatial reference system URI for y,x and reverses the supplied
     * geometry coordinates.
     *
     * @param geometry
     * @param srsURI
     * @return Geometry in x,y coordinate order.
     */
    public static final Geometry check(Geometry geometry, String srsURI) {

        Boolean isAxisXY = SRSRegistry.getAxisXY(srsURI);
        return check(geometry, isAxisXY);
    }

    /**
     * Checks the spatial reference system URI for y,x and reverses the supplied
     * geometry coordinates.
     *
     * @param geometry
     * @param srsInfo
     * @return Geometry in x,y coordinate order.
     */
    public static final Geometry check(Geometry geometry, SRSInfo srsInfo) {
        return check(geometry, srsInfo.isAxisXY());
    }

    /**
     * Checks the spatial reference system URI for y,x and reverses the supplied
     * geometry coordinates.
     *
     * @param geometry
     * @param isAxisXY
     * @return Geometry in x,y coordinate order.
     */
    public static final Geometry check(Geometry geometry, Boolean isAxisXY) {

        Geometry finalGeometry;
        if (isAxisXY) {
            finalGeometry = geometry;
        } else {
            finalGeometry = reverseGeometry(geometry);
        }
        return finalGeometry;
    }

    /**
     * Reverses coordinate order of the supplied geometry and produces a new
     * geometry.
     *
     * @param geometry
     * @return Geometry in x,y coordinate order.
     */
    public static Geometry reverseGeometry(Geometry geometry) {

        if (geometry.isEmpty()) {
            return geometry.copy();
        }

        GeometryFactory factory = geometry.getFactory();
        Geometry finalGeometry;
        Coordinate[] coordinates;

        String type = geometry.getGeometryType();

        switch (type) {
            case "LineString":
                coordinates = getReversedCoordinates(geometry);
                finalGeometry = factory.createLineString(coordinates);
                break;
            case "LinearRing":
                coordinates = getReversedCoordinates(geometry);
                finalGeometry = factory.createLinearRing(coordinates);
                break;
            case "MultiPoint":
                coordinates = getReversedCoordinates(geometry);
                finalGeometry = factory.createMultiPointFromCoords(coordinates);
                break;
            case "Polygon":
                finalGeometry = reversePolygon(geometry, factory);
                break;
            case "Point":
                coordinates = getReversedCoordinates(geometry);
                finalGeometry = factory.createPoint(coordinates[0]);
                break;
            case "MultiPolygon":
                Polygon[] polygons = unpackPolygons((GeometryCollection) geometry);
                finalGeometry = factory.createMultiPolygon(polygons);
                break;
            case "MultiLineString":
                LineString[] lineString = unpackLineStrings((GeometryCollection) geometry);
                finalGeometry = factory.createMultiLineString(lineString);
                break;
            case "GeometryCollection":
                Geometry[] geometries = unpackGeometryCollection((GeometryCollection) geometry);
                finalGeometry = factory.createGeometryCollection(geometries);
                break;
            default:
                finalGeometry = geometry;
                break;
        }

        return finalGeometry;
    }

    private static Coordinate[] getReversedCoordinates(Geometry geometry) {

        Coordinate[] original = geometry.getCoordinates();
        Coordinate[] reversed = new Coordinate[original.length];

        for (int i = 0; i < original.length; i++) {
            reversed[i] = new Coordinate(original[i].y, original[i].x);
        }

        return reversed;

    }

    private static Polygon reversePolygon(Geometry geometry, GeometryFactory factory) {

        Polygon finalGeometry;
        Polygon polygon = (Polygon) geometry;
        if (polygon.getNumInteriorRing() == 0) {
            //There are no interior rings so perform the standard reversal.
            Coordinate[] coordinates = getReversedCoordinates(geometry);
            finalGeometry = factory.createPolygon(coordinates);
        } else {

            LineString exteriorRing = polygon.getExteriorRing();
            Coordinate[] reversedExteriorCoordinates = getReversedCoordinates(exteriorRing);
            LinearRing reversedExteriorRing = factory.createLinearRing(reversedExteriorCoordinates);

            LinearRing[] reversedInteriorRings = new LinearRing[polygon.getNumInteriorRing()];
            for (int i = 0; i < polygon.getNumInteriorRing(); i++) {
                LineString interiorRing = polygon.getInteriorRingN(i);
                Coordinate[] reversedInteriorCoordinates = getReversedCoordinates(interiorRing);
                LinearRing reversedInteriorRing = factory.createLinearRing(reversedInteriorCoordinates);
                reversedInteriorRings[i] = reversedInteriorRing;
            }

            finalGeometry = factory.createPolygon(reversedExteriorRing, reversedInteriorRings);
        }

        return finalGeometry;
    }

    private static Polygon[] unpackPolygons(GeometryCollection geoCollection) {

        GeometryFactory factory = geoCollection.getFactory();

        int count = geoCollection.getNumGeometries();
        Polygon[] polygons = new Polygon[count];

        for (int i = 0; i < count; i++) {
            Geometry geometry = geoCollection.getGeometryN(i);
            Polygon polygon = reversePolygon(geometry, factory);
            polygons[i] = polygon;
        }

        return polygons;
    }

    private static LineString[] unpackLineStrings(GeometryCollection geoCollection) {

        GeometryFactory factory = geoCollection.getFactory();

        int count = geoCollection.getNumGeometries();
        LineString[] lineStrings = new LineString[count];

        for (int i = 0; i < count; i++) {
            Geometry geometry = geoCollection.getGeometryN(i);
            Coordinate[] coordinates = getReversedCoordinates(geometry);
            LineString lineString = factory.createLineString(coordinates);
            lineStrings[i] = lineString;
        }

        return lineStrings;
    }

    private static Geometry[] unpackGeometryCollection(GeometryCollection geoCollection) {

        int count = geoCollection.getNumGeometries();
        Geometry[] geometries = new Geometry[count];

        for (int i = 0; i < count; i++) {
            Geometry geometry = geoCollection.getGeometryN(i);
            geometries[i] = reverseGeometry(geometry);
        }

        return geometries;
    }

}
