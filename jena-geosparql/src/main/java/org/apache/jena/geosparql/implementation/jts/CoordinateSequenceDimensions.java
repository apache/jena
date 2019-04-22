/*
 * Copyright 2019 the original author or authors.
 * See the notice.md file distributed with this work for additional
 * information regarding copyright ownership.
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
package org.apache.jena.geosparql.implementation.jts;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.CoordinateXY;
import org.locationtech.jts.geom.CoordinateXYM;
import org.locationtech.jts.geom.CoordinateXYZM;

/**
 *
 *
 */
public enum CoordinateSequenceDimensions {
    XY, XYZ, XYZM, XYM;

    public static final CoordinateSequenceDimensions find(Coordinate coordinate) {
        if (coordinate instanceof CoordinateXY) {
            return XY;
        } else if (coordinate instanceof CoordinateXYM) {
            return XYM;
        } else if (coordinate instanceof CoordinateXYZM) {
            return XYZM;
        }

        //Coordinate can be either XY or XYZ.
        if (Double.isNaN(coordinate.getZ())) {
            return XY;
        } else {
            return XYZ;
        }
    }

    public static CoordinateSequenceDimensions find(int srsDimension) {

        switch (srsDimension) {
            case 4:
                return CoordinateSequenceDimensions.XYZM;
            case 3:
                return CoordinateSequenceDimensions.XYZ;
            default:
                return CoordinateSequenceDimensions.XY;
        }
    }

    public static String convertDimensions(final CoordinateSequenceDimensions dimensions) {

        switch (dimensions) {
            case XYZ:
                return " Z";
            case XYM:
                return " M";
            case XYZM:
                return " ZM";
            default:
                return "";
        }
    }

    public static int convertToInt(final CoordinateSequenceDimensions dimensions) {

        switch (dimensions) {
            case XYZ:
                return 3;
            case XYM:
                return 3;
            case XYZM:
                return 4;
            default:
                return 2;
        }
    }

}
