/*
 * (c) Copyright 2004, 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * (c) Copyright 2010 Talis Systems Ltd
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.lang;

import java.util.Stack ;

import com.hp.hpl.jena.query.Query ;
import com.hp.hpl.jena.sparql.modify.request.Update ;
import com.hp.hpl.jena.update.UpdateRequest ;

public class ParserQueryBase extends ParserBase 
{
    private Stack<Query> stack = new Stack<Query>() ;
    protected Query query ;

    public void setQuery(Query q)
    { 
        query = q ;
        setPrologue(q) ;
    }

    public Query getQuery() { return query ; }

    // The ARQ parser is both query and update languages.

    // ---- SPARQL/Update (Submission)
    private UpdateRequest requestSubmission = null ;

    protected UpdateRequest getUpdateRequestSubmission() { return requestSubmission ; }
    public void setUpdateRequest(UpdateRequest request)
    {
        setPrologue(request) ;
        this.requestSubmission = request ;
        // And create a query because we may have nested selects.
        this.query = new Query () ;
    }

    // SPARQL Update (W3C RECommendation)
    private com.hp.hpl.jena.sparql.modify.request.UpdateRequest request = null ;

    protected com.hp.hpl.jena.sparql.modify.request.UpdateRequest getUpdate() { return request ; }
    public void setUpdateRequest(com.hp.hpl.jena.sparql.modify.request.UpdateRequest request)
    { 
        this.request = request ;
        setPrologue(request) ;
    }
    
    // Move down to SPARQL 1.1 or rename as ParserBase
    protected void startUpdateOperation() {}// { System.out.println("Start update operation") ; }
    protected void finishUpdateOperation() {}// { System.out.println("Finish update operation") ; }
    
    protected void startUpdateRequest() {}// { System.out.println("Start update request") ; }
    protected void finishUpdateRequest() {}// { System.out.println("Finish update request") ; }
    
    protected void startDataInsert() {}// { System.out.println("Start INSERT DATA") ; }
    protected void finishDataInsert() {}// { System.out.println("Finish INSERT DATA") ; }
    
    protected void startDataDelete() {}// { System.out.println("Start DELETE DATA") ; }
    protected void finishDataDelete() {}// { System.out.println("Finish DELETE DATA") ; }
    
    protected void emitUpdate(Update update)
    {
        request.add(update) ;
    }
    
    protected void startSubSelect()
    {
        // Query is null in an update.
        stack.push(query) ;
        Query subQuery = new Query(getPrologue()) ;
        query = subQuery ;
    }
    
    protected Query endSubSelect(int line, int column)
    {
        Query subQuery = query ;
        if ( ! subQuery.isSelectType() )
            throwParseException("Subquery not a SELECT query", line, column) ;
        query = stack.pop();
        return subQuery ;
    }
}

/*
 * (c) Copyright 2004, 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * (c) Copyright 2010 Talis Systems Ltd
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
