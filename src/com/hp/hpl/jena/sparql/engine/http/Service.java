/*
 * (c) Copyright 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * (c) Copyright 2010 Epimorphics Ltd.
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.engine.http;

import java.io.InputStream ;

import com.hp.hpl.jena.query.Query ;
import com.hp.hpl.jena.query.QueryExecException ;
import com.hp.hpl.jena.query.ResultSet ;
import com.hp.hpl.jena.query.ResultSetFactory ;
import com.hp.hpl.jena.sparql.algebra.Op ;
import com.hp.hpl.jena.sparql.algebra.OpAsQuery ;
import com.hp.hpl.jena.sparql.algebra.op.OpService ;
import com.hp.hpl.jena.sparql.engine.QueryIterator ;
import com.hp.hpl.jena.sparql.engine.VarRename ;
import com.hp.hpl.jena.sparql.engine.iterator.QueryIteratorResultSet ;
import com.hp.hpl.jena.sparql.mgt.Explain ;
import com.hp.hpl.jena.sparql.util.Context ;

/** Execution of OpService */

public class Service
{
    public static QueryIterator exec(OpService op, Context context)
    {
        if ( ! op.getService().isURI() )
            throw new QueryExecException("Service URI not bound: "+op.getService()) ; 
        
        
        // This relies on the observation that the query was originally correct,
        // so reversing the scope renaming is safe (it merely restores the algebra expression).
        // Any variables that reappear should be internal ones that were hidden by renaming
        // in teh first place.
        // Any substitution is also safe because it replaced variables by values. 
        Op opRemote = VarRename.reverseRename(op.getSubOp(), true) ;

        //Explain.explain("HTTP", opRemote, context) ;
        
        Query query ;
//        if ( op.getServiceElement() != null )
//        {
// does not cope with substitution?
//            query = QueryFactory.make() ;
//            query.setQueryPattern(op.getServiceElement().getElement()) ;
//            query.setQuerySelectType() ;
//            query.setQueryResultStar(true) ;
//        }
//        else
            query = OpAsQuery.asQuery(opRemote) ;
            
        Explain.explain("HTTP", query, context) ;            
        HttpQuery httpQuery = new HttpQuery(op.getService().getURI()) ;
        httpQuery.addParam(HttpParams.pQuery, query.toString() );
        httpQuery.setAccept(HttpParams.contentTypeResultsXML) ;
        InputStream in = httpQuery.exec() ;
        ResultSet rs = ResultSetFactory.fromXML(in) ;
        return new QueryIteratorResultSet(rs) ; 
    }
}

/*
 * (c) Copyright 2007, 2008, 2009 Hewlett-Packard Development Company, LP
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