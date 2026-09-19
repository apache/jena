/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 *   SPDX-License-Identifier: Apache-2.0
 */
package org.apache.jena.geosparql.implementation.function_registration;

import org.apache.jena.geosparql.geo.topological.property_functions.geometry_property.CoordinateDimensionPF;
import org.apache.jena.geosparql.geo.topological.property_functions.geometry_property.DimensionPF;
import org.apache.jena.geosparql.geo.topological.property_functions.geometry_property.IsEmptyPF;
import org.apache.jena.geosparql.geo.topological.property_functions.geometry_property.IsSimplePF;
import org.apache.jena.geosparql.geo.topological.property_functions.geometry_property.IsValidPF;
import org.apache.jena.geosparql.geo.topological.property_functions.geometry_property.SpatialDimensionPF;
import org.apache.jena.geosparql.geof.topological.filter_functions.geometry_property.CoordinateDimensionFF;
import org.apache.jena.geosparql.geof.topological.filter_functions.geometry_property.DimensionFF;
import org.apache.jena.geosparql.geof.topological.filter_functions.geometry_property.GeometryTypeFF;
import org.apache.jena.geosparql.geof.topological.filter_functions.geometry_property.Is3DFF;
import org.apache.jena.geosparql.geof.topological.filter_functions.geometry_property.IsEmptyFF;
import org.apache.jena.geosparql.geof.topological.filter_functions.geometry_property.IsMeasuredFF;
import org.apache.jena.geosparql.geof.topological.filter_functions.geometry_property.IsSimpleFF;
import org.apache.jena.geosparql.geof.topological.filter_functions.geometry_property.IsValidFF;
import org.apache.jena.geosparql.geof.topological.filter_functions.geometry_property.NumGeometriesFF;
import org.apache.jena.geosparql.geof.topological.filter_functions.geometry_property.SpatialDimensionFF;
import org.apache.jena.geosparql.implementation.vocabulary.Geo;
import org.apache.jena.geosparql.implementation.vocabulary.Geof;
import org.apache.jena.sparql.function.FunctionRegistry;
import org.apache.jena.sparql.pfunction.PropertyFunctionRegistry;

/**
 * Registers functions that expose geometry metadata.
 * The {@code geo:} predicates use Jena property functions; the {@code geof:} IRIs
 * use SPARQL expression functions.
 */
public class GeometryProperty {

    /**
     * Registers {@code geo:} predicates as Jena property functions used in
     * SPARQL triple patterns to access geometry metadata.
     *
     * @param registry - the PropertyFunctionRegistry to be used
     */
    public static void loadPropertyFunctions(PropertyFunctionRegistry registry) {

        registry.put(Geo.DIMENSION, DimensionPF.class);
        registry.put(Geo.COORDINATE_DIMENSION, CoordinateDimensionPF.class);
        registry.put(Geo.SPATIAL_DIMENSION, SpatialDimensionPF.class);
        registry.put(Geo.IS_SIMPLE, IsSimplePF.class);
        registry.put(Geo.IS_EMPTY, IsEmptyPF.class);
        registry.put(Geo.IS_VALID, IsValidPF.class);
    }

    /**
     * Registers {@code geof:} geometry metadata expression functions for use in
     * {@code FILTER}, {@code BIND}, and projection expressions.
     * Includes GeoSPARQL 1.1 geometry metadata functions and the isValid extension.
     *
     * @param registry - the FunctionRegistry to be used
     */
    public static void loadFilterFunctions(FunctionRegistry registry) {

        registry.put(Geof.GEOMETRY_TYPE, GeometryTypeFF.class);
        registry.put(Geof.IS_3D, Is3DFF.class);
        registry.put(Geof.IS_MEASURED, IsMeasuredFF.class);
        registry.put(Geof.NUM_GEOMETRIES, NumGeometriesFF.class);
        registry.put(Geof.DIMENSION, DimensionFF.class);
        registry.put(Geof.COORDINATE_DIMENSION, CoordinateDimensionFF.class);
        registry.put(Geof.SPATIAL_DIMENSION, SpatialDimensionFF.class);
        registry.put(Geof.IS_SIMPLE, IsSimpleFF.class);
        registry.put(Geof.IS_EMPTY, IsEmptyFF.class);
        registry.put(Geof.IS_VALID, IsValidFF.class);
    }

}
