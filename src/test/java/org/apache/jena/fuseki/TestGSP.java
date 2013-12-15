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

import static org.apache.jena.fuseki.ServerTest.serviceQuery ;
import static org.apache.jena.fuseki.ServerTest.serviceREST ;
import static org.apache.jena.fuseki.ServerTest.urlDataset ;

import java.io.IOException ;
import java.io.OutputStream ;
import java.io.StringReader ;

import org.apache.http.HttpEntity ;
import org.apache.http.entity.ContentProducer ;
import org.apache.http.entity.EntityTemplate ;
import org.apache.jena.atlas.junit.BaseTest ;
import org.apache.jena.atlas.lib.StrUtils ;
import org.apache.jena.atlas.web.HttpException ;
import org.apache.jena.riot.Lang ;
import org.apache.jena.riot.RDFDataMgr ;
import org.apache.jena.riot.RDFFormat ;
import org.apache.jena.riot.web.HttpOp ;
import org.apache.jena.web.HttpSC ;
import org.junit.AfterClass ;
import org.junit.Before ;
import org.junit.BeforeClass ;
import org.junit.Test ;

import com.hp.hpl.jena.sparql.core.DatasetGraph ;
import com.hp.hpl.jena.sparql.core.DatasetGraphFactory ;
import com.hp.hpl.jena.sparql.sse.SSE ;

/** TestDatasetAccessorHTTP does most of the GSP testing.
 *  This class adds the testing of Fuseki extras.
 */
public class TestGSP extends BaseTest 
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
    
    static DatasetGraph data = SSE.parseDatasetGraph(StrUtils.strjoinNL
        ("(prefix ((: <http://example/>))",
         "  (dataset",
         "    (graph (_:x :p 1) (_:x :p 2))" ,
         "    (graph :g (_:x :p 3))",
         "))"
         )) ;
    
    /** Create an HttpEntity for the graph */  
    protected HttpEntity datasetToHttpEntity(final DatasetGraph dsg) {

        final RDFFormat syntax = RDFFormat.NQUADS ;
        ContentProducer producer = new ContentProducer() {
            @Override
            public void writeTo(OutputStream out) throws IOException {
                RDFDataMgr.write(out, dsg, syntax) ;
            }
        } ;
        EntityTemplate entity = new EntityTemplate(producer) ;
        String ct = syntax.getLang().getContentType().getContentType() ;
        entity.setContentType(ct) ;
        return entity ;
    }
    
    @Test public void gsp_x_01() {
        gsp_x(urlDataset, urlDataset) ;
    }

    @Test public void gsp_x_02() {
        gsp_x(urlDataset, serviceREST) ;
    }

    @Test public void gsp_x_03() {
        gsp_x(serviceREST, urlDataset) ;
    }

    @Test public void gsp_x_04() {
        gsp_x(serviceREST, urlDataset) ;
    }
    private void gsp_x(String outward, String inward) {
        HttpEntity e = datasetToHttpEntity(data) ;
        HttpOp.execHttpPut(outward, e);
        String x = HttpOp.execHttpGetString(inward, "application/n-quads") ;
        DatasetGraph dsg = DatasetGraphFactory.createMem() ;
        RDFDataMgr.read(dsg, new StringReader(x), null, Lang.NQUADS) ;
        assertEquals(2, dsg.getDefaultGraph().size()) ;
    }

    @Test 
    public void gsp_x_10()
    {
        HttpEntity e = datasetToHttpEntity(data) ;
        try { 
            HttpOp.execHttpPost(serviceQuery, e);
        } catch (HttpException ex) {
            assertTrue(HttpSC.isClientError(ex.getResponseCode())) ;
        }
    }
}