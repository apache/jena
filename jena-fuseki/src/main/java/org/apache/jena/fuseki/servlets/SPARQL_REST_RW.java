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

import org.apache.jena.fuseki.HttpNames ;

import com.hp.hpl.jena.sparql.core.DatasetGraph ;

/** The WRITE operations added to the READ operations */
public class SPARQL_REST_RW extends SPARQL_REST_R
{
    public SPARQL_REST_RW(boolean verbose)
    { super(verbose) ; }

    public SPARQL_REST_RW()
    { this(false) ; }

    @Override
    protected void doOptions(HttpActionREST action)
    {
        action.response.setHeader(HttpNames.hAllow, "GET,HEAD,OPTIONS,PUT,DELETE,POST");
        action.response.setHeader(HttpNames.hContentLengh, "0") ;
        success(action) ;
    }
    
    @Override
    protected void doDelete(HttpActionREST action)
    {
        action.beginWrite() ;
        try {
            boolean existedBefore = action.getTarget().exists() ; 
            if ( ! existedBefore)
            {
                // commit, not abort, because locking "transactions" don't support abort. 
                action.commit() ;
                errorNotFound("No such graph: "+action.getTarget().name) ;
            }
            deleteGraph(action) ;
            action.commit() ;
        }
        finally { action.endWrite() ; }
        SPARQL_ServletBase.successNoContent(action) ;
    }

    @Override
    protected void doPut(HttpActionREST action)
    {
        DatasetGraph body = parseBody(action) ;
        action.beginWrite() ;
        boolean existedBefore = false ;
        try {
            existedBefore = action.getTarget().exists() ; 
            if ( existedBefore )
                clearGraph(action.getTarget()) ;
            addDataInto(body.getDefaultGraph(), action) ;
            action.commit() ;
        } finally { action.endWrite() ; }
        // Differentiate: 201 Created or 204 No Content 
        if ( existedBefore )
            SPARQL_ServletBase.successNoContent(action) ;
        else
            SPARQL_ServletBase.successCreated(action) ;
    }

    @Override
    protected void doPost(HttpActionREST action)
    {
        DatasetGraph body = parseBody(action) ;
        action.beginWrite() ;
        boolean existedBefore ; 
        try {
            existedBefore = action.getTarget().exists() ; 
            addDataInto(body.getDefaultGraph(), action) ;
            action.commit() ;
        } finally { action.endWrite() ; }
        if ( existedBefore )
            SPARQL_ServletBase.successNoContent(action) ;
        else
            SPARQL_ServletBase.successCreated(action) ;
    }
}
