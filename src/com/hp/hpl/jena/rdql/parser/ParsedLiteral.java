/*
 * (c) Copyright 2001-2003, Hewlett-Packard Company, all rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.rdql.parser;

import com.hp.hpl.jena.rdql.*;
import com.hp.hpl.jena.rdf.model.*;

import java.io.PrintWriter;

// An implementation of value that is created from the parsing process.


public class ParsedLiteral extends SimpleNode implements Value, Expr, Settable
{
    // Used to create resources and literals
    static Model model = ModelFactory.createDefaultModel() ;

    protected boolean isSet = false ;

    protected boolean isInt = false ;
    protected boolean isBoolean = false ;
    protected boolean isDouble = false ;
    protected boolean isURI = false ;
    protected boolean isString = false ;
    protected boolean isRDFResource = false ;
    protected boolean isRDFLiteral = false ;

    protected long valInt ;
    protected boolean valBoolean ;
    protected double valDouble ;
    protected String valString ;
    protected String valURI ;
    protected Literal valRDFLiteral ;
    protected Resource valRDFResource ;  
    
    //RDFNode rdfNode ;

    // Constructors used by the parser
    ParsedLiteral(int id) { super(id); }

    ParsedLiteral(RDQLParser p, int id) { super(p, id); }

    public ParsedLiteral() { super(-1) ; unset() ; }
    public ParsedLiteral(Value v)
    {
        super(-1) ;
        if ( v.isBoolean() )
        {
            setBoolean(v.getBoolean()) ;
            return ;
        }
            
        if ( v.isInt() )
        {
            setInt(v.getInt()) ;
            return ;
        }

        if ( v.isDouble() )
        {
            setDouble(v.getDouble()) ;
            return ;
        }

        if ( v.isURI() )
        {
            setURI(v.getURI()) ;
            return ;
        }

        if ( v.isRDFLiteral() )
        {
            setRDFLiteral(v.getRDFLiteral()) ;
            return ;
        }

        if ( v.isRDFResource())
        {
            setRDFResource(v.getRDFResource()) ;
            return ;
        }
        
        if ( v.isString() )
        {
            setString(v.getString()) ;
            return ;
        }

    }
        


    protected void unset()
    {
        isSet = false ;
        // Throw away any old values

        if ( isString )
            valString = null ;

        if ( isURI )
            valURI = null ;
            
        if ( isRDFResource )
            valRDFResource = null ; 

        if ( isRDFLiteral )
            valRDFLiteral = null ; 

        isInt = false ;
        isBoolean = false ;
        isDouble = false ;
        isURI = false ;
        isString = false ;
    }

    public Value eval(Query q, ResultBinding env)
    {
        if ( ! isSet )
            throw new EvalFailureException("Literal value not set") ;

         return this ;
    }

    public boolean isSet() { return isSet ; }

    public boolean isNumber()       { return isSet && (isInt || isDouble) ; }
    public boolean isInt()          { return isSet && isInt ; }
    public boolean isDouble()       { return isSet && isDouble ; }
    public boolean isBoolean()      { return isSet && isBoolean ; }
    public boolean isString()       { return isSet && isString ; }
    public boolean isURI()          { return isSet && isURI ; }
    public boolean isRDFResource()  { return isSet && isRDFResource ; }
    public boolean isRDFLiteral()   { return isSet && isRDFLiteral ; }
    

    public void setInt(long i)               { unset() ; isSet = true ; isInt = true ; valInt = i ; }
    public void setDouble(double d)          { unset() ; isSet = true ; isDouble = true ; valDouble = d ; }
    public void setBoolean(boolean b)        { unset() ; isSet = true ; isBoolean = true ; valBoolean = b ; }
    public void setString(String s)          { unset() ; isSet = true ; isString = true ; valString = s ; }
    public void setURI(String uri)           { unset() ; isSet = true ; isURI = true ; isString = true ; valURI = uri ; valString = uri ; }
    
    public void setRDFLiteral(Literal l)
    {
        unset();
        isSet = true;
        isString = true ;
        isRDFLiteral = true;
        valString = l.toString() ;
        valRDFLiteral = l;
    }
    
    public void setRDFResource(Resource r)
    {
        unset();
        isSet = true;
        isURI = true ;
        isRDFResource = true;
        valURI = r.toString() ;
        valRDFResource = r;
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

    public Literal getRDFLiteral()
    {
        if ( ! isSet || ! isRDFLiteral ) throw new ValueException("Not a Literal: "+this) ;
        return valRDFLiteral ;
    }

    public Resource getRDFResource()
    {
        if ( ! isSet || ! isRDFResource ) throw new ValueException("Not a Resource: "+this) ;
        return valRDFResource ;
    }



    // In all these stringification operations, order matters e.g. URI before string
    public String asQuotedString()
    {
        if ( ! isSet ) return "literal:unset" ;
        if ( isInt ) return Long.toString(valInt) ;
        if ( isDouble ) return Double.toString(valDouble) ;
        if ( isBoolean ) return (valBoolean?"true":"false") ;
        
        if ( isRDFLiteral )
        {
            StringBuffer sb = new StringBuffer() ;
            sb.append('"').append(valRDFLiteral.toString()).append('"') ;
            if ( ! valRDFLiteral.getLanguage().equals("") )
                sb.append('@').append(valRDFLiteral.getLanguage()) ;
            if ( valRDFLiteral.getDatatypeURI() != null )
            {
                com.hp.hpl.jena.datatypes.RDFDatatype tmp = valRDFLiteral.getDatatype() ;
                sb.append("^^<").append(valRDFLiteral.getDatatypeURI()).append(">") ; 
            }
            return  sb.toString() ;
        }

        if ( isRDFResource) return "<"+valRDFResource.toString()+">" ;
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
        if ( isRDFLiteral) return valRDFLiteral.toString() ;
        if ( isRDFResource ) return valRDFResource.toString() ;
        if ( isURI ) return valURI ;
        if ( isString ) return valString ;

        return "literal:unknown" ;
    }

    public String asInfixString() { return asQuotedString() ; }

    public String asPrefixString()
    {
        if ( ! isSet ) return "literal:unset" ;
        if ( isInt ) return "int:"+Long.toString(valInt) ;
        if ( isDouble ) return "double:"+Double.toString(valDouble) ;
        if ( isBoolean ) return "boolean:"+(valBoolean?"true":"false") ;
        if ( isRDFLiteral) return "RDF:\""+valRDFLiteral.toString()+"\"" ;
        if ( isRDFResource ) return "RDF:<"+valRDFResource.toString()+">" ;
        if ( isURI ) return "URI:"+valURI ;
        if ( isString ) return "string:"+valString ;

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
		l.setString(s) ;
		return l ;
	}
	
	// Q_URI is not public.
	public static Q_URI makeURI(String s) { return Q_URI.makeURI(s) ; }

}

/*
 *  (c) Copyright Hewlett-Packard Company 2001-2003
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
 *
 * This product includes software developed by the
 * Apache Software Foundation (http://www.apache.org/).
 *
 */
