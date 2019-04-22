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

/**
 * Determines final Latitude and Longitude based on a bearing.<br>
 * Constant values are calculated once to avoid repetition.<br>
 * Latitude and longitude calculated in stages to avoid unnecessary
 * calculations.
 *
 */
public class GreatCirclePointDistance {

    private final double distance;
    private final double angDistance;
    private final double startLat;
    private final double startLon;
    private final double latRad;
    private final double lonRad;
    private final double sinStartLat;
    private final double cosStartLat;
    private final double sinAngDistance;
    private final double cosAngDistance;

    public GreatCirclePointDistance(double startLat, double startLon, double distance) {
        this.distance = distance;
        this.startLat = startLat;
        this.startLon = startLon;

        this.angDistance = distance / UnitsOfMeasure.EARTH_MEAN_RADIUS;
        this.latRad = Math.toRadians(startLat);
        this.lonRad = Math.toRadians(startLon);

        this.sinStartLat = Math.sin(latRad);
        this.cosStartLat = Math.cos(latRad);
        this.sinAngDistance = Math.sin(angDistance);
        this.cosAngDistance = Math.cos(angDistance);
    }

    public double getDistance() {
        return distance;
    }

    public double getAngDistance() {
        return angDistance;
    }

    public double getStartLat() {
        return startLat;
    }

    public double getStartLon() {
        return startLon;
    }

    public double getLatRad() {
        return latRad;
    }

    public double getLonRad() {
        return lonRad;
    }

    public double getSinStartLat() {
        return sinStartLat;
    }

    public double getCosStartLat() {
        return cosStartLat;
    }

    public double getSinAngDistance() {
        return sinAngDistance;
    }

    public double getCosAngDistance() {
        return cosAngDistance;
    }

    @Override
    public String toString() {
        return "GreatCirclePointDistance{" + "distance=" + distance + ", angDistance=" + angDistance + ", startLat=" + startLat + ", startLon=" + startLon + ", latRad=" + latRad + ", lonRad=" + lonRad + ", sinStartLat=" + sinStartLat + ", cosStartLat=" + cosStartLat + ", sinAngDistance=" + sinAngDistance + ", cosAngDistance=" + cosAngDistance + '}';
    }

    public double latitude(double bearingRad) {
        return Math.asin(sinStartLat * cosAngDistance + cosStartLat * sinAngDistance * Math.cos(bearingRad));
    }

    public double longitude(double endLatRad, double bearingRad) {
        return lonRad + Math.atan2(Math.sin(bearingRad) * sinAngDistance * cosStartLat, cosAngDistance - sinStartLat * Math.sin(endLatRad));
    }

    /**
     * Convert Lat/Lon in radians to Point in degrees.<br>
     * Longitude normalised between -180 and 180.
     *
     * @param latRad
     * @param lonRad
     * @return Lat/Lon Point in degrees.
     */
    public static final LatLonPoint radToPoint(double latRad, double lonRad) {
        return radToPoint(latRad, lonRad, true);
    }

    /**
     * Convert Lat/Lon in radians to Point in degrees.
     *
     * @param latRad
     * @param lonRad
     * @param isNormaliseLon Normalise Longitude between -180 and 180.
     * @return Lat/Lon Point in degrees.
     */
    public static final LatLonPoint radToPoint(double latRad, double lonRad, boolean isNormaliseLon) {
        double lat = Math.toDegrees(latRad);
        double lon = Math.toDegrees(lonRad);
        if (isNormaliseLon) {
            lon = normaliseLongitude(lon);
        }

        LatLonPoint point = new LatLonPoint(lat, lon);
        return point;
    }

    /**
     * Normalise Longitude in degrees to the range -180 to 180.
     *
     * @param lonDegrees
     * @return Lat/Lon Point in degrees.
     */
    public static double normaliseLongitude(double lonDegrees) {
        if (lonDegrees > 180) {
            return lonDegrees - 360;
        } else if (lonDegrees < -180) {
            return lonDegrees + 380;
        }
        return lonDegrees;

    }

    /**
     * Lat/Lon Point from start Lat/Lon Point following bearing degrees
     * (clockwise from north) and distance (metres).
     *
     * @param startPoint
     * @param distance
     * @param bearing
     * @return Lat/Lon point in the distance and bearing from start point.
     */
    public static final LatLonPoint getPoint(LatLonPoint startPoint, double distance, double bearing) {

        double startLat = startPoint.getLat();
        double startLon = startPoint.getLon();

        return GreatCirclePointDistance.getPoint(startLat, startLon, distance, bearing);
    }

    /**
     * Lat/Lon Point from start Lat/x,Lon/y following bearing degrees (clockwise
     * from north) and distance (metres).
     *
     * @param startLat
     * @param startLon
     * @param distance
     * @param bearing
     * @return Lat/Lon point in the distance and bearing from start point.
     */
    public static final LatLonPoint getPoint(double startLat, double startLon, double distance, double bearing) {
        //Based on: https://www.movable-type.co.uk/scripts/latlong.html
        double bearingRad = Math.toRadians(bearing);
        GreatCirclePointDistance pointDistance = new GreatCirclePointDistance(startLat, startLon, distance);

        double endLatRad = pointDistance.latitude(bearingRad);

        double endLonRad = pointDistance.longitude(endLatRad, bearingRad);

        LatLonPoint endPoint = radToPoint(endLatRad, endLonRad);
        return endPoint;
    }

}
