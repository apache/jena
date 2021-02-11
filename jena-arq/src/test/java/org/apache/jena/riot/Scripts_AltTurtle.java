/**
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

package org.apache.jena.riot;

import org.apache.jena.arq.junit.manifest.Label;
import org.apache.jena.arq.junit.manifest.Manifests;
import org.apache.jena.arq.junit.riot.ParseForTest;
import org.apache.jena.arq.junit.runners.RunnerRIOT;
import org.apache.jena.riot.lang.extra.TurtleJCC;
import org.apache.jena.sys.JenaSystem;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.runner.RunWith ;

/** Execute turtle test with alt parser. */

@RunWith(RunnerRIOT.class)
@Label("RIOT-TurtleJCC")
@Manifests({
    "testing/RIOT/Lang/TurtleStd/manifest.ttl",
    "testing/RIOT/Lang/Turtle2/manifest.ttl",
    "testing/RIOT/Lang/TurtleSubm/manifest.ttl",
    "testing/ARQ/RDF-star/Turtle-star/manifest.ttl"
})

public class Scripts_AltTurtle
{
    // Switch parsers!
    // ParseForTest is the wrapper code to parse test input.
    @BeforeClass public static void beforeClass() {
        JenaSystem.init();
        // Register language and parser factory.
        TurtleJCC.register();
        ParseForTest.alternativeReaderFactories.put(Lang.TURTLE, TurtleJCC.factory);
    }
    
    @AfterClass public static void afterClass() {
        ParseForTest.alternativeReaderFactories.remove(Lang.TURTLE);
    }
}

