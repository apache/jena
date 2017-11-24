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
import org.apache.jena.fuseki.server.OperationName;
import org.apache.jena.riot.WebContent;

/** The global mapping of content-type to OperationName. */

public class Dispatch {
    /** Map ContentType (lowercase, no charset) to the {@code OperationName} for handling it. */  
    public static Map<String, OperationName> contentTypeToOpName = new ConcurrentHashMap<>();
    static {
        contentTypeToOpName.put(WebContent.contentTypeSPARQLQuery, OperationName.Query);
        contentTypeToOpName.put(WebContent.contentTypeSPARQLUpdate, OperationName.Update);
    }
    
    /** Map {@link OperationName} to servlet handler.
     * {@code OperartionNames} are the internal symbol identifying an operation,
     * not the name used in the configuration file, which is mapped by {@link DataService#getOperation}. 
     *   
     *  */  
    public static Map<OperationName, ActionSPARQL> OpNameToHandler = new ConcurrentHashMap<>();
    
    public static final ActionSPARQL queryServlet    = new SPARQL_QueryDataset() ;
    public static final ActionSPARQL updateServlet   = new SPARQL_Update() ;
    public static final ActionSPARQL uploadServlet   = new SPARQL_Upload() ;
    public static final ActionSPARQL gspServlet_R    = new SPARQL_GSP_R() ;
    public static final ActionSPARQL gspServlet_RW   = new SPARQL_GSP_RW() ;
    public static final ActionSPARQL restQuads_R     = new REST_Quads_R() ;
    public static final ActionSPARQL restQuads_RW    = new REST_Quads_RW() ;

    static {
        OpNameToHandler.put(OperationName.Query,    queryServlet);
        OpNameToHandler.put(OperationName.Update,   updateServlet);
        OpNameToHandler.put(OperationName.Upload,   uploadServlet);
        OpNameToHandler.put(OperationName.GSP_R,    gspServlet_R);
        OpNameToHandler.put(OperationName.GSP_RW,   gspServlet_RW);
        OpNameToHandler.put(OperationName.Quads_R,  restQuads_R);
        OpNameToHandler.put(OperationName.Quads_RW, restQuads_RW);
    }
}
