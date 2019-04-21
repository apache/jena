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

import java.util.HashMap;

/**
 *
 *
 */
public class GeoSPARQL_URI {

    //URI
    public static final String SF_URI = "http://www.opengis.net/ont/sf#";
    public static final String GML_URI = "http://www.opengis.net/ont/gml#";
    public static final String GEOF_URI = "http://www.opengis.net/def/function/geosparql/";
    public static final String GEOR_URI = "http://www.opengis.net/def/rule/geosparql/";
    public static final String GEO_URI = "http://www.opengis.net/ont/geosparql#";
    public static final String XSD_URI = "http://www.w3.org/2001/XMLSchema#";
    public static final String RDF_URI = "http://www.w3.org/1999/02/22-rdf-syntax-ns#";
    public static final String RDFS_URI = "http://www.w3.org/2000/01/rdf-schema#";
    public static final String UOM_URI = "http://www.opengis.net/def/uom/OGC/1.0/";
    public static final String SPATIAL_URI = "http://jena.apache.org/spatial#";
    public static final String SPATIAL_FUNCTION_URI = "http://jena.apache.org/function/spatial#";
    public static final String GEO_POS_URI = "http://www.w3.org/2003/01/geo/wgs84_pos#";

    private static final HashMap<String, String> PREFIXES = new HashMap<>();

    public static final HashMap<String, String> getPrefixes() {

        if (PREFIXES.isEmpty()) {
            PREFIXES.put("sf", SF_URI);
            PREFIXES.put("gml", GML_URI);
            PREFIXES.put("geof", GEOF_URI);
            PREFIXES.put("geo", GEO_URI);
            PREFIXES.put("xsd", XSD_URI);
            PREFIXES.put("rdf", RDF_URI);
            PREFIXES.put("rdfs", RDFS_URI);
            PREFIXES.put("uom", UOM_URI);
            PREFIXES.put("spatial", SPATIAL_URI);
            PREFIXES.put("spatial-f", SPATIAL_FUNCTION_URI);
            PREFIXES.put("geo-pos", GEO_POS_URI);

        }
        return PREFIXES;
    }

}
