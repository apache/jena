/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 *   SPDX-License-Identifier: Apache-2.0
 */
package org.apache.jena.geosparql.implementation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.sis.geometry.DirectPosition2D;
import org.apache.sis.measure.Units;
import org.apache.sis.referencing.CRS;
import org.apache.sis.referencing.CommonCRS;
import org.apache.sis.referencing.GeodeticCalculator;
import org.apache.sis.referencing.GeodeticException;
import org.apache.sis.referencing.crs.AbstractCRS;
import org.apache.sis.referencing.crs.DefaultProjectedCRS;
import org.apache.sis.referencing.cs.AxesConvention;
import org.apache.sis.referencing.operation.DefaultConversion;
import org.apache.sis.referencing.operation.transform.DefaultMathTransformFactory;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LinearRing;
import org.locationtech.jts.geom.Polygon;
import org.opengis.geometry.DirectPosition;
import org.opengis.parameter.ParameterValueGroup;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.crs.GeographicCRS;
import org.opengis.referencing.cs.CartesianCS;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.OperationMethod;
import org.opengis.referencing.operation.TransformException;
import org.opengis.util.FactoryException;

/** Approximates ellipsoidal polygon area in square metres without changing the input geometry. */
final class GeographicArea {
    // These limits bound the projection and boundary approximation. They are not
    // presented as an absolute area-error guarantee, which also depends on geometry.
    private static final double MAX_CENTER_ANGLE_DEGREES = 89.0;
    private static final double MAX_CHORD_ERROR_METRES = 0.25;
    private static final double MAX_SEGMENT_METRES = 50_000.0;
    private static final int MAX_RECURSION_DEPTH = 24;
    private static final int MAX_COORDINATES = 50_000;

    private GeographicArea() {
    }

    static double calculate(GeometryWrapper geometry) {
        Polygon polygon = (Polygon) geometry.getParsingGeometry();
        CoordinateReferenceSystem horizontal = CRS.getHorizontalComponent(geometry.getSrsInfo().getCrs());
        if (!(horizontal instanceof GeographicCRS sourceCrs)
                || sourceCrs.getCoordinateSystem().getDimension() != 2) {
            throw new UnitsConversionException("Geographic area requires a two-dimensional geographic horizontal CRS.");
        }

        try {
            GeographicCRS normalizedCrs = (GeographicCRS) AbstractCRS.castOrCopy(sourceCrs)
                    .forConvention(AxesConvention.NORMALIZED);
            MathTransform toNormalized = CRS.findOperation(sourceCrs, normalizedCrs, null).getMathTransform();
            double[] center = center(polygon, toNormalized);

            DefaultMathTransformFactory factory = DefaultMathTransformFactory.provider();
            OperationMethod method = factory.getOperationMethod("Lambert Azimuthal Equal Area");
            ParameterValueGroup parameters = method.getParameters().createValue();
            parameters.parameter("latitude_of_center").setValue(center[1]);
            parameters.parameter("longitude_of_center").setValue(center[0]);
            DefaultConversion conversion = new DefaultConversion(Map.of("name", "Local equal-area conversion"),
                    method, null, parameters);
            CartesianCS cartesian = (CartesianCS) CommonCRS.WGS84.universal(0, 0).getCoordinateSystem();
            DefaultProjectedCRS projectedCrs = new DefaultProjectedCRS(Map.of("name", "Local equal-area CRS"),
                    sourceCrs, conversion, cartesian);
            MathTransform projection = CRS.findOperation(sourceCrs, projectedCrs, null).getMathTransform();

            CoordinateBudget budget = new CoordinateBudget();
            GeodeticCalculator calculator = GeodeticCalculator.create(sourceCrs);
            return projectPolygon(polygon, sourceCrs, projection, calculator, budget).getArea();
        } catch (FactoryException | TransformException | GeodeticException e) {
            throw new UnitsConversionException("Geographic area could not be calculated reliably.", e);
        }
    }

    private static double[] center(Polygon polygon, MathTransform toNormalized) throws TransformException {
        List<double[]> vertices = new ArrayList<>();
        List<Double> longitudes = new ArrayList<>();
        double[] sum = new double[3];
        double minLatitude = Double.POSITIVE_INFINITY;
        double maxLatitude = Double.NEGATIVE_INFINITY;
        Coordinate[] shell = polygon.getExteriorRing().getCoordinates();
        for (int i = 0; i < shell.length - 1; i++) {
            double[] lonLat = transform(shell[i], toNormalized);
            double lon = Math.toRadians(lonLat[0]);
            double lat = Math.toRadians(lonLat[1]);
            if (!Double.isFinite(lon) || !Double.isFinite(lat) || Math.abs(lat) > Math.PI / 2) {
                throw new UnitsConversionException("Geographic area requires finite coordinates within latitude limits.");
            }
            vertices.add(new double[] {lon, lat});
            if (vertices.size() > MAX_COORDINATES) {
                throw new UnitsConversionException("Geographic area exceeds the supported edge approximation limit.");
            }
            longitudes.add(Math.atan2(Math.sin(lon), Math.cos(lon)));
            minLatitude = Math.min(minLatitude, lat);
            maxLatitude = Math.max(maxLatitude, lat);
            sum[0] += Math.cos(lat) * Math.cos(lon);
            sum[1] += Math.cos(lat) * Math.sin(lon);
            sum[2] += Math.sin(lat);
        }
        Collections.sort(longitudes);
        double largestGap = -1;
        double arcStart = 0;
        for (int i = 0; i < longitudes.size(); i++) {
            double current = longitudes.get(i);
            double next = i + 1 < longitudes.size() ? longitudes.get(i + 1)
                    : longitudes.get(0) + 2 * Math.PI;
            if (next - current > largestGap) {
                largestGap = next - current;
                arcStart = next;
            }
        }
        double centerLon = arcStart + (2 * Math.PI - largestGap) / 2;
        double centerLat = (minLatitude + maxLatitude) / 2;
        double bestMinimumCosine = minimumCosine(vertices, centerLon, centerLat);
        double norm = Math.hypot(Math.hypot(sum[0], sum[1]), sum[2]);
        if (norm >= 1e-9) {
            double meanLon = Math.atan2(sum[1], sum[0]);
            double meanLat = Math.atan2(sum[2], Math.hypot(sum[0], sum[1]));
            double meanMinimumCosine = minimumCosine(vertices, meanLon, meanLat);
            if (meanMinimumCosine > bestMinimumCosine) {
                centerLon = meanLon;
                centerLat = meanLat;
                bestMinimumCosine = meanMinimumCosine;
            }
        }
        double maxAngleCosine = Math.cos(Math.toRadians(MAX_CENTER_ANGLE_DEGREES));
        if (bestMinimumCosine < maxAngleCosine) {
            throw new UnitsConversionException("Geographic area extends beyond one supported local hemisphere.");
        }
        return new double[] {Math.toDegrees(Math.atan2(Math.sin(centerLon), Math.cos(centerLon))),
                Math.toDegrees(centerLat)};
    }

    private static double minimumCosine(List<double[]> vertices, double centerLon, double centerLat) {
        double minimum = 1;
        for (double[] vertex : vertices) {
            double lon = vertex[0];
            double lat = vertex[1];
            double cosine = Math.sin(centerLat) * Math.sin(lat)
                    + Math.cos(centerLat) * Math.cos(lat) * Math.cos(lon - centerLon);
            minimum = Math.min(minimum, cosine);
        }
        return minimum;
    }

    private static Polygon projectPolygon(Polygon polygon, GeographicCRS sourceCrs,
            MathTransform projection, GeodeticCalculator calculator, CoordinateBudget budget) throws TransformException {
        GeometryFactory factory = polygon.getFactory();
        LinearRing shell = projectRing(polygon.getExteriorRing(), sourceCrs, projection, calculator, budget);
        LinearRing[] holes = new LinearRing[polygon.getNumInteriorRing()];
        for (int i = 0; i < holes.length; i++) {
            holes[i] = projectRing(polygon.getInteriorRingN(i), sourceCrs, projection, calculator, budget);
        }
        return factory.createPolygon(shell, holes);
    }

    private static LinearRing projectRing(LinearRing ring, GeographicCRS sourceCrs,
            MathTransform projection, GeodeticCalculator calculator, CoordinateBudget budget) throws TransformException {
        Coordinate[] source = ring.getCoordinates();
        List<Coordinate> target = new ArrayList<>();
        target.add(project(source[0], projection));
        for (int i = 1; i < source.length; i++) {
            Coordinate end = project(source[i], projection);
            appendGeodesic(source[i - 1], source[i], target.get(target.size() - 1), end,
                    sourceCrs, projection, calculator, target, budget, 0);
        }
        target.set(target.size() - 1, new Coordinate(target.get(0)));
        return ring.getFactory().createLinearRing(target.toArray(Coordinate[]::new));
    }

    private static void appendGeodesic(Coordinate start, Coordinate end, Coordinate projectedStart,
            Coordinate projectedEnd, GeographicCRS sourceCrs, MathTransform projection,
            GeodeticCalculator calculator, List<Coordinate> target, CoordinateBudget budget, int depth) throws TransformException {
        calculator.setStartPoint(new DirectPosition2D(sourceCrs, start.x, start.y));
        calculator.setEndPoint(new DirectPosition2D(sourceCrs, end.x, end.y));
        double length = calculator.getGeodesicDistance();
        double metres = calculator.getDistanceUnit().getConverterTo(Units.METRE).convert(length);
        if (!Double.isFinite(metres)) {
            throw new UnitsConversionException("Geographic area has a non-finite polygon edge.");
        }
        if (metres == 0) {
            add(target, projectedEnd, budget);
            return;
        }
        double azimuth = calculator.getStartingAzimuth();
        calculator.setStartingAzimuth(azimuth);
        calculator.setGeodesicDistance(length / 2);
        DirectPosition middle = calculator.getEndPoint();
        Coordinate midpoint = new Coordinate(middle.getOrdinate(0), middle.getOrdinate(1));
        Coordinate projectedMidpoint = project(midpoint, projection);
        double chordError = distanceToSegment(projectedMidpoint, projectedStart, projectedEnd);
        if (metres > MAX_SEGMENT_METRES || chordError > MAX_CHORD_ERROR_METRES) {
            if (depth >= MAX_RECURSION_DEPTH) {
                throw new UnitsConversionException("Geographic area exceeds the supported edge approximation limit.");
            }
            appendGeodesic(start, midpoint, projectedStart, projectedMidpoint,
                    sourceCrs, projection, calculator, target, budget, depth + 1);
            appendGeodesic(midpoint, end, projectedMidpoint, projectedEnd,
                    sourceCrs, projection, calculator, target, budget, depth + 1);
        } else {
            add(target, projectedEnd, budget);
        }
    }

    private static void add(List<Coordinate> target, Coordinate point, CoordinateBudget budget) {
        if (++budget.count > MAX_COORDINATES) {
            throw new UnitsConversionException("Geographic area exceeds the supported edge approximation limit.");
        }
        target.add(point);
    }

    private static double distanceToSegment(Coordinate point, Coordinate start, Coordinate end) {
        double dx = end.x - start.x;
        double dy = end.y - start.y;
        double lengthSquared = dx * dx + dy * dy;
        if (lengthSquared == 0) {
            return point.distance(start);
        }
        double fraction = Math.max(0, Math.min(1,
                ((point.x - start.x) * dx + (point.y - start.y) * dy) / lengthSquared));
        return Math.hypot(point.x - start.x - fraction * dx,
                point.y - start.y - fraction * dy);
    }

    private static Coordinate project(Coordinate point, MathTransform transform) throws TransformException {
        double[] projected = transform(point, transform);
        if (!Double.isFinite(projected[0]) || !Double.isFinite(projected[1])) {
            throw new UnitsConversionException("Geographic area has a coordinate outside the local projection.");
        }
        return new Coordinate(projected[0], projected[1]);
    }

    private static double[] transform(Coordinate point, MathTransform transform) throws TransformException {
        double[] result = new double[2];
        transform.transform(new double[] {point.x, point.y}, 0, result, 0, 1);
        return result;
    }

    private static final class CoordinateBudget {
        int count;
    }
}
