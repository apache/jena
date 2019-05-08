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
package org.apache.jena.geosparql.implementation.vocabulary;

/**
 *
 *
 */
public interface Unit_URI {

    //Angular - radians
    public static final String RADIAN_URL = GeoSPARQL_URI.UOM_URI + "radian";
    public static final String MICRORADIAN_URL = GeoSPARQL_URI.UOM_URI + "microRadian";
    public static final String DEGREE_URL = GeoSPARQL_URI.UOM_URI + "degree";
    public static final String ARC_MINUTE_URL = GeoSPARQL_URI.UOM_URI + "minute";
    public static final String ARC_SECOND_URL = GeoSPARQL_URI.UOM_URI + "second";
    public static final String GRAD_URL = GeoSPARQL_URI.UOM_URI + "grad";

    //Linear - SI
    public static final String METRE_URL = GeoSPARQL_URI.UOM_URI + "metre";
    public static final String METER_URL = GeoSPARQL_URI.UOM_URI + "meter";
    public static final String KILOMETRE_URL = GeoSPARQL_URI.UOM_URI + "kilometre";
    public static final String KILOMETER_URL = GeoSPARQL_URI.UOM_URI + "kilometer";
    public static final String CENTIMETRE_URL = GeoSPARQL_URI.UOM_URI + "centimetre";
    public static final String CENTIMETER_URL = GeoSPARQL_URI.UOM_URI + "centimeter";
    public static final String MILLIMETRE_URL = GeoSPARQL_URI.UOM_URI + "millimetre";
    public static final String MILLIMETER_URL = GeoSPARQL_URI.UOM_URI + "millimeter";

    //Linear - Non-SI
    public static final String MILE_URL = GeoSPARQL_URI.UOM_URI + "mile";
    public static final String STATUTE_MILE_URL = GeoSPARQL_URI.UOM_URI + "statuteMile";
    public static final String YARD_URL = GeoSPARQL_URI.UOM_URI + "yard";
    public static final String FOOT_URL = GeoSPARQL_URI.UOM_URI + "foot";
    public static final String INCH_URL = GeoSPARQL_URI.UOM_URI + "inch";
    public static final String NAUTICAL_MILE_URL = GeoSPARQL_URI.UOM_URI + "nauticalMile";
    public static final String US_SURVEY_FOOT_URL = GeoSPARQL_URI.UOM_URI + "surveyFootUS";

    //URN references in: https://sis.apache.org/apidocs/org/apache/sis/measure/Units.html
    //Angular
    public static final String RADIAN_URN = "urn:ogc:def:uom:EPSG::9101";
    public static final String MICRORADIAN_URN = "urn:ogc:def:uom:EPSG::9109";
    public static final String DEGREE_URN = "urn:ogc:def:uom:EPSG::9102";
    public static final String ARC_MINUTE_URN = "urn:ogc:def:uom:EPSG::9103";
    public static final String ARC_SECOND_URN = "urn:ogc:def:uom:EPSG::9104";
    public static final String GRAD_URN = "urn:ogc:def:uom:EPSG::9105";

    //Linear
    public static final String METRE_URN = "urn:ogc:def:uom:EPSG::9001";
    public static final String KILOMETRE_URN = "urn:ogc:def:uom:EPSG::9036";
    public static final String CENTIMETRE_URN = "urn:ogc:def:uom:EPSG::1033";
    public static final String MILLIMETRE_URN = "urn:ogc:def:uom:EPSG::1025";
    public static final String STATUTE_MILE_URN = "urn:ogc:def:uom:EPSG::9093";
    public static final String FOOT_URN = "urn:ogc:def:uom:EPSG::9002";
    public static final String YARD_URN = "urn:ogc:def:uom:EPSG::9096";
    public static final String NAUTICAL_MILE_URN = "urn:ogc:def:uom:EPSG::9030";
    public static final String US_SURVEY_FOOT_URN = "urn:ogc:def:uom:EPSG::9003";

}
