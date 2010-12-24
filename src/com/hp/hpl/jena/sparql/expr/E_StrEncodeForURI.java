/*
 * (c) Copyright 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.expr;

import org.openjena.atlas.lib.StrUtils ;

import com.hp.hpl.jena.datatypes.xsd.XSDDatatype ;
import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.sparql.sse.Tags ;

public class E_StrEncodeForURI extends ExprFunction1
{
    private static final String symbol = Tags.tagStrEncodeForURI ;

    public E_StrEncodeForURI(Expr expr)
    {
        super(expr, symbol) ;
    }
    
    @Override
    public NodeValue eval(NodeValue v)
    { 
        String str = plainString(v) ;
        String encStr = StrUtils.encodeHex(str,'%', all) ;
        return NodeValue.makeString(encStr) ;
    }
    
    // Share with ExprDigest
    static String plainString(NodeValue v)
    {
        Node n = v.asNode() ;
        if ( n.getLiteralLanguage() != null && ! n.getLiteralLanguage().equals("") )
            throw new ExprEvalException("Not allowed: RDF term with a language tag") ; 
        if ( ! n.isLiteral() )
            throw new ExprEvalException("Not a literal") ;
        // Literal, no language tag.
        if ( n.getLiteralDatatype() != null && ! XSDDatatype.XSDstring.equals(n.getLiteralDatatype()) )
            throw new ExprEvalException("Not a simple literal nor an XSD string") ;
        return n.getLiteralLexicalForm() ;
    }
    
    // Put somewhere
    static char reserved[] = 
    {' ',
     '!', '*', '"', '\'', '(', ')', ';', ':', '@', '&', 
     '=', '+', '$', ',', '/', '?', '%', '#', '[', ']'} ;

    static char[] other = {'<', '>', '~', '.', '{', '}', '|', '\\', '-', '`', '_', '^'} ;     
    
    static char[] other2 = {'\n', '\r', '\t' } ;
    
    static char[] all = {  ' ', '!', '*', '"', '\'', '(', ')', ';', ':', '@', '&', 
                           '=', '+', '$', ',', '/', '?', '%', '#', '[', ']',
                           '<', '>', '~', '.', '{', '}', '|', '\\', '-', '`', '_', '^',
                           '\n', '\r', '\t'} ;
        
        /* The encodeURI() function is used to encode a URI.
special except: , / ? : @ & = + $ #
special + , / ? : @ & = + $ #
 *
 *
 */
        
    
    @Override
    public Expr copy(Expr expr) { return new E_StrEncodeForURI(expr) ; } 
}

/*
 *  (c) Copyright 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 *  All rights reserved.
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
