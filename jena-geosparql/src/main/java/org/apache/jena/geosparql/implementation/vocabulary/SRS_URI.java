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
package org.apache.jena.geosparql.implementation.vocabulary;

/**
 *
 *
 */
public interface SRS_URI {

    public static final String OSGB36_CRS = "http://www.opengis.net/def/crs/EPSG/0/27700";
    public static final String GREEK_GRID_CRS = "http://www.opengis.net/def/crs/EPSG/0/2100";
    /**
     * Default SRS_URI Name as GeoSPARQL Standard. Equivalent to WGS84 with axis
     * reversed, i.e. Longitude, Latitude.
     */
    public static final String DEFAULT_WKT_CRS84 = "http://www.opengis.net/def/crs/OGC/1.3/CRS84";

    /**
     * Legacy SRS_URI Name used prior to finalisation of GeoSPARQL standard 1.0.
     */
    public static final String WGS84_CRS_GEOSPARQL_LEGACY = "http://www.opengis.net/def/crs/EPSG/4326";

    /**
     * WGS84 SRS_URI Name used for Latitude, Longitude in Geographic coordinate
     * reference system with units of radians.
     */
    public static final String WGS84_CRS = "http://www.opengis.net/def/crs/EPSG/0/4326";

    /**
     * Base part of the EPSG SRS URI. Needs a specific EPSG code adding to it.
     */
    public static final String EPSG_BASE_SRS_URI = "http://www.opengis.net/def/crs/EPSG/0/";

    /**
     * WGS84 World Mercator SRS_URI Name used for Latitude, Longitude in
     * Projected coordinate reference system with units of metres.
     */
    public static final String WGS84_WORLD_MERCATOR_CRS = "http://www.opengis.net/def/crs/EPSG/0/3395";

}
