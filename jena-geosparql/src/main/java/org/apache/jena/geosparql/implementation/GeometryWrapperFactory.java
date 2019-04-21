/*
 * Copyright 2019 .
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.jena.geosparql.implementation;

import org.apache.jena.geosparql.implementation.jts.CustomGeometryFactory;
import org.apache.jena.geosparql.implementation.vocabulary.SRS_URI;
import java.util.List;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryCollection;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.LinearRing;
import org.locationtech.jts.geom.MultiLineString;
import org.locationtech.jts.geom.MultiPoint;
import org.locationtech.jts.geom.MultiPolygon;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;

/**
 *
 *
 */
public class GeometryWrapperFactory {

    private static final GeometryFactory GEOMETRY_FACTORY = CustomGeometryFactory.theInstance();

    /**
     * Create Point GeometryWrapper.
     *
     * @param coordinate In X/Y order.
     * @param srsURI
     * @param geometryDatatypeURI
     * @return GeometryWrapper with SRS URI and GeometryDatatype URI.
     */
    public static final GeometryWrapper createPoint(Coordinate coordinate, String srsURI, String geometryDatatypeURI) {
        Point geometry = GEOMETRY_FACTORY.createPoint(coordinate);
        return createGeometry(geometry, srsURI, geometryDatatypeURI);
    }

    /**
     * Create LineString GeometryWrapper.
     *
     * @param coordinates In X/Y order.
     * @param srsURI
     * @param geometryDatatypeURI
     * @return GeometryWrapper with SRS URI and GeometryDatatype URI.
     */
    public static final GeometryWrapper createLineString(List<Coordinate> coordinates, String srsURI, String geometryDatatypeURI) {
        return createLineString(coordinates.toArray(new Coordinate[coordinates.size()]), srsURI, geometryDatatypeURI);
    }

    /**
     * Create LineString GeometryWrapper.
     *
     * @param coordinates In X/Y order.
     * @param srsURI
     * @param geometryDatatypeURI
     * @return GeometryWrapper with SRS URI and GeometryDatatype URI.
     */
    public static final GeometryWrapper createLineString(Coordinate[] coordinates, String srsURI, String geometryDatatypeURI) {
        LineString geometry = GEOMETRY_FACTORY.createLineString(coordinates);
        return createGeometry(geometry, srsURI, geometryDatatypeURI);
    }

    /**
     * Create LineString GeometryWrapper.
     *
     * @param lineString In X/Y order.
     * @param srsURI
     * @param geometryDatatypeURI
     * @return GeometryWrapper with SRS URI and GeometryDatatype URI.
     */
    public static final GeometryWrapper createLineString(LineString lineString, String srsURI, String geometryDatatypeURI) {
        LineString geometry = lineString;
        return createGeometry(geometry, srsURI, geometryDatatypeURI);
    }

    /**
     * Create Polygon GeometryWrapper.
     *
     * @param coordinates In X/Y order.
     * @param srsURI
     * @param geometryDatatypeURI
     * @return GeometryWrapper with SRS URI and GeometryDatatype URI.
     */
    public static final GeometryWrapper createPolygon(List<Coordinate> coordinates, String srsURI, String geometryDatatypeURI) {
        Polygon geometry = GEOMETRY_FACTORY.createPolygon(coordinates.toArray(new Coordinate[coordinates.size()]));
        return createGeometry(geometry, srsURI, geometryDatatypeURI);
    }

    /**
     * Create Polygon GeometryWrapper.
     *
     * @param shell In X/Y order.
     * @param holes In X/Y order.
     * @param srsURI
     * @param geometryDatatypeURI
     * @return GeometryWrapper with SRS URI and GeometryDatatype URI.
     */
    public static final GeometryWrapper createPolygon(LinearRing shell, LinearRing[] holes, String srsURI, String geometryDatatypeURI) {
        Polygon geometry = GEOMETRY_FACTORY.createPolygon(shell, holes);
        return createGeometry(geometry, srsURI, geometryDatatypeURI);
    }

    /**
     * Create Polygon GeometryWrapper.
     *
     * @param shell In X/Y order.
     * @param srsURI
     * @param geometryDatatypeURI
     * @return GeometryWrapper with SRS URI and GeometryDatatype URI.
     */
    public static final GeometryWrapper createPolygon(LinearRing shell, String srsURI, String geometryDatatypeURI) {
        Polygon geometry = GEOMETRY_FACTORY.createPolygon(shell);
        return createGeometry(geometry, srsURI, geometryDatatypeURI);
    }

    /**
     * Create Polygon GeometryWrapper from Envelope.
     *
     * @param envelope In X/Y order.
     * @param srsURI
     * @param geometryDatatypeURI
     * @return GeometryWrapper with SRS URI and GeometryDatatype URI.
     */
    public static final GeometryWrapper createPolygon(Envelope envelope, String srsURI, String geometryDatatypeURI) {
        LinearRing linearRing = GEOMETRY_FACTORY.createLinearRing(
                new Coordinate[]{
                    new Coordinate(envelope.getMinX(), envelope.getMinY()),
                    new Coordinate(envelope.getMaxX(), envelope.getMinY()),
                    new Coordinate(envelope.getMaxX(), envelope.getMaxY()),
                    new Coordinate(envelope.getMinX(), envelope.getMaxY()),
                    new Coordinate(envelope.getMinX(), envelope.getMinY())
                });

        return createPolygon(linearRing, srsURI, geometryDatatypeURI);
    }

    /**
     * Create MultiPoint GeometryWrapper.
     *
     * @param coordinates In X/Y order.
     * @param srsURI
     * @param geometryDatatypeURI
     * @return GeometryWrapper with SRS URI and GeometryDatatype URI.
     */
    public static final GeometryWrapper createMultiPoint(List<Coordinate> coordinates, String srsURI, String geometryDatatypeURI) {
        MultiPoint geometry = GEOMETRY_FACTORY.createMultiPointFromCoords(coordinates.toArray(new Coordinate[coordinates.size()]));
        return createGeometry(geometry, srsURI, geometryDatatypeURI);
    }

    /**
     * Create MultiLineString GeometryWrapper.
     *
     * @param lineStrings In X/Y order.
     * @param srsURI
     * @param geometryDatatypeURI
     * @return GeometryWrapper with SRS URI and GeometryDatatype URI.
     */
    public static final GeometryWrapper createMultiLineString(List<LineString> lineStrings, String srsURI, String geometryDatatypeURI) {
        MultiLineString geometry = GEOMETRY_FACTORY.createMultiLineString(lineStrings.toArray(new LineString[lineStrings.size()]));
        return createGeometry(geometry, srsURI, geometryDatatypeURI);
    }

    /**
     * Create MultiPolygon GeometryWrapper.
     *
     * @param polygons In X/Y order.
     * @param srsURI
     * @param geometryDatatypeURI
     * @return GeometryWrapper with SRS URI and GeometryDatatype URI.
     */
    public static final GeometryWrapper createMultiPolygon(List<Polygon> polygons, String srsURI, String geometryDatatypeURI) {
        MultiPolygon geometry = GEOMETRY_FACTORY.createMultiPolygon(polygons.toArray(new Polygon[polygons.size()]));
        return createGeometry(geometry, srsURI, geometryDatatypeURI);
    }

    /**
     * Create GeometryCollection GeometryWrapper.
     *
     * @param geometries In X/Y order.
     * @param srsURI
     * @param geometryDatatypeURI
     * @return GeometryWrapper with SRS URI and GeometryDatatype URI.
     */
    public static final GeometryWrapper createGeometryCollection(List<Geometry> geometries, String srsURI, String geometryDatatypeURI) {
        GeometryCollection geometry = GEOMETRY_FACTORY.createGeometryCollection(geometries.toArray(new Geometry[geometries.size()]));
        return createGeometry(geometry, srsURI, geometryDatatypeURI);
    }

    /**
     * Create Geometry GeometryWrapper.
     *
     * @param geometry In X/Y order.
     * @param srsURI
     * @param geometryDatatypeURI
     * @return GeometryWrapper with SRS URI and GeometryDatatype URI.
     */
    public static final GeometryWrapper createGeometry(Geometry geometry, String srsURI, String geometryDatatypeURI) {
        Geometry xyGeometry = geometry;
        Geometry parsingGeometry = GeometryReverse.check(xyGeometry, srsURI);
        DimensionInfo dimsInfo = DimensionInfo.find(geometry.getCoordinate(), xyGeometry);

        return new GeometryWrapper(parsingGeometry, xyGeometry, srsURI, geometryDatatypeURI, dimsInfo);
    }

    /**
     * Create Point GeometryWrapper using the default WKT CRS84 SRS URI.
     *
     * @param coordinate In X/Y order.
     * @param geometryDatatypeURI
     * @return GeometryWrapper with SRS URI and GeometryDatatype URI.
     */
    public static final GeometryWrapper createPoint(Coordinate coordinate, String geometryDatatypeURI) {
        return createPoint(coordinate, SRS_URI.DEFAULT_WKT_CRS84, geometryDatatypeURI);
    }

    /**
     * Create LineString GeometryWrapper using the default WKT CRS84 SRS URI.
     *
     * @param coordinates In X/Y order.
     * @param geometryDatatypeURI
     * @return GeometryWrapper with SRS URI and GeometryDatatype URI.
     */
    public static final GeometryWrapper createLineString(Coordinate[] coordinates, String geometryDatatypeURI) {
        return createLineString(coordinates, SRS_URI.DEFAULT_WKT_CRS84, geometryDatatypeURI);
    }

    /**
     * Create LineString GeometryWrapper using the default WKT CRS84 SRS URI.
     *
     * @param coordinates In X/Y order.
     * @param geometryDatatypeURI
     * @return GeometryWrapper with SRS URI and GeometryDatatype URI.
     */
    public static final GeometryWrapper createLineString(List<Coordinate> coordinates, String geometryDatatypeURI) {
        return createLineString(coordinates, SRS_URI.DEFAULT_WKT_CRS84, geometryDatatypeURI);
    }

    /**
     * Create LineString GeometryWrapper using the default WKT CRS84 SRS URI.
     *
     * @param lineString In X/Y order.
     * @param geometryDatatypeURI
     * @return GeometryWrapper with SRS URI and GeometryDatatype URI.
     */
    public static final GeometryWrapper createLineString(LineString lineString, String geometryDatatypeURI) {
        return createLineString(lineString, SRS_URI.DEFAULT_WKT_CRS84, geometryDatatypeURI);
    }

    /**
     * Create Polygon GeometryWrapper using the default WKT CRS84 SRS URI.
     *
     * @param coordinates In X/Y order.
     * @param geometryDatatypeURI
     * @return GeometryWrapper with SRS URI and GeometryDatatype URI.
     */
    public static final GeometryWrapper createPolygon(List<Coordinate> coordinates, String geometryDatatypeURI) {
        return createPolygon(coordinates, SRS_URI.DEFAULT_WKT_CRS84, geometryDatatypeURI);
    }

    /**
     * Create Polygon GeometryWrapper using the default WKT CRS84 SRS URI.
     *
     * @param shell In X/Y order.
     * @param holes In X/Y order.
     * @param geometryDatatypeURI
     * @return GeometryWrapper with SRS URI and GeometryDatatype URI.
     */
    public static final GeometryWrapper createPolygon(LinearRing shell, LinearRing[] holes, String geometryDatatypeURI) {
        return createPolygon(shell, holes, SRS_URI.DEFAULT_WKT_CRS84, geometryDatatypeURI);
    }

    /**
     * Create Polygon GeometryWrapper using the default WKT CRS84 SRS URI.
     *
     * @param shell In X/Y order.
     * @param geometryDatatypeURI
     * @return GeometryWrapper with SRS URI and GeometryDatatype URI.
     */
    public static final GeometryWrapper createPolygon(LinearRing shell, String geometryDatatypeURI) {
        return createPolygon(shell, SRS_URI.DEFAULT_WKT_CRS84, geometryDatatypeURI);
    }

    /**
     * Create Polygon GeometryWrapper using the default WKT CRS84 SRS URI.
     *
     * @param envelope In X/Y order.
     * @param geometryDatatypeURI
     * @return GeometryWrapper with SRS URI and GeometryDatatype URI.
     */
    public static final GeometryWrapper createPolygon(Envelope envelope, String geometryDatatypeURI) {
        return createPolygon(envelope, SRS_URI.DEFAULT_WKT_CRS84, geometryDatatypeURI);
    }

    /**
     * Create MultiPoint GeometryWrapper using the default WKT CRS84 SRS URI.
     *
     * @param coordinates In X/Y order.
     * @param geometryDatatypeURI
     * @return GeometryWrapper with SRS URI and GeometryDatatype URI.
     */
    public static final GeometryWrapper createMultiPoint(List<Coordinate> coordinates, String geometryDatatypeURI) {
        return createMultiPoint(coordinates, SRS_URI.DEFAULT_WKT_CRS84, geometryDatatypeURI);
    }

    /**
     * Create MultiLineString GeometryWrapper using the default WKT CRS84 SRS
     * URI.
     *
     * @param lineStrings In X/Y order.
     * @param geometryDatatypeURI
     * @return GeometryWrapper with SRS URI and GeometryDatatype URI.
     */
    public static final GeometryWrapper createMultiLineString(List<LineString> lineStrings, String geometryDatatypeURI) {
        return createMultiLineString(lineStrings, SRS_URI.DEFAULT_WKT_CRS84, geometryDatatypeURI);
    }

    /**
     * Create MultiPolygon GeometryWrapper using the default WKT CRS84 SRS URI.
     *
     * @param polygons In X/Y order.
     * @param geometryDatatypeURI
     * @return GeometryWrapper with SRS URI and GeometryDatatype URI.
     */
    public static final GeometryWrapper createMultiPolygon(List<Polygon> polygons, String geometryDatatypeURI) {
        return createMultiPolygon(polygons, SRS_URI.DEFAULT_WKT_CRS84, geometryDatatypeURI);
    }

    /**
     * Create Geometry GeometryWrapper using the default WKT CRS84 SRS URI.
     *
     * @param geometry In X/Y order.
     * @param geometryDatatypeURI
     * @return GeometryWrapper with SRS URI and GeometryDatatype URI.
     */
    public static final GeometryWrapper createGeometry(Geometry geometry, String geometryDatatypeURI) {
        return createGeometry(geometry, SRS_URI.DEFAULT_WKT_CRS84, geometryDatatypeURI);
    }
}
