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
package org.apache.jena.geosparql.spatial;

import java.lang.invoke.MethodHandles;
import java.util.HashSet;
import java.util.Objects;
import org.apache.jena.geosparql.implementation.GeometryWrapper;
import org.apache.jena.geosparql.implementation.SRSInfo;
import org.apache.jena.geosparql.implementation.UnitsOfMeasure;
import org.apache.jena.geosparql.implementation.great_circle.GreatCirclePointDistance;
import org.apache.jena.geosparql.implementation.great_circle.LatLonPoint;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.sparql.expr.ExprEvalException;
import org.locationtech.jts.geom.Envelope;
import org.opengis.geometry.MismatchedDimensionException;
import org.opengis.referencing.operation.TransformException;
import org.opengis.util.FactoryException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 *
 */
public class SearchEnvelope {

    private final Envelope mainEnvelope;
    private final Envelope wrapEnvelope;
    private final SRSInfo srsInfo;

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    protected SearchEnvelope(Envelope envelope, SRSInfo srsInfo) {
        this.srsInfo = srsInfo;

        //Find the valid range for the envelope.
        Envelope domainEnvelope = srsInfo.getDomainEnvelope();
        double domMinX = domainEnvelope.getMinX();
        double domMaxX = domainEnvelope.getMaxX();

        if (srsInfo.isGeographic()) {

            double domRange = srsInfo.getDomainRangeX();

            //Check whether Envelope exceeds bounds so requires wrapping.
            double minX = envelope.getMinX();
            double maxX = envelope.getMaxX();

            if (minX < domMinX) {
                this.mainEnvelope = new Envelope(domMinX, maxX, envelope.getMinY(), envelope.getMaxY());
                this.wrapEnvelope = new Envelope(minX + domRange, domMaxX, envelope.getMinY(), envelope.getMaxY());
            } else if (maxX > domMaxX) {
                this.mainEnvelope = new Envelope(minX, domMaxX, envelope.getMinY(), envelope.getMaxY());
                this.wrapEnvelope = new Envelope(domMinX, maxX - domRange, envelope.getMinY(), envelope.getMaxY());
            } else {
                this.mainEnvelope = envelope;
                this.wrapEnvelope = null;
            }

            double envRange = Math.abs(minX) + Math.abs(maxX);
            if (envRange > domRange * 2) {
                //Will trigger if the envelope tries to wrap around twice.
                LOGGER.warn("Search Envelope {} is outside of valid domain {} for SRS URI {}.", envelope, domainEnvelope, srsInfo.getSrsURI());
            }
        } else {
            //No wrapping required, accept the provided envelope.
            this.mainEnvelope = envelope;
            this.wrapEnvelope = null;
            double minX = envelope.getMinX();
            double maxX = envelope.getMaxX();
            if (minX < domMinX || maxX > domMaxX) {
                LOGGER.warn("Search Envelope {} is outside of valid domain {} for SRS URI {}.", envelope, domainEnvelope, srsInfo.getSrsURI());
            }
        }

    }

    public Envelope getMainEnvelope() {
        return mainEnvelope;
    }

    public Envelope getWrapEnvelope() {
        return wrapEnvelope;
    }

    public String getSrsURI() {
        return srsInfo.getSrsURI();
    }

    public SRSInfo getCrsInfo() {
        return srsInfo;
    }

    public HashSet<Resource> check(SpatialIndex spatialIndex) {
        HashSet<Resource> features = spatialIndex.query(mainEnvelope);

        if (wrapEnvelope != null) {
            HashSet<Resource> wrapFeatures = spatialIndex.query(wrapEnvelope);
            features.addAll(wrapFeatures);
        }
        return features;
    }

    public boolean check(Envelope envelope) {
        boolean result = mainEnvelope.intersects(envelope);

        if (!result && wrapEnvelope != null) {
            result = wrapEnvelope.intersects(envelope);
        }
        return result;
    }

    @Override
    public String toString() {
        return "SearchEnvelope{" + "envelope=" + mainEnvelope + ", wrappedEnvelope=" + wrapEnvelope + '}';
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 47 * hash + Objects.hashCode(this.mainEnvelope);
        hash = 47 * hash + Objects.hashCode(this.wrapEnvelope);
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
        final SearchEnvelope other = (SearchEnvelope) obj;
        if (!Objects.equals(this.mainEnvelope, other.mainEnvelope)) {
            return false;
        }
        return Objects.equals(this.wrapEnvelope, other.wrapEnvelope);
    }

    public static SearchEnvelope build(GeometryWrapper geometryWrapper, SRSInfo srsInfo, double radius, String unitsURI) {

        try {
            //Get the envelope of the target GeometryWrapper and convert to SRS URI, in case it is a complex polygon.
            GeometryWrapper envelopeGeometryWrapper = geometryWrapper.envelope();
            //Convert to SRS URI.
            GeometryWrapper srsGeometryWrapper = envelopeGeometryWrapper.convertSRS(srsInfo.getSrsURI());
            Envelope envelope = srsGeometryWrapper.getEnvelope();

            //Expand the envelope by the radius distance in all directions,
            //i.e. a bigger box rather than circle. More precise checks made later.
            SearchEnvelope searchEnvelope;

            if (srsInfo.isGeographic()) {
                double radiusMetres = UnitsOfMeasure.convertToMetres(radius, unitsURI, srsGeometryWrapper.getLatitude());
                searchEnvelope = expandGeographicEnvelope(envelope, radiusMetres, srsInfo);
            } else {
                String targetUnitsURI = srsInfo.getUnitsOfMeasure().getUnitURI();
                double targetRadius = UnitsOfMeasure.conversion(radius, unitsURI, targetUnitsURI);
                searchEnvelope = expandEnvelope(envelope, targetRadius, srsInfo);
            }
            return searchEnvelope;
        } catch (FactoryException | MismatchedDimensionException | TransformException ex) {
            throw new ExprEvalException(ex.getMessage() + ": " + geometryWrapper.asLiteral(), ex);
        }
    }

    private static final double NORTH_BEARING = 0;
    private static final double EAST_BEARING = Math.toRadians(90);
    private static final double SOUTH_BEARING = Math.toRadians(180);
    private static final double WEST_BEARING = Math.toRadians(270);

    private static SearchEnvelope expandGeographicEnvelope(Envelope envelope, double distanceMetres, SRSInfo srsInfo) {
        //Travel out by the radius in the cardinal directions.

        //Envelope is in X/Y coordinate order.
        double minLon = envelope.getMinX();
        double minLat = envelope.getMinY();
        double maxLon = envelope.getMaxX();
        double maxLat = envelope.getMaxY();

        //Find the extreme values for Lat and Lon.
        double extLat = Math.abs(maxLat) > Math.abs(minLat) ? maxLat : minLat;
        double extLon = Math.abs(maxLon) > Math.abs(minLon) ? maxLon : minLon;

        //Find the greatest change: in North then use North bearing or in South then use South bearing.
        double latBearing;
        if (extLat > 0) {
            latBearing = NORTH_BEARING;
        } else {
            latBearing = SOUTH_BEARING;
        }

        //Find the greatest change: in East then use East bearing or in West then use West bearing.
        double lonBearing;
        if (extLon > 0) {
            lonBearing = EAST_BEARING;
        } else {
            lonBearing = WEST_BEARING;
        }

        //Find the new latitiude and longitude by moving the radius distance.
        //Splitting the calculation will oversize the bounding box by up to a few kilometres for a 100km distance.
        //However, only calculating what is needed. More precise checks done later.
        GreatCirclePointDistance pointDistance = new GreatCirclePointDistance(extLat, extLon, distanceMetres);
        double latRad = pointDistance.latitude(latBearing);
        double lonRad = pointDistance.longitude(latRad, lonBearing);

        LatLonPoint point = GreatCirclePointDistance.radToPoint(latRad, lonRad, false);

        //Find the difference between the outer point and the extreme values.
        double latDiff = Math.abs(extLat - point.getLat());
        double lonDiff = Math.abs(extLon - point.getLon());

        //Find differences of the longitude and wrap the values if required.
        double normMinLon = GreatCirclePointDistance.normaliseLongitude(minLon - lonDiff);
        double normMaxLon = GreatCirclePointDistance.normaliseLongitude(maxLon + lonDiff);

        //Apply the differences to expand the envelope.
        Envelope expandedEnvelope = new Envelope(normMinLon, normMaxLon, minLat - latDiff, maxLat + latDiff);

        SearchEnvelope searchEnvelope = new SearchEnvelope(expandedEnvelope, srsInfo);
        return searchEnvelope;
    }

    private static SearchEnvelope expandEnvelope(Envelope envelope, double distance, SRSInfo srsInfo) {
        //Travel out by the radius in the cardinal directions.

        //Envelope is in X/Y coordinate order.
        double x1 = envelope.getMinX() - distance;
        double y1 = envelope.getMinY() - distance;
        double x2 = envelope.getMaxX() + distance;
        double y2 = envelope.getMaxY() + distance;

        Envelope expandedEnvelope = new Envelope(x1, x2, y1, y2);

        SearchEnvelope searchEnvelope = new SearchEnvelope(expandedEnvelope, srsInfo);
        return searchEnvelope;
    }

    /**
     *
     * @param geometryWrapper
     * @param srsInfo
     * @return Search envelope of the geometry in target SRS.
     */
    public static SearchEnvelope build(GeometryWrapper geometryWrapper, SRSInfo srsInfo) {

        try {
            //Get the envelope of the target GeometryWrapper and convert that to SRS URI, in case it is a complex polygon.
            GeometryWrapper envelopeGeometryWrapper = geometryWrapper.envelope();

            //Convert to SRS URI.
            GeometryWrapper srsGeometryWrapper = envelopeGeometryWrapper.convertSRS(srsInfo.getSrsURI());

            Envelope envelope = srsGeometryWrapper.getEnvelope();
            SearchEnvelope searchEnvelope = new SearchEnvelope(envelope, srsInfo);
            return searchEnvelope;
        } catch (FactoryException | MismatchedDimensionException | TransformException ex) {
            throw new ExprEvalException(ex.getMessage() + ": " + geometryWrapper.asLiteral(), ex);
        }
    }

    /**
     * Build search envelope in the indicated cardinal direction.<br>
     * Geographic SRS will wrap for half world in East/West directions.<br>
     * Other SRS will extend to the valid domain.
     *
     * @param geometryWrapper
     * @param srsInfo
     * @param direction
     * @return Search envelope in cardinal direction.
     */
    public static SearchEnvelope build(GeometryWrapper geometryWrapper, SRSInfo srsInfo, CardinalDirection direction) {

        try {
            //Get the envelope of the target GeometryWrapper and convert to SRS URI, in case it is a complex polygon.
            GeometryWrapper envelopeGeometryWrapper = geometryWrapper.envelope();
            //Convert to SRS URI.
            GeometryWrapper srsGeometryWrapper = envelopeGeometryWrapper.convertSRS(srsInfo.getSrsURI());
            Envelope envelope = srsGeometryWrapper.getEnvelope();

            Envelope domEnvelope = srsInfo.getDomainEnvelope();

            double x1 = domEnvelope.getMinX();
            double x2 = domEnvelope.getMaxX();
            double y1 = domEnvelope.getMinY();
            double y2 = domEnvelope.getMaxY();

            //Exclusive search so anything within envelope of a LineString or Polygon is excluded.
            switch (direction) {
                case NORTH:
                    y1 = envelope.getMaxY();
                    break;
                case SOUTH:
                    y2 = envelope.getMinY();
                    break;
                case EAST:
                    x1 = envelope.getMaxX();
                    if (srsInfo.isGeographic()) {
                        //Extend to the Eastern half from the origin.
                        x2 = x1 + domEnvelope.getMaxX();
                    }

                    break;
                case WEST:
                    x2 = envelope.getMinX();
                    if (srsInfo.isGeographic()) {
                        //Extend to the West half from the origin.
                        x1 = x2 + domEnvelope.getMinX();
                    }
                    break;
            }

            Envelope cardinalEnvelope = new Envelope(x1, x2, y1, y2);

            return new SearchEnvelope(cardinalEnvelope, srsInfo);
        } catch (FactoryException | MismatchedDimensionException | TransformException ex) {
            throw new ExprEvalException(ex.getMessage() + ": " + geometryWrapper.asLiteral(), ex);
        }
    }

}
