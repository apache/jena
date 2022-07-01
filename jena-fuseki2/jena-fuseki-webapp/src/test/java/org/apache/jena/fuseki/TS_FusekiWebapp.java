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

package org.apache.jena.fuseki;

import org.apache.jena.atlas.lib.FileOps;
import org.apache.jena.fuseki.system.FusekiLogging;
import org.apache.jena.fuseki.webapp.FusekiEnv;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;


@RunWith(Suite.class)
@Suite.SuiteClasses( {
      TestWebappSPARQLProtocol.class
    , TestWebappAuthQuery_JDK.class
    , TestWebappAuthUpdate_JDK.class
    , TestWebappFileUpload.class
    , TestAdmin.class
    , TestAdminAPI.class
    , TestWebappServerReadOnly.class
    , TestBuilder.class
    , TestWebappMetrics.class
})

public class TS_FusekiWebapp extends ServerTest
{
    public static String FusekiTestHome = "target/FusekiHome";
    public static String FusekiTestBase = FusekiTestHome+"/run";

    @BeforeClass public static void setupForFusekiServer() {
        AbstractTestWebappAuth_JDK.RunDependently = false;
        FileOps.ensureDir(FusekiTestHome);
        FileOps.clearAll(FusekiTestHome);
        System.setProperty("FUSEKI_HOME", FusekiTestHome);
        FusekiEnv.setEnvironment();
        FusekiLogging.setLogging();
        ServerCtl.ctlBeforeTestSuite();
    }

    @AfterClass
    static public void afterSuiteClass() {
        ServerCtl.ctlAfterTestSuite();
        AbstractTestWebappAuth_JDK.RunDependently = false;
    }
}
