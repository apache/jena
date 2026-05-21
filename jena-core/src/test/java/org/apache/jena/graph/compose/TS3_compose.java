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

package org.apache.jena.graph.compose;

import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.GraphMemFactory;
import org.apache.jena.rdf.model.AbstractTestPackage;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.helpers.ModelCreator;

public class TS3_compose extends TestCase {

    public static TestSuite suite() {
        TestSuite result = new TestSuite();

        ModelCreator gmf1 = ()-> {
            Graph graph = new Intersection(GraphMemFactory.createGraphMemForModel(), GraphMemFactory.createGraphMemForModel());
            Model model = ModelFactory.createModelForGraph(graph);
            return model;
        };

        AbstractTestPackage atp = new AbstractTestPackage("Intersection", gmf1) {};
        for ( int i = 0; i < atp.testCount(); i++ ) {
            result.addTest(atp.testAt(i));
        }

        ModelCreator gmf2 = ()-> {
                Graph graph = new Difference(GraphMemFactory.createGraphMemForModel(), GraphMemFactory.createGraphMemForModel());
                Model model = ModelFactory.createModelForGraph(graph);
                return model;
        };


        atp = new AbstractTestPackage("Difference", gmf2) {};
        for ( int i = 0; i < atp.testCount(); i++ ) {
            result.addTest(atp.testAt(i));
        }

        ModelCreator gmf3 = ()-> {
            Graph graph = new Union(GraphMemFactory.createGraphMemForModel(), GraphMemFactory.createGraphMemForModel());
            Model model = ModelFactory.createModelForGraph(graph);
            return model;
        };

        atp = new AbstractTestPackage("Union", gmf3) {};
        for ( int i = 0; i < atp.testCount(); i++ ) {
            result.addTest(atp.testAt(i));
        }
        /* */
        result.addTest(TestDelta.suite());
        result.addTest(TestUnion.suite());
        result.addTest(TestDisjointUnion.suite());
        result.addTest(TestDifference.suite());
        result.addTest(TestIntersection.suite());
        result.addTest(TestMultiUnion.suite());
        /* */
        result.addTest(TestPolyadicPrefixMapping.suite());
        return result;
    }
}
