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
import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedClass;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import org.apache.jena.tdb2.loader.main.LoaderMain;
import org.apache.jena.tdb2.loader.main.LoaderPlan;
import org.apache.jena.tdb2.loader.main.LoaderPlans;

/**
 * Test of loading using a {@link LoaderPlan} and with {@link LoaderMain}.
 */

@ParameterizedClass(name="{index}: {0}")
@MethodSource("provideArgs")

public class TestLoaderMainPlan extends AbstractTestLoader {

    private static Stream<Arguments> provideArgs() {
        List<Arguments> x = List.of
                (Arguments.of( "Simple plan",   LoaderPlans.loaderPlanSimple),
                 Arguments.of( "Minimal plan",  LoaderPlans.loaderPlanMinimal),
                 Arguments.of( "Phased Plan",   LoaderPlans.loaderPlanPhased),
                 Arguments.of( "Light plan",    LoaderPlans.loaderPlanLight),
                 Arguments.of( "Parallel plan", LoaderPlans.loaderPlanParallel)
                        );
        return x.stream();
    }

    public TestLoaderMainPlan(String name, LoaderPlan loaderPlan) {
        super(name,
              (dsg, gn) -> new LoaderMain(loaderPlan, dsg, gn, output));
    }
}
