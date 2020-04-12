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

package org.apache.jena.sparql;

import junit.framework.JUnit4TestAdapter;
import junit.framework.TestSuite;
import org.apache.jena.atlas.legacy.BaseTest2;
import org.apache.jena.atlas.legacy.TC_Atlas_ARQ;
import org.apache.jena.common.TC_Common;
import org.apache.jena.rdf_star.TS_RDF_Star;
import org.apache.jena.riot.TC_Riot;
import org.apache.jena.sparql.engine.main.QueryEngineMain;
import org.apache.jena.sparql.engine.ref.QueryEngineRef;
import org.apache.jena.sparql.expr.E_Function;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sys.JenaSystem;
import org.apache.jena.system.TS_System;
import org.apache.jena.web.TS_Web;

/**
 * All the ARQ tests
 */

public class ARQTestSuite extends TestSuite {
    public static final String testDirARQ                  = "testing/ARQ";
    public static final String testDirUpdate               = "testing/Update";

    public static final String log4jPropertiesResourceName = "log4j2.properties";
    static {
        JenaSystem.init();
    }

    static public TestSuite suite() {
        // We have to do things JUnit3 style in order to
        // have scripted tests, which use JUnit3-style dynamic test building.
        // This does not seem to be possible in org.junit.*
        TestSuite ts = new ARQTestSuite();

        // No warnings (e.g. bad lexical forms).
        BaseTest2.setTestLogging();

        // ARQ dependencies
        ts.addTest(new JUnit4TestAdapter(TC_Atlas_ARQ.class));
        ts.addTest(new JUnit4TestAdapter(TC_Common.class));
        ts.addTest(new JUnit4TestAdapter(TC_Riot.class));

        ts.addTest(new JUnit4TestAdapter(TS_Web.class));
        ts.addTest(new JUnit4TestAdapter(TS_System.class));

        // Main ARQ internal test suite.
        ts.addTest(new JUnit4TestAdapter(TC_General.class));

        ts.addTest(TC_Scripted.suite());
        ts.addTest(TC_DAWG.suite());
        // ts.addTest(TC_SPARQL11.suite()) ;
        ts.addTest(new JUnit4TestAdapter(TS_RDF_Star.class));

        // Fiddle around with the config if necessary
        if ( false ) {
            QueryEngineMain.unregister();
            QueryEngineRef.register();
        }
        return ts;
    }

    private ARQTestSuite() {
        super("All ARQ tests");
        NodeValue.VerboseWarnings = false;
        E_Function.WarnOnUnknownFunction = false;
    }
}
