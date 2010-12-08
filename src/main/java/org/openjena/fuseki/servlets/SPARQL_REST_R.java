/*
 * (c) Copyright 2010 Epimorphics Ltd.
 * All rights reserved.
 * [See end of file]
 */

package org.openjena.fuseki.servlets;

import static java.lang.String.format ;
import static org.openjena.fuseki.Fuseki.serverlog ;

import java.io.IOException ;

import org.openjena.fuseki.FusekiLib ;
import org.openjena.fuseki.HttpNames ;
import org.openjena.fuseki.conneg.MediaType ;
import org.openjena.fuseki.conneg.TypedOutputStream ;
import org.openjena.riot.Lang ;

import com.hp.hpl.jena.rdf.model.Model ;
import com.hp.hpl.jena.rdf.model.ModelFactory ;
import com.hp.hpl.jena.rdf.model.RDFWriter ;

/** Only the READ operations */
public class SPARQL_REST_R extends SPARQL_REST
{
    public SPARQL_REST_R(boolean verbose)
    { super(verbose) ; }

    public SPARQL_REST_R()
    { this(false) ; }

    @Override
    protected void doGet(HttpActionREST action)
    {
        try {
            // Creating target creates the graph in some datasets.
            if ( ! action.target.isDefault )
            {
                if ( ! action.dsg.containsGraph(action.target.graphName) )
                    SPARQL_ServletBase.errorNotFound("No such graph: "+action.target.name) ;
            }

            MediaType mediaType = contentNegotationRDF(action) ; 
            TypedOutputStream out = new TypedOutputStream(action.response.getOutputStream(), mediaType) ;
            Lang lang = FusekiLib.langFromContentType(mediaType.getContentType()) ;

            if ( action.verbose )
            {
                serverlog.info(format("[%d]   Get: Content-Type=%s, Charset=%s => %s", 
                                      action.id, mediaType.getContentType(), mediaType.getCharset(), lang.getName())) ;
            }

            action.beginRead() ;
            try {
                // If we want to set the Content-Length, we need to buffer.
                //response.setContentLength(??) ;
                RDFWriter writer = FusekiLib.chooseWriter(lang) ;
                Model model = ModelFactory.createModelForGraph(action.target.graph()) ;
                writer.write(model, action.response.getOutputStream(), null) ;
                success(action) ;
            } finally { action.endRead() ; }
        } catch (IOException ex) { errorOccurred(ex) ; }
    }
    
    @Override
    protected void doOptions(HttpActionREST action)
    {
        action.response.setHeader(HttpNames.hAllow, "GET,HEAD,OPTIONS") ;
        success(action) ;
    }

    @Override
    protected void doHead(HttpActionREST action)
    {
        if ( ! action.target.alreadyExisted )
        {
            successNotFound(action) ;
            return ;
        }
        MediaType mediaType = contentNegotationRDF(action) ;
        success(action) ;
    }

    @Override
    protected void doPost(HttpActionREST action)
    { errorMethodNotAllowed("POST") ; }

    @Override
    protected void doDelete(HttpActionREST action)
    { errorMethodNotAllowed("DELETE") ; }

    @Override
    protected void doPut(HttpActionREST action)
    { errorMethodNotAllowed("PUT") ; }

    @Override
    protected void doPatch(HttpActionREST action)
    { errorMethodNotAllowed("PATCH") ; }
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