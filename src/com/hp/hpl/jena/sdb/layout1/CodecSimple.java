/*
 * (c) Copyright 2006, 2007 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sdb.layout1;

import java.io.Reader;
import java.io.StringReader;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.rdf.model.AnonId;
import com.hp.hpl.jena.shared.PrefixMapping;
import com.hp.hpl.jena.shared.impl.PrefixMappingImpl;

import com.hp.hpl.jena.sparql.lang.sparql.ParseException;
import com.hp.hpl.jena.sparql.lang.sparql.SPARQLParser;
import com.hp.hpl.jena.sparql.lang.sparql.Token;
import com.hp.hpl.jena.sparql.lang.sparql.TokenMgrError;
import com.hp.hpl.jena.sparql.util.FmtUtils;

import com.hp.hpl.jena.query.Query;


public class CodecSimple implements EncoderDecoder
{
    private PrefixMapping prefixMapping ;
    
    public CodecSimple() { prefixMapping = new PrefixMappingImpl() ; }
    
    public CodecSimple(PrefixMapping pMap) { prefixMapping = pMap ; }
    
    // Does not need to make the string SQL-safe
    public String encode(Node node)
    {
        if ( node.isBlank() )
            return "_:"+node.getBlankNodeId().getLabelString() ;
        String s = FmtUtils.stringForNode(node, prefixMapping) ;
        return s ; 
    }
    
    public Node decode(String s)
    {
        if ( s.startsWith("Double"))
            System.err.println(s) ;
        
        if ( s.startsWith("_:") )
            return Node.createAnon(new AnonId(s.substring(2))) ;
        return stringToNode(s, prefixMapping) ; 
    }
    
    // ParserUtils??
    // -> expr
    // -> GraphTerm
    // -> ??
    static Node stringToNode(String string, PrefixMapping pmap)
    {
        try {
            //            // Hooking direcly into the tokenizer might be more efficient
            //            // if this ever becomes a problem.  Need to resolve qname => URIs.
            //            JavaCharStream str = new JavaCharStream(new StringReader(string)) ;
            //            SPARQLParserTokenManager tMgr = new SPARQLParserTokenManager(str) ;
            //            Token t2 = tMgr.getNextToken() ;
            
            Query query = new Query() ;
            query.setPrefixMapping(pmap) ;
            Reader in = new StringReader(string) ;
            SPARQLParser p = new SPARQLParser(in) ;
            p.setQuery(query) ;
            Node n = p.GraphTerm() ;
            Token t = p.getNextToken() ;
            if ( t.kind != SPARQLParser.EOF )
                throw new ParseException("More to parse: "+t.image) ;
            return n ;
        }
        catch (TokenMgrError ex)
        {
            System.err.println(ex.getMessage()) ;
            ex.printStackTrace(System.err) ;
        }
        catch (ParseException ex)
        {
            if ( ex.currentToken != null )
            {
                int x = ex.currentToken.beginColumn ;
                System.out.println(string) ;
                for ( int i = 0 ; i < x ; i++ )
                    System.err.print(' ') ;
                System.err.println("^^") ;
            }
            System.err.println(ex.getMessage()) ;
        } catch (Throwable th)
        {
            String m = th.getMessage() ;
            if ( m == null )
                m = "No Message" ;
            
            System.err.println(m) ;
            th.printStackTrace(System.err) ;
        }
        return null ;
    }
}

/*
 * (c) Copyright 2006, 2007 Hewlett-Packard Development Company, LP
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