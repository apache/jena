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
package org.apache.jena.geosparql.implementation.function_registration;

import org.apache.jena.geosparql.geof.nontopological.filter_functions.AsGeoJSONFF;
import org.apache.jena.geosparql.geof.nontopological.filter_functions.BoundaryFF;
import org.apache.jena.geosparql.geof.nontopological.filter_functions.BufferFF;
import org.apache.jena.geosparql.geof.nontopological.filter_functions.ConvexHullFF;
import org.apache.jena.geosparql.geof.nontopological.filter_functions.DifferenceFF;
import org.apache.jena.geosparql.geof.nontopological.filter_functions.DistanceFF;
import org.apache.jena.geosparql.geof.nontopological.filter_functions.EnvelopFF;
import org.apache.jena.geosparql.geof.nontopological.filter_functions.GetSRIDFF;
import org.apache.jena.geosparql.geof.nontopological.filter_functions.IntersectionFF;
import org.apache.jena.geosparql.geof.nontopological.filter_functions.SymmetricDifferenceFF;
import org.apache.jena.geosparql.geof.nontopological.filter_functions.UnionFF;
import org.apache.jena.geosparql.implementation.vocabulary.Geof;
import org.apache.jena.sparql.function.FunctionRegistry;

/**
 *
 *
 *
 */
public class NonTopological {

    /**
     * This method loads all the Non-Topological Filter Functions, such as
     * distance, buffer, envelop, etc.
     *
     * @param registry - the FunctionRegistry to be used
     */
    public static void loadFilterFunctions(FunctionRegistry registry) {

        // Non Topological Filter Functions
        registry.put(Geof.BOUNDARY_NAME, BoundaryFF.class);
        registry.put(Geof.BUFFER_NAME, BufferFF.class);
        registry.put(Geof.CONVEXHULL_NAME, ConvexHullFF.class);
        registry.put(Geof.DIFFERENCE_NAME, DifferenceFF.class);
        registry.put(Geof.DISTANCE_NAME, DistanceFF.class);
        registry.put(Geof.ENVELOPE_NAME, EnvelopFF.class);
        registry.put(Geof.GETSRID_NAME, GetSRIDFF.class);
        registry.put(Geof.INTERSECTION_NAME, IntersectionFF.class);
        registry.put(Geof.SYMDIFFERENCE_NAME, SymmetricDifferenceFF.class);
        registry.put(Geof.UNION_NAME, UnionFF.class);
        registry.put(Geof.AS_GEOJSON, AsGeoJSONFF.class);
    }
}
