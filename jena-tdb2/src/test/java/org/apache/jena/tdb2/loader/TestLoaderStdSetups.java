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

package org.apache.jena.tdb2.loader;

import java.util.List;
import java.util.function.BiFunction;
import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedClass;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import org.apache.jena.graph.Node;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.tdb2.loader.main.LoaderPlans;

/**
 * Test of loading using {@link LoaderFactory} operations.
 */
@ParameterizedClass(name="{index}: {0}")
@MethodSource("provideArgs")
public class TestLoaderStdSetups extends AbstractTestLoader {

    private static Stream<Arguments> provideArgs() {
        BiFunction<DatasetGraph, Node, DataLoader> basic =      (dsg, gn)->LoaderFactory.basicLoader(dsg, gn, output);
        BiFunction<DatasetGraph, Node, DataLoader> phased =     (dsg, gn)->LoaderFactory.phasedLoader(dsg, gn, output);
        BiFunction<DatasetGraph, Node, DataLoader> sequential = (dsg, gn)->LoaderFactory.sequentialLoader(dsg, gn, output);
        BiFunction<DatasetGraph, Node, DataLoader> parallel =   (dsg, gn)->LoaderFactory.parallelLoader(dsg, gn, output);
        BiFunction<DatasetGraph, Node, DataLoader> light =      (dsg, gn)->LoaderFactory.createLoader(LoaderPlans.loaderPlanLight, dsg, gn, output);

        List<Arguments> x = List.of
                (Arguments.of("Basic loader",      basic),
                 Arguments.of("Phased loader",     phased),
                 Arguments.of("Sequential loader", sequential),
                 Arguments.of("Parallel loader",   parallel),
                 Arguments.of("Light loader",      light)
        );
        return x.stream();
    }

    public TestLoaderStdSetups(String name, BiFunction<DatasetGraph, Node, DataLoader> maker) {
        super(name, maker);
    }
}
