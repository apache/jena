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

import static org.apache.jena.riot.web.HttpNames.* ;

import java.io.IOException ;
import java.util.Locale ;

import javax.servlet.ServletException ;
import javax.servlet.http.HttpServletRequest ;
import javax.servlet.http.HttpServletResponse ;

import org.apache.jena.fuseki.server.CounterName ;

/** Common point for operations that are "REST"ish (use GET/PUT etc as operations). */ 
public abstract class ActionREST extends ActionSPARQL
{
    public ActionREST()
    { super() ; }

    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // Direct all verbs to our common framework.
        doCommon(request, response) ;
    }
    
    @Override
    protected void perform(HttpAction action) {
        dispatch(action) ;
    }

    private void dispatch(HttpAction action) {
        HttpServletRequest req = action.request ;
        HttpServletResponse resp = action.response ;
        String method = req.getMethod().toUpperCase(Locale.ROOT) ;

        if (method.equals(METHOD_GET))
            doGet$(action);
        else if (method.equals(METHOD_HEAD))
            doHead$(action);
        else if (method.equals(METHOD_POST))
            doPost$(action);
        else if (method.equals(METHOD_PATCH))
            doPatch$(action) ;
        else if (method.equals(METHOD_OPTIONS))
            doOptions$(action) ;
        else if (method.equals(METHOD_TRACE))
            //doTrace(action) ;
            ServletOps.errorMethodNotAllowed("TRACE") ;
        else if (method.equals(METHOD_PUT))
            doPut$(action) ;   
        else if (method.equals(METHOD_DELETE))
            doDelete$(action) ;
        else
            ServletOps.errorNotImplemented("Unknown method: "+method) ;
    }

    // Counter wrappers
    
    private final void doGet$(HttpAction action) {
        incCounter(action.getOperation(), CounterName.HTTPget) ;
        try {
            doGet(action) ;
            incCounter(action.getOperation(), CounterName.HTTPgetGood) ;
        } catch ( ActionErrorException ex) {
            incCounter(action.getOperation(), CounterName.HTTPGetBad) ;
            throw ex ;
        }
    }

    private final void doHead$(HttpAction action) {
        incCounter(action.getOperation(), CounterName.HTTPhead) ;
        try {
            doHead(action) ;
            incCounter(action.getOperation(), CounterName.HTTPheadGood) ;
        } catch ( ActionErrorException ex) {
            incCounter(action.getOperation(), CounterName.HTTPheadBad) ;
            throw ex ;
        }
    }

    private final void doPost$(HttpAction action) {
        incCounter(action.getOperation(), CounterName.HTTPpost) ;
        try {
            doPost(action) ;
            incCounter(action.getOperation(), CounterName.HTTPpostGood) ;
        } catch ( ActionErrorException ex) {
            incCounter(action.getOperation(), CounterName.HTTPpostBad) ;
            throw ex ;
        }
    }

    private final void doPatch$(HttpAction action) {
        incCounter(action.getOperation(), CounterName.HTTPpatch) ;
        try {
            doPatch(action) ;
            incCounter(action.getOperation(), CounterName.HTTPpatchGood) ;
        } catch ( ActionErrorException ex) {
            incCounter(action.getOperation(), CounterName.HTTPpatchBad) ;
            throw ex ;
        }
    }

    private final void doDelete$(HttpAction action) {
        incCounter(action.getOperation(), CounterName.HTTPdelete) ;
        try {
            doDelete(action) ;
            incCounter(action.getOperation(), CounterName.HTTPdeleteGood) ;
        } catch ( ActionErrorException ex) {
            incCounter(action.getOperation(), CounterName.HTTPdeleteBad) ;
            throw ex ;
        }
    }

    private final void doPut$(HttpAction action) {
        incCounter(action.getOperation(), CounterName.HTTPput) ;
        try {
            doPut(action) ;
            incCounter(action.getOperation(), CounterName.HTTPputGood) ;
        } catch ( ActionErrorException ex) {
            incCounter(action.getOperation(), CounterName.HTTPputBad) ;
            throw ex ;
        }
    }

    private final void doOptions$(HttpAction action) {
        incCounter(action.getOperation(), CounterName.HTTPoptions) ;
        try {
            doOptions(action) ;
            incCounter(action.getOperation(), CounterName.HTTPoptionsGood) ;
        } catch ( ActionErrorException ex) {
            incCounter(action.getOperation(), CounterName.HTTPoptionsBad) ;
            throw ex ;
        }
    }
    
    protected abstract void doGet(HttpAction action) ;
    protected abstract void doHead(HttpAction action) ;
    protected abstract void doPost(HttpAction action) ;
    protected abstract void doPatch(HttpAction action) ;
    protected abstract void doDelete(HttpAction action) ;
    protected abstract void doPut(HttpAction action) ;
    protected abstract void doOptions(HttpAction action) ;
}
