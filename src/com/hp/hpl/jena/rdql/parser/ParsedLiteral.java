/*
 * (c) Copyright 2001, 2002, 2003, 2004, Hewlett-Packard Development Company, LP
 * [See end of file]
 */

package com.hp.hpl.jena.rdql.parser;


import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.rdql.*;
import com.hp.hpl.jena.graph.query.IndexValues ;
import com.hp.hpl.jena.graph.impl.LiteralLabel;

import java.io.PrintWriter;

// An implementation of value that is created from the parsing process.
// Explict declaration of the Expr interface causes Eclipse to put it
// in the type hierarchy directly which helps development.

public class ParsedLiteral extends ExprNode implements Expr, NodeValue
{
    // Used to create resources and literals
    static Model model = ModelFactory.createDefaultModel() ;

    protected boolean isSet = false ;

    private boolean isInt = false ;
    private boolean isBoolean = false ;
    private boolean isDouble = false ;
    private boolean isURI = false ;
    private boolean isString = false ;
    private boolean isGraphNode = false ;
    
    //private boolean isRDFResource = false ;
    //private boolean isRDFLiteral = false ;

    private long valInt ;
    private boolean valBoolean ;
    private double valDouble ;
    private String valString ;
    private String valURI ;
    private com.hp.hpl.jena.graph.Node valGraphNode ;
    
    //private Literal valRDFLiteral ;
    //private Resource valRDFResource ;  
    
    // Constructors used by the parser
    ParsedLiteral(int id) { super(id); }

    ParsedLiteral(RDQLParser p, int id) { super(p, id); }

    public ParsedLiteral() { super(-1) ; unset() ; }
    
    // Used by working var to clone values.
    protected ParsedLiteral(NodeValue v)
    {
        super(-1) ;
        if ( v.isBoolean() )
        {
            _setBoolean(v.getBoolean()) ;
            return ;
        }
            
        if ( v.isInt() )
        {
            _setInt(v.getInt()) ;
            return ;
        }

        if ( v.isDouble() )
        {
            _setDouble(v.getDouble()) ;
            return ;
        }

        if ( v.isURI() )
        {
            _setURI(v.getURI()) ;
            return ;
        }

        if ( v.isNode() )
        {
            _setNode(v.getNode()) ;
            return ;
        }
        
        if ( v.isString() )
        {
            _setString(v.getString()) ;
            return ;
        }

    }
        


    protected void unset()
    {
        isSet = false ;
        valString = null ;
        valGraphNode = null ;
        valInt = 0 ;
        valBoolean = false ;
        valDouble = 0 ;
        valURI = null ;
        valGraphNode = null ;       

        isInt = false ;
        isBoolean = false ;
        isDouble = false ;
        isURI = false ;
        isString = false ;
    }

    public NodeValue eval(Query q, IndexValues env)
    {
        if ( ! isSet )
            throw new EvalFailureException("Literal value not set") ;

         return this ;
    }

    public boolean isSet() { return isSet ; }

    public boolean isNumber()       { forceNumber() ; return isSet && (isInt || isDouble) ; }
    public boolean isInt()          { forceInt() ;    return isSet && isInt ; }
    public boolean isDouble()       { forceDouble() ; return isSet && isDouble ; }
    public boolean isBoolean()      { return isSet && isBoolean ; }
    public boolean isString()       { return isSet && isString ; }
    public boolean isURI()          { return isSet && isURI ; }
    public boolean isNode()         { return isSet && isGraphNode ; }
    

    protected void _setInt(long i)               { unset() ; isSet = true ; isInt = true ; valInt = i ; }
    protected void _setDouble(double d)          { unset() ; isSet = true ; isDouble = true ; valDouble = d ; }
    protected void _setBoolean(boolean b)        { unset() ; isSet = true ; isBoolean = true ; valBoolean = b ; }
    protected void _setString(String s)          { unset() ; isSet = true ; isString = true ; valString = s ; }
    protected void _setURI(String uri)           { unset() ; isSet = true ; isURI = true ; isString = true ; valURI = uri ; valString = uri ; }
    
    protected void _setNode(com.hp.hpl.jena.graph.Node n)
    {
        unset();
        isSet = true;
        isGraphNode = true;
        valGraphNode = n ;
        isString = false ;
        valString = null ;
        
        if ( n.isLiteral() )
            valString = n.getLiteral().getLexicalForm() ;
        if ( n.isURI() )
        {
            valString = n.getURI() ;
            valURI = n.getURI() ;
            isURI = true ;
        }
        if ( n.isBlank() )
            valString = n.getBlankNodeId().toString() ;
        
        if ( valString != null )
            isString = true ;
    }
    
    private void forceInt()
    {
        if ( ! isSet || isInt || ! isString ) return ;
        try {
            valInt = Long.parseLong(valString) ;
            isInt = true ;
            isDouble = true ;
            valDouble = valInt ;
        } catch (NumberFormatException e) { return ; }
    }

    private void forceDouble()
    {
        if ( ! isSet || isDouble || ! isString ) return ;
        try {
            valDouble = Double.parseDouble(valString) ;
            isDouble = true ;
        } catch (NumberFormatException e) { return ; }
    }

    private void forceNumber()
    {
        if ( ! isSet || isInt || isDouble || ! isString )
                return ;
        
        forceInt() ;
        if ( ! isInt )
            forceDouble() ;
    }


    public long getInt()
    {
        if ( ! isSet || ! isInt ) throw new ValueException("Not an int: "+this) ;
        return valInt ;
    }

    public double getDouble()
    {
        if ( ! isSet || ! ( isDouble || isInt ) ) throw new ValueException("Not a double: "+this) ;
        if ( isInt )
            return valInt ;
        return valDouble ;
    }

    public boolean getBoolean()
    {
        if ( ! isSet || ! isBoolean ) throw new ValueException("Not a boolean: "+this) ;
        return valBoolean ;
    }

    public String getString()
    {
        if ( ! isSet || ! isString ) throw new ValueException("Not a string: "+this) ;
        return valString ;
    }

    public String getURI()
    {
        if ( ! isSet || ! isURI ) throw new ValueException("Not a URI: "+this) ;
        return valURI ;
    }

    public com.hp.hpl.jena.graph.Node getNode()
    {
        if ( ! isSet ) throw new ValueException("Not a graph node: "+this) ;
        return valGraphNode ;
    }
    
    // Expressions
    
    // -- Constants (literals - but that name is confusing with RDF literals). 
    public boolean isConstant()      { return true; }
    // This may be null (as it is not a jena.graph value).
    public Object getValue()         { return getNode() ; }
        
    // In all these stringification operations, order matters e.g. URI before string
    public String asQuotedString()
    {
        if ( ! isSet ) return "literal:unset" ;
        if ( isInt ) return Long.toString(valInt) ;
        if ( isDouble ) return Double.toString(valDouble) ;
        if ( isBoolean ) return (valBoolean?"true":"false") ;
        
        if ( isGraphNode )
        {
            if ( valGraphNode.isLiteral() )
            {
                StringBuffer sBuff = new StringBuffer() ;
                
                LiteralLabel l = valGraphNode.getLiteral() ;
                sBuff.append('"') ;
                sBuff.append(l.getLexicalForm()) ;
                sBuff.append('"') ;

                String dt = l.getDatatypeURI() ;
                if ( dt != null ) { sBuff.append("^^") ; sBuff.append(dt) ; }
                
                String lang = l.language() ;
                if ( lang != null ) { sBuff.append("@") ; sBuff.append(lang) ; }
            }

            if ( valGraphNode.isURI() )
                valString = "<"+valGraphNode.getURI()+">" ;
            if ( valGraphNode.isBlank() )
                valString = valGraphNode.getBlankNodeId().toString() ;
        }
            
        // Escaping needed
        if ( isURI ) return "<"+valURI+">" ;
        // Escaping needed
        if ( isString ) return "\""+valString+"\"" ;

        return "literal:unknown" ;
    }

    // Does not quote strings or URIs
    public String asUnquotedString()
    {
        if ( ! isSet ) return "literal:unset" ;
        if ( isInt ) return Long.toString(valInt) ;
        if ( isDouble ) return Double.toString(valDouble) ;
        if ( isBoolean ) return (valBoolean?"true":"false") ;
        if ( isURI ) return valURI ;
        if ( isString ) return valString ;
        if ( isGraphNode ) return valGraphNode.toString() ;

        return "literal:unknown" ;
    }

    public String asInfixString() { return asQuotedString() ; }

    public String asPrefixString()
    {
        if ( ! isSet ) return "literal:unset" ;
        if ( isInt ) return "int:"+Long.toString(valInt) ;
        if ( isDouble ) return "double:"+Double.toString(valDouble) ;
        if ( isBoolean ) return "boolean:"+(valBoolean?"true":"false") ;
        if ( isURI ) return "URI:"+valURI ;
        if ( isString ) return "string:"+valString ;
        if ( isGraphNode) return "node:"+valGraphNode ;
        
        return "literal:unknown" ;
    }

    // Print prefix notation (multiline) for debugging
    public void print(PrintWriter pw, int level)
    {
        QueryPrintUtils.indent(pw, level) ;
        pw.println(this.asPrefixString()) ;
    }

	// Subclasses may override this.
	public String valueString() { return asUnquotedString() ; }

    // This is used in the filtering stage to get values for testing - must be unquoted.
    public String toString()
    {
        return asUnquotedString() ;
    }
    
	// Used by QueryResultsMem to build values

	public static ParsedLiteral makeString(String s)
	{
		ParsedLiteral l = new ParsedLiteral(0) ; 
		l._setString(s) ;
		return l ;
	}
	
	// Q_URI is not public.
	public static Q_URI makeURI(String s) { return Q_URI.makeURI(s) ; }
}

/*
 *  (c) Copyright 2001, 2002, 2003, 2004 Hewlett-Packard Development Company, LP
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
