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

import org.apache.sis.measure.Quantities;
import org.apache.sis.referencing.CRS;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.MultiPolygon;
import org.locationtech.jts.geom.Polygon;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.cs.CoordinateSystem;

/**
 * Calculates planar area for polygonal geometries and converts to target area units.
 *
 * <p>Area is calculated for {@link Polygon} and {@link MultiPolygon} geometries;
 * empty geometries and non-polygonal types return zero. Non-empty polygonal geometries
 * require a non-geographic horizontal CRS with equivalent linear units on both axes.
 */
final class GeometryArea {
    private GeometryArea() {
    }

    static double calculate(GeometryWrapper geometry, String targetUnitUri) {
        Unit<Area> targetUnit = AreaUnitsOfMeasure.getUnit(targetUnitUri);
        Geometry xyGeometry = geometry.getXYGeometry();
        if (!(xyGeometry instanceof Polygon || xyGeometry instanceof MultiPolygon)
                || xyGeometry.isEmpty()) {
            return 0.0;
        }
        if (geometry.getSrsInfo().isGeographic()) {
            throw new UnitsConversionException("Area is not supported for geographic coordinate reference systems.");
        }
        Unit<Area> sourceUnit = equivalentHorizontalAxisUnits(geometry).getUnit()
                .pow(2).asType(Area.class);
        return Quantities.create(xyGeometry.getArea(), sourceUnit)
                .to(targetUnit).getValue().doubleValue();
    }

    private static UnitsOfMeasure equivalentHorizontalAxisUnits(GeometryWrapper geometry) {
        CoordinateReferenceSystem horizontalCrs = CRS.getHorizontalComponent(
                geometry.getSrsInfo().getCrs());
        if (horizontalCrs == null || horizontalCrs.getCoordinateSystem() == null
                || horizontalCrs.getCoordinateSystem().getDimension() != 2) {
            throw new UnitsConversionException(
                    "Area requires a two-dimensional horizontal source coordinate system.");
        }
        CoordinateSystem coordinateSystem = horizontalCrs.getCoordinateSystem();
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
