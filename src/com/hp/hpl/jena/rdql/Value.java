/*
 * (c) Copyright 2002, Hewlett-Packard Company, all rights reserved.
 * [See end of file]
 */

/**
 * @author   Andy Seaborne
 * @version  $Id: Value.java,v 1.3 2003-02-20 16:22:00 andy_seaborne Exp $
 */


package com.hp.hpl.jena.rdql;
import com.hp.hpl.jena.rdf.model.Literal ;
import com.hp.hpl.jena.rdf.model.Resource ;

// There is a separate settable interface

public interface Value extends Printable
{
    public boolean isNumber() ;
    public boolean isInt() ;
    public boolean isDouble() ;
    public boolean isBoolean() ;
    public boolean isString() ;
    public boolean isURI() ;
    public boolean isRDFLiteral() ;
    public boolean isRDFResource() ;

    public long getInt() ;
    public double getDouble() ;
    public boolean getBoolean() ;
    public String getString() ;
    public String getURI() ;
    public Literal getRDFLiteral() ;
    public Resource getRDFResource() ;   

    // Should be in the form usable to print out : this may depend on the
    // intended interpretation context.  For RDQL-the-language, this should
    // be a string that can be parsed, so unquothed numbers, ""-quoted strings with
    // escapes.
    public String asQuotedString() ;
    public String asUnquotedString() ;
    // Should be the literal value appropriate to filter computation.
    public String valueString() ;
	// Displayable form
    public String toString() ;
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
