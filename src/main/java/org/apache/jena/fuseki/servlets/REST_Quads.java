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

package org.apache.jena.fuseki.servlets ;

import static java.lang.String.format ;

import java.io.IOException ;

import javax.servlet.ServletOutputStream ;

import org.apache.jena.atlas.web.MediaType ;
import org.apache.jena.atlas.web.TypedOutputStream ;
import org.apache.jena.fuseki.FusekiLib ;
import org.apache.jena.fuseki.HttpNames ;
import org.apache.jena.riot.Lang ;
import org.apache.jena.riot.RDFDataMgr ;
import org.apache.jena.riot.RDFLanguages ;
import org.apache.jena.riot.RiotException ;
import org.apache.jena.riot.system.StreamRDF ;
import org.apache.jena.riot.system.StreamRDFLib ;

import com.hp.hpl.jena.sparql.core.DatasetGraph ;
import com.hp.hpl.jena.sparql.core.DatasetGraphFactory ;

/**
 * Servlet for operations directly on a dataset - REST(ish) behaviour on the
 * dataset URI.
 */

public class REST_Quads extends SPARQL_REST {
    // Not supported: GSP direct naming.

    public REST_Quads() {
        super() ;
    }

    @Override
    protected void validate(HttpAction action) {
        // already checked?
    }

    @Override
    protected void doGet(HttpAction action) {
        MediaType mediaType = HttpAction.contentNegotationQuads(action) ;
        ServletOutputStream output ;
        try {
            output = action.response.getOutputStream() ;
        } catch (IOException ex) {
            ServletOps.errorOccurred(ex) ;
            output = null ;
        }

        TypedOutputStream out = new TypedOutputStream(output, mediaType) ;
        Lang lang = RDFLanguages.contentTypeToLang(mediaType.getContentType()) ;
        if ( lang == null )
            lang = RDFLanguages.TRIG ;

        if ( action.verbose )
            action.log.info(format("[%d]   Get: Content-Type=%s, Charset=%s => %s", action.id,
                                   mediaType.getContentType(), mediaType.getCharset(), lang.getName())) ;
        if ( !RDFLanguages.isQuads(lang) )
            ServletOps.errorBadRequest("Not a quads format: " + mediaType) ;

        action.beginRead() ;
        try {
            DatasetGraph dsg = action.getActiveDSG() ;
            RDFDataMgr.write(out, dsg, lang) ;
            ServletOps.success(action) ;
        } finally {
            action.endRead() ;
        }
    }

    @Override
    protected void doOptions(HttpAction action) {
        action.response.setHeader(HttpNames.hAllow, "GET, HEAD, OPTIONS") ;
        action.response.setHeader(HttpNames.hContentLengh, "0") ;
        ServletOps.success(action) ;
    }

    @Override
    protected void doHead(HttpAction action) {
        action.beginRead() ;
        try {
            MediaType mediaType = HttpAction.contentNegotationQuads(action) ;
            ServletOps.success(action) ;
        } finally {
            action.endRead() ;
        }
    }

    @Override
    protected void doPost(HttpAction action) {
        if ( !action.getDatasetRef().allowDatasetUpdate )
            ServletOps.errorMethodNotAllowed("POST") ;

        if ( action.isTransactional() )
            doPutPostTxn(action, false) ;
        else
            doPutPostNonTxn(action, false) ;
    }

    @Override
    protected void doPut(HttpAction action) {
        if ( !action.getDatasetRef().allowDatasetUpdate )
            ServletOps.errorMethodNotAllowed("POST") ;

        if ( action.isTransactional() )
            doPutPostTxn(action, false) ;
        else
            doPutPostNonTxn(action, false) ;
    }

    // These are very similar to SPARQL_REST_RW.addDataIntoTxn/nonTxn
    // Maybe can be usually DRYed.

    @Override
    protected void doDelete(HttpAction action) {
        ServletOps.errorMethodNotAllowed("DELETE") ;
    }

    @Override
    protected void doPatch(HttpAction action) {
        ServletOps.errorMethodNotAllowed("PATCH") ;
    }

    private void doPutPostTxn(HttpAction action, boolean clearFirst) {
        action.beginWrite() ;
        try {
            DatasetGraph dsg = action.getActiveDSG() ;
            StreamRDF dest = StreamRDFLib.dataset(dsg) ;
            Upload.incomingData(action, dest, false) ;
            action.commit() ;
            ServletOps.success(action) ;
        } catch (RiotException ex) {
            // Parse error
            action.abort() ;
            ServletOps.errorBadRequest(ex.getMessage()) ;
        } catch (Exception ex) {
            // Something else went wrong. Backout.
            action.abort() ;
            ServletOps.errorOccurred(ex.getMessage()) ;
        } finally {
            action.endWrite() ;
        }
    }
    
    // XXX Logging
    // XXX Logging in Upload.incomingData

    private void doPutPostNonTxn(HttpAction action, boolean clearFirst) {
        DatasetGraph dsgTmp = DatasetGraphFactory.createMem() ;
        StreamRDF dest = StreamRDFLib.dataset(dsgTmp) ;

        try {
            Upload.incomingData(action, dest, false) ;
        } catch (RiotException ex) {
            ServletOps.errorBadRequest(ex.getMessage()) ;
        }
        // Now insert into dataset
        action.beginWrite() ;
        try {
            FusekiLib.addDataInto(dsgTmp, action.getActiveDSG()) ;
            action.commit() ;
            ServletOps.success(action) ;
        } catch (Exception ex) {
            // We're in the non-transactional branch, this probably will not
            // work
            // but it might and there is no harm safely trying.
            try {
                action.abort() ;
            } catch (Exception ex2) {}
            ServletOps.errorOccurred(ex.getMessage()) ;
        } finally {
            action.endWrite() ;
        }

    }

//    static int counter = 0 ;
//
//    protected void doPostTriplesGSP(HttpAction action, Lang lang) {
//        // Old code.
//        // Assumes transactional.
//        action.beginWrite() ;
//        try {
//            DatasetGraph dsg = action.getActiveDSG() ;
//            // log.info(format("[%d] ** Content-length: %d", action.id,
//            // action.request.getContentLength())) ;
//
//            String name = action.request.getRequestURL().toString() ;
//            if ( !name.endsWith("/") )
//                name = name + "/" ;
//            name = name + (++counter) ;
//            Node gn = NodeFactory.createURI(name) ;
//            Graph g = dsg.getGraph(gn) ;
//            StreamRDF dest = StreamRDFLib.graph(g) ;
//            LangRIOT parser = RiotReader.createParser(action.request.getInputStream(), lang, name, dest) ;
//            parser.parse() ;
//            action.log.info(format("[%d] Location: %s", action.id, name)) ;
//            action.response.setHeader("Location", name) ;
//            action.commit() ;
//            ServletOps.successCreated(action) ;
//        } catch (IOException ex) {
//            action.abort() ;
//        } finally {
//            action.endWrite() ;
//        }
//    }
}
