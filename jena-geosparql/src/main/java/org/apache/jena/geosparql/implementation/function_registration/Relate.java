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

import org.apache.jena.geosparql.geof.topological.RelateFF;
import org.apache.jena.geosparql.implementation.vocabulary.Geof;
import org.apache.jena.sparql.function.FunctionRegistry;

/**
 *
 *
 *
 */
public class Relate {

    /**
     * This method loads the Dimensionally extended 9 intersection model
     * Function: relate
     *
     * The use of Relate (Geometry g1, Geometry g2, IntersectionMatrix matrix)
     * get the IntersectionMatrix of g1 and g2 (based on g1), then compare with
     * matrix returns true if they are same
     *
     * @param registry - the FunctionRegistry to be used
     */
    public static void loadRelateFunction(FunctionRegistry registry) {

        registry.put(Geof.RELATE, RelateFF.class);
    }
}
