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
import org.apache.jena.fuseki.http.TestDatasetAccessorHTTP ;
import org.apache.jena.fuseki.http.TestDatasetGraphAccessorHTTP ;
import org.apache.jena.fuseki.http.TestHttpOp ;
import org.apache.jena.fuseki.server.FusekiEnv ;
import org.junit.BeforeClass ;
import org.junit.runner.RunWith ;
import org.junit.runners.Suite ;


@RunWith(Suite.class)
@Suite.SuiteClasses( {
    TestHttpOp.class
    , TestSPARQLProtocol.class
    , TestDatasetGraphAccessorHTTP.class
    , TestDatasetAccessorHTTP.class
    , TestQuery.class
    , TestAuth.class
    , TestDatasetOps.class
    , TestFileUpload.class
    , TestAdmin.class
})




public class TS_Fuseki extends ServerTest
{
    public static final String FusekiTestHome = "target/FusekiHome" ;
    public static final String FusekiTestBase = FusekiTestHome+"/run" ;
    
    @BeforeClass public static void setupForFusekiServer() {
        FileOps.ensureDir(FusekiTestHome);
        FileOps.clearDirectory(FusekiTestHome);
        System.setProperty("FUSEKI_HOME", FusekiTestHome) ;
        FusekiEnv.setEnvironment() ;
        FusekiLogging.setLogging();
        
        org.apache.log4j.Level WARN1 = org.apache.log4j.Level.WARN ; 
        java.util.logging.Level WARN2 = java.util.logging.Level.WARNING ;

        // Occasionally log4j.properties gets out of step.
        LogCtl.logLevel("org.apache.shiro",    WARN1, WARN2);
        LogCtl.logLevel("org.eclipse.jetty",    WARN1, WARN2);
        
        LogCtl.logLevel(Fuseki.serverLogName,   WARN1, WARN2);
        LogCtl.logLevel(Fuseki.configLogName,   WARN1, WARN2);
        LogCtl.logLevel(Fuseki.adminLogName,    WARN1, WARN2);
        LogCtl.logLevel(Fuseki.builderLogName,  WARN1, WARN2);
        LogCtl.logLevel(Fuseki.actionLogName,   WARN1, WARN2);
        LogCtl.logLevel(Fuseki.requestLogName,  WARN1, WARN2);
        LogCtl.logLevel(Fuseki.servletRequestLogName,   WARN1, WARN2);
    }
}