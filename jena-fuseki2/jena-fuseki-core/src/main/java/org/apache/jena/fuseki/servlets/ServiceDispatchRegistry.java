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
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.ServletContext;

import org.apache.jena.atlas.logging.Log;
import org.apache.jena.fuseki.Fuseki;
import org.apache.jena.fuseki.server.DataService;
import org.apache.jena.fuseki.server.Operation;
import org.apache.jena.riot.WebContent;

/**
 * Mapping of dispatching operations. {@link Operation} is the operation reference (name)
 * but the {@link Operation} does not carry the implementation. The registry, which is
 * per-server, maps:
 * <ul>
 * <li>Content-type to {@code Operation}.
 * <li>{@code Operation} to {@link ActionService} implementation
 * </ul>
 */

public class ServiceDispatchRegistry {

    public static final ActionService queryServlet    = new SPARQL_QueryDataset() ;
    public static final ActionService updateServlet   = new SPARQL_Update() ;
    public static final ActionService uploadServlet   = new SPARQL_Upload() ;
    public static final ActionService gspServlet_R    = new SPARQL_GSP_R() ;
    public static final ActionService gspServlet_RW   = new SPARQL_GSP_RW() ;
    public static final ActionService restQuads_R     = new REST_Quads_R() ;
    public static final ActionService restQuads_RW    = new REST_Quads_RW() ;
    
    /** Map ContentType (lowercase, no charset) to the {@code Operation} for handling it. */  
    private final Map<String, Operation> contentTypeToOperation = new ConcurrentHashMap<>();
    public Map<String, Operation> contentTypeToOperation() { return contentTypeToOperation; } 
    
    /** Map {@link Operation} to servlet handler.
     * {@code Operation}s are the internal symbol identifying an operation,
     * not the name used in the configuration file, 
     * which is mapped by {@link DataService#getEndpoint(String)}. 
     */  
    private final Map<Operation, ActionService> operationToHandler = new ConcurrentHashMap<>();
    public Map<Operation, ActionService> operationToHandler() { return operationToHandler; } 
    
    public ServiceDispatchRegistry(ServiceDispatchRegistry other) {
        contentTypeToOperation.putAll(other.contentTypeToOperation);
        operationToHandler.putAll(other.operationToHandler);
    }
    
    public ServiceDispatchRegistry(boolean includeStdConfig) {
        if ( includeStdConfig ) {
            register(Operation.Query, WebContent.contentTypeSPARQLQuery, queryServlet);
            register(Operation.Update, WebContent.contentTypeSPARQLUpdate, updateServlet);
            register(Operation.Upload,   null, uploadServlet);
            register(Operation.GSP_R,    null, gspServlet_R);
            register(Operation.GSP_RW,   null, gspServlet_RW);
            register(Operation.Quads_R,  null, restQuads_R);
            register(Operation.Quads_RW, null, restQuads_RW);
        }
    }
    
    /** Find the {@link Operation} for a {@code Content-Type}, or return null. */ 
    public Operation findOperation(String contentType) {
        if ( contentType == null )
            return null;
        return contentTypeToOperation.get(contentType);
    }
    
    /** Find the {@link ActionService} implementation for an {@link Operation}, or return null..*/ 
    public ActionService findHandler(Operation operation) {
        if ( operation == null )
            return null;
        return operationToHandler.get(operation);
    }
    
    public boolean isRegistered(Operation operation) {
        return operationToHandler.containsKey(operation);
    }

    /**
     * Register a new {@link Operation}, with its {@code Content-Type} (may be null,
     * meaning no dispatch by content type), and the implementation handler.
     * <p>
     * The application needs to enable an operation on a service endpoint.
     * <p>
     * Replaces any existing registration.  
     */
    public void register(Operation operation, String contentType, ActionService action) {
        Objects.requireNonNull(operation);
        Objects.requireNonNull(action);
        if ( contentType != null )
            contentTypeToOperation.put(contentType, operation);
        else
            // Remove any mapping.
            contentTypeToOperation.values().remove(operation);
        operationToHandler.put(operation, action);
    }
    
    // The server DataAccessPointRegistry is held in the ServletContext for the server.
    
    public static ServiceDispatchRegistry get(ServletContext servletContext) {
        ServiceDispatchRegistry registry = (ServiceDispatchRegistry)servletContext.getAttribute(Fuseki.attrServiceRegistry) ;
        if ( registry == null )
            Log.warn(ServiceDispatchRegistry.class, "No service registry for ServletContext") ;
        return registry ;
    }
    
    public static void set(ServletContext cxt, ServiceDispatchRegistry registry) {
        cxt.setAttribute(Fuseki.attrServiceRegistry, registry) ;
    }
}
