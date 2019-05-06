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
package org.apache.jena.geosparql.implementation;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import org.apache.jena.geosparql.implementation.vocabulary.SRS_URI;
import static org.apache.jena.geosparql.implementation.vocabulary.SRS_URI.EPSG_BASE_SRS_URI;
import org.apache.sis.referencing.CRS;
import org.locationtech.jts.geom.Envelope;
import org.opengis.geometry.DirectPosition;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.crs.GeographicCRS;
import org.opengis.referencing.cs.AxisDirection;
import org.opengis.util.FactoryException;

/**
 *
 *
 */
public class SRSInfo {

    private final String srsURI;
    private final CoordinateReferenceSystem crs;
    private final UnitsOfMeasure unitsOfMeasure;
    private final Boolean isAxisXY;
    private final Boolean isGeographic;
    private final Boolean isSRSRecognised;
    private final Boolean isWktDefault;
    private final Envelope domainEnvelope;
    private final double domainRangeX;

    public static final String DEFAULT_WKT_CRS84_CODE = "CRS:84";
    public static final SRSInfo DEFAULT_WKT_CRS84 = getDefaultWktCRS84(SRS_URI.DEFAULT_WKT_CRS84);

    private static final List<AxisDirection> OTHER_Y_AXIS_DIRECTIONS = Arrays.asList(AxisDirection.NORTH_EAST, AxisDirection.NORTH_WEST, AxisDirection.SOUTH_EAST, AxisDirection.SOUTH_WEST, AxisDirection.NORTH_NORTH_EAST, AxisDirection.NORTH_NORTH_WEST, AxisDirection.SOUTH_SOUTH_EAST, AxisDirection.SOUTH_SOUTH_WEST);

    public SRSInfo(String srsURI) throws SRSInfoException {
        this.srsURI = srsURI;

        try {
            this.crs = CRS.forCode(srsURI);
            this.isAxisXY = checkAxisXY(crs);
            this.unitsOfMeasure = new UnitsOfMeasure(crs);
            this.isSRSRecognised = true;
            this.isGeographic = crs instanceof GeographicCRS;
            this.isWktDefault = srsURI.equals(SRS_URI.DEFAULT_WKT_CRS84);
            this.domainEnvelope = buildDomainEnvelope(crs, isAxisXY);
            this.domainRangeX = Math.abs(domainEnvelope.getMinX()) + Math.abs(domainEnvelope.getMaxX());
        } catch (FactoryException ex) {
            throw new SRSInfoException("Unrecognised SRS URI code: " + srsURI + " - " + ex.getMessage(), ex);
        }
    }

    /**
     * SRID will be converted to EPSG URI:
     * http://www.opengis.net/def/crs/EPSG/0/srid.
     *
     * @param srid
     */
    public SRSInfo(int srid) throws SRSInfoException {
        this(convertSRID(srid));
    }

    private SRSInfo(String srsURI, CoordinateReferenceSystem crs, boolean isSRSRecognised) {
        this.srsURI = srsURI;
        this.crs = crs;
        this.isAxisXY = checkAxisXY(crs);
        this.unitsOfMeasure = new UnitsOfMeasure(crs);
        this.isSRSRecognised = isSRSRecognised;
        this.isGeographic = crs instanceof GeographicCRS;
        this.isWktDefault = srsURI.equals(SRS_URI.DEFAULT_WKT_CRS84);
        this.domainEnvelope = buildDomainEnvelope(crs, isAxisXY);
        this.domainRangeX = Math.abs(domainEnvelope.getMinX()) + Math.abs(domainEnvelope.getMaxX());
    }

    public static final Boolean checkAxisXY(CoordinateReferenceSystem crs) {

        AxisDirection axisDirection = crs.getCoordinateSystem().getAxis(0).getDirection();

        if (axisDirection.equals(AxisDirection.NORTH) || axisDirection.equals(AxisDirection.SOUTH)) {
            return false;
        } else if (axisDirection.equals(AxisDirection.EAST) || axisDirection.equals(AxisDirection.WEST)) {
            return true;
        } else {
            return !OTHER_Y_AXIS_DIRECTIONS.contains(axisDirection);
        }
    }

    public static final Envelope buildDomainEnvelope(CoordinateReferenceSystem crs, Boolean isAxisXY) {

        org.opengis.geometry.Envelope crsDomain = CRS.getDomainOfValidity(crs);
        DirectPosition lowerCorner = crsDomain.getLowerCorner();
        DirectPosition upperCorner = crsDomain.getUpperCorner();

        int xAxis;
        int yAxis;
        if (isAxisXY) {
            xAxis = 0;
            yAxis = 1;
        } else {
            xAxis = 1;
            yAxis = 0;
        }

        double x1 = lowerCorner.getOrdinate(xAxis);
        double y1 = lowerCorner.getOrdinate(yAxis);
        double x2 = upperCorner.getOrdinate(xAxis);
        double y2 = upperCorner.getOrdinate(yAxis);

        Envelope envelope = new Envelope(x1, x2, y1, y2);
        return envelope;
    }

    /**
     *
     * @param srid
     * @return srsURI using SRID
     */
    public static final String convertSRID(BigInteger srid) {
        return convertSRID(srid.intValue());
    }

    /**
     *
     * @param srid
     * @return srsURI using SRID
     */
    public static final String convertSRID(int srid) {
        return EPSG_BASE_SRS_URI + srid;
    }

    /**
     * URI of the Spatial Reference System<br>
     * Identical values to SRID.
     *
     * @return SRS URI
     */
    public String getSrsURI() {
        return srsURI;
    }

    /**
     * OpenGIS Coordinate Reference System.
     *
     * @return Coordinate Reference System
     */
    public CoordinateReferenceSystem getCrs() {
        return crs;
    }

    /**
     * Units of Measure for the coordinate reference system.
     *
     * @return Units of Measure
     */
    public UnitsOfMeasure getUnitsOfMeasure() {
        return unitsOfMeasure;
    }

    /**
     * Check if axis is in XY order.
     *
     * @return True if XY order.
     */
    public Boolean isAxisXY() {
        return isAxisXY;
    }

    /**
     * Check if the SRS URI is recognised as a OpenGIS coordinate reference
     * system.
     *
     * @return True if SRS is recognised.
     */
    public Boolean isSRSRecognised() {
        return isSRSRecognised;
    }

    /**
     * Check if the SRS is geographic (i.e. latitude, longitude on a sphere).
     *
     * @return True if a Geographic SRS, e.g. WGS84.
     */
    public Boolean isGeographic() {
        return isGeographic;
    }

    /**
     * Check if the SRS is default for WKT Literals.
     *
     * @return True if CRS84 SRS, i.e.
     * http://www.opengis.net/def/crs/OGC/1.3/CRS84.
     */
    public Boolean isWktDefault() {
        return isWktDefault;
    }

    /**
     * Domain of validity in XY coordinate order.
     *
     * @return Bounding box of valid values.
     */
    public Envelope getDomainEnvelope() {
        return domainEnvelope;
    }

    /**
     * Range of domain of validity in X axis.
     *
     * @return Difference between min and max values in X axis.
     */
    public double getDomainRangeX() {
        return domainRangeX;
    }

    /**
     *
     * @param srsURI Allows alternative srsURI to be associated with CRS84.
     * @return SRSInfo using default setup for WKT but alternative srsURI.
     */
    public static final SRSInfo getDefaultWktCRS84(String srsURI) throws SRSInfoException {

        try {
            CoordinateReferenceSystem crs = CRS.forCode(DEFAULT_WKT_CRS84_CODE);
            return new SRSInfo(srsURI, crs, true);
        } catch (FactoryException ex) {
            throw new SRSInfoException("Unrecognised SRS code: " + DEFAULT_WKT_CRS84_CODE + " - " + ex.getMessage(), ex);
        }
    }

    /**
     * Unrecognised SRS URI are assumed to follow the default CRS84 so that
     * operations do not error but may not complete as expected.
     *
     * @param srsURI
     * @return SRSInfo with default setup for WKT without SRS URI
     */
    public static final SRSInfo getUnrecognised(String srsURI) throws SRSInfoException {

        try {
            CoordinateReferenceSystem crs = CRS.forCode(DEFAULT_WKT_CRS84_CODE);
            return new SRSInfo(srsURI, crs, false);
        } catch (FactoryException ex) {
            throw new SRSInfoException("Unrecognised SRS URI code: " + srsURI + " - " + ex.getMessage(), ex);
        }
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 59 * hash + Objects.hashCode(this.srsURI);
        hash = 59 * hash + Objects.hashCode(this.crs);
        hash = 59 * hash + Objects.hashCode(this.unitsOfMeasure);
        hash = 59 * hash + Objects.hashCode(this.isAxisXY);
        hash = 59 * hash + Objects.hashCode(this.isGeographic);
        hash = 59 * hash + Objects.hashCode(this.isSRSRecognised);
        hash = 59 * hash + Objects.hashCode(this.isWktDefault);
        hash = 59 * hash + Objects.hashCode(this.domainEnvelope);
        hash = 59 * hash + (int) (Double.doubleToLongBits(this.domainRangeX) ^ (Double.doubleToLongBits(this.domainRangeX) >>> 32));
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
        final SRSInfo other = (SRSInfo) obj;
        if (Double.doubleToLongBits(this.domainRangeX) != Double.doubleToLongBits(other.domainRangeX)) {
            return false;
        }
        if (!Objects.equals(this.srsURI, other.srsURI)) {
            return false;
        }
        if (!Objects.equals(this.crs, other.crs)) {
            return false;
        }
        if (!Objects.equals(this.unitsOfMeasure, other.unitsOfMeasure)) {
            return false;
        }
        if (!Objects.equals(this.isAxisXY, other.isAxisXY)) {
            return false;
        }
        if (!Objects.equals(this.isGeographic, other.isGeographic)) {
            return false;
        }
        if (!Objects.equals(this.isSRSRecognised, other.isSRSRecognised)) {
            return false;
        }
        if (!Objects.equals(this.isWktDefault, other.isWktDefault)) {
            return false;
        }
        return Objects.equals(this.domainEnvelope, other.domainEnvelope);
    }

    @Override
    public String toString() {
        return "SRSInfo{" + "srsURI=" + srsURI + ", crs=" + crs + ", unitsOfMeasure=" + unitsOfMeasure + ", isAxisXY=" + isAxisXY + ", isGeographic=" + isGeographic + ", isSRSRecognised=" + isSRSRecognised + ", isWktDefault=" + isWktDefault + ", domainEnvelope=" + domainEnvelope + ", domainRangeX=" + domainRangeX + '}';
    }

}
