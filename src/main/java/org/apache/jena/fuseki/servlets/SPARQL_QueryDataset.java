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
import javax.servlet.http.HttpServletResponse ;

import org.apache.jena.fuseki.HttpNames ;

import com.hp.hpl.jena.query.Dataset ;
import com.hp.hpl.jena.query.DatasetFactory ;
import com.hp.hpl.jena.query.Query ;

public class SPARQL_QueryDataset extends SPARQL_Query
{
    public SPARQL_QueryDataset(boolean verbose)
    { super(verbose) ; }

    public SPARQL_QueryDataset()
    { this(false) ; }
    
    static String[] tails = { HttpNames.ServiceQuery, HttpNames.ServiceQueryAlt } ;
    
    @Override
    protected void validate(HttpServletRequest request)
    {
        validate(request, allParams) ;
    }

    @Override
    protected void validateQuery(HttpActionQuery action, Query query)
    { }
   
    @Override
    protected Dataset decideDataset(HttpActionQuery action, Query query, String queryStringLog) 
    { 
        return DatasetFactory.create(action.getActiveDSG()) ;
    }

    @Override
    protected String mapRequestToDataset(String uri)
    {
        for ( String tail : tails )
        {
            String x = mapRequestToDataset(uri, tail) ;
            if ( x != null )
                return x ;
        }
        return uri ; 
    }

    @Override
    protected boolean requestNoQueryString(HttpServletRequest request, HttpServletResponse response)
    {
        if ( HttpNames.METHOD_POST.equals(request.getMethod().toUpperCase()) )
            return true ;
        
        if ( ! HttpNames.METHOD_GET.equals(request.getMethod().toUpperCase()) )
        {
            errorNotImplemented("Not a GET or POST request") ;
            return false ;
        }
        warning("Service Description / SPARQL Query / "+request.getRequestURI()) ;
        errorNotFound("Service Description: "+request.getRequestURI()) ;
        return false ;
    }
}
