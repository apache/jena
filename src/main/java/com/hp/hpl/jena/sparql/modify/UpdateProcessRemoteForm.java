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

package com.hp.hpl.jena.sparql.modify;

import java.util.ArrayList ;
import java.util.HashMap ;
import java.util.List ;
import java.util.Map ;

import org.apache.http.protocol.HttpContext ;
import org.apache.jena.atlas.lib.Pair ;
import org.apache.jena.riot.web.HttpOp ;
import org.apache.jena.riot.web.HttpResponseHandler ;
import org.apache.jena.riot.web.HttpResponseLib ;

import com.hp.hpl.jena.sparql.ARQException ;
import com.hp.hpl.jena.sparql.util.Context ;
import com.hp.hpl.jena.sparql.util.Symbol ;
import com.hp.hpl.jena.update.GraphStore ;
import com.hp.hpl.jena.update.UpdateProcessor ;
import com.hp.hpl.jena.update.UpdateRequest ;

/**
 * UpdateProcess that send the request to a SPARQL endpoint by using an HTML form and POST
 * Use of application/sparql-update via @link{UpdateProcessRemote} is preferred.
 */
public class UpdateProcessRemoteForm implements UpdateProcessor
{
    public static final Symbol HTTP_CONTEXT = Symbol.create("httpContext") ;
    
    private final UpdateRequest request ;
    private final String endpoint ;
    private final Context context ;
    
    public UpdateProcessRemoteForm(UpdateRequest request , String endpoint, Context context )
    {
        this.request = request ;
        this.endpoint = endpoint ;
        this.context = Context.setupContext(context, null) ;
    }

    @Override
    public GraphStore getGraphStore()
    {
        return null ;
    }

    @Override
    public void execute()
    {
        if ( endpoint == null )
            throw new ARQException("Null endpoint for remote update by form") ;
        String reqStr = request.toString() ;
        List<Pair<String, String>> params = new ArrayList<Pair<String, String>>() ;
        params.add(Pair.create("update", reqStr)) ;
        Map<String, HttpResponseHandler> handlers = new HashMap<String, HttpResponseHandler>() ;
        //handlers.put("text/html", HttpResponseLib.nullResponse) ;
        handlers.put("*", HttpResponseLib.nullResponse) ;
        HttpOp.execHttpPostForm(endpoint, params, handlers, getHttpContext()) ;
    }

    @Override
    public Context getContext()
    {
        return context ;
    }
    
    /**
     * Convenience method to set the {@link HttpContext}
     * @param httpContext
     */
    public void setHttpContext(HttpContext httpContext) {
        getContext().put(HTTP_CONTEXT, httpContext) ;
    }
    
    /**
     * Convenience method to get the {@link HttpContext}
     * @returns HttpContext
     */
    public HttpContext getHttpContext() {
        return (HttpContext) getContext().get(HTTP_CONTEXT);
    }
}

