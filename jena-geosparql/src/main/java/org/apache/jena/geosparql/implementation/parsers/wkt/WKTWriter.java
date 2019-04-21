/*
 * Copyright 2018 the original author or authors.
 * See the notice.md file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.jena.geosparql.implementation.parsers.wkt;

import org.apache.jena.geosparql.implementation.GeometryWrapper;
import org.apache.jena.geosparql.implementation.SRSInfo;
import org.apache.jena.geosparql.implementation.jts.CoordinateSequenceDimensions;
import org.apache.jena.geosparql.implementation.jts.CustomCoordinateSequence;
import org.locationtech.jts.geom.CoordinateSequence;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryCollection;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.MultiLineString;
import org.locationtech.jts.geom.MultiPoint;
import org.locationtech.jts.geom.MultiPolygon;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;

/**
 *
 *
 */
public class WKTWriter {

    public static final String write(GeometryWrapper geometryWrapper) {

        StringBuilder sb = new StringBuilder();

        SRSInfo srsInfo = geometryWrapper.getSrsInfo();

        if (!srsInfo.isWktDefault()) {
            sb.append("<").append(geometryWrapper.getSrsURI()).append("> ");
        }

        Geometry geometry = geometryWrapper.getParsingGeometry();
        CoordinateSequenceDimensions dimensions = geometryWrapper.getCoordinateSequenceDimensions();
        String wktText = expand(geometry, dimensions);

        sb.append(wktText);

        return sb.toString();
    }

    private static String expand(final Geometry geometry, final CoordinateSequenceDimensions dimensions) {

        String wktString = "";
        String dimensionString = CoordinateSequenceDimensions.convertDimensions(dimensions);
        switch (geometry.getGeometryType()) {
            case "Point":
                Point point = (Point) geometry;
                wktString = buildWKT("POINT", point.getCoordinateSequence(), dimensionString);
                break;
            case "LineString":
            case "LinearRing":
                LineString lineString = (LineString) geometry;
                wktString = buildWKT("LINESTRING", lineString.getCoordinateSequence(), dimensionString);
                break;
            case "Polygon":
                Polygon polygon = (Polygon) geometry;
                wktString = buildPolygon(polygon, true, dimensionString);
                break;
            case "MultiPoint":
                MultiPoint multiPoint = (MultiPoint) geometry;
                wktString = buildMultiPoint(multiPoint, dimensionString);
                break;
            case "MultiLineString":
                MultiLineString multiLineString = (MultiLineString) geometry;
                wktString = buildMultiLineString(multiLineString, dimensionString);
                break;
            case "MultiPolygon":
                MultiPolygon multiPolygon = (MultiPolygon) geometry;
                wktString = buildMultiPolygon(multiPolygon, dimensionString);
                break;
            case "GeometryCollection":
                GeometryCollection geometryCollection = (GeometryCollection) geometry;
                wktString = buildGeometryCollection(geometryCollection, dimensions);
                break;
        }

        return wktString;
    }

    private static String convertToWKTText(CustomCoordinateSequence coordSequence) {

        StringBuilder sb = new StringBuilder();
        int size = coordSequence.getSize();
        if (size != 0) {
            sb.append("(");
            String coordText = coordSequence.getCoordinateText(0);
            sb.append(coordText);

            for (int i = 1; i < size; i++) {

                sb.append(", ");
                coordText = coordSequence.getCoordinateText(i);
                sb.append(coordText);
            }
            sb.append(")");
        } else {
            sb.append(" EMPTY");
        }

        return sb.toString();
    }

    private static String buildWKT(final String geometryType, final CoordinateSequence coordSeq) {
        return buildWKT(geometryType, coordSeq, "");
    }

    private static String buildWKT(final String geometryType, final CoordinateSequence coordSeq, final String dimensionString) {

        CustomCoordinateSequence coordSequence = (CustomCoordinateSequence) coordSeq;
        String wktText = convertToWKTText(coordSequence);

        StringBuilder sb = new StringBuilder(geometryType);

        if (!wktText.equals(" EMPTY")) {
            sb.append(dimensionString);
        }

        sb.append(wktText);

        return sb.toString();
    }

    private static String buildPolygon(final Polygon polygon, final boolean isIncludeGeometryType, final String dimensionString) {

        StringBuilder sb = new StringBuilder();

        if (isIncludeGeometryType) {
            sb.append("POLYGON");
        }

        if (!polygon.isEmpty()) {
            if (isIncludeGeometryType) {
                sb.append(dimensionString);
            }
            sb.append("(");

            //Find exterior shell
            LineString lineString = polygon.getExteriorRing();
            CustomCoordinateSequence coordSequence = (CustomCoordinateSequence) lineString.getCoordinateSequence();

            sb.append(convertToWKTText(coordSequence));

            //Find inner holes
            int interiorRings = polygon.getNumInteriorRing();
            for (int i = 0; i < interiorRings; i++) {
                sb.append(", ");
                LineString innerLineString = polygon.getInteriorRingN(i);
                CustomCoordinateSequence innerCoordSequence = (CustomCoordinateSequence) innerLineString.getCoordinateSequence();
                sb.append(convertToWKTText(innerCoordSequence));
            }

            sb.append(")");
        } else {
            sb.append(" EMPTY");
        }
        return sb.toString();
    }

    private static String buildMultiPoint(final MultiPoint multiPoint, final String dimensionString) {

        StringBuilder sb = new StringBuilder("MULTIPOINT");

        if (!multiPoint.isEmpty()) {

            sb.append(dimensionString);
            sb.append("(");
            //Find first point
            Point point = (Point) multiPoint.getGeometryN(0);
            CustomCoordinateSequence coordSequence = (CustomCoordinateSequence) point.getCoordinateSequence();

            sb.append(buildWKT("", coordSequence));
            //Encode remaining points
            int geomCount = multiPoint.getNumGeometries();
            for (int i = 1; i < geomCount; i++) {
                sb.append(", ");
                point = (Point) multiPoint.getGeometryN(i);
                coordSequence = (CustomCoordinateSequence) point.getCoordinateSequence();
                sb.append(buildWKT("", coordSequence));
            }
            sb.append(")");
        } else {
            sb.append(" EMPTY");
        }

        return sb.toString();
    }

    private static String buildMultiLineString(final MultiLineString multiLineString, final String dimensionString) {

        StringBuilder sb = new StringBuilder("MULTILINESTRING");

        if (!multiLineString.isEmpty()) {
            sb.append(dimensionString);
            sb.append("(");

            //Find first linestring
            LineString lineString = (LineString) multiLineString.getGeometryN(0);
            CustomCoordinateSequence coordSequence = (CustomCoordinateSequence) lineString.getCoordinateSequence();

            sb.append(buildWKT("", coordSequence));
            //Encode remaining points
            int geomCount = multiLineString.getNumGeometries();
            for (int i = 1; i < geomCount; i++) {
                sb.append(", ");
                lineString = (LineString) multiLineString.getGeometryN(i);
                coordSequence = (CustomCoordinateSequence) lineString.getCoordinateSequence();
                sb.append(buildWKT("", coordSequence));
            }
            sb.append(")");
        } else {
            sb.append(" EMPTY");
        }

        return sb.toString();
    }

    private static String buildMultiPolygon(final MultiPolygon multiPolygon, final String dimensionString) {

        StringBuilder sb = new StringBuilder("MULTIPOLYGON");

        if (!multiPolygon.isEmpty()) {
            sb.append(dimensionString);
            sb.append("(");

            //Find first polygon
            Polygon polygon = (Polygon) multiPolygon.getGeometryN(0);

            sb.append(buildPolygon(polygon, false, dimensionString));
            //Encode remaining points
            int geomCount = multiPolygon.getNumGeometries();
            for (int i = 1; i < geomCount; i++) {
                sb.append(", ");
                polygon = (Polygon) multiPolygon.getGeometryN(i);

                sb.append(buildPolygon(polygon, false, dimensionString));
            }
            sb.append(")");
        } else {
            sb.append(" EMPTY");
        }

        return sb.toString();
    }

    private static String buildGeometryCollection(final GeometryCollection geometryCollection, final CoordinateSequenceDimensions dimensions) {

        StringBuilder sb = new StringBuilder("GEOMETRYCOLLECTION");

        if (!geometryCollection.isEmpty()) {
            String dimensionString = CoordinateSequenceDimensions.convertDimensions(dimensions);
            sb.append(dimensionString);

            Geometry geometry = geometryCollection.getGeometryN(0);

            sb.append("(");
            sb.append(expand(geometry, dimensions));

            int geomCount = geometryCollection.getNumGeometries();
            for (int i = 1; i < geomCount; i++) {
                sb.append(", ");
                geometry = geometryCollection.getGeometryN(i);
                sb.append(expand(geometry, dimensions));
            }
            sb.append(")");
        } else {
            sb.append(" EMPTY");
        }

        return sb.toString();
    }

}
