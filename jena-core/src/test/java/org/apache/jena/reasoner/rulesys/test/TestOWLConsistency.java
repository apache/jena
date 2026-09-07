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


package org.apache.jena.reasoner.rulesys.test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import org.apache.jena.rdf.model.InfModel;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.reasoner.Reasoner;
import org.apache.jena.reasoner.ReasonerRegistry;
import org.apache.jena.reasoner.ValidityReport;
import org.apache.jena.util.FileManager;

/**
 * Test the preliminary OWL validation rules.
 */
public class TestOWLConsistency {

    /** The tbox/ontology file to test against sample data */
    public static final String testTbox = "file:testing/reasoners/owl/tbox.owl";

    /** A cached copy of the bound reasoner */
    public static Reasoner reasonerCache;

    /**
     * Create, or retrieve from cache, an OWL reasoner already bound
     * to the test tbox.
     */
    public Reasoner makeReasoner() {
        if (reasonerCache == null) {
            Model tbox = FileManager.getInternal().loadModelInternal(testTbox);
            reasonerCache = ReasonerRegistry.getOWLReasoner().bindSchema(tbox.getGraph());
        }
        return reasonerCache;
    }

    /**
     * Should be consistent.
     */
    @Test
    public void testConsistent() {
        assertTrue(doTestOn("file:testing/reasoners/owl/consistentData.rdf"));
    }

    /**
     * Should find problem due to overlap of disjoint classes.
     */
    @Test
    public void testInconsistent1() {
        assertFalse(doTestOn("file:testing/reasoners/owl/inconsistent1.rdf"));
    }

    /**
     * Should find problem due to type violations
     */
    @Test
    public void testInconsistent2() {
        assertFalse(doTestOn("file:testing/reasoners/owl/inconsistent2.rdf"));
    }

    /**
     * Should find problem due to count violations
     */
    @Test
    public void testInconsistent3() {
        assertFalse(doTestOn("file:testing/reasoners/owl/inconsistent3.rdf"));
    }

    /**
     * Should find distinct values for a functional property
     */
    @Test
    public void testInconsistent4() {
        assertFalse(doTestOn("file:testing/reasoners/owl/inconsistent4.rdf"));
    }

    /**
     * Should find type clash due to allValuesFrom rdfs:Literal
     */
    @Test
    public void testInconsistent5() {
        assertFalse(doTestOn("file:testing/reasoners/owl/inconsistent5.rdf"));
    }

    /**
     * Should find distinct literal values for a functional property
     * via an indirect sameAs
     */
    @Test
    public void testInconsistent7() {
        assertFalse(doTestOn("file:testing/reasoners/owl/inconsistent7.rdf"));
    }

    /**
     * Run a single consistency test on the given data file.
     */
    private boolean doTestOn(String dataFile) {
        Model data = FileManager.getInternal().loadModelInternal(dataFile);
        InfModel infmodel = ModelFactory.createInfModel(makeReasoner(), data);
        ValidityReport reportList = infmodel.validate();
        return reportList.isValid();
    }
}
