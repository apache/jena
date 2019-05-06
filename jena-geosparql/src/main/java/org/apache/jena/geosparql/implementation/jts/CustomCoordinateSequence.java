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

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;
import static org.apache.jena.geosparql.implementation.WKTLiteralFactory.reducePrecision;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.CoordinateSequence;
import org.locationtech.jts.geom.Envelope;

/**
 *
 * Based on: OGC 06-103r4 http://www.opengeospatial.org/standards/sfa
 *
 *
 */
public class CustomCoordinateSequence implements CoordinateSequence, Serializable {

    private final double[] x;
    private final double[] y;
    private final double[] z;
    private final double[] m;
    private final int size;
    private final int coordinateDimension;
    private final int spatialDimension;
    private final int measuresDimension;
    private final CoordinateSequenceDimensions dimensions;

    public CustomCoordinateSequence() {
        this.size = 0;
        this.x = new double[size];
        this.y = new double[size];
        this.z = new double[size];
        this.m = new double[size];
        this.coordinateDimension = 4;
        this.spatialDimension = 3;
        this.dimensions = CoordinateSequenceDimensions.XYZM;
        this.measuresDimension = 1;
    }

    public CustomCoordinateSequence(int size, CoordinateSequenceDimensions dimensions) {
        this.size = size;
        this.x = new double[size];
        this.y = new double[size];
        this.z = new double[size];
        this.m = new double[size];

        for (int i = 0; i < size; i++) {
            this.x[i] = Double.NaN;
            this.y[i] = Double.NaN;
            this.z[i] = Double.NaN;
            this.m[i] = Double.NaN;
        }

        int[] dims = getDimensionValues(dimensions);
        this.coordinateDimension = dims[0];
        this.spatialDimension = dims[1];
        this.dimensions = dimensions;
        this.measuresDimension = dimensions.equals(CoordinateSequenceDimensions.XYM) || dimensions.equals(CoordinateSequenceDimensions.XYZM) ? 1 : 0;
    }

    public CustomCoordinateSequence(int size, int dimension) {
        this.size = size;
        this.x = new double[size];
        this.y = new double[size];
        this.z = new double[size];
        this.m = new double[size];

        for (int i = 0; i < size; i++) {
            this.x[i] = Double.NaN;
            this.y[i] = Double.NaN;
            this.z[i] = Double.NaN;
            this.m[i] = Double.NaN;
        }

        this.coordinateDimension = dimension;

        //Doesn't handle XYM....
        if (dimension == 4) {
            this.spatialDimension = 3;
            this.dimensions = CoordinateSequenceDimensions.XYZM;
            this.measuresDimension = 1;
        } else {
            this.spatialDimension = dimension;
            if (dimension == 2) {
                this.dimensions = CoordinateSequenceDimensions.XY;
            } else {
                this.dimensions = CoordinateSequenceDimensions.XYZ;
            }
            this.measuresDimension = 0;
        }
    }

    public CustomCoordinateSequence(CoordinateSequenceDimensions dimensions) {
        this(dimensions, "");
    }

    public CustomCoordinateSequence(CoordinateSequenceDimensions dimensions, List<Coordinate> coordinates) {

        this.dimensions = dimensions;
        if (!coordinates.isEmpty()) {

            this.size = coordinates.size();
            this.x = new double[size];
            this.y = new double[size];
            this.z = new double[size];
            this.m = new double[size];

            for (int i = 0; i < size; i++) {
                Coordinate coord = coordinates.get(i);

                switch (dimensions) {
                    default:
                        this.x[i] = coord.getX();
                        this.y[i] = coord.getY();
                        this.z[i] = Double.NaN;
                        this.m[i] = Double.NaN;
                        break;
                    case XYZ:
                        this.x[i] = coord.getX();
                        this.y[i] = coord.getY();
                        this.z[i] = coord.getZ();
                        this.m[i] = Double.NaN;
                        break;
                    case XYM:
                        this.x[i] = coord.getX();
                        this.y[i] = coord.getY();
                        this.z[i] = Double.NaN;
                        this.m[i] = coord.getM();
                        break;
                    case XYZM:
                        this.x[i] = coord.getX();
                        this.y[i] = coord.getY();
                        this.z[i] = coord.getZ();
                        this.m[i] = coord.getM();
                        break;
                }

            }

        } else {
            this.size = 0;
            this.x = new double[size];
            this.y = new double[size];
            this.z = new double[size];
            this.m = new double[size];
        }

        int[] dims = getDimensionValues(dimensions);
        this.coordinateDimension = dims[0];
        this.spatialDimension = dims[1];
        this.measuresDimension = dimensions.equals(CoordinateSequenceDimensions.XYM) || dimensions.equals(CoordinateSequenceDimensions.XYZM) ? 1 : 0;
    }

    public CustomCoordinateSequence(CoordinateSequenceDimensions dimensions, String sequence) {

        this.dimensions = dimensions;
        if (!sequence.isEmpty()) {

            String parts[] = sequence.split(",");

            this.size = parts.length;
            this.x = new double[size];
            this.y = new double[size];
            this.z = new double[size];
            this.m = new double[size];

            for (int i = 0; i < size; i++) {
                String part = parts[i].trim();
                String[] coords = part.split(" ");

                switch (dimensions) {
                    default:
                        this.x[i] = Double.parseDouble(coords[0]);
                        this.y[i] = Double.parseDouble(coords[1]);
                        this.z[i] = Double.NaN;
                        this.m[i] = Double.NaN;
                        break;
                    case XYZ:
                        this.x[i] = Double.parseDouble(coords[0]);
                        this.y[i] = Double.parseDouble(coords[1]);
                        this.z[i] = Double.parseDouble(coords[2]);
                        this.m[i] = Double.NaN;
                        break;
                    case XYM:
                        this.x[i] = Double.parseDouble(coords[0]);
                        this.y[i] = Double.parseDouble(coords[1]);
                        this.z[i] = Double.NaN;
                        this.m[i] = Double.parseDouble(coords[2]);
                        break;
                    case XYZM:
                        this.x[i] = Double.parseDouble(coords[0]);
                        this.y[i] = Double.parseDouble(coords[1]);
                        this.z[i] = Double.parseDouble(coords[2]);
                        this.m[i] = Double.parseDouble(coords[3]);
                        break;
                }

            }

        } else {
            this.size = 0;
            this.x = new double[size];
            this.y = new double[size];
            this.z = new double[size];
            this.m = new double[size];
        }

        int[] dims = getDimensionValues(dimensions);
        this.coordinateDimension = dims[0];
        this.spatialDimension = dims[1];
        this.measuresDimension = dimensions.equals(CoordinateSequenceDimensions.XYM) || dimensions.equals(CoordinateSequenceDimensions.XYZM) ? 1 : 0;
    }

    @Override
    public CustomCoordinateSequence copy() {
        return new CustomCoordinateSequence(x, y, z, m);
    }

    public int getSize() {
        return size;
    }

    private static int[] getDimensionValues(CoordinateSequenceDimensions dimensions) {

        int coordinateDimension;
        int spatialDimension;
        switch (dimensions) {
            default:
                coordinateDimension = 2;
                spatialDimension = 2;
                break;
            case XYZ:
                coordinateDimension = 3;
                spatialDimension = 3;
                break;
            case XYM:
                coordinateDimension = 3;
                spatialDimension = 2;
                break;
            case XYZM:
                coordinateDimension = 4;
                spatialDimension = 3;
                break;
        }

        return new int[]{coordinateDimension, spatialDimension};
    }

    public CustomCoordinateSequence(Coordinate[] coordinates) {

        this.size = coordinates.length;
        this.x = new double[size];
        this.y = new double[size];
        this.z = new double[size];
        this.m = new double[size];

        for (int i = 0; i < size; i++) {
            this.x[i] = coordinates[i].getX();
            this.y[i] = coordinates[i].getY();
            this.z[i] = coordinates[i].getZ();
            this.m[i] = Double.NaN;
        }

        //Check whether Z coordinateDimension is in use - m cannot be in use with "jts.geom.Coordinate".
        boolean isZPresent = checkDimensionality(this.z);

        if (isZPresent) {
            this.coordinateDimension = 3;
            this.spatialDimension = 3;
            this.dimensions = CoordinateSequenceDimensions.XYZ;
        } else {
            this.coordinateDimension = 2;
            this.spatialDimension = 2;
            this.dimensions = CoordinateSequenceDimensions.XY;
        }
        this.measuresDimension = 0;
    }

    public static final CustomCoordinateSequence createPoint(double x, double y) {
        return new CustomCoordinateSequence(new double[]{x}, new double[]{y}, new double[]{Double.NaN}, new double[]{Double.NaN});
    }

    public CustomCoordinateSequence(double[] x, double[] y, double[] z, double[] m) {

        this.size = x.length;
        this.x = new double[size];
        this.y = new double[size];
        this.z = new double[size];
        this.m = new double[size];

        for (int i = 0; i < size; i++) {
            this.x[i] = x[i];
            this.y[i] = y[i];
            this.z[i] = z[i];
            this.m[i] = m[i];
        }

        //Check the dimensionality
        boolean isZPresent = checkDimensionality(this.z);

        boolean isMPresent = checkDimensionality(this.m);

        if (!isZPresent && !isMPresent) {
            this.coordinateDimension = 2;
            this.spatialDimension = 2;
            this.dimensions = CoordinateSequenceDimensions.XY;
            this.measuresDimension = 0;
        } else if (isZPresent && !isMPresent) {
            this.coordinateDimension = 3;
            this.spatialDimension = 3;
            this.dimensions = CoordinateSequenceDimensions.XYZ;
            this.measuresDimension = 0;
        } else if (!isZPresent && isMPresent) {
            this.coordinateDimension = 3;
            this.spatialDimension = 2;
            this.dimensions = CoordinateSequenceDimensions.XYM;
            this.measuresDimension = 1;
        } else {
            this.coordinateDimension = 4;
            this.spatialDimension = 3;
            this.dimensions = CoordinateSequenceDimensions.XYZM;
            this.measuresDimension = 1;
        }

    }

    private boolean checkDimensionality(double[] dim) {

        if (dim.length > 0) {
            return !Double.isNaN(dim[0]);
        } else {
            return false;
        }

    }

    @Override
    public int getDimension() {
        return coordinateDimension;
    }

    @Override
    public int getMeasures() {
        return measuresDimension;
    }

    @Override
    public boolean hasZ() {
        return spatialDimension > 2;
    }

    @Override
    public boolean hasM() {
        return measuresDimension == 1;
    }

    public CoordinateSequenceDimensions getDimensions() {
        return dimensions;
    }

    public int getSpatialDimension() {
        return spatialDimension;
    }

    @Override
    public Coordinate getCoordinate(int i) {
        return new Coordinate(x[i], y[i], z[i]);
    }

    @Override
    public Coordinate getCoordinateCopy(int i) {
        return new Coordinate(x[i], y[i], z[i]);
    }

    @Override
    public void getCoordinate(int index, Coordinate coord) {
        coord.setX(x[index]);
        coord.setY(y[index]);
        coord.setZ(z[index]);
    }

    @Override
    public double getX(int index) {
        return x[index];
    }

    @Override
    public double getY(int index) {
        return y[index];
    }

    @Override
    public double getZ(int index) {
        return z[index];
    }

    @Override
    public double getM(int index) {
        return m[index];
    }

    @Override
    public double getOrdinate(int index, int ordinateIndex) {
        switch (ordinateIndex) {
            case X:
                return x[index];
            case Y:
                return y[index];
            case Z:
                return z[index];
            case M:
                return m[index];
        }
        return Double.NaN;
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public void setOrdinate(int index, int ordinateIndex, double value) {

        switch (ordinateIndex) {
            case X:
                x[index] = value;
            case Y:
                y[index] = value;
            case Z:
                z[index] = value;
            case M:
                m[index] = value;
        }
    }

    @Override
    public Coordinate[] toCoordinateArray() {

        Coordinate[] coordinates = new Coordinate[size];

        for (int i = 0; i < size; i++) {
            coordinates[i] = new Coordinate(x[i], y[i], z[i]);
        }

        return coordinates;
    }

    @Override
    public Envelope expandEnvelope(Envelope env) {
        Envelope newEnv = new Envelope(env);

        for (int i = 0; i < size; i++) {
            newEnv.expandToInclude(x[i], y[i]);
        }

        return newEnv;
    }

    @Override
    @Deprecated
    public CustomCoordinateSequence clone() {
        return new CustomCoordinateSequence(x, y, z, m);
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 67 * hash + Arrays.hashCode(this.x);
        hash = 67 * hash + Arrays.hashCode(this.y);
        hash = 67 * hash + Arrays.hashCode(this.z);
        hash = 67 * hash + Arrays.hashCode(this.m);
        hash = 67 * hash + this.size;
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
        final CustomCoordinateSequence other = (CustomCoordinateSequence) obj;
        if (this.size != other.size) {
            return false;
        }
        if (!Arrays.equals(this.x, other.x)) {
            return false;
        }
        if (!Arrays.equals(this.y, other.y)) {
            return false;
        }
        if (!Arrays.equals(this.z, other.z)) {
            return false;
        }
        return Arrays.equals(this.m, other.m);
    }

    @Override
    public String toString() {
        return "CustomCoordinateSequence{" + "x=" + Arrays.toString(x) + ", y=" + Arrays.toString(y) + ", z=" + Arrays.toString(z) + ", m=" + Arrays.toString(m) + ", size=" + size + '}';
    }

    public String getCoordinateText(int index) {

        StringBuilder sb = new StringBuilder();

        String xValue = reducePrecision(x[index]);
        String yValue = reducePrecision(y[index]);
        String zValue;
        String mValue;
        switch (dimensions) {
            case XY:
                sb.append(xValue).append(" ").append(yValue);
                break;
            case XYZ:
                zValue = reducePrecision(z[index]);
                sb.append(xValue).append(" ").append(yValue).append(" ").append(zValue);
                break;
            case XYM:
                mValue = reducePrecision(m[index]);
                sb.append(xValue).append(" ").append(yValue).append(" ").append(mValue);
                break;
            default:
                zValue = reducePrecision(z[index]);
                mValue = reducePrecision(m[index]);
                sb.append(xValue).append(" ").append(yValue).append(" ").append(zValue).append(" ").append(mValue);
                break;

        }
        return sb.toString();
    }

    public static final CoordinateSequenceDimensions findCoordinateSequenceDimensions(int coordinateDimension, int spatialDimension) {
        if (coordinateDimension == 2 && spatialDimension == 2) {
            return CoordinateSequenceDimensions.XY;
        } else if (coordinateDimension == 3 && spatialDimension == 3) {
            return CoordinateSequenceDimensions.XYZ;
        } else if (coordinateDimension == 3 && spatialDimension == 2) {
            return CoordinateSequenceDimensions.XYM;
        } else {
            return CoordinateSequenceDimensions.XYZM;
        }
    }

}
