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

package org.apache.jena.fuseki.http;

import org.apache.jena.fuseki.BaseServerTest ;
import org.apache.jena.fuseki.ServerTest ;
import org.apache.jena.web.TestDatasetGraphAccessorBase ;
import org.apache.jena.web.impl.DatasetGraphAccessor ;
import org.apache.jena.web.impl.DatasetGraphAccessorHTTP ;
import org.junit.AfterClass ;
import org.junit.Before ;
import org.junit.BeforeClass ;

public class TestDatasetGraphAccessorHTTP extends TestDatasetGraphAccessorBase
{
    @BeforeClass public static void beforeClass() { ServerTest.allocServer() ; }
    @AfterClass public static void afterClass() { ServerTest.freeServer() ; }
    @Before public void before() { ServerTest.resetServer() ; }

    
    @Override
    protected DatasetGraphAccessor getDatasetUpdater()
    {
        return new DatasetGraphAccessorHTTP(BaseServerTest.serviceREST) ;
    }
}
