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
package org.apache.jena.geosparql.implementation.registry;

import java.io.Serializable;
import java.lang.invoke.MethodHandles;
import java.text.DecimalFormat;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.apache.jena.geosparql.implementation.SRSInfo;
import org.apache.jena.geosparql.implementation.SRSInfoException;
import org.apache.jena.geosparql.implementation.UnitsOfMeasure;
import org.apache.jena.geosparql.implementation.vocabulary.SRS_URI;
import static org.apache.jena.geosparql.implementation.vocabulary.SRS_URI.EPSG_BASE_SRS_URI;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 *
 */
public class SRSRegistry implements Serializable {

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private static final Map<String, SRSInfo> SRS_REGISTRY = Collections.synchronizedMap(new HashMap<>());

    public static final UnitsOfMeasure getUnitsOfMeasure(String srsURI) {
        SRSInfo srsInfo = storeSRS(srsURI);
        return srsInfo.getUnitsOfMeasure();
    }

    public static final Boolean getAxisXY(String srsURI) {
        SRSInfo srsInfo = storeSRS(srsURI);
        return srsInfo.isAxisXY();
    }

    public static final CoordinateReferenceSystem getCRS(String srsURI) {
        SRSInfo srsInfo = storeSRS(srsURI);
        return srsInfo.getCrs();
    }

    public static final SRSInfo getSRSInfo(String srsURI) {
        return storeSRS(srsURI);
    }

    private static SRSInfo storeSRS(String srsURI) {

        SRSInfo srsInfo;
        if (SRS_REGISTRY.containsKey(srsURI)) {
            srsInfo = SRS_REGISTRY.get(srsURI);
        } else {

            //Find the SRS based on the SRS URI.
            try {
                srsInfo = new SRSInfo(srsURI);
            } catch (SRSInfoException ex) {
                LOGGER.warn("SRS URI not recognised so operation results may not be correct or accurate: {} - {}", srsURI, ex.getMessage());
                srsInfo = SRSInfo.getUnrecognised(srsURI);
            }

            SRS_REGISTRY.put(srsURI, srsInfo);
        }

        return srsInfo;
    }

    public static final void setupDefaultSRS() {

        //CRS_84
        SRSInfo srsInfo = SRSInfo.getDefaultWktCRS84(SRS_URI.DEFAULT_WKT_CRS84);
        SRS_REGISTRY.put(SRS_URI.DEFAULT_WKT_CRS84, srsInfo);

        //WGS_84 Legacy for CRS_84
        SRS_REGISTRY.put(SRS_URI.WGS84_CRS_GEOSPARQL_LEGACY, SRSInfo.getDefaultWktCRS84(SRS_URI.WGS84_CRS_GEOSPARQL_LEGACY));

        //Add the Units of Measure
        UnitsRegistry.addUnit(srsInfo.getUnitsOfMeasure());
    }

    public static final void reset() {
        SRS_REGISTRY.clear();
        setupDefaultSRS();
    }

    private static final String NORTH_UTM_EPSG = EPSG_BASE_SRS_URI + "326";
    private static final String SOUTH_UTM_EPSG = EPSG_BASE_SRS_URI + "327";
    private static final DecimalFormat ZONE_FORMATTER = new DecimalFormat("##");

    /**
     * Find UTM CRS/SRS from WGS84 coordinates.<br>
     * Based on calculation from Stack Overflow.
     *
     * @param latitude
     * @param longitude
     * @return URI of UTM zone CRS/SRS.
     * @see
     * <a href="https://stackoverflow.com/questions/176137/java-convert-lat-lon-to-utm">Stack
     * Overflow question relating to WGS84 to UTM conversion.</a>
     * @see
     * <a href="https://en.wikipedia.org/wiki/Universal_Transverse_Mercator_coordinate_system">Wikipedia
     * article on UTM.</a>
     * @see
     * <a href="http://epsg.io/32600">EPSG for UTM</a>
     *
     */
    public static final String findUTMZoneURIFromWGS84(double latitude, double longitude) {
        int zone = (int) Math.floor(longitude / 6 + 31);
        String zoneString = ZONE_FORMATTER.format(zone);

        boolean isNorth = latitude >= 0;

        String epsgURI;
        if (isNorth) {
            epsgURI = NORTH_UTM_EPSG + zoneString;
        } else {
            epsgURI = SOUTH_UTM_EPSG + zoneString;
        }

        return epsgURI;
    }

}
