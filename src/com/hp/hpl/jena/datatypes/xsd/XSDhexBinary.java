/******************************************************************
 * File:        XSDhexbinary.java
 * Created by:  Dave Reynolds
 * Created on:  01-May-2004
 * 
 * (c) Copyright 2004, 2005 Hewlett-Packard Development Company, LP, all rights reserved.
 * [See end of file]
 * $Id: XSDhexBinary.java,v 1.3 2005-02-21 12:02:16 andy_seaborne Exp $
 *****************************************************************/
package com.hp.hpl.jena.datatypes.xsd;

import java.util.Arrays;

import org.apache.xerces.impl.dv.util.HexBin;
import com.hp.hpl.jena.datatypes.DatatypeFormatException;
import com.hp.hpl.jena.graph.impl.LiteralLabel;

/**
 * Implement hexbinary type. Most of the work is done in the superclass.
 * This only needs to implement the unparsing.
 * 
 * @author <a href="mailto:der@hplb.hpl.hp.com">Dave Reynolds</a>
 * @version $Revision: 1.3 $ on $Date: 2005-02-21 12:02:16 $
 */
public class XSDhexBinary extends XSDDatatype {
    
    /**
     * Constructor. 
     * @param typeName the name of the XSD type to be instantiated, this is 
     * used to lookup a type definition from the Xerces schema factory.
     */
    public XSDhexBinary(String typeName) {
        super(typeName, null);
    }
         
    /**
     * Test whether the given object is a legal value form
     * of this datatype. Brute force implementation.
     */
    public boolean isValidValue(Object valueForm) {
        return (valueForm instanceof byte[]);
    }
    
    /**
     * Convert a value of this datatype out
     * to lexical form.
     */
    public String unparse(Object value) {
        if (value instanceof byte[]) {
            return HexBin.encode((byte[])value);
        } else {
            throw new DatatypeFormatException("base64 asked encode a non-byte arrary");
        }
    }
    
    /**
     * Compares two instances of values of the given datatype.
     * This ignores lang tags and just uses the java.lang.Number 
     * equality.
     */
    public boolean isEqual(LiteralLabel value1, LiteralLabel value2) {
        return value1.getDatatype() == value2.getDatatype()
            && Arrays.equals((byte[])value1.getValue(), (byte[])value2.getValue());
//      && value1.getLexicalForm().equals(value2.getLexicalForm());  // bug tracking, not real code
    }
   

}



/*
    (c) Copyright 2004, 2005 Hewlett-Packard Development Company, LP
    All rights reserved.

    Redistribution and use in source and binary forms, with or without
    modification, are permitted provided that the following conditions
    are met:

    1. Redistributions of source code must retain the above copyright
       notice, this list of conditions and the following disclaimer.

    2. Redistributions in binary form must reproduce the above copyright
       notice, this list of conditions and the following disclaimer in the
       documentation and/or other materials provided with the distribution.

    3. The name of the author may not be used to endorse or promote products
       derived from this software without specific prior written permission.

    THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
    IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
    OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
    IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
    INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
    NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
    DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
    THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
    (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
    THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/
