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

package org.apache.jena.fuseki.servlets;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.jena.fuseki.server.DataService;
import org.apache.jena.fuseki.server.Operation;
import org.apache.jena.riot.WebContent;

/** The global mapping of content-type to Operation. */

public class Dispatch {
    /** Map ContentType (lowercase, no charset) to the {@code Operation} for handling it. */  
    public static Map<String, Operation> contentTypeToOperation = new ConcurrentHashMap<>();
    static {
        contentTypeToOperation.put(WebContent.contentTypeSPARQLQuery, Operation.Query);
        contentTypeToOperation.put(WebContent.contentTypeSPARQLUpdate, Operation.Update);
    }
    
    /** Map {@link Operation} to servlet handler.
     * {@code Operation}s are the internal symbol identifying an operation,
     * not the name used in the configuration file, 
     * which is mapped by {@link DataService#getEndpoint(String)}. 
     */  
    public static Map<Operation, ActionService> operationToHandler = new ConcurrentHashMap<>();
    
    public static final ActionService queryServlet    = new SPARQL_QueryDataset() ;
    public static final ActionService updateServlet   = new SPARQL_Update() ;
    public static final ActionService uploadServlet   = new SPARQL_Upload() ;
    public static final ActionService gspServlet_R    = new SPARQL_GSP_R() ;
    public static final ActionService gspServlet_RW   = new SPARQL_GSP_RW() ;
    public static final ActionService restQuads_R     = new REST_Quads_R() ;
    public static final ActionService restQuads_RW    = new REST_Quads_RW() ;

    static {
        operationToHandler.put(Operation.Query,    queryServlet);
        operationToHandler.put(Operation.Update,   updateServlet);
        operationToHandler.put(Operation.Upload,   uploadServlet);
        operationToHandler.put(Operation.GSP_R,    gspServlet_R);
        operationToHandler.put(Operation.GSP_RW,   gspServlet_RW);
        operationToHandler.put(Operation.Quads_R,  restQuads_R);
        operationToHandler.put(Operation.Quads_RW, restQuads_RW);
    }
}
