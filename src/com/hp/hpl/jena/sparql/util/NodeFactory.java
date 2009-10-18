/*
 * (c) Copyright 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.util;

import com.hp.hpl.jena.datatypes.RDFDatatype ;
import com.hp.hpl.jena.datatypes.TypeMapper ;
import com.hp.hpl.jena.datatypes.xsd.XSDDatatype ;
import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.graph.impl.LiteralLabel ;
import com.hp.hpl.jena.query.QueryParseException ;
import com.hp.hpl.jena.sparql.sse.SSE ;

public class NodeFactory
{
    public static Node parseNode(String nodeString)
    {
        return SSE.parseNode(nodeString) ;
    }
    
    private static QueryParseException makeException(String msg, int line, int column)
    {
        return new QueryParseException(msg, line, column) ;
    }
    
    public static Node createLiteralNode(String lex, String lang, String datatypeURI)
    {
        if ( datatypeURI != null && datatypeURI.equals("") )
            datatypeURI = null ;
        
        if ( lang != null && lang.equals("") )
            lang = null ;
        
        RDFDatatype dType = null ;
        if ( datatypeURI != null )
            dType = TypeMapper.getInstance().getSafeTypeByName(datatypeURI);
        
        Node n = Node.createLiteral(lex, lang, dType) ;
        return n ;
    }
    
    public static int nodeToInt(Node node)
    {
        LiteralLabel lit = node.getLiteral() ;
        
        if ( ! XSDDatatype.XSDinteger.isValidLiteral(lit) )
            return Integer.MIN_VALUE ;
        int i = ((Number)lit.getValue()).intValue() ;
        return i ;
    }
    
    public static long nodeToLong(Node node)
    {
        LiteralLabel lit = node.getLiteral() ;
        
        if ( ! XSDDatatype.XSDinteger.isValidLiteral(lit) )
            return Long.MIN_VALUE ;
        long i = ((Number)lit.getValue()).longValue() ;
        return i ;
    }
    
    public static Node intToNode(int integer)
    {
        return Node.createLiteral(Integer.toString(integer), "", XSDDatatype.XSDinteger) ;
    }

    public static Node intToNode(long integer)
    {
        return Node.createLiteral(Long.toString(integer), "", XSDDatatype.XSDinteger) ;
    }

    public static Node floatToNode(float value)
    {
        return Node.createLiteral(Float.toString(value), "", XSDDatatype.XSDfloat) ;
    }

    public static Node nowAsDateTime()
    {
        String lex = Utils.nowAsXSDDateTimeString() ;
        return Node.createLiteral(lex, null, XSDDatatype.XSDdateTime) ;
    }

    public static Node todayAsDate()
    {
        String lex = Utils.todayAsXSDDateString() ;
        return Node.createLiteral(lex, null, XSDDatatype.XSDdate) ;
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