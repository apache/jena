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

import static org.apache.jena.fuseki.server.CounterName.Requests ;
import static org.apache.jena.fuseki.server.CounterName.RequestsBad ;
import static org.apache.jena.fuseki.server.CounterName.RequestsGood ;

import java.io.InputStream ;

import org.apache.jena.atlas.RuntimeIOException ;
import org.apache.jena.fuseki.Fuseki ;
import org.apache.jena.fuseki.server.* ;
import org.apache.jena.query.QueryCancelledException ;
import org.apache.jena.riot.Lang ;
import org.apache.jena.riot.RDFDataMgr ;
import org.apache.jena.riot.ReaderRIOT ;
import org.apache.jena.riot.RiotException ;
import org.apache.jena.riot.system.ErrorHandler ;
import org.apache.jena.riot.system.ErrorHandlerFactory ;
import org.apache.jena.riot.system.StreamRDF ;

/** SPARQL request lifecycle */
public abstract class ActionSPARQL extends ActionBase
{
    protected ActionSPARQL() { super(Fuseki.actionLog) ; }
    
    protected abstract void validate(HttpAction action) ;
    protected abstract void perform(HttpAction action) ;

    /**
     * Executes common tasks, including mapping the request to the right dataset, setting the dataset into the HTTP
     * action, and retrieving the service for the dataset requested. Finally, it calls the
     * {@link #executeAction(HttpAction)} method, which executes the HTTP Action life cycle.
     * @param action HTTP Action
     */
    @Override
    protected void execCommonWorker(HttpAction action) {
        DataAccessPoint dataAccessPoint ;
        DataService dSrv ;
        
        String datasetUri = mapRequestToDataset(action) ;
        if ( datasetUri != null ) {
            dataAccessPoint = DataAccessPointRegistry.get().get(datasetUri) ;
            if ( dataAccessPoint == null ) {
                ServletOps.errorNotFound("No dataset for URI: "+datasetUri) ;
                return ;
            }
            //dataAccessPoint.
            dSrv = dataAccessPoint.getDataService() ;
            if ( ! dSrv.isAcceptingRequests() ) {
                ServletOps.errorNotFound("Dataset not active: "+datasetUri) ;
                return ;
            }
        } else {
            dataAccessPoint = null ;
            dSrv = DataService.serviceOnlyDataService() ;
        }

        String operationName = mapRequestToOperation(action, dataAccessPoint) ;
        action.setRequest(dataAccessPoint, dSrv) ;
        
        //operationName = ""
        
        Endpoint op = dSrv.getOperation(operationName) ;
        action.setEndpoint(op, operationName);
        executeAction(action) ;
    }

    /** Execute a SPARQL request. Statistics have not been adjusted at this point.
     * 
     * @param action
     */
    protected void executeAction(HttpAction action) {
        executeLifecycle(action) ;
    }
    
    /** 
     * Standard execution lifecycle for a SPARQL Request.
     * <ul>
     * <li>{@link #startRequest(HttpAction)}</li>
     * <li>initial statistics,</li>
     * <li>{@link #validate(HttpAction)} request,</li>
     * <li>{@link #perform(HttpAction)} request,</li>
     * <li>completion/error statistics,</li>
     * <li>{@link #finishRequest(HttpAction)}
     * </ul>
     * 
     * @param action
     */
    // This is the service request lifecycle.
    final
    protected void executeLifecycle(HttpAction action) {
        startRequest(action) ;
        // And also HTTP counter
        CounterSet csService = action.getDataService().getCounters() ;
        CounterSet csOperation = null ;
        if ( action.getEndpoint() != null )
            // Direct naming GSP does not have an "endpoint".
            csOperation = action.getEndpoint().getCounters() ;
        
        incCounter(csService, Requests) ;
        incCounter(csOperation, Requests) ;
        try {
            // Either exit this via "bad request" on validation
            // or in execution in perform. 
            try {
                validate(action) ;
            } catch (ActionErrorException ex) {
                incCounter(csOperation, RequestsBad) ;
                incCounter(csService, RequestsBad) ;
                throw ex ;
            }

            try {
                perform(action) ;
                // Success
                incCounter(csOperation, RequestsGood) ;
                incCounter(csService, RequestsGood) ;
            } catch (ActionErrorException | QueryCancelledException | RuntimeIOException ex) {
                incCounter(csOperation, RequestsBad) ;
                incCounter(csService, RequestsBad) ;
                throw ex ;
            }
        } finally {
            finishRequest(action) ;
        }
    }
    
    /**
     * Map request {@link HttpAction} to uri in the registry.
     * A return of ull means no mapping done (passthrough).
     * @param uri the URI
     * @return the dataset
     */
    protected String mapRequestToDataset(HttpAction action) {
        return ActionLib.mapRequestToDataset(action) ;
    }

    /**
     * Map request to uri in the registry. null means no mapping done
     * (passthrough).
     */
    protected String mapRequestToOperation(HttpAction action, DataAccessPoint dataAccessPoint) {
        return ActionLib.mapRequestToOperation(action, dataAccessPoint) ;
    }

    /** Increment counter */
    protected static void incCounter(Counters counters, CounterName name) {
        if ( counters == null ) return ;
        incCounter(counters.getCounters(), name) ; 
    }
    
    /** Decrement counter */
    protected static void decCounter(Counters counters, CounterName name) {
        if ( counters == null ) return ;
        decCounter(counters.getCounters(), name) ; 
    }

    protected static void incCounter(CounterSet counters, CounterName name) {
        if ( counters == null )
            return ;
        try {
            if ( counters.contains(name) )
                counters.inc(name) ;
        } catch (Exception ex) {
            Fuseki.serverLog.warn("Exception on counter inc", ex) ;
        }
    }
    
    protected static void decCounter(CounterSet counters, CounterName name) {
        if ( counters == null )
            return ;
        try {
            if ( counters.contains(name) )
                counters.dec(name) ;
        } catch (Exception ex) {
            Fuseki.serverLog.warn("Exception on counter dec", ex) ;
        }
    }

    public static void parse(HttpAction action, StreamRDF dest, InputStream input, Lang lang, String base) {
        try {
            ReaderRIOT r = RDFDataMgr.createReader(lang) ;
            if ( r == null )
                ServletOps.errorBadRequest("No parser for language '"+lang.getName()+"'") ;
            ErrorHandler errorHandler = ErrorHandlerFactory.errorHandlerStd(action.log);
            r.setErrorHandler(errorHandler); 
            r.read(input, base, null, dest, null) ; 
        } 
        catch (RiotException ex) { ServletOps.errorBadRequest("Parse error: "+ex.getMessage()) ; }
    }
}
