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

package org.apache.jena.rdf.model.helpers;

import java.util.stream.Stream;

import org.junit.jupiter.api.Named;
import org.junit.jupiter.params.provider.Arguments;

import org.apache.jena.graph.Graph;
import org.apache.jena.graph.GraphMemFactory;
import org.apache.jena.graph.compose.Difference;
import org.apache.jena.graph.compose.Intersection;
import org.apache.jena.graph.compose.Union;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;

/**
 * The {@link ModelCreator}s that the {@code rdf.model} tests run against.
 * <p>
 * The JUnit 3 suite built the whole package once per creator - once with
 * {@link ModelCreator#plain} and three more times with the composition graphs. A
 * parameterized test class covers all four in one place.
 */
public class ModelCreators {

    private static Model composed(Graph graph) {
        return ModelFactory.createModelForGraph(graph);
    }

    public static final ModelCreator plain = ModelCreator.plain;

    public static final ModelCreator intersection =
        ()->composed(new Intersection(GraphMemFactory.createGraphMemForModel(), GraphMemFactory.createGraphMemForModel()));

    public static final ModelCreator difference =
        ()->composed(new Difference(GraphMemFactory.createGraphMemForModel(), GraphMemFactory.createGraphMemForModel()));

    public static final ModelCreator union =
        ()->composed(new Union(GraphMemFactory.createGraphMemForModel(), GraphMemFactory.createGraphMemForModel()));

    /** Argument source for {@code @ParameterizedClass} model tests. */
    public static Stream<Arguments> creators() {
        return Stream.of(Arguments.of(Named.of("plain", plain)),
                         Arguments.of(Named.of("Intersection", intersection)),
                         Arguments.of(Named.of("Difference", difference)),
                         Arguments.of(Named.of("Union", union)));
    }
}
