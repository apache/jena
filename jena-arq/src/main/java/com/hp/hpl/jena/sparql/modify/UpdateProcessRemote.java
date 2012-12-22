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

import org.apache.jena.riot.WebContent ;
import org.apache.jena.riot.web.HttpOp ;

import com.hp.hpl.jena.query.QuerySolution ;
import com.hp.hpl.jena.sparql.ARQException ;
import com.hp.hpl.jena.sparql.util.Context ;
import com.hp.hpl.jena.update.GraphStore ;
import com.hp.hpl.jena.update.UpdateProcessor ;
import com.hp.hpl.jena.update.UpdateRequest ;

/**
 * UpdateProcess that send the request to a SPARQL endpoint by using POST of application/sparql-update.  
 */
public class UpdateProcessRemote implements UpdateProcessor
{
    private final UpdateRequest request ;
    private final String endpoint ;
    
    public UpdateProcessRemote(UpdateRequest request , String endpoint )
    {
        this.request = request ;
        this.endpoint = endpoint ;
    }

    @Override
    public void setInitialBinding(QuerySolution binding)
    {
        throw new ARQException("Initial bindings for a remote update execution request not supported") ;
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
            throw new ARQException("Null endpoint for remote update") ;
        String reqStr = request.toString() ;
        HttpOp.execHttpPost(endpoint, WebContent.contentTypeSPARQLUpdate, reqStr) ;
    }

    @Override
    public Context getContext()
    {
        return null ;
    }
    
}

