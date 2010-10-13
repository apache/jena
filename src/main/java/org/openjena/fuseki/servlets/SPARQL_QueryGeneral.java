/*
 * (c) Copyright 2010 Epimorphics Ltd.
 * All rights reserved.
 * [See end of file]
 */

package org.openjena.fuseki.servlets;

import static org.openjena.fuseki.HttpNames.paramAccept ;
import static org.openjena.fuseki.HttpNames.paramCallback ;
import static org.openjena.fuseki.HttpNames.paramDefaultGraphURI ;
import static org.openjena.fuseki.HttpNames.paramForceAccept ;
import static org.openjena.fuseki.HttpNames.paramNamedGraphURI ;
import static org.openjena.fuseki.HttpNames.paramOutput1 ;
import static org.openjena.fuseki.HttpNames.paramOutput2 ;
import static org.openjena.fuseki.HttpNames.paramQuery ;
import static org.openjena.fuseki.HttpNames.paramQueryRef ;
import static org.openjena.fuseki.HttpNames.paramStyleSheet ;

import java.util.Arrays ;
import java.util.HashSet ;
import java.util.Set ;

import javax.servlet.http.HttpServletRequest ;

import com.hp.hpl.jena.query.Dataset ;
import com.hp.hpl.jena.query.Query ;

public class SPARQL_QueryGeneral extends SPARQL_Query
{
    public SPARQL_QueryGeneral(boolean verbose)
    { super(verbose) ; }

    public SPARQL_QueryGeneral()
    { this(false) ; }

//    // (1) Param to constructor.
//    // (2) DRY : to super class.
//    static String[] tails = { HttpNames.ServiceQuery, HttpNames.ServiceQueryAlt } ;
//    
//    @Override
//    protected String mapRequestToDataset(String uri)
//    {
//        for ( String tail : tails )
//        {
//            String x = mapRequestToDataset(uri, tail) ;
//            if ( x != null )
//                return x ;
//        }
//        return uri ; 
//    }
    
    // All the params we support
    private static String[] params_ = { paramQuery, 
                                        paramDefaultGraphURI, paramNamedGraphURI,
                                        paramQueryRef,
                                        paramStyleSheet,
                                        paramAccept,
                                        paramOutput1, paramOutput2, 
                                        paramCallback, 
                                        paramForceAccept } ;
    private static Set<String> params = new HashSet<String>(Arrays.asList(params_)) ;
    
    @Override
    protected void validate(HttpServletRequest request)
    {
        validate(request, params) ;
    }

    @Override
    protected void validateQuery(HttpActionQuery action, Query query)
    {
        if ( query.hasDatasetDescription() )
            errorBadRequest("Query may not include a dataset description (FROM/FROM NAMED)") ;
    }

    
    @Override
    protected Dataset decideDataset(HttpActionQuery action, Query query, String queryStringLog) 
    {
        errorNotImplemented("General SPARQL query with dataset description") ;
        return null ;
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