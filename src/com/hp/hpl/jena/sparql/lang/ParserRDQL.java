/*
 * (c) Copyright 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.lang;

import java.io.Reader ;
import java.io.StringReader ;

import com.hp.hpl.jena.query.Query ;
import com.hp.hpl.jena.query.QueryException ;
import com.hp.hpl.jena.query.QueryParseException ;
import com.hp.hpl.jena.query.Syntax ;
import com.hp.hpl.jena.shared.PrefixMapping ;
import com.hp.hpl.jena.sparql.ARQConstants ;
import com.hp.hpl.jena.sparql.lang.rdql.Q_Query ;
import com.hp.hpl.jena.sparql.lang.rdql.RDQLParser ;
import com.hp.hpl.jena.sparql.util.PrefixMapping2 ;

class ParserRDQL extends Parser
{
    @Override
    protected Query parse$(Query q, String s)
    {
        q.setSyntax(Syntax.syntaxRDQL) ;
        PrefixMapping pm = new PrefixMapping2(ARQConstants.getGlobalPrefixMap(), q.getPrefixMapping()) ;
        q.setPrefixMapping(pm) ;
        
        q.getPrefixMapping().setNsPrefixes(ARQConstants.getGlobalPrefixMap()) ;
        Q_Query query = null ;
        try {
            //ByteArrayInputStream in = new ByteArrayInputStream(s.getBytes()) ;
            Reader in = new StringReader(s) ;
            
            RDQLParser parser = new RDQLParser(in) ;
            parser.CompilationUnit();
            query = (Q_Query)parser.top() ;
            // Post-parsing work on the query tree
            query.rdqlPhase2(q) ;
            
            if ( q.getGraphURIs() != null )
            {
                for ( int i = 0 ; i < q.getGraphURIs().size() ; i++ )
                {
                    // SPARQL resolves FROM IRIs during parsing.
                    // This is the old RDQL parser ...
                    String u = q.getGraphURIs().get(i) ;
                    u = q.getResolver().resolve(u) ;
                    q.getGraphURIs().set(i, u) ;
                }
            }
            return q ;
        }
        catch (QueryParseException qEx) { throw qEx ; }
        catch (com.hp.hpl.jena.sparql.lang.rdql.ParseException ex)
        {
            throw new QueryParseException("Parse error: "+ex.getMessage(),
                                          ex.currentToken.beginLine,
                                          ex.currentToken.beginColumn) ;
        }
        catch (Error e)
        {
            throw new QueryParseException("Parse error: "+e.getMessage(),-1,-1) ;
        }
        catch (Throwable th)
        {
            //e.printStackTrace(System.err) ;
            throw new QueryException("Unexpected throwable: "+th.getMessage(), th) ;
        }
    }
}
/*
 * (c) Copyright 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
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