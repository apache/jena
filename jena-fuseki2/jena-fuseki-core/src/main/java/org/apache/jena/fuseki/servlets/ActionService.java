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

import static org.apache.jena.fuseki.server.CounterName.Requests;
import static org.apache.jena.fuseki.server.CounterName.RequestsBad;
import static org.apache.jena.fuseki.server.CounterName.RequestsGood;
import static org.apache.jena.fuseki.servlets.ActionExecLib.incCounter;

import org.apache.jena.atlas.RuntimeIOException;
import org.apache.jena.fuseki.server.CounterSet;
import org.apache.jena.query.QueryCancelledException;

public abstract class ActionService extends ActionBase {

    /** Add counters to the validate-execute lifecycle. */
    @Override
    protected void executeLifecycle(HttpAction action) {
        // And also HTTP counter
        
        CounterSet csService = 
            (action.getDataService() == null) ? null : action.getDataService().getCounters();
        CounterSet csOperation = null;
        if ( action.getEndpoint() != null )
            // Direct naming GSP does not have an "endpoint".
            csOperation = action.getEndpoint().getCounters();

        incCounter(csService, Requests);
        incCounter(csOperation, Requests);
        // Either exit this via "bad request" on validation
        // or in execution in perform.
        try {
            validate(action);
        }
        catch (ActionErrorException ex) {
            incCounter(csOperation, RequestsBad);
            incCounter(csService, RequestsBad);
            throw ex;
        }

        try {
            execute(action);
            // Success
            incCounter(csOperation, RequestsGood);
            incCounter(csService, RequestsGood);
        }
        catch (ActionErrorException | QueryCancelledException | RuntimeIOException ex) {
            incCounter(csOperation, RequestsBad);
            incCounter(csService, RequestsBad);
            throw ex;
        }
    }
}
