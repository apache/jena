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

package org.apache.jena.fuseki.main;

import org.apache.jena.atlas.logging.LogCtl ;
import org.apache.jena.fuseki.Fuseki ;
import org.junit.BeforeClass ;
import org.junit.runner.RunWith ;
import org.junit.runners.Suite ;
import org.junit.runners.Suite.SuiteClasses ;

@RunWith(Suite.class)
@SuiteClasses({
  TestEmbeddedFuseki.class
  , TestMultipleEmbedded.class
  , TestFusekiTestServer.class
  , TestFusekiTestAuth.class
  , TestFusekiCustomOperation.class
})
public class TS_EmbeddedFuseki {
    @BeforeClass public static void setupForFusekiServer() {
        LogCtl.setLevel(Fuseki.serverLogName,        "WARN");
        LogCtl.setLevel(Fuseki.actionLogName,        "WARN");
        LogCtl.setLevel(Fuseki.requestLogName,       "WARN");
        LogCtl.setLevel(Fuseki.adminLogName,         "WARN");
        LogCtl.setLevel("org.eclipse.jetty",         "WARN");
        
        // Shouldn't see these in the embedded server.
//        LogCtl.setLevel("org.apache.shiro",          "WARN") ;
//        LogCtl.setLevel(Fuseki.configLogName,        "WARN");

//        LogCtl.setLevel(Fuseki.builderLogName,       "WARN");
//        LogCtl.setLevel(Fuseki.servletRequestLogName,"WARN");
    }
}
