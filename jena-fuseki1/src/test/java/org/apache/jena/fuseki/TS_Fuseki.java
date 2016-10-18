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

import org.apache.jena.fuseki.http.TestDatasetAccessorHTTP ;
import org.apache.jena.fuseki.http.TestDatasetGraphAccessorHTTP ;
import org.apache.jena.fuseki.http.TestHttpOp ;
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

public class TS_Fuseki
{
    @BeforeClass
    static public void beforeClass() {
        ServerCtl.ctlBeforeTestSuite(); 
    }

    @AfterClass
    static public void afterClass() {
        ServerCtl.ctlAfterTestSuite(); 
    }
}
