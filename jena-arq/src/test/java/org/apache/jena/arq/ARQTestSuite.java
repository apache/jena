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

package org.apache.jena.arq;

import org.apache.jena.atlas.TC_Atlas_ARQ;
import org.apache.jena.atlas.legacy.BaseTest2;
import org.apache.jena.common.TC_Common;
import org.apache.jena.http.auth.TS_HttpAuth;
import org.apache.jena.rdfs.TS_InfRdfs;
import org.apache.jena.riot.TC_Riot;
import org.apache.jena.sparql.*;
import org.apache.jena.sparql.expr.E_Function;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sys.JenaSystem;
import org.apache.jena.system.TS_System;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * All the ARQ tests
 */

@RunWith(Suite.class)
@Suite.SuiteClasses( {
    TC_Atlas_ARQ.class,
    TC_Common.class,

    TC_Riot.class,

    TS_System.class,

    TS_InfRdfs.class,
    TS_HttpAuth.class,

    // Main ARQ java tests
    TC_ARQ.class,

    // ARQ SPARQL scripted.
    Scripts_ARQ.class,
    Scripts_DAWG.class,
    //Scripts_SPARQL11.class, // Covered by Scripts_ARQ
    Scripts_RefEngine.class,
    Scripts_TIM.class

    // Only runs when src-examples is a source folder, which it isn't in the build.
    //, org.apache.jena.arq.examples.TC_Examples.class
})
public class ARQTestSuite {
    static {
        JenaSystem.init();
    }

    public static final String testDirARQ                  = "testing/ARQ";
    public static final String testDirUpdate               = "testing/Update";

    public static final String log4jPropertiesResourceName = "log4j2.properties";

    @BeforeClass
    public static void beforeClass() {
        NodeValue.VerboseWarnings = false;
        E_Function.WarnOnUnknownFunction = false;
        BaseTest2.setTestLogging();
    }

    @AfterClass
    public static void afterClass() {
        BaseTest2.unsetTestLogging();
    }
}
