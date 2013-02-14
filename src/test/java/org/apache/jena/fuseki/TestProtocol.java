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

import org.apache.jena.web.DatasetAccessor ;
import org.apache.jena.web.DatasetAccessorFactory ;
import org.junit.AfterClass ;
import org.junit.BeforeClass ;
import org.junit.Test ;

import com.hp.hpl.jena.sparql.util.Convert ;

// generally poke the server.
public class TestProtocol extends BaseServerTest
{
    @BeforeClass public static void beforeClass()
    {
        ServerTest.allocServer() ;
        ServerTest.resetServer() ;
        // Load some data.
        DatasetAccessor du = DatasetAccessorFactory.createHTTP(serviceREST) ;
        du.putModel(model1) ;
        du.putModel(gn1, model2) ;
    }
    
    @AfterClass public static void afterClass()
    {
        ServerTest.resetServer() ;
        ServerTest.freeServer() ;
    }
    
    static String query(String base, String queryString)
    {
        return base+"?query="+Convert.encWWWForm(queryString) ;
    }
    
    @Test public void protocol_01()
    {
        // TODO
        String url = null ;
        String mimeType = null ; 
        execGet(url, mimeType) ;
    }

    private void execGet(String url, String mimeType)
    {}
    
    private void execPost(String url, String mimeType, String content)
    {}

}
