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

import org.apache.jena.fuseki.Fuseki ;
import org.apache.jena.fuseki.server.* ;
import org.apache.jena.riot.Lang ;
import org.apache.jena.riot.RiotException ;
import org.apache.jena.riot.RiotReader ;
import org.apache.jena.riot.lang.LangRIOT ;
import org.apache.jena.riot.system.ErrorHandler ;
import org.apache.jena.riot.system.ErrorHandlerFactory ;
import org.apache.jena.riot.system.StreamRDF ;

import com.hp.hpl.jena.query.QueryCancelledException ;

/** SPARQL request lifecycle */
public abstract class ActionSPARQL extends ActionBase
{
    protected ActionSPARQL() { super(Fuseki.requestLog) ; }
    
    protected abstract void validate(HttpAction action) ;
    protected abstract void perform(HttpAction action) ;

    @Override
    protected void execCommonWorker(HttpAction action)
    {
        DatasetRef dsRef = null ;

        String datasetUri = mapRequestToDataset(action) ;
        
        if ( datasetUri != null ) {
            dsRef = DatasetRegistry.get().get(datasetUri) ;
            if ( dsRef == null ) {
                ServletOps.errorNotFound("No dataset for URI: "+datasetUri) ;
                return ;
            }
        } else
            dsRef = FusekiConfig.serviceOnlyDatasetRef() ;

        String uri = action.request.getRequestURI() ;
        String serviceEndpointName = ActionLib.mapRequestToService(dsRef, uri, datasetUri) ;
        ServiceRef srvRef = dsRef.getServiceRef(serviceEndpointName) ;

        action.setRequestRef(dsRef, datasetUri) ;
        action.setService(srvRef, serviceEndpointName) ;
        
        executeAction(action) ;
    }

    // Execute - allow interception before stats added.
    protected void executeAction(HttpAction action) {
        executeLifecycle(action) ;
    }
    
    // This is the service request lifecycle.
    final
    protected void executeLifecycle(HttpAction action)
    {
        incCounter(action.dsRef, Requests) ;
        incCounter(action.srvRef, Requests) ;

        startRequest(action) ;
        try {
            validate(action) ;
        } catch (ActionErrorException ex) {
            incCounter(action.dsRef,RequestsBad) ;
            incCounter(action.srvRef, RequestsBad) ;
            throw ex ;
        }

        try {
            perform(action) ;
            // Success
            incCounter(action.srvRef, RequestsGood) ;
            incCounter(action.dsRef, RequestsGood) ;
        } catch (ActionErrorException ex) {
            incCounter(action.srvRef, RequestsBad) ;
            incCounter(action.dsRef, RequestsBad) ;
            throw ex ;
        } catch (QueryCancelledException ex) {
            incCounter(action.srvRef, RequestsBad) ;
            incCounter(action.dsRef, RequestsBad) ;
            throw ex ;
        } finally {
            finishRequest(action) ;
        }
    }
    
    /** Map request to uri in the registry.
     *  null means no mapping done (passthrough). 
     */
    protected String mapRequestToDataset(HttpAction action) 
    {
        return ActionLib.mapRequestToDataset(action.request.getRequestURI()) ;
    }
    
    protected static void incCounter(Counters counters, CounterName name) {
        if ( counters == null )
            return ;
        try {
            if ( counters.getCounters().contains(name) )
                counters.getCounters().inc(name) ;
        } catch (Exception ex) {
            Fuseki.serverLog.warn("Exception on counter inc", ex) ;
        }
    }
    
    protected static void decCounter(Counters counters, CounterName name) {
        if ( counters == null )
            return ;
        try {
            if ( counters.getCounters().contains(name) )
                counters.getCounters().dec(name) ;
        } catch (Exception ex) {
            Fuseki.serverLog.warn("Exception on counter dec", ex) ;
        }
    }

    public static void parse(HttpAction action, StreamRDF dest, InputStream input, Lang lang, String base) {
            // Need to adjust the error handler.
    //        try { RDFDataMgr.parse(dest, input, base, lang) ; }
    //        catch (RiotException ex) { errorBadRequest("Parse error: "+ex.getMessage()) ; }
            LangRIOT parser = RiotReader.createParser(input, lang, base, dest) ;
            ErrorHandler errorHandler = ErrorHandlerFactory.errorHandlerStd(action.log); 
            parser.getProfile().setHandler(errorHandler) ;
            try { parser.parse() ; } 
            catch (RiotException ex) { ServletOps.errorBadRequest("Parse error: "+ex.getMessage()) ; }
        }
}
