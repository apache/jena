/*
 * (c) Copyright 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.util;

import com.hp.hpl.jena.sparql.algebra.Algebra;
import com.hp.hpl.jena.sparql.algebra.Op;
import com.hp.hpl.jena.sparql.core.QueryCheckException;
import com.hp.hpl.jena.sparql.lang.ParserRegistry;
import com.hp.hpl.jena.sparql.sse.SSE;
import com.hp.hpl.jena.sparql.sse.SSEParseException;
import com.hp.hpl.jena.sparql.sse.WriterSSE;
import com.hp.hpl.jena.sparql.sse.builders.BuildException;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryException;
import com.hp.hpl.jena.query.QueryFactory;

public class QueryUtils
{
    public static void checkQuery(Query query, boolean optimizeAlgebra)
    {
        checkParse(query) ;
        checkOp(query, optimizeAlgebra) ;
    }

    public static void checkOp(Query query, boolean optimizeAlgebra)
    {
        IndentedLineBuffer buff = new IndentedLineBuffer() ;
        Op op = Algebra.compile(query) ;
        if ( optimizeAlgebra )
            op = Algebra.optimize(op) ;
        WriterSSE.out(buff, op, query) ;
        String str = buff.toString() ;
        
        try {
            Op op2 = SSE.parseOp(str) ;
            if ( op.hashCode() != op2.hashCode() )
                throw new QueryCheckException("reparsed algebra expression hashCode does not equal algebra from query") ;
            if ( ! op.equals(op2) )
                throw new QueryCheckException("reparsed algebra expression does not equal query algebra") ;
        } catch (SSEParseException ex)
        { 
            System.err.println(str);
            throw ex ; 
        }      // Breakpoint
        catch (BuildException ex)
        {
            System.err.println(str);
            throw ex ; 
        }
    }
    
    public static void checkParse(Query query)
    {
        if ( ! ParserRegistry.get().containsFactory(query.getSyntax()) )
            return ;
        
        IndentedLineBuffer buff = new IndentedLineBuffer() ;
        query.serialize(buff, query.getSyntax()) ;
        
        String tmp = buff.toString() ;
        
        Query query2 = null ;
        try {
            String baseURI = null ;
            if ( ! query.explicitlySetBaseURI() )
                // Not in query - use the same one (e.g. file read from) .  
                baseURI = query.getBaseURI() ;
            
            query2 = QueryFactory.create(tmp, baseURI, query.getSyntax()) ;
            
            if ( query2 == null )
                return ;
        } catch (UnsupportedOperationException ex)
        {
            // No parser after all.
            return ;
        }
        catch (QueryException ex)
        {
            System.err.println(tmp) ;
            throw new QueryCheckException("could not parse output query", ex) ;
        }
        
        if ( query.hashCode() != query2.hashCode() )
            throw new QueryCheckException("reparsed query hashCode does not equal parsed input query") ;
        
        if ( ! query.equals(query2) ) 
            throw new QueryCheckException("reparsed output does not equal parsed input") ;
    }
}

/*
 * (c) Copyright 2007, 2008, 2009 Hewlett-Packard Development Company, LP
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