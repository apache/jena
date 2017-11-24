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

import static org.apache.jena.fuseki.ServerCtl.* ; 
import org.apache.http.HttpEntity ;
import org.apache.http.entity.EntityTemplate ;
import org.apache.jena.atlas.lib.StrUtils ;
import org.apache.jena.atlas.web.HttpException ;
import org.apache.jena.atlas.web.TypedInputStream ;
import org.apache.jena.riot.* ;
import org.apache.jena.riot.system.StreamRDF ;
import org.apache.jena.riot.system.StreamRDFLib ;
import org.apache.jena.riot.web.HttpOp ;
import org.apache.jena.sparql.core.DatasetGraph ;
import org.apache.jena.sparql.core.DatasetGraphFactory ;
import org.apache.jena.sparql.sse.SSE ;
import org.apache.jena.web.HttpSC ;
import org.junit.Test ;

/** TestDatasetAccessorHTTP does most of the GSP testing.
 *  This class adds the testing of Fuseki extras.
 */
public class TestDatasetOps extends AbstractFusekiTest 
{
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
        EntityTemplate entity = new EntityTemplate((out) -> RDFDataMgr.write(out, dsg, syntax)) ;
        String ct = syntax.getLang().getContentType().getContentType() ;
        entity.setContentType(ct) ;
        return entity ;
    }
    
    @Test public void gsp_x_01() {
        gsp_x(urlDataset(), urlDataset()) ;
    }

    @Test public void gsp_x_02() {
        gsp_x(urlDataset(), serviceGSP()) ;
    }

    @Test public void gsp_x_03() {
        gsp_x(serviceGSP(), urlDataset()) ;
    }

    private void gsp_x(String outward, String inward) {
        HttpEntity e = datasetToHttpEntity(data) ;
        int expectedSize = data.getDefaultGraph().size();
        HttpOp.execHttpPut(outward, e);
        DatasetGraph dsg = DatasetGraphFactory.create() ;
        RDFDataMgr.read(dsg, inward, Lang.NQUADS);
        assertEquals(expectedSize, dsg.getDefaultGraph().size()) ;
    }

    // Get dataset.  Tests conneg.
    @Test 
    public void gsp_x_10() {
        gsp_x_ct(urlDataset(), WebContent.contentTypeNQuads, WebContent.contentTypeNQuads) ;
    }

    @Test 
    public void gsp_x_11() {
        gsp_x_ct(urlDataset(), WebContent.contentTypeNQuadsAlt1, WebContent.contentTypeNQuads) ;
    }

    @Test 
    public void gsp_x_12() {
        gsp_x_ct(urlDataset(), WebContent.contentTypeTriG, WebContent.contentTypeTriG) ;
    }

    @Test 
    public void gsp_x_13() {
        gsp_x_ct(urlDataset(), WebContent.contentTypeTriGAlt1, WebContent.contentTypeTriG) ;
    }

    @Test 
    public void gsp_x_14() {
        gsp_x_ct(urlDataset(), WebContent.defaultDatasetAcceptHeader, WebContent.contentTypeTriG) ;
    }

    @Test 
    public void gsp_x_15() {
        // Anything!
        gsp_x_ct(urlDataset(), WebContent.defaultRDFAcceptHeader, WebContent.contentTypeTriG) ;
    }
    
    private void gsp_x_ct(String urlDataset, String acceptheader, String contentTypeResponse) {
        HttpEntity e = datasetToHttpEntity(data) ;
        HttpOp.execHttpPut(urlDataset(), e);
        
        // Do manually so the test can validate the expected ContentType
        try ( TypedInputStream in = HttpOp.execHttpGet(urlDataset, acceptheader) ) {
            assertEqualsIgnoreCase(contentTypeResponse, in.getContentType()) ;
            Lang lang = RDFLanguages.contentTypeToLang(in.getContentType());
            DatasetGraph dsg = DatasetGraphFactory.create() ;
            StreamRDF dest = StreamRDFLib.dataset(dsg) ;
            RDFParser.source(in).lang(lang).parse(dest);
        }
    }

    @Test 
    public void gsp_x_20()
    {
        HttpEntity e = datasetToHttpEntity(data) ;
        try { 
            HttpOp.execHttpPost(serviceQuery(), e);
        } catch (HttpException ex) {
            assertTrue(HttpSC.isClientError(ex.getResponseCode())) ;
        }
    }
}