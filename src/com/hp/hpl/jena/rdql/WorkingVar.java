/*
 * (c) Copyright 2002, Hewlett-Packard Company, all rights reserved.
 * [See end of file]
 */

/**
 * @author   Andy Seaborne
 * @version  $Id: WorkingVar.java,v 1.2 2003-02-20 16:22:00 andy_seaborne Exp $
 */


package com.hp.hpl.jena.rdql;

import java.io.PrintWriter ;

import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Resource;

/** Query engine working variable - different from variables in the query language.
 *  Working variables are settable values for holding intermediate results.
 */

public class WorkingVar implements /*Value,*/ Printable, Settable, Cloneable
{
    boolean isSet = false ;

    boolean isInt = false ;
    boolean isBoolean = false ;
    boolean isDouble = false ;
    boolean isURI = false ;
    boolean isString = false ;

    long valInt ;
    boolean valBoolean ;
    double valDouble;
    String valString ;
    String valURI ;


    public WorkingVar() { unset() ; }

    private void unset()
    {
        isSet = false ;
        // Throw away any old values

        if ( isString )
            valString = null ;

        if ( isURI )
            valURI = null ;

        isInt = false ;
        isBoolean = false ;
        isDouble = false ;
        isURI = false ;
        isString = false ;
    }

    public void setInt(long i)        { unset() ; isSet = true ; isInt = true ; valInt = i ; }
    public void setDouble(double d)   { unset() ; isSet = true ; isDouble = true ; valDouble = d ; }
    public void setBoolean(boolean b) { unset() ; isSet = true ; isBoolean = true ; valBoolean = b ; }
    public void setString(String s)   { unset() ; isSet = true ; isString = true ; valString = s ; }
    public void setURI(String uri)    { unset() ; isSet = true ; isURI = true ; valURI = uri ; }

    //public boolean isNumber()   { return isSet && (isInt || isDouble) ; }
    public boolean isNumber()
    {
        forceInt() ;
        if ( ! isInt )
            forceDouble() ;
         return isSet && (isInt || isDouble) ;
    }


    public boolean isInt()      { forceInt() ;     return isSet && isInt ; }
    public boolean isDouble()   { forceDouble() ;  return isSet && isDouble ; }
    public boolean isBoolean()  {                  return isSet && isBoolean ; }
    public boolean isString()   {                  return isSet && isString ; }
    public boolean isURI()      {                  return isSet && isURI ; }

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


    public long getInt()
    {
        forceInt() ;
        if ( ! isSet || ! isInt ) throw new ValueException("Not an int: "+this) ;
        return valInt ;
    }

    public double getDouble()
    {
        forceDouble() ;
        if ( ! isSet || ! isDouble ) throw new ValueException("Not a long: "+this) ;
        return valDouble ;
    }

    public boolean getBoolean()
    {
        if ( ! isSet || ! isBoolean ) throw new ValueException("Not a boolean: "+this) ;
        return valBoolean ;
    }

    // No quoting or escape processing done
    public String getString()
    {
        if ( ! isSet ) return "<<unset>>" ;
        if ( isInt ) return Long.toString(valInt) ;
        if ( isDouble ) return Double.toString(valDouble) ;
        if ( isURI ) return valURI ;
        if ( isBoolean ) return (valBoolean?"true":"false") ;
        if ( isString ) return valString ;

        return "<<unknown>>" ;
    }

    public String getURI()
    {
        if ( ! isSet || ! isURI ) throw new ValueException("Not a URI: "+this) ;
        return valURI ;
    }

    public String asInfixString() { return asQuotedString() ; }

    public String asQuotedString()
    {
        if ( ! isSet ) return "<<unset>>" ;
        if ( isInt ) return Long.toString(valInt) ;
        if ( isDouble ) return Double.toString(valDouble) ;
        // Escaping needed
        if ( isURI ) return "<"+valURI+">" ;
        if ( isBoolean ) return (valBoolean?"true":"false") ;
        // Escaping needed
        if ( isString ) return "\""+valString+"\"" ;

        return "<<unknown>>" ;
    }

    public String asPrefixString() { return asStringWithType() ; }

    private String asStringWithType()
    {
        if ( ! isSet ) return "<<unset>>" ;
        if ( isInt ) return "int:"+Long.toString(valInt) ;
        if ( isDouble ) return "long:"+Double.toString(valDouble) ;
        if ( isURI ) return "URI:"+valURI ;
        if ( isBoolean ) return "boolean:"+(valBoolean?"true":"false") ;
        if ( isString ) return "string:"+valString ;

        return "<<unknown>>" ;
    }

    // Print prefix notation (multiline) for debugging
    public void print(PrintWriter pw, int level)
    {
        QueryPrintUtils.indent(pw, level) ;
        pw.println(this.asPrefixString()) ;
    }

    public String asUnquotedString()  { return getString() ; }
    public String valueString()       { return getString() ; }
    public String toString()          { return getString() ; }

    public boolean isRDFLiteral()
    {
        return false;
    }

    public boolean isRDFResource()
    {
        return false;
    }

    public Literal getRDFLiteral()
    {
        throw new RDQL_InternalErrorException("A WorkingVar is never an RDF Literal") ;
        //return null;
    }

    public Resource getRDFResource()
    {
        throw new RDQL_InternalErrorException("A WorkingVar is never an RDF Resource") ;
        //return null;
    }
}

/*
 *  (c) Copyright Hewlett-Packard Company 2001
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
