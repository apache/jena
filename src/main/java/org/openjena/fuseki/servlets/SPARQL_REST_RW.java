/*
 * (c) Copyright 2010 Epimorphics Ltd.
 * All rights reserved.
 * [See end of file]
 */

package org.openjena.fuseki.servlets;

import com.hp.hpl.jena.sparql.core.DatasetGraph ;

/** The WRITE operations added to the READ oeprations */
public class SPARQL_REST_RW extends SPARQL_REST_R
{
    public SPARQL_REST_RW(boolean verbose)
    { super(verbose) ; }

    public SPARQL_REST_RW()
    { this(false) ; }

    @Override
    protected void doDelete(HttpActionREST action)
    {
        action.beginWrite() ;
        try {
            deleteGraph(action) ;
        } finally { action.endWrite() ; }
        SPARQL_ServletBase.successNoContent(action) ;
    }

    @Override
    protected void doPut(HttpActionREST action)
    {
        boolean existedBefore = action.target.alreadyExisted ; 
        DatasetGraph body = parseBody(action) ;
        action.beginWrite() ;
        try {
            clearGraph(action.target) ;
            //deleteGraph(target) ;   // Opps. Deletes the target!
            addDataInto(body.getDefaultGraph(), action.target) ;
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
        boolean existedBefore = action.target.alreadyExisted ; 
        DatasetGraph body = parseBody(action) ;
        action.beginWrite() ;
        try {
            addDataInto(body.getDefaultGraph(), action.target) ;
        } finally { action.endWrite() ; }
        if ( existedBefore )
            SPARQL_ServletBase.successNoContent(action) ;
        else
            SPARQL_ServletBase.successCreated(action) ;
    }
}

/*
 * (c) Copyright 2010 Epimorphics Ltd.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. The name of the author may not be used to endorse or promote products
 *    derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */