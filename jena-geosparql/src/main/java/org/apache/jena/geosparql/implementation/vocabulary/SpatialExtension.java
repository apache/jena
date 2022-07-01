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

import static org.apache.jena.geosparql.implementation.vocabulary.GeoSPARQL_URI.GEO_POS_URI;
import static org.apache.jena.geosparql.implementation.vocabulary.GeoSPARQL_URI.SPATIAL_FUNCTION_URI;
import static org.apache.jena.geosparql.implementation.vocabulary.GeoSPARQL_URI.SPATIAL_URI;
import org.apache.jena.graph.Node;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.ResourceFactory;

/**
 *
 *
 */
public class SpatialExtension {

    //Filter Functions
    public static final String EQUALS = SPATIAL_FUNCTION_URI + "equals";
    public static final String CONVERT_LAT_LON = SPATIAL_FUNCTION_URI + "convertLatLon";
    public static final String CONVERT_LAT_LON_BOX = SPATIAL_FUNCTION_URI + "convertLatLonBox";
    public static final String NEARBY = SPATIAL_FUNCTION_URI + "nearby";
    public static final String WITHIN_CIRCLE = SPATIAL_FUNCTION_URI + "withinCircle";
    public static final String ANGLE = SPATIAL_FUNCTION_URI + "angle";
    public static final String ANGLE_DEGREES = SPATIAL_FUNCTION_URI + "angleDeg";
    public static final String DISTANCE = SPATIAL_FUNCTION_URI + "distance";
    public static final String AZIMUTH = SPATIAL_FUNCTION_URI + "azimuth";
    public static final String AZIMUTH_DEGREES = SPATIAL_FUNCTION_URI + "azimuthDeg";
    public static final String GREAT_CIRCLE = SPATIAL_FUNCTION_URI + "greatCircle";
    public static final String GREAT_CIRCLE_GEOM = SPATIAL_FUNCTION_URI + "greatCircleGeom";
    public static final String TRANSFORM_DATATYPE = SPATIAL_FUNCTION_URI + "transformDatatype";
    public static final String TRANSFORM_SRS = SPATIAL_FUNCTION_URI + "transformSRS";
    public static final String TRANSFORM = SPATIAL_FUNCTION_URI + "transform";

    //Property Functions
    public static final String EQUALS_PROP = SPATIAL_URI + "equals";
    public static final String NEARBY_PROP = SPATIAL_URI + "nearby";
    public static final String WITHIN_CIRCLE_PROP = SPATIAL_URI + "withinCircle";
    public static final String WITHIN_BOX_PROP = SPATIAL_URI + "withinBox";
    public static final String INTERSECT_BOX_PROP = SPATIAL_URI + "intersectBox";
    public static final String NORTH_PROP = SPATIAL_URI + "north";
    public static final String SOUTH_PROP = SPATIAL_URI + "south";
    public static final String EAST_PROP = SPATIAL_URI + "east";
    public static final String WEST_PROP = SPATIAL_URI + "west";
    public static final String NEARBY_GEOM_PROP = SPATIAL_URI + "nearbyGeom";
    public static final String WITHIN_CIRCLE_GEOM_PROP = SPATIAL_URI + "withinCircleGeom";
    public static final String WITHIN_BOX_GEOM_PROP = SPATIAL_URI + "withinBoxGeom";
    public static final String INTERSECT_BOX_GEOM_PROP = SPATIAL_URI + "intersectBoxGeom";
    public static final String NORTH_GEOM_PROP = SPATIAL_URI + "northGeom";
    public static final String SOUTH_GEOM_PROP = SPATIAL_URI + "southGeom";
    public static final String EAST_GEOM_PROP = SPATIAL_URI + "eastGeom";
    public static final String WEST_GEOM_PROP = SPATIAL_URI + "westGeom";

    //Lat/Lon
    public static final String GEO_POS_LAT = GEO_POS_URI + "lat";
    public static final String GEO_POS_LON = GEO_POS_URI + "long";
    public static final Property GEO_LAT_PROP = ResourceFactory.createProperty(GEO_POS_LAT);
    public static final Property GEO_LON_PROP = ResourceFactory.createProperty(GEO_POS_LON);
    public static final Node GEO_LAT_NODE = GEO_LAT_PROP.asNode();
    public static final Node GEO_LON_NODE = GEO_LON_PROP.asNode();
}
