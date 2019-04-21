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
package org.apache.jena.geosparql.implementation;

import org.apache.jena.geosparql.implementation.jts.CoordinateSequenceDimensions;
import static org.apache.jena.geosparql.implementation.jts.CustomCoordinateSequence.findCoordinateSequenceDimensions;
import java.io.Serializable;
import java.util.List;
import java.util.Objects;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.CoordinateXY;
import org.locationtech.jts.geom.Geometry;

/**
 *
 *
 */
public class DimensionInfo implements Serializable {

    private final int coordinate;
    private final int spatial;
    private final int topological;
    private final CoordinateSequenceDimensions coordinateSequenceDimensions;

    private final boolean isPoint;
    private final boolean isLine;
    private final boolean isArea;

    public DimensionInfo(CoordinateSequenceDimensions coordinateSequenceDimensions, int topological) {
        this.coordinateSequenceDimensions = coordinateSequenceDimensions;
        this.topological = topological;
        this.coordinate = findCoordinateDimension(coordinateSequenceDimensions);
        this.spatial = findSpatialDimension(coordinateSequenceDimensions);
        this.isPoint = topological == 0;
        this.isLine = topological == 1;
        this.isArea = topological == 2;
    }

    public DimensionInfo(int coordinate, int spatial, int topological) {
        this.coordinate = coordinate;
        this.spatial = spatial;
        this.topological = topological;
        this.coordinateSequenceDimensions = findCoordinateSequenceDimensions(coordinate, spatial);
        this.isPoint = topological == 0;
        this.isLine = topological == 1;
        this.isArea = topological == 2;
    }

    public static int findSpatialDimension(CoordinateSequenceDimensions dims) {

        switch (dims) {
            case XYZ:
            case XYZM:
                return 3;
            default:
                return 2;
        }
    }

    public static int findCoordinateDimension(CoordinateSequenceDimensions dims) {
        switch (dims) {
            case XYZ:
            case XYM:
                return 3;
            case XYZM:
                return 4;
            default:
                return 2;
        }
    }

    public static DimensionInfo find(Coordinate coordinate, Geometry geometry) {
        CoordinateSequenceDimensions coordDims = CoordinateSequenceDimensions.find(coordinate);
        return new DimensionInfo(coordDims, geometry.getDimension());
    }

    private static final Coordinate XY_COORDINATE = new CoordinateXY(0, 0);

    public static DimensionInfo find(Coordinate[] coordinates, Geometry geometry) {
        Coordinate coordinate;
        if (coordinates.length == 0) {
            coordinate = XY_COORDINATE;
        } else {
            coordinate = coordinates[0];
        }
        return find(coordinate, geometry);
    }

    public static DimensionInfo find(List<Coordinate> coordinates, Geometry geometry) {
        Coordinate coordinate;
        if (coordinates.isEmpty()) {
            coordinate = XY_COORDINATE;
        } else {
            coordinate = coordinates.get(0);
        }
        return find(coordinate, geometry);
    }

    public static DimensionInfo findCollection(List<? extends Geometry> geometries, Geometry geometry) {
        Coordinate coordinate;
        if (geometries.isEmpty()) {
            coordinate = XY_COORDINATE;
        } else {
            Geometry geom = geometries.get(0);
            coordinate = geom.getCoordinate();
        }
        return find(coordinate, geometry);
    }

    public int getCoordinate() {
        return coordinate;
    }

    public int getSpatial() {
        return spatial;
    }

    public int getTopological() {
        return topological;
    }

    public boolean isPoint() {
        return isPoint;
    }

    public boolean isLine() {
        return isLine;
    }

    public boolean isArea() {
        return isArea;
    }

    public CoordinateSequenceDimensions getDimensions() {
        return coordinateSequenceDimensions;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 53 * hash + this.coordinate;
        hash = 53 * hash + this.spatial;
        hash = 53 * hash + this.topological;
        hash = 53 * hash + Objects.hashCode(this.coordinateSequenceDimensions);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final DimensionInfo other = (DimensionInfo) obj;
        if (this.coordinate != other.coordinate) {
            return false;
        }
        if (this.spatial != other.spatial) {
            return false;
        }
        if (this.topological != other.topological) {
            return false;
        }
        return this.coordinateSequenceDimensions == other.coordinateSequenceDimensions;
    }

    @Override
    public String toString() {
        return "DimensionInfo{" + "coordinate=" + coordinate + ", spatial=" + spatial + ", topological=" + topological + ", coordinateSequenceDimensions=" + coordinateSequenceDimensions + ", isPoint=" + isPoint + ", isLine=" + isLine + ", isArea=" + isArea + '}';
    }

    public static DimensionInfo XY_POINT = new DimensionInfo(2, 2, 0);

    public static DimensionInfo XYZ_POINT = new DimensionInfo(3, 3, 0);

    public static DimensionInfo XYM_POINT = new DimensionInfo(3, 2, 0);

    public static DimensionInfo XYZM_POINT = new DimensionInfo(4, 3, 0);

    public static DimensionInfo XY_LINESTRING = new DimensionInfo(2, 2, 1);

    public static DimensionInfo XYZ_LINESTRING = new DimensionInfo(3, 3, 1);

    public static DimensionInfo XYM_LINESTRING = new DimensionInfo(3, 2, 1);

    public static DimensionInfo XYZM_LINESTRING = new DimensionInfo(4, 3, 1);

    public static DimensionInfo XY_POLYGON = new DimensionInfo(2, 2, 2);

    public static DimensionInfo XYZ_POLYGON = new DimensionInfo(3, 3, 2);

    public static DimensionInfo XYM_POLYGON = new DimensionInfo(3, 2, 2);

    public static DimensionInfo XYZM_POLYGON = new DimensionInfo(4, 3, 2);

}
