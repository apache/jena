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
package org.apache.jena.geosparql.implementation.jts;

import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import org.apache.jena.datatypes.DatatypeFormatException;
import static org.apache.jena.geosparql.configuration.GeoSPARQLOperations.cleanUpPrecision;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.CoordinateSequence;
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
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.TransformException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 *
 */
public class GeometryTransformation {

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    public static final Geometry transform(Geometry sourceGeometry, MathTransform transform) throws TransformException {

        String geometryType = sourceGeometry.getGeometryType();
        switch (geometryType) {
            case "Point":
                return transformPoint(sourceGeometry, transform);
            case "LineString":
                return transformLineString(sourceGeometry, transform);
            case "LinearRing":
                return transformLinearRing(sourceGeometry, transform);
            case "Polygon":
                return transformPolygon(sourceGeometry, transform);
            case "MultiPoint":
                return transformMultiPoint(sourceGeometry, transform);
            case "MultiLineString":
                return transformMultiLineString(sourceGeometry, transform);
            case "MultiPolygon":
                return transformMultiPolygon(sourceGeometry, transform);
            case "GeometryCollection":
                return transformGeometryCollection(sourceGeometry, transform);
            default:
                throw new DatatypeFormatException("Geometry type not supported: " + geometryType);
        }
    }

    private static Point transformPoint(Geometry sourceGeometry, MathTransform transform) throws TransformException {
        GeometryFactory geometryFactory = sourceGeometry.getFactory();

        Point point = (Point) sourceGeometry;
        CoordinateSequence coordSeq = point.getCoordinateSequence();
        CoordinateSequence transformCoordSeq = transformCoordSeq(coordSeq, transform);

        return geometryFactory.createPoint(transformCoordSeq);
    }

    private static LineString transformLineString(Geometry sourceGeometry, MathTransform transform) throws TransformException {
        GeometryFactory geometryFactory = sourceGeometry.getFactory();

        LineString lineString = (LineString) sourceGeometry;
        CoordinateSequence coordSeq = lineString.getCoordinateSequence();
        CoordinateSequence transformCoordSeq = transformCoordSeq(coordSeq, transform);

        return geometryFactory.createLineString(transformCoordSeq);
    }

    private static LinearRing transformLinearRing(Geometry sourceGeometry, MathTransform transform) throws TransformException {
        GeometryFactory geometryFactory = sourceGeometry.getFactory();

        LinearRing linearRing = (LinearRing) sourceGeometry;
        CoordinateSequence coordSeq = linearRing.getCoordinateSequence();
        CoordinateSequence transformCoordSeq = transformCoordSeq(coordSeq, transform);

        return geometryFactory.createLinearRing(transformCoordSeq);
    }

    private static Polygon transformPolygon(Geometry sourceGeometry, MathTransform transform) throws TransformException {
        GeometryFactory geometryFactory = sourceGeometry.getFactory();

        Polygon polygon = (Polygon) sourceGeometry;
        LinearRing exterior = transformLinearRing(polygon.getExteriorRing(), transform);

        int interiorsNumber = polygon.getNumInteriorRing();
        ArrayList<LinearRing> interiors = new ArrayList<>(interiorsNumber);
        for (int i = 0; i < polygon.getNumInteriorRing(); i++) {
            LinearRing interior = transformLinearRing(polygon.getInteriorRingN(i), transform);
            interiors.add(interior);
        }

        return geometryFactory.createPolygon(exterior, interiors.toArray(new LinearRing[interiors.size()]));
    }

    private static MultiPoint transformMultiPoint(Geometry sourceGeometry, MathTransform transform) throws TransformException {
        GeometryFactory geometryFactory = sourceGeometry.getFactory();

        MultiPoint multiPoint = (MultiPoint) sourceGeometry;

        int geometryNumber = multiPoint.getNumGeometries();
        ArrayList<Point> points = new ArrayList<>();
        for (int i = 0; i < geometryNumber; i++) {
            Point point = transformPoint(multiPoint.getGeometryN(i), transform);
            points.add(point);
        }

        return geometryFactory.createMultiPoint(points.toArray(new Point[points.size()]));
    }

    private static MultiLineString transformMultiLineString(Geometry sourceGeometry, MathTransform transform) throws TransformException {
        GeometryFactory geometryFactory = sourceGeometry.getFactory();

        MultiLineString multiLineString = (MultiLineString) sourceGeometry;

        int geometryNumber = multiLineString.getNumGeometries();
        ArrayList<LineString> lineStrings = new ArrayList<>();
        for (int i = 0; i < geometryNumber; i++) {
            LineString lineString = transformLineString(multiLineString.getGeometryN(i), transform);
            lineStrings.add(lineString);
        }

        return geometryFactory.createMultiLineString(lineStrings.toArray(new LineString[lineStrings.size()]));
    }

    private static MultiPolygon transformMultiPolygon(Geometry sourceGeometry, MathTransform transform) throws TransformException {
        GeometryFactory geometryFactory = sourceGeometry.getFactory();

        MultiPolygon multiPolygon = (MultiPolygon) sourceGeometry;

        int geometryNumber = multiPolygon.getNumGeometries();
        ArrayList<Polygon> polygons = new ArrayList<>();
        for (int i = 0; i < geometryNumber; i++) {
            Polygon polygon = transformPolygon(multiPolygon.getGeometryN(i), transform);
            polygons.add(polygon);
        }

        return geometryFactory.createMultiPolygon(polygons.toArray(new Polygon[polygons.size()]));
    }

    private static GeometryCollection transformGeometryCollection(Geometry sourceGeometry, MathTransform transform) throws TransformException {
        GeometryFactory geometryFactory = sourceGeometry.getFactory();

        GeometryCollection geometryCollection = (GeometryCollection) sourceGeometry;

        int geometryNumber = geometryCollection.getNumGeometries();
        ArrayList<Geometry> geometries = new ArrayList<>();
        for (int i = 0; i < geometryNumber; i++) {
            Geometry geometry = transform(geometryCollection.getGeometryN(i), transform);
            geometries.add(geometry);
        }

        return geometryFactory.createGeometryCollection(geometries.toArray(new Geometry[geometries.size()]));
    }

    private static CoordinateSequence transformCoordSeq(CoordinateSequence coordSeq, MathTransform transform) throws TransformException {

        int size = coordSeq.size();
        int sourceDims = transform.getSourceDimensions();
        int targetDims = transform.getTargetDimensions();

        double[] sourcePts = new double[size * sourceDims];
        double[] targetPts = new double[size * targetDims];

        //Setup source array for transform.
        boolean isZSource = sourceDims > 2;
        for (int i = 0; i < size; i++) {
            Coordinate coord = coordSeq.getCoordinate(i);
            int j = i * targetDims;
            sourcePts[j] = coord.getX();
            sourcePts[j + 1] = coord.getY();
            if (isZSource) {
                sourcePts[j + 2] = coord.getZ();
            }
        }

        //Transform the ordinates.
        transform.transform(sourcePts, 0, targetPts, 0, size);

        //Extract into coordiante sequence.
        double[] x = new double[size];
        double[] y = new double[size];
        double[] z = new double[size];
        double[] m = new double[size];

        boolean isZTransformed = sourceDims > 2 && targetDims > 2;
        for (int i = 0; i < size; i++) {
            Coordinate coord = coordSeq.getCoordinate(i);
            int j = i * targetDims;
            x[i] = cleanUpPrecision(targetPts[j]);
            y[i] = cleanUpPrecision(targetPts[j + 1]);
            if (isZTransformed) {
                z[i] = cleanUpPrecision(targetPts[j + 2]);
            } else {
                if (coordSeq.hasZ()) {
                    z[i] = cleanUpPrecision(coord.getZ());
                } else {
                    z[i] = Double.NaN;
                }
            }
            if (coordSeq.hasM()) {
                m[i] = coord.getM();
            } else {
                m[i] = Double.NaN;
            }

        }

        return new CustomCoordinateSequence(x, y, z, m);
    }

}
