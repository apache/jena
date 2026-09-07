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

package org.apache.jena.rdf.model;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedClass;
import org.junit.jupiter.params.provider.MethodSource;

import org.apache.jena.graph.Graph;
import org.apache.jena.graph.GraphMemFactory;
import org.apache.jena.rdf.model.helpers.ModelHelper;
import org.apache.jena.rdf.model.impl.ModelCom;

@ParameterizedClass(name = "{0}")
@MethodSource("org.apache.jena.rdf.model.helpers.ModelCreators#creators")
public class TestContains extends AbstractModelTestBase {

    private Property prop(final String uri) {
        return ResourceFactory.createProperty("eh:/" + uri);
    }

    private Resource res(final String uri) {
        return ResourceFactory.createResource("eh:/" + uri);
    }

    @Test
    public void testContains() {
        checkContains(false, "", "x");
        checkContains(false, "a R b", "x");
        checkContains(false, "a R b; c P d", "x");
        /* */
        checkContains(false, "a R b", "z");
        /* */
        checkContains(true, "x R y", "x");
        checkContains(true, "a P b", "P");
        checkContains(true, "i  Q  j", "j");
        checkContains(true, "x R y; a P b; i Q j", "y");
        /* */
        checkContains(true, "x R y; a P b; i Q j", "y");
        checkContains(true, "x R y; a P b; i Q j", "R");
        checkContains(true, "x R y; a P b; i Q j", "a");
    }

    public void checkContains(final boolean yes, final String facts, final String resource) {
        final Model m = modelWithStatements(facts);
        final RDFNode r = ModelHelper.rdfNode(m, resource);
        if ( modelWithStatements(facts).containsResource(r) != yes ) {
            fail("[" + facts + "] should" + (yes ? "" : " not") + " contain " + resource);
        }
    }

    @Test
    public void testContainsWithNull() {
        checkCWN(false, "", null, null, null);
        checkCWN(true, "x R y", null, null, null);
        checkCWN(false, "x R y", null, null, res("z"));
        checkCWN(true, "x RR y", res("x"), prop("RR"), null);
        checkCWN(true, "a BB c", null, prop("BB"), res("c"));
        checkCWN(false, "a BB c", null, prop("ZZ"), res("c"));
    }

    public void checkCWN(final boolean yes, final String facts, final Resource S, final Property P, final RDFNode O) {
        assertEquals(yes, modelWithStatements(facts).contains(S, P, O));
    }

    @Test
    public void testModelComContainsSPcallsContainsSPO() {
        final Graph g = GraphMemFactory.createDefaultGraph();
        final boolean[] wasCalled = {false};
        // FIXME change to dynamic proxy
        final Model m = new ModelCom(g) {
            @Override
            public boolean contains(final Resource s, final Property p, final RDFNode o) {
                wasCalled[0] = true;
                return super.contains(s, p, o);
            }
        };
        assertFalse(m.contains(ModelHelper.resource("r"), ModelHelper.property("p")));
        assertTrue(wasCalled[0], "contains(S,P) should call contains(S,P,O)");
    }
}
