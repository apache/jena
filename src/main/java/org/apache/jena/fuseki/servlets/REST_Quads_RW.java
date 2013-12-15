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

import org.apache.jena.fuseki.FusekiLib ;
import org.apache.jena.riot.RiotException ;
import org.apache.jena.riot.system.StreamRDF ;
import org.apache.jena.riot.system.StreamRDFLib ;

import com.hp.hpl.jena.sparql.core.DatasetGraph ;
import com.hp.hpl.jena.sparql.core.DatasetGraphFactory ;

/**
 * Servlet for operations directly on a dataset - REST(ish) behaviour on the
 * dataset URI.
 */

public class REST_Quads_RW extends REST_Quads_R {

    public REST_Quads_RW() {
        super() ;
    }

    @Override
    protected void validate(HttpAction action) { }

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
