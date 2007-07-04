/*
 	(c) Copyright 2007 Hewlett-Packard Development Company, LP
 	All rights reserved.
 	$Id: NodeCreateUtils.java,v 1.1 2007-07-04 15:21:57 chris-dollin Exp $
*/

package com.hp.hpl.jena.graph.test;

import com.hp.hpl.jena.datatypes.xsd.XSDDatatype;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.impl.LiteralLabel;
import com.hp.hpl.jena.rdf.model.AnonId;
import com.hp.hpl.jena.shared.*;

/**
    Creating nodes from string specifications.
*/
public class NodeCreateUtils
    {
    /**
        Returns a Node described by the string, primarily for testing purposes.
        The string represents a URI, a numeric literal, a string literal, a bnode label,
        or a variable.        
        <ul>
        <li> 'some text' :: a string literal with that text
        <li> 'some text'someLanguage:: a string literal with that text and language
        <li> 'some text'someURI:: a typed literal with that text and datatype
        <li> digits :: a literal [OF WHAT TYPE] with that [numeric] value
        <li> _XXX :: a bnode with an AnonId built from _XXX
        <li> ?VVV :: a variable with name VVV
        <li> &PPP :: to be done
        <li> name:stuff :: the URI; name may be expanded using the Extended map
        </ul>
        @param x the string describing the node
        @return a node of the appropriate type with the appropriate label
    */
    public static Node create( String x )
        { return create( PrefixMapping.Extended, x ); }
    
    /**
    Returns a Node described by the string, primarily for testing purposes.
    The string represents a URI, a numeric literal, a string literal, a bnode label,
    or a variable.        
    <ul>
    <li> 'some text' :: a string literal with that text
    <li> 'some text'someLanguage:: a string literal with that text and language
    <li> 'some text'someURI:: a typed literal with that text and datatype
    <li> digits :: a literal [OF WHAT TYPE] with that [numeric] value
    <li> _XXX :: a bnode with an AnonId built from _XXX
    <li> ?VVV :: a variable with name VVV
    <li> &PPP :: to be done
    <li> name:stuff :: the URI; name may be expanded using the Extended map
    </ul>
    
    @param pm the PrefixMapping for translating pre:X strings
    @param x the string encoding the node to create
    @return a node with the appropriate type and label
    */
    public static Node create( PrefixMapping pm, String x )
        {
        if (x.equals( "" ))
            throw new JenaException( "Node.create does not accept an empty string as argument" );
        char first = x.charAt( 0 );
        if (first == '\'' || first == '\"')
            return Node.createLiteral( newString( pm, first, x ) );
        if (Character.isDigit( first )) 
            return Node.createLiteral( x, "", XSDDatatype.XSDinteger );
        if (first == '_')
            return Node.createAnon( new AnonId( x ) );
        if (x.equals( "??" ))
            return Node.ANY;
        if (first == '?')
            return Node.createVariable( x.substring( 1 ) );
        if (first == '&')
            return Node.createURI( "q:" + x.substring( 1 ) );        
        int colon = x.indexOf( ':' );
        String d = pm.getNsPrefixURI( "" );
        return colon < 0 
            ? Node.createURI( (d == null ? "eh:/" : d) + x )
            : Node.createURI( pm.expandPrefix( x ) )
            ;
        }

    public static String unEscape( String spelling )
        {
        if (spelling.indexOf( '\\' ) < 0) return spelling;
        StringBuffer result = new StringBuffer( spelling.length() );
        int start = 0;
        while (true)
            {
            int b = spelling.indexOf( '\\', start );
            if (b < 0) break;
            result.append( spelling.substring( start, b ) );
            result.append( unEscape( spelling.charAt( b + 1 ) ) );
            start = b + 2;
            }
        result.append( spelling.substring( start ) );
        return result.toString();
        }
    
    public static char unEscape( char ch )
        {
        switch (ch)
        	{
            case '\\':
            case '\"':
            case '\'': return ch;
            case 'n': return '\n';
            case 's': return ' ';
            case 't': return '\t';
            default: return 'Z';
        	}
        }

    public static LiteralLabel literal( PrefixMapping pm, String spelling, String langOrType )
        {
        String content = unEscape( spelling );
        int colon = langOrType.indexOf( ':' );
        return colon < 0 
            ? new LiteralLabel( content, langOrType, false )
            : LiteralLabel.createLiteralLabel( content, "", Node.getType( pm.expandPrefix( langOrType ) ) )
            ;
        }

    public static LiteralLabel newString( PrefixMapping pm, char quote, String nodeString )
        {
        int close = nodeString.lastIndexOf( quote );
        return literal( pm, nodeString.substring( 1, close ), nodeString.substring( close + 1 ) );
        }
    }


/*
 * (c) Copyright 2005, 2006, 2007 Hewlett-Packard Development Company, LP
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