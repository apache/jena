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

import javax.measure.IncommensurableException;
import javax.measure.Unit;
import javax.measure.quantity.Area;

import org.apache.jena.geosparql.configuration.GeoSPARQLConfig;
import org.apache.sis.measure.Quantities;
import org.apache.sis.measure.Units;
import org.apache.sis.referencing.CRS;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.Polygon;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.crs.GeographicCRS;
import org.opengis.referencing.cs.CartesianCS;
import org.opengis.referencing.cs.CoordinateSystem;

/**
 * Calculates the area of a Polygon and converts to target area units.
 *
 * <p>Empty geometries and types other than {@link Polygon} return zero, including
 * collections containing polygons. Non-empty geometries require finite X/Y
 * coordinates. For geographic CRSs, area is approximated on the source ellipsoid
 * using a local equal-area projection. Other non-empty Polygons require a Cartesian
 * horizontal CRS with equivalent linear units on both axes.
 */
final class GeometryArea {
    private GeometryArea() {
    }

    static double calculate(GeometryWrapper geometry, String targetUnitUri) {
        Unit<Area> targetUnit = AreaUnitsOfMeasure.getUnit(targetUnitUri);
        Geometry xyGeometry = geometry.getXYGeometry();
        for (Coordinate coordinate : xyGeometry.getCoordinates()) {
            if (!coordinate.isValid()) {
                throw new UnitsConversionException("Area requires finite X/Y coordinates.");
            }
        }
        // GeoSPARQL 1.1 requires zero for every geometry type other than Polygon.
        if (!(xyGeometry instanceof Polygon polygon) || polygon.isEmpty()) {
            return 0.0;
        }
        double sourceArea;
        Unit<Area> sourceUnit;
        if (CRS.getHorizontalComponent(geometry.getSrsInfo().getCrs()) instanceof GeographicCRS) {
            if (!geometry.getSrsInfo().isSRSRecognised()) {
                throw new UnitsConversionException("Area requires a recognised geographic coordinate reference system.");
            }
            if (!GeoSPARQLConfig.ALLOW_GEOMETRY_SRS_TRANSFORMATION) {
                throw new UnitsConversionException("Geographic area requires geometry SRS transformation to be enabled.");
            }
            sourceArea = GeographicArea.calculate(geometry);
            sourceUnit = Units.SQUARE_METRE;
        } else {
            sourceUnit = equivalentHorizontalAxisUnits(geometry).getUnit()
                    .pow(2).asType(Area.class);
            sourceArea = polygon.getArea();
        }
        double area = Quantities.create(sourceArea, sourceUnit)
                .to(targetUnit).getValue().doubleValue();
        if (!Double.isFinite(area)) {
            throw new UnitsConversionException("Area result is not finite.");
        }
        return area;
    }

    /**
     * Returns the common linear unit of a two-dimensional Cartesian horizontal CRS.
     * JTS calculates area from coordinate values as though the axes were orthogonal.
     * This method rejects non-Cartesian axes because it does not correct for skew.
     */
    private static UnitsOfMeasure equivalentHorizontalAxisUnits(GeometryWrapper geometry) {
        CoordinateReferenceSystem horizontalCrs = CRS.getHorizontalComponent(
                geometry.getSrsInfo().getCrs());
        if (horizontalCrs == null || horizontalCrs.getCoordinateSystem() == null
                || horizontalCrs.getCoordinateSystem().getDimension() != 2) {
            throw new UnitsConversionException(
                    "Area requires a two-dimensional horizontal source coordinate system.");
        }
        CoordinateSystem coordinateSystem = horizontalCrs.getCoordinateSystem();
        if (!(coordinateSystem instanceof CartesianCS)) {
            throw new UnitsConversionException("Area requires Cartesian horizontal source axes.");
        }
        Unit<?> firstAxisUnit = coordinateSystem.getAxis(0).getUnit();
        Unit<?> secondAxisUnit = coordinateSystem.getAxis(1).getUnit();
        try {
            if (!firstAxisUnit.isCompatible(secondAxisUnit)
                    || !firstAxisUnit.getConverterToAny(secondAxisUnit).isIdentity()) {
                throw new UnitsConversionException(
                        "Area requires equivalent linear units on both horizontal source axes.");
            }
        } catch (IncommensurableException e) {
            throw new UnitsConversionException(
                    "Area requires equivalent linear units on both horizontal source axes.", e);
        }
        UnitsOfMeasure sourceUnits = new UnitsOfMeasure(horizontalCrs);
        if (!sourceUnits.isLinearUnits()) {
            throw new UnitsConversionException(
                    "Area requires linear units on both horizontal source axes.");
        }
        return sourceUnits;
    }
}
