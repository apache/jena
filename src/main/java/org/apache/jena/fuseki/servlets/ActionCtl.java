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

import org.apache.jena.fuseki.Fuseki ;
import org.apache.jena.fuseki.server.* ;

/** Control/admin request lifecycle */
public abstract class ActionCtl extends ActionBase
{
    protected ActionCtl() { super(Fuseki.adminLog) ; }
    
    @Override
    final
    protected void execCommonWorker(HttpAction action)
    {
        DatasetRef dsRef = null ;

        String datasetUri = mapRequestToDataset(action) ;
        
        if ( datasetUri != null ) {
            dsRef = DatasetRegistry.get().get(datasetUri) ;
            if ( dsRef == null ) {
                errorNotFound("No dataset for URI: "+datasetUri) ;
                return ;
            }
        } else
            dsRef = FusekiConfig.serviceOnlyDatasetRef() ;

        action.setDataset(dsRef) ;
        String uri = action.request.getRequestURI() ;
        String serviceName = ActionLib.mapRequestToService(dsRef, uri, datasetUri) ;
        ServiceRef srvRef = dsRef.getServiceRef(serviceName) ;
        action.setService(srvRef) ;
        executeAction(action) ;
    }

    // Execute - no stats.
    // Intercept point for the UberServlet 
    protected void executeAction(HttpAction action) {
        executeLifecycle(action) ;
    }
    
    // This is the service request lifecycle.
    protected void executeLifecycle(HttpAction action)
    {
        startRequest(action) ;
        perform(action) ;
        finishRequest(action) ;
    }
    
    protected abstract void perform(HttpAction action) ;

    /** Map request to uri in the registry.
     *  null means no mapping done (passthrough). 
     */
    protected String mapRequestToDataset(HttpAction action) 
    {
        return ActionLib.mapRequestToDataset(action.request.getRequestURI()) ;
    }
    
    protected static void incCounter(Counters counters, CounterName name) {
        try {
            if ( counters.getCounters().contains(name) )
                counters.getCounters().inc(name) ;
        } catch (Exception ex) {
            Fuseki.serverLog.warn("Exception on counter inc", ex) ;
        }
    }
    
    protected static void decCounter(Counters counters, CounterName name) {
        try {
            if ( counters.getCounters().contains(name) )
                counters.getCounters().dec(name) ;
        } catch (Exception ex) {
            Fuseki.serverLog.warn("Exception on counter dec", ex) ;
        }
    }
}
