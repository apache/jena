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

import javax.servlet.http.HttpServletRequest ;

import org.apache.jena.fuseki.HttpNames ;

import com.hp.hpl.jena.query.Dataset ;
import com.hp.hpl.jena.query.DatasetFactory ;
import com.hp.hpl.jena.query.Query ;
import com.hp.hpl.jena.sparql.core.DatasetDescription ;

public class SPARQL_QueryDataset extends SPARQL_Query
{
    public SPARQL_QueryDataset(boolean verbose)
    { super(verbose) ; }

    public SPARQL_QueryDataset()
    { this(false) ; }
    
    @Override
    protected void validate(HttpServletRequest request)
    {
        String method = request.getMethod().toUpperCase() ;
        
        if ( ! HttpNames.METHOD_POST.equals(method) && ! HttpNames.METHOD_GET.equals(method) )
            errorMethodNotAllowed("Not a GET or POST request") ;
        
        if ( HttpNames.METHOD_GET.equals(method) && request.getQueryString() == null )
        {
            warning("Service Description / SPARQL Query / "+request.getRequestURI()) ;
            errorNotFound("Service Description: "+request.getRequestURI()) ;
        }
        
        // Use of the dataset describing parameters is check later.
        validate(request, allParams) ;
    }

    @Override
    protected void validateQuery(HttpActionQuery action, Query query)
    { }
   
    @Override
    protected Dataset decideDataset(HttpActionQuery action, Query query, String queryStringLog) 
    { 
        // Protocol.
        DatasetDescription dsDesc = getDatasetDescription(action) ;
        if (dsDesc != null )
            warning("SPARQL Query: Ignoring dataset description in the protocol request") ;  
        
        return DatasetFactory.create(action.getActiveDSG()) ;
    }
}
