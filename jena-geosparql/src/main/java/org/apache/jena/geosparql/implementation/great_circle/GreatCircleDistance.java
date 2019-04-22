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
package org.apache.jena.geosparql.implementation.great_circle;

import org.apache.jena.geosparql.implementation.UnitsOfMeasure;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Point;

/**
 *
 *
 */
public class GreatCircleDistance {

    /**
     * Great circle distance between Lat/Lon Points using Vincenty formula.
     *
     * @param point1 LatLon Point in degrees.
     * @param point2 LatLon Point in degrees.
     * @return Distance in metres.
     */
    public static final double vincentyFormula(LatLonPoint point1, LatLonPoint point2) {
        //Based on Vincenty formula: https://en.wikipedia.org/wiki/Great-circle_distance
        double lat1 = point1.getLat();
        double lon1 = point1.getLon();

        double lat2 = point2.getLat();
        double lon2 = point2.getLon();
        return vincentyFormula(lat1, lon1, lat2, lon2);
    }

    /**
     * Great circle distance between Points (x is Lon, y is Lat) using Vincenty
     * formula.
     *
     * @param point1 Point in degrees.
     * @param point2 Point in degrees.
     * @return Distance in metres.
     */
    public static final double vincentyFormula(Point point1, Point point2) {
        //Based on Vincenty formula: https://en.wikipedia.org/wiki/Great-circle_distance
        double lat1 = point1.getY();
        double lon1 = point1.getX();

        double lat2 = point2.getY();
        double lon2 = point2.getX();
        return vincentyFormula(lat1, lon1, lat2, lon2);
    }

    /**
     * Great circle distance between Points (x is Lon, y is Lat) using Vincenty
     * formula.
     *
     * @param coord1 Point in degrees.
     * @param coord2 Point in degrees.
     * @return Distance in metres.
     */
    public static final double vincentyFormula(Coordinate coord1, Coordinate coord2) {
        //Based on Vincenty formula: https://en.wikipedia.org/wiki/Great-circle_distance
        double lat1 = coord1.getY();
        double lon1 = coord1.getX();

        double lat2 = coord2.getY();
        double lon2 = coord2.getX();
        return vincentyFormula(lat1, lon1, lat2, lon2);
    }

    /**
     * Great circle distance between Points using Vincenty formula.
     *
     * @param lat1 Lat in degrees of first point.
     * @param lon1 Lon in degrees of first point.
     * @param lat2 Lat in degrees of second point.
     * @param lon2 Lon in degrees of second point.
     * @return Distance in metres.
     */
    public static final double vincentyFormula(double lat1, double lon1, double lat2, double lon2) {
        double lat1Rad = Math.toRadians(lat1);
        double lat2Rad = Math.toRadians(lat2);

        //double diffLatRad = Math.toRadians(lat2 - lat1);
        double diffLonRad = Math.toRadians(lon2 - lon1);

        double a = Math.pow(Math.cos(lat2Rad) * Math.sin(diffLonRad), 2);
        double b = Math.pow(Math.cos(lat1Rad) * Math.sin(lat2Rad) - Math.sin(lat1Rad) * Math.cos(lat2Rad) * Math.cos(diffLonRad), 2);

        double c = Math.sqrt(a + b);
        double d = Math.sin(lat1Rad) * Math.sin(lat2Rad) + Math.cos(lat1Rad) * Math.cos(lat2Rad) * Math.cos(diffLonRad);

        double e = Math.atan(c / d);

        double distance = UnitsOfMeasure.EARTH_MEAN_RADIUS * e;

        //Distance is in metres.
        return distance;
    }

    /**
     * Great circle distance between Lat/Lon Points using Haversine formula.
     *
     * @param point1 LatLon Point in degrees.
     * @param point2 LatLon Point in degrees.
     * @return Distance in metres.
     */
    public static final double haversineFormula(LatLonPoint point1, LatLonPoint point2) {
        double lat1 = point1.getLat();
        double lon1 = point1.getLon();

        double lat2 = point2.getLat();
        double lon2 = point2.getLon();

        return haversineFormula(lat1, lon1, lat2, lon2);
    }

    /**
     * Great circle distance between Points (x is Lon, y is Lat) using Haversine
     * formula.
     *
     * @param point1 Point in degrees.
     * @param point2 Point in degrees.
     * @return Distance in metres.
     */
    public static final double haversineFormula(Point point1, Point point2) {
        //Based on Vincenty formula: https://en.wikipedia.org/wiki/Great-circle_distance
        double lat1 = point1.getY();
        double lon1 = point1.getX();

        double lat2 = point2.getY();
        double lon2 = point2.getX();
        return vincentyFormula(lat1, lon1, lat2, lon2);
    }

    /**
     * Great circle distance between Points (x is Lon, y is Lat) using Haversine
     * formula.
     *
     * @param coord1 Point in degrees.
     * @param coord2 Point in degrees.
     * @return Distance in metres.
     */
    public static final double haversineFormula(Coordinate coord1, Coordinate coord2) {
        //Based on Vincenty formula: https://en.wikipedia.org/wiki/Great-circle_distance
        double lat1 = coord1.getY();
        double lon1 = coord1.getX();

        double lat2 = coord2.getY();
        double lon2 = coord2.getX();
        return vincentyFormula(lat1, lon1, lat2, lon2);
    }

    /**
     * Great circle distance between Points using Haversine formula.
     *
     * @param lat1 Lat in degrees of first point.
     * @param lon1 Lon in degrees of first point.
     * @param lat2 Lat in degrees of second point.
     * @param lon2 Lon in degrees of second point.
     * @return Distance in metres.
     */
    public static final double haversineFormula(double lat1, double lon1, double lat2, double lon2) {
        //Based on Haversine formula: https://www.movable-type.co.uk/scripts/latlong.html
        //Apparently there are inaccurcies for distances of points on opposite sides of the sphere so prefer Vincenty formula.
        double lat1Rad = Math.toRadians(lat1);
        double lat2Rad = Math.toRadians(lat2);

        double diffLatRad = Math.toRadians(lat2 - lat1);
        double diffLonRad = Math.toRadians(lon2 - lon1);

        double a = Math.pow(Math.sin(diffLatRad / 2), 2)
                + Math.cos(lat1Rad) * Math.cos(lat2Rad)
                * Math.pow(Math.sin(diffLonRad / 2), 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        double distance = UnitsOfMeasure.EARTH_MEAN_RADIUS * c;
        return distance;
    }

}
