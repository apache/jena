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

import org.apache.http.client.HttpClient ;
import org.apache.jena.atlas.logging.LogCtl ;
import org.apache.jena.fuseki.http.TestDatasetAccessorHTTP ;
import org.apache.jena.fuseki.http.TestDatasetGraphAccessorHTTP ;
import org.apache.jena.fuseki.http.TestHttpOp ;
import org.apache.jena.riot.web.HttpOp ;
import org.junit.AfterClass ;
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
})
public class TS_Fuseki extends ServerTest
{
    // Use HttpOp caching of connections during testing to stop
    // swamping  kernel socket management (seems to be most
    // acute on Java 1.6)
    
    static HttpClient defaultHttpClient = HttpOp.getDefaultHttpClient() ;
    // Used for all tests except auth tests.
    static HttpClient globalCachingClient = HttpOp.createCachingHttpClient() ;
    
    @BeforeClass public static void beforeClassAbstract1() {
        HttpOp.setDefaultHttpClient(globalCachingClient) ;
    }
    
    @AfterClass public static void afterClassAbstract1() {
        HttpOp.setDefaultHttpClient(defaultHttpClient) ;
    }
    
    @BeforeClass static public void beforeClass() { LogCtl.disable(Fuseki.requestLogName) ; }
    @AfterClass static public void afterClass()   { LogCtl.setInfo(Fuseki.requestLogName) ;}
}
