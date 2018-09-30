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

package org.apache.jena.tdb2.loader ;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;

import org.apache.jena.graph.Node ;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.tdb2.loader.main.LoaderMain;
import org.apache.jena.tdb2.loader.main.LoaderPlan;
import org.apache.jena.tdb2.loader.main.LoaderPlans;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

/**
 * Test of loading using a {@link LoaderPlan} and with {@link LoaderMain}.
 */
@RunWith(Parameterized.class)
public class TestLoaderMain extends AbstractTestLoader {
    
    @Parameters(name = "{index}: {0}")
    public static Iterable<Object[]> data() {
        List<Object[]> x = new ArrayList<>() ;
        add(x, "Simple plan", LoaderPlans.loaderPlanSimple);
        add(x, "Minimal plan", LoaderPlans.loaderPlanMinimal);
        add(x, "Phased Plan", LoaderPlans.loaderPlanPhased);
        add(x, "Parallel plan", LoaderPlans.loaderPlanParallel);
        return x ; 
    }

    private static void add(List<Object[]> x, String name, LoaderPlan loaderPlan) {
        BiFunction<DatasetGraph, Node, DataLoader> maker = (dsg, gn) -> new LoaderMain(loaderPlan, dsg, gn, output);
        x.add(new Object[]{name, maker});
    }

    public TestLoaderMain(String name, BiFunction<DatasetGraph, Node, DataLoader> maker) {
        super(name, maker);
    }
}
