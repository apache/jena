/*
 * (c) Copyright 2001-2003, Hewlett-Packard Company, all rights reserved.
 * [See end of file]
 */

/**
 * @author   Andy Seaborne
 * @version  $Id: WorkingVar.java,v 1.4 2003-03-11 18:03:17 andy_seaborne Exp $
 */


package com.hp.hpl.jena.rdql;

import com.hp.hpl.jena.rdql.parser.ParsedLiteral ;

/**  Working variables are settable values for holding intermediate results.
 */

public class WorkingVar extends ParsedLiteral implements Value, Printable, Settable, Cloneable
{
    public WorkingVar() { super() ; }
    public WorkingVar(Value v) { super(v) ; } 
    
    //public boolean isNumber()   { return isSet && (isInt || isDouble) ; }
    
    // Unlike ParsedLiterals, trying to get a WorkingVar as a number (say)
    // invokes an attempt to make it a number.
     
    public boolean isNumber()
    {
        forceInt() ;
        if ( ! isInt )
            forceDouble() ;
         return isSet && (isInt || isDouble) ;
    }


    public boolean isInt()      { forceInt() ;     return isSet && isInt ; }
    public boolean isDouble()   { forceDouble() ;  return isSet && isDouble ; }

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
