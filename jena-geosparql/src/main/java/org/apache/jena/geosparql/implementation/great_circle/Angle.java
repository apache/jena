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
package org.apache.jena.geosparql.implementation.great_circle;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Point;

/**
 *
 *
 */
public class Angle {

    private static final double HALF_PI = Math.PI / 2;
    private static final double PI_AND_HALF = Math.PI + HALF_PI;

    /**
     * Angle in radians with y-axis being 0 in clockwise direction.
     *
     * @param x1
     * @param y1
     * @param x2
     * @param y2
     * @return Angle from y-axis in 0 to 2π radians.
     */
    public static final double find(double x1, double y1, double x2, double y2) {

        //α = arccos [(b² + c² - a²)/(2bc)]
        double numer = Math.abs(y1 - y2);
        double denom = Math.hypot(x1 - x2, numer);
        double result = Math.acos(numer / denom);

        //Adjust the result around the four quadrants based on relative position of orginal coordinates.
        //Not found an equation that doesn't shift the origin.
        boolean y2Small = y2 < y1;
        boolean x2Small = x2 < x1;
        if (y2Small && x2Small) {
            result += Math.PI;
        } else if (y2Small && !x2Small) {
            result += HALF_PI;
        } else if (x2Small) {
            result += PI_AND_HALF;
        }

        return result;
    }

    /**
     * Angle in radians with y-axis being 0 in clockwise direction.<br>
     * LatLon Point may be more useful using Azimuth.
     *
     * @param point1 LatLon Point in degrees.
     * @param point2 LatLon Point in degrees.
     * @return Angle from y-axis in 0 to 2π radians.
     */
    public static final double find(LatLonPoint point1, LatLonPoint point2) {
        //Lon is X, Lat is Y.
        double lat1 = point1.getLat();
        double lon1 = point1.getLon();

        double lat2 = point2.getLat();
        double lon2 = point2.getLon();
        return find(lon1, lat1, lon2, lat2);
    }

    /**
     * Angle in radians with y-axis being 0 in clockwise direction.
     *
     * @param point1 Point in degrees.
     * @param point2 Point in degrees.
     * @return Angle from y-axis in 0 to 2π radians.
     */
    public static final double find(Point point1, Point point2) {
        double x1 = point1.getX();
        double y1 = point1.getY();

        double x2 = point2.getX();
        double y2 = point2.getY();
        return find(x1, y1, x2, y2);
    }

    /**
     * Angle in radians with y-axis being 0 in clockwise direction.
     *
     * @param coord1 Point in degrees.
     * @param coord2 Point in degrees.
     * @return Angle from y-axis in 0 to 2π radians.
     */
    public static final double find(Coordinate coord1, Coordinate coord2) {
        double x1 = coord1.getX();
        double y1 = coord1.getY();

        double x2 = coord2.getX();
        double y2 = coord2.getY();
        return find(x1, y1, x2, y2);
    }
}
