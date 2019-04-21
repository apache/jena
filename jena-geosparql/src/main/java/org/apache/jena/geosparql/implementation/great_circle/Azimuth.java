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
public class Azimuth {

    private static final double RADIANS_ADJUST = 2 * Math.PI;

    /**
     * Forward azimuth in radians with North being 0 in clockwise direction.
     *
     * @param lat1 Lat in degrees of first point.
     * @param lon1 Lon in degrees of first point.
     * @param lat2 Lat in degrees of second point.
     * @param lon2 Lon in degrees of second point.
     * @return Azimuth from North in 0 to 2π radians.
     */
    public static final double find(double lat1, double lon1, double lat2, double lon2) {

        double lat1Rad = Math.toRadians(lat1);
        double lat2Rad = Math.toRadians(lat2);

        //Δλ
        double diffLonRad = Math.toRadians(lon2 - lon1);

        //https://www.omnicalculator.com/other/azimuth#how-to-calculate-the-azimuth-from-latitude-and-longitude
        //λ is lon, φ is lat.
        //θ = atan2 [(sin Δλ * cos φ2), (cos φ1 * sin φ2 − sin φ1 * cos φ2 * cos Δλ)]
        double x = Math.cos(lat1Rad) * Math.sin(lat2Rad) - Math.sin(lat1Rad) * Math.cos(lat2Rad) * Math.cos(diffLonRad);
        double y = Math.sin(diffLonRad) * Math.cos(lat2Rad);
        double azimuth = Math.atan2(y, x);
        if (azimuth < 0) {
            return azimuth + RADIANS_ADJUST;
        } else {
            return azimuth;
        }
    }

    /**
     * Forward azimuth in radians with North being 0 in clockwise direction.
     *
     * @param point1 LatLon Point in degrees.
     * @param point2 LatLon Point in degrees.
     * @return Azimuth from North in 0 to 2π radians.
     */
    public static final double find(LatLonPoint point1, LatLonPoint point2) {
        double lat1 = point1.getLat();
        double lon1 = point1.getLon();

        double lat2 = point2.getLat();
        double lon2 = point2.getLon();
        return find(lat1, lon1, lat2, lon2);
    }

    /**
     * Forward azimuth in radians with North being 0 in clockwise direction.
     *
     * @param point1 Point in degrees.
     * @param point2 Point in degrees.
     * @return Azimuth from North in 0 to 2π radians.
     */
    public static final double find(Point point1, Point point2) {
        double lat1 = point1.getY();
        double lon1 = point1.getX();

        double lat2 = point2.getY();
        double lon2 = point2.getX();
        return find(lat1, lon1, lat2, lon2);
    }

    /**
     * Forward azimuth in radians with North being 0 in clockwise direction.
     *
     * @param coord1 Point in degrees.
     * @param coord2 Point in degrees.
     * @return Azimuth from North in 0 to 2π radians.
     */
    public static final double find(Coordinate coord1, Coordinate coord2) {
        double lat1 = coord1.getY();
        double lon1 = coord1.getX();

        double lat2 = coord2.getY();
        double lon2 = coord2.getX();
        return find(lat1, lon1, lat2, lon2);
    }

}
