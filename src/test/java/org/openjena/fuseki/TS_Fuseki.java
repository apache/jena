/**
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

package org.openjena.fuseki;

import org.junit.AfterClass ;
import org.junit.BeforeClass ;
import org.junit.runner.RunWith ;
import org.junit.runners.Suite ;
import org.openjena.atlas.logging.Log ;
import org.openjena.fuseki.http.TestDatasetGraphAccessorHTTP ;
import org.openjena.fuseki.http.TestDatasetGraphAccessorMem ;
import org.openjena.fuseki.http.TestDatasetGraphAccessorTDB ;
import org.openjena.fuseki.http.TestDatasetAccessorHTTP ;


@RunWith(Suite.class)
@Suite.SuiteClasses( {
    TestContentNegotiation.class
    , TestDatasetGraphAccessorMem.class
    , TestDatasetGraphAccessorTDB.class
    , TestDatasetGraphAccessorHTTP.class
    , TestDatasetAccessorHTTP.class
    , TestProtocol.class
    , TestUpdate.class
    , TestQuery.class
})
public class TS_Fuseki extends BaseServerTest
{
    @BeforeClass static public void beforeClass() { Log.disable(Fuseki.requestLogName) ; }
    @AfterClass static public void afterClass()   { Log.setInfo(Fuseki.requestLogName) ;}
}
