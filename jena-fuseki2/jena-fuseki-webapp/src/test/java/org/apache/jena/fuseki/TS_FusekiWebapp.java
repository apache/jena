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

import org.apache.jena.atlas.lib.FileOps ;
import org.apache.jena.atlas.logging.LogCtl ;
import org.apache.jena.fuseki.system.FusekiLogging;
import org.apache.jena.fuseki.webapp.FusekiEnv;
import org.junit.AfterClass ;
import org.junit.BeforeClass ;
import org.junit.runner.RunWith ;
import org.junit.runners.Suite ;


@RunWith(Suite.class)
@Suite.SuiteClasses( {
    TestHttpOp.class
    , TestSPARQLProtocol.class
    , TestHttpOperations.class
    , TestHttpOptions.class
    , TestDatasetGraphAccessorHTTP.class
    , TestDatasetAccessorHTTP.class
    , TestQuery.class
    , TestAuth.class
    , TestDatasetOps.class
    , TestFileUpload.class
    , TestAdmin.class
    , TestAdminAPI.class
    , TestServerReadOnly.class
    , TestBuilder.class
    , TestMetrics.class
})

public class TS_FusekiWebapp extends ServerTest
{
    public static final String FusekiTestHome = "target/FusekiHome" ;
    public static final String FusekiTestBase = FusekiTestHome+"/run" ;
    
    @BeforeClass public static void setupForFusekiServer() {
        FileOps.ensureDir(FusekiTestHome);
        FileOps.clearDirectory(FusekiTestHome);
        System.setProperty("FUSEKI_HOME", FusekiTestHome) ;
        FusekiEnv.setEnvironment() ;
        FusekiLogging.setLogging();
        // To avoid confusion with log4j.properties in the main part of the server,
        // we modify in place the logging, not try to set it with another
        // Log4j properties file from the classpath.  
        LogCtl.setLevel("org.apache.shiro",          "WARN") ;
        LogCtl.setLevel("org.eclipse.jetty",         "WARN");
        
        LogCtl.setLevel(Fuseki.serverLogName,        "WARN");
        LogCtl.setLevel(Fuseki.configLogName,        "WARN");
        LogCtl.setLevel(Fuseki.adminLogName,         "WARN");
        LogCtl.setLevel(Fuseki.builderLogName,       "WARN");
        LogCtl.setLevel(Fuseki.actionLogName,        "WARN");
        LogCtl.setLevel(Fuseki.requestLogName,       "WARN");
        LogCtl.setLevel(Fuseki.servletRequestLogName,"WARN");
        
        ServerCtl.ctlBeforeTestSuite();
    }
    
    @AfterClass
    static public void afterSuiteClass() {
        ServerCtl.ctlAfterTestSuite() ;
    }
}