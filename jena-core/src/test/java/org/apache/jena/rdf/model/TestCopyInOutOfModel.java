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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedClass;
import org.junit.jupiter.params.provider.MethodSource;

@ParameterizedClass(name = "{0}")
@MethodSource("org.apache.jena.rdf.model.helpers.ModelCreators#creators")
public class TestCopyInOutOfModel extends AbstractModelTestBase {
    private Resource S;
    private Property P;
    private RDFNode O;

    @Override
    @BeforeEach
    public void setUp() {
        super.setUp();
        S = ResourceFactory.createResource();
        P = ResourceFactory.createProperty("http://example.com/property");
        O = ResourceFactory.createResource();
    }

    /**
     * test moving things between models
     */
    @Test
    public void testCopyStatement() {
        final Model model2 = createModel();

        final Statement stmt = model.createStatement(S, P, O);
        assertEquals(model, stmt.getModel());
        assertEquals(0, model.size());
        assertEquals(model, stmt.getSubject().getModel());
        assertEquals(model, stmt.getPredicate().getModel());
        assertEquals(model, stmt.getObject().getModel());
        model.add(stmt);
        assertEquals(1, model.size());
        assertEquals(model, stmt.getSubject().getModel());
        assertEquals(model, stmt.getPredicate().getModel());
        assertEquals(model, stmt.getObject().getModel());
        model2.add(stmt);
        assertEquals(1, model.size());
        assertEquals(model, stmt.getSubject().getModel());
        assertEquals(model, stmt.getPredicate().getModel());
        assertEquals(model, stmt.getObject().getModel());
        assertEquals(1, model2.size());
        final Statement stmt2 = model2.listStatements().next();
        assertEquals(model2, stmt2.getSubject().getModel());
        assertEquals(model2, stmt2.getPredicate().getModel());
        assertEquals(model2, stmt2.getObject().getModel());
    }
    /* try { Statement stmt; StmtIterator sIter; // System.out.println("Beginning " +
     * test);
     * 
     * try { n=100; Resource r11 = m1.createResource(); // Resource r12 =
     * m2.createResource(new ResTestObjF()); long size1 = m1.size(); long size2 =
     * m2.size();
     * 
     * r11.addLiteral(RDF.value, 1); n++; if (! (m1.size() == ++size1)) error(test,
     * n); n++; if (! (m2.size() == size2)) error(test,n);
     * 
     * // stmt = m2.createStatement(r11, RDF.value, r12); // n++; if (!
     * (stmt.getSubject().getModel() == m2)) error(test,n); // n++; if (!
     * (stmt.getResource().getModel() == m2)) error(test,n); // // m1.add(stmt); //
     * n++; if (! (m1.size() == ++size1)) error(test, n); // n++; if (! (m2.size() ==
     * size2)) error(test,n); // // sIter = m1.listStatements( new SimpleSelector(
     * r11, RDF.value, r12 ) ); // n++; if (! sIter.hasNext()) error(test, n); //
     * n++; stmt = sIter.nextStatement(); // n++; if (! (stmt.getSubject().getModel()
     * == m1)) error(test,n); // n++; if (! (stmt.getResource().getModel() == m1))
     * error(test,n); // sIter.close();
     * 
     * 
     * } catch (Exception e) { error(test, n, e); } } catch (Exception e) {
     * logger.error( "test " + test + "[" + n + "]", e ); errors = true; } //
     * System.out.println("End of " + test); } */

}
