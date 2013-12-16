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


/**
 * Servlet for operations directly on a dataset - REST(ish) behaviour on the
 * dataset URI.
 */

public abstract class REST_Quads extends SPARQL_GSP {
    // Not supported: GSP direct naming.

    public REST_Quads() {
        super() ;
    }

    @Override
    protected void validate(HttpAction action) {
        // Check in the operations itself.
    }

    @Override
    protected void doOptions(HttpAction action) {
        ServletOps.errorMethodNotAllowed("OPTIONS") ;
    }

    @Override
    protected void doHead(HttpAction action) {
        ServletOps.errorMethodNotAllowed("HEAD") ;
    }

    @Override
    protected void doPost(HttpAction action) {
        ServletOps.errorMethodNotAllowed("POST") ;
    }

    @Override
    protected void doPut(HttpAction action) {
        ServletOps.errorMethodNotAllowed("PUT") ;
    }

    @Override
    protected void doDelete(HttpAction action) {
        ServletOps.errorMethodNotAllowed("DELETE") ;
    }

    @Override
    protected void doPatch(HttpAction action) {
        ServletOps.errorMethodNotAllowed("PATCH") ;
    }

//    static int counter = 0 ;
//
//    protected void doPostTriplesGSP(HttpAction action, Lang lang) {
//        // Old code.
//        // Assumes transactional.
//        // Auto create sub-graph on POST    
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
