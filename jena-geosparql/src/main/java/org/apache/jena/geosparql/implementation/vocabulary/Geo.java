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

import static org.apache.jena.geosparql.implementation.vocabulary.GeoSPARQL_URI.GEO_URI;
import org.apache.jena.graph.Node;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;

/**
 *
 *
 *
 */
public interface Geo {

    public static final Property HAS_SERIALIZATION_PROP = ResourceFactory.createProperty(GEO_URI + "hasSerialization");
    public static final Node HAS_SERIALIZATION_NODE = HAS_SERIALIZATION_PROP.asNode();

    public static final Property AS_WKT_PROP = ResourceFactory.createProperty(GEO_URI + "asWKT");
    public static final Node AS_WKT_NODE = AS_WKT_PROP.asNode();

    public static final Property AS_GML_PROP = ResourceFactory.createProperty(GEO_URI + "asGML");
    public static final Node AS_GML_NODE = AS_GML_PROP.asNode();

    public static final Property HAS_DEFAULT_GEOMETRY_PROP = ResourceFactory.createProperty(GEO_URI + "hasDefaultGeometry");
    public static final Node HAS_DEFAULT_GEOMETRY_NODE = HAS_DEFAULT_GEOMETRY_PROP.asNode();

    public static final Property HAS_GEOMETRY_PROP = ResourceFactory.createProperty(GEO_URI + "hasGeometry");
    public static final Node HAS_GEOMETRY_NODE = HAS_GEOMETRY_PROP.asNode();

    public static final Resource GEOMETRY_RES = ResourceFactory.createResource(GEO_URI + "Geometry");
    public static final Node GEOMETRY_NODE = GEOMETRY_RES.asNode();

    public static final Resource FEATURE_RES = ResourceFactory.createResource(GEO_URI + "Feature");
    public static final Node FEATURE_NODE = FEATURE_RES.asNode();

    public static final Resource SPATIAL_OBJECT_RES = ResourceFactory.createResource(GEO_URI + "SpatialObject");
    public static final Node SPATIAL_OBJECT_NODE = SPATIAL_OBJECT_RES.asNode();

    //Simple Feature Topological function names:
    public static final String SF_CONTAINS_NAME = GEO_URI + "sfContains";
    public static final String SF_INTERSECTS_NAME = GEO_URI + "sfIntersects";
    public static final String SF_EQUALS_NAME = GEO_URI + "sfEquals";
    public static final String SF_TOUCHES_NAME = GEO_URI + "sfTouches";
    public static final String SF_DISJOINT_NAME = GEO_URI + "sfDisjoint";
    public static final String SF_OVERLAPS_NAME = GEO_URI + "sfOverlaps";
    public static final String SF_CROSSES_NAME = GEO_URI + "sfCrosses";
    public static final String SF_WITHIN_NAME = GEO_URI + "sfWithin";

    //Simple Feature Topological function properties:
    public static final Property SF_CONTAINS_PROP = ResourceFactory.createProperty(SF_CONTAINS_NAME);
    public static final Property SF_INTERSECTS_PROP = ResourceFactory.createProperty(SF_INTERSECTS_NAME);
    public static final Property SF_EQUALS_PROP = ResourceFactory.createProperty(SF_EQUALS_NAME);
    public static final Property SF_TOUCHES_PROP = ResourceFactory.createProperty(SF_TOUCHES_NAME);
    public static final Property SF_DISJOINT_PROP = ResourceFactory.createProperty(SF_DISJOINT_NAME);
    public static final Property SF_OVERLAPS_PROP = ResourceFactory.createProperty(SF_OVERLAPS_NAME);
    public static final Property SF_CROSSES_PROP = ResourceFactory.createProperty(SF_CROSSES_NAME);
    public static final Property SF_WITHIN_PROP = ResourceFactory.createProperty(SF_WITHIN_NAME);

    //Simple Feature Topological function nodes:
    public static final Node SF_CONTAINS_NODE = SF_CONTAINS_PROP.asNode();
    public static final Node SF_INTERSECTS_NODE = SF_INTERSECTS_PROP.asNode();
    public static final Node SF_EQUALS_NODE = SF_EQUALS_PROP.asNode();
    public static final Node SF_TOUCHES_NODE = SF_TOUCHES_PROP.asNode();
    public static final Node SF_DISJOINT_NODE = SF_DISJOINT_PROP.asNode();
    public static final Node SF_OVERLAPS_NODE = SF_OVERLAPS_PROP.asNode();
    public static final Node SF_CROSSES_NODE = SF_CROSSES_PROP.asNode();
    public static final Node SF_WITHIN_NODE = SF_WITHIN_PROP.asNode();

    //Egenhofer Topological function names:
    public static final String EH_DISJOINT_NAME = GEO_URI + "ehDisjoint";
    public static final String EH_CONTAINS_NAME = GEO_URI + "ehContains";
    public static final String EH_COVERED_BY_NAME = GEO_URI + "ehCoveredBy";
    public static final String EH_EQUALS_NAME = GEO_URI + "ehEquals";
    public static final String EH_OVERLAP_NAME = GEO_URI + "ehOverlap";
    public static final String EH_COVERS_NAME = GEO_URI + "ehCovers";
    public static final String EH_INSIDE_NAME = GEO_URI + "ehInside";
    public static final String EH_MEET_NAME = GEO_URI + "ehMeet";

    //Egenhofer Topological function properties:
    public static final Property EH_DISJOINT_PROP = ResourceFactory.createProperty(EH_DISJOINT_NAME);
    public static final Property EH_CONTAINS_PROP = ResourceFactory.createProperty(EH_CONTAINS_NAME);
    public static final Property EH_COVERED_BY_PROP = ResourceFactory.createProperty(EH_COVERED_BY_NAME);
    public static final Property EH_EQUALS_PROP = ResourceFactory.createProperty(EH_EQUALS_NAME);
    public static final Property EH_OVERLAP_PROP = ResourceFactory.createProperty(EH_OVERLAP_NAME);
    public static final Property EH_COVERS_PROP = ResourceFactory.createProperty(EH_COVERS_NAME);
    public static final Property EH_INSIDE_PROP = ResourceFactory.createProperty(EH_INSIDE_NAME);
    public static final Property EH_MEET_PROP = ResourceFactory.createProperty(EH_MEET_NAME);

    //Egenhofer Topological function nodes:
    public static final Node EH_DISJOINT_NODE = EH_DISJOINT_PROP.asNode();
    public static final Node EH_CONTAINS_NODE = EH_CONTAINS_PROP.asNode();
    public static final Node EH_COVERED_BY_NODE = EH_COVERED_BY_PROP.asNode();
    public static final Node EH_EQUALS_NODE = EH_EQUALS_PROP.asNode();
    public static final Node EH_OVERLAP_NODE = EH_OVERLAP_PROP.asNode();
    public static final Node EH_COVERS_NODE = EH_COVERS_PROP.asNode();
    public static final Node EH_INSIDE_NODE = EH_INSIDE_PROP.asNode();
    public static final Node EH_MEET_NODE = EH_MEET_PROP.asNode();

    //RCC8 Topological function names:
    public static final String RCC_DISCONNECTED_NAME = GEO_URI + "rcc8dc";
    public static final String RCC_NON_TAN_PROPER_PART_NAME = GEO_URI + "rcc8ntpp";
    public static final String RCC_NON_TAN_PROPER_PART_INVERSE_NAME = GEO_URI + "rcc8ntppi";
    public static final String RCC_TAN_PROPER_PART_NAME = GEO_URI + "rcc8tpp";
    public static final String RCC_TAN_PROPER_PART_INVERSE_NAME = GEO_URI + "rcc8tppi";
    public static final String RCC_EQUALS_NAME = GEO_URI + "rcc8eq";
    public static final String RCC_PARTIALLY_OVERLAPPING_NAME = GEO_URI + "rcc8po";
    public static final String RCC_EXTERNALLY_CONNECTED_NAME = GEO_URI + "rcc8ec";

    //RCC8 Topological function properties:
    public static final Property RCC_DISCONNECTED_PROP = ResourceFactory.createProperty(RCC_DISCONNECTED_NAME);
    public static final Property RCC_NON_TAN_PROPER_PART_PROP = ResourceFactory.createProperty(RCC_NON_TAN_PROPER_PART_NAME);
    public static final Property RCC_NON_TAN_PROPER_PART_INVERSE_PROP = ResourceFactory.createProperty(RCC_NON_TAN_PROPER_PART_INVERSE_NAME);
    public static final Property RCC_TAN_PROPER_PART_PROP = ResourceFactory.createProperty(RCC_TAN_PROPER_PART_NAME);
    public static final Property RCC_TAN_PROPER_PART_INVERSE_PROP = ResourceFactory.createProperty(RCC_TAN_PROPER_PART_INVERSE_NAME);
    public static final Property RCC_EQUALS_PROP = ResourceFactory.createProperty(RCC_EQUALS_NAME);
    public static final Property RCC_PARTIALLY_OVERLAPPING_PROP = ResourceFactory.createProperty(RCC_PARTIALLY_OVERLAPPING_NAME);
    public static final Property RCC_EXTERNALLY_CONNECTED_PROP = ResourceFactory.createProperty(RCC_EXTERNALLY_CONNECTED_NAME);

    //RCC8 Topological function nodes:
    public static final Node RCC_DISCONNECTED_NODE = RCC_DISCONNECTED_PROP.asNode();
    public static final Node RCC_NON_TAN_PROPER_PART_NODE = RCC_NON_TAN_PROPER_PART_PROP.asNode();
    public static final Node RCC_NON_TAN_PROPER_PART_INVERSE_NODE = RCC_NON_TAN_PROPER_PART_INVERSE_PROP.asNode();
    public static final Node RCC_TAN_PROPER_PART_NODE = RCC_TAN_PROPER_PART_PROP.asNode();
    public static final Node RCC_TAN_PROPER_PART_INVERSE_NODE = RCC_TAN_PROPER_PART_INVERSE_PROP.asNode();
    public static final Node RCC_EQUALS_NODE = RCC_EQUALS_PROP.asNode();
    public static final Node RCC_PARTIALLY_OVERLAPPING_NODE = RCC_PARTIALLY_OVERLAPPING_PROP.asNode();
    public static final Node RCC_EXTERNALLY_CONNECTED_NODE = RCC_EXTERNALLY_CONNECTED_PROP.asNode();

    //Topological DE-9IM function: relate
    public static final String RELATE_NAME = GEO_URI + "relate";

    //Geometry Properties
    public static final String DIMENSION = GEO_URI + "dimension";
    public static final String COORDINATE_DIMENSION = GEO_URI + "coordinateDimension";
    public static final String SPATIAL_DIMENSION = GEO_URI + "spatialDimension";
    public static final String IS_EMPTY = GEO_URI + "isEmpty";
    public static final String IS_SIMPLE = GEO_URI + "isSimple";
    public static final String IS_VALID = GEO_URI + "isValid";

    public static final Property DIMENSION_PROP = ResourceFactory.createProperty(DIMENSION);
    public static final Node DIMENSION_NODE = DIMENSION_PROP.asNode();
    public static final Property COORDINATE_DIMENSION_PROP = ResourceFactory.createProperty(COORDINATE_DIMENSION);
    public static final Node COORDINATE_DIMENSION_NODE = COORDINATE_DIMENSION_PROP.asNode();
    public static final Property SPATIAL_DIMENSION_PROP = ResourceFactory.createProperty(SPATIAL_DIMENSION);
    public static final Node SPATIAL_DIMENSION_NODE = SPATIAL_DIMENSION_PROP.asNode();
    public static final Property IS_EMPTY_PROP = ResourceFactory.createProperty(IS_EMPTY);
    public static final Node IS_EMPTY_NODE = IS_EMPTY_PROP.asNode();
    public static final Property IS_SIMPLE_PROP = ResourceFactory.createProperty(IS_SIMPLE);
    public static final Node IS_SIMPLE_NODE = IS_SIMPLE_PROP.asNode();
    public static final Property IS_VALID_PROP = ResourceFactory.createProperty(IS_VALID);
    public static final Node IS_VALID_NODE = IS_VALID_PROP.asNode();

    //Geometry Literal Datatypes
    public static final String WKT = GEO_URI + "wktLiteral";
    public static final String GML = GEO_URI + "gmlLiteral";
}
