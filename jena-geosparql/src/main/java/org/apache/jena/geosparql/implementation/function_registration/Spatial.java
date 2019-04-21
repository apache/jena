/*
 * Copyright 2018 .
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
package org.apache.jena.geosparql.implementation.function_registration;

import org.apache.jena.geosparql.implementation.vocabulary.SpatialExtension;
import org.apache.jena.geosparql.spatial.filter_functions.AngleDegreesFF;
import org.apache.jena.geosparql.spatial.filter_functions.AngleFF;
import org.apache.jena.geosparql.spatial.filter_functions.AzimuthDegreesFF;
import org.apache.jena.geosparql.spatial.filter_functions.AzimuthFF;
import org.apache.jena.geosparql.spatial.filter_functions.ConvertLatLonBoxFF;
import org.apache.jena.geosparql.spatial.filter_functions.ConvertLatLonFF;
import org.apache.jena.geosparql.spatial.filter_functions.DistanceFF;
import org.apache.jena.geosparql.spatial.filter_functions.EqualsFF;
import org.apache.jena.geosparql.spatial.filter_functions.GreatCircleFF;
import org.apache.jena.geosparql.spatial.filter_functions.GreatCircleGeomFF;
import org.apache.jena.geosparql.spatial.filter_functions.NearbyFF;
import org.apache.jena.geosparql.spatial.filter_functions.TransformDatatypeFF;
import org.apache.jena.geosparql.spatial.filter_functions.TransformFF;
import org.apache.jena.geosparql.spatial.filter_functions.TransformSRSFF;
import org.apache.jena.geosparql.spatial.property_functions.EqualsPF;
import org.apache.jena.geosparql.spatial.property_functions.box.IntersectBoxGeomPF;
import org.apache.jena.geosparql.spatial.property_functions.box.IntersectBoxPF;
import org.apache.jena.geosparql.spatial.property_functions.box.WithinBoxGeomPF;
import org.apache.jena.geosparql.spatial.property_functions.box.WithinBoxPF;
import org.apache.jena.geosparql.spatial.property_functions.cardinal.EastGeomPF;
import org.apache.jena.geosparql.spatial.property_functions.cardinal.EastPF;
import org.apache.jena.geosparql.spatial.property_functions.cardinal.NorthGeomPF;
import org.apache.jena.geosparql.spatial.property_functions.cardinal.NorthPF;
import org.apache.jena.geosparql.spatial.property_functions.cardinal.SouthGeomPF;
import org.apache.jena.geosparql.spatial.property_functions.cardinal.SouthPF;
import org.apache.jena.geosparql.spatial.property_functions.cardinal.WestGeomPF;
import org.apache.jena.geosparql.spatial.property_functions.cardinal.WestPF;
import org.apache.jena.geosparql.spatial.property_functions.nearby.NearbyGeomPF;
import org.apache.jena.geosparql.spatial.property_functions.nearby.NearbyPF;
import org.apache.jena.sparql.function.FunctionRegistry;
import org.apache.jena.sparql.pfunction.PropertyFunctionRegistry;

/**
 *
 *
 */
public class Spatial {

    /**
     * This method loads all the Jena Spatial Property Functions
     *
     * @param registry - the PropertyFunctionRegistry to be used
     */
    public static void loadPropertyFunctions(PropertyFunctionRegistry registry) {

        registry.put(SpatialExtension.EQUALS_PROP, EqualsPF.class);
        registry.put(SpatialExtension.NEARBY_PROP, NearbyPF.class);
        registry.put(SpatialExtension.NEARBY_GEOM_PROP, NearbyGeomPF.class);
        registry.put(SpatialExtension.WITHIN_CIRCLE_PROP, NearbyPF.class);
        registry.put(SpatialExtension.WITHIN_CIRCLE_GEOM_PROP, NearbyGeomPF.class);
        registry.put(SpatialExtension.WITHIN_BOX_PROP, WithinBoxPF.class);
        registry.put(SpatialExtension.WITHIN_BOX_GEOM_PROP, WithinBoxGeomPF.class);
        registry.put(SpatialExtension.INTERSECT_BOX_PROP, IntersectBoxPF.class);
        registry.put(SpatialExtension.INTERSECT_BOX_GEOM_PROP, IntersectBoxGeomPF.class);
        registry.put(SpatialExtension.NORTH_PROP, NorthPF.class);
        registry.put(SpatialExtension.NORTH_GEOM_PROP, NorthGeomPF.class);
        registry.put(SpatialExtension.SOUTH_PROP, SouthPF.class);
        registry.put(SpatialExtension.SOUTH_GEOM_PROP, SouthGeomPF.class);
        registry.put(SpatialExtension.EAST_PROP, EastPF.class);
        registry.put(SpatialExtension.EAST_GEOM_PROP, EastGeomPF.class);
        registry.put(SpatialExtension.WEST_PROP, WestPF.class);
        registry.put(SpatialExtension.WEST_GEOM_PROP, WestGeomPF.class);
    }

    /**
     * This method loads all the Jena Spatial Filter Functions
     *
     * @param functionRegistry
     */
    public static void loadFilterFunctions(FunctionRegistry functionRegistry) {

        functionRegistry.put(SpatialExtension.EQUALS, EqualsFF.class);
        functionRegistry.put(SpatialExtension.CONVERT_LAT_LON, ConvertLatLonFF.class);
        functionRegistry.put(SpatialExtension.CONVERT_LAT_LON_BOX, ConvertLatLonBoxFF.class);
        functionRegistry.put(SpatialExtension.NEARBY, NearbyFF.class);
        functionRegistry.put(SpatialExtension.WITHIN_CIRCLE, NearbyFF.class);
        functionRegistry.put(SpatialExtension.ANGLE, AngleFF.class);
        functionRegistry.put(SpatialExtension.ANGLE_DEGREES, AngleDegreesFF.class);
        functionRegistry.put(SpatialExtension.DISTANCE, DistanceFF.class);
        functionRegistry.put(SpatialExtension.AZIMUTH, AzimuthFF.class);
        functionRegistry.put(SpatialExtension.AZIMUTH_DEGREES, AzimuthDegreesFF.class);
        functionRegistry.put(SpatialExtension.GREAT_CIRCLE, GreatCircleFF.class);
        functionRegistry.put(SpatialExtension.GREAT_CIRCLE_GEOM, GreatCircleGeomFF.class);
        functionRegistry.put(SpatialExtension.TRANSFORM_DATATYPE, TransformDatatypeFF.class);
        functionRegistry.put(SpatialExtension.TRANSFORM_SRS, TransformSRSFF.class);
        functionRegistry.put(SpatialExtension.TRANSFORM, TransformFF.class);
    }

}
