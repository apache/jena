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

import static org.apache.jena.fuseki.ServerTest.serviceREST ;
import org.apache.jena.atlas.junit.BaseTest ;
import org.junit.AfterClass ;
import org.junit.Before ;
import org.junit.BeforeClass ;
import org.junit.Test ;

import com.hp.hpl.jena.query.DatasetAccessor ;
import com.hp.hpl.jena.query.DatasetAccessorFactory ;
import com.hp.hpl.jena.rdf.model.Model ;

public class TestFileUpload extends BaseTest 
{
    @BeforeClass
    public static void beforeClass() {
        ServerTest.allocServer() ;
        //ServerTest.resetServer() ;
    }

    @AfterClass
    public static void afterClass() {
        ServerTest.freeServer() ;
    }
    
    @Before public void beforeTest() {
        ServerTest.resetServer() ;
    }
    
    @Test public void upload_gsp_01()
    {
        FileSender x = new FileSender(ServerTest.serviceREST+"?default") ;
        x.add("D.ttl", "<http://example/s> <http://example/p> <http://example/o> .", "text/turtle") ;
        x.send("POST") ;
        
        DatasetAccessor du = DatasetAccessorFactory.createHTTP(serviceREST) ;
        Model m = du.getModel() ;
        assertEquals(1, m.size()) ;
    }
    
    @Test public void upload_gsp_02()
    {
        FileSender x = new FileSender(ServerTest.serviceREST+"?default") ;
        x.add("D.ttl", "<http://example/s> <http://example/p> 123 .", "text/turtle") ;
        x.add("D.nt", "<http://example/s> <http://example/p> <http://example/o-456> .", "application/n-triples") ;
        x.send("PUT") ;
        
        DatasetAccessor du = DatasetAccessorFactory.createHTTP(serviceREST) ;
        Model m = du.getModel() ;
        assertEquals(2, m.size()) ;
    }

    // Beyond strict GSP - no graph selector => dataset
    @Test public void upload_gsp_03()
    {
        FileSender x = new FileSender(ServerTest.serviceREST) ;
        x.add("D.ttl", "<http://example/s> <http://example/p> <http://example/o> .", "text/turtle") ;
        x.add("D.trig", "<http://example/g> { <http://example/s> <http://example/p> <http://example/o> }", "text/trig") ;
        x.send("POST") ;
        
        DatasetAccessor du = DatasetAccessorFactory.createHTTP(serviceREST) ;
        Model m = du.getModel() ;
        assertEquals(1, m.size()) ;
    }

    @Test public void upload_gsp_04()
    {
        {
            DatasetAccessor du = DatasetAccessorFactory.createHTTP(serviceREST) ;
            Model m = du.getModel() ;
            assertEquals(0, m.size()) ;
        }
        FileSender x = new FileSender(ServerTest.urlDataset) ;
        x.add("D.ttl", "<http://example/s> <http://example/p> <http://example/o> .", "text/plain") ;
        x.add("D.trig", "<http://example/g> { <http://example/s> <http://example/p> 123,456 }", "text/plain") ;
        x.send("POST") ;
        
        DatasetAccessor du = DatasetAccessorFactory.createHTTP(serviceREST) ;
        Model m = du.getModel() ;
        assertEquals(1, m.size()) ;
        m = du.getModel("http://example/g") ;
        assertEquals(2, m.size()) ;
    }

    @Test public void upload_dataset_01() {
        FileSender x = new FileSender(ServerTest.urlDataset) ;
        x.add("D.nq", "", "application/-n-quads") ;
        x.send("PUT") ;
        
        DatasetAccessor du = DatasetAccessorFactory.createHTTP(serviceREST) ;
        Model m = du.getModel() ;
        assertEquals(0, m.size()) ;
        
    }
    
    @Test public void upload_dataset_02() {
        FileSender x = new FileSender(ServerTest.urlDataset) ;
        x.add("D.nq", "<http://example/s> <http://example/p> <http://example/o-456> <http://example/g> .", "application/-n-quads") ;
        x.send("PUT") ;
        
        DatasetAccessor du = DatasetAccessorFactory.createHTTP(serviceREST) ;
        Model m = du.getModel("http://example/g") ;
        assertEquals(1, m.size()) ;
        m = du.getModel() ;
        assertEquals(0, m.size()) ;
        
    }

}
