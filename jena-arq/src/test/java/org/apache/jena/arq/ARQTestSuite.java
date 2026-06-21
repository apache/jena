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

package org.apache.jena.arq;


import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;

import org.apache.jena.atlas.TC_Atlas_ARQ;
import org.apache.jena.atlas.legacy.BaseTest2;
import org.apache.jena.riot.*;
import org.apache.jena.riot.lang.rdfxml.Scripts_ARP1_RDFXML;
import org.apache.jena.riot.lang.rdfxml.Scripts_RRX11;
import org.apache.jena.riot.lang.rdfxml.Scripts_RRX12;
import org.apache.jena.riot.lang.rdfxml.Scripts_RRX_RDFXML;
import org.apache.jena.sparql.*;
import org.apache.jena.sparql.expr.E_Function;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sys.JenaSystem;
import org.apache.jena.system.TC_System;

/**
 * All the ARQ tests, group by TC.
 */

@Suite
@SelectClasses({
    // All TC_*.
    // Should cover all TS_*
    // Should not have any TS here (if in doubt, put in TC_System

    TC_System.class,

    TC_Atlas_ARQ.class,
    TC_RIOT.class,

    // Main ARQ java tests
    TC_ARQ.class,

    // All Scripts_*
    // RIOT
    // rdf-tests CG - RDF language tests
    Scripts_RIOT_rdf_tests_std.class,
    Scripts_RIOT_extra.class,
    Scripts_AltTurtle.class,

    // RDF/XML
    Scripts_ARP1_RDFXML.class,
    Scripts_RRX_RDFXML.class,
    Scripts_RRX11.class,
    Scripts_RRX12.class,

    Scripts_C14N.class,
    // ARQ, SPARQL 1.0, SPARQL 1.1, SPARQL 1.2 - main engine, default in-memory dataset.
    Scripts_SPARQL.class,
    Scripts_RefEngine.class,
    Scripts_SPARQL_Dataset.class,
    Scripts_ARQ.class,

    // Composite datatypes extension
    Scripts_CDTs.class

    // Only runs when src-examples is a source folder, which it isn't in the build.
    //, org.apache.jena.arq.examples.TC_Examples.class
})
public class ARQTestSuite {
    static { JenaSystem.init(); }

    public static final String log4jPropertiesResourceName = "log4j2.properties";

    @BeforeAll
    public static void beforeClass() {
        NodeValue.VerboseWarnings = false;
        E_Function.WarnOnUnknownFunction = false;
        BaseTest2.setTestLogging();
    }

    @AfterAll
    public static void afterClass() {
        BaseTest2.unsetTestLogging();
    }
}
