/******************************************************************
 * File:        XSDBaseStringType.java
 * Created by:  Dave Reynolds
 * Created on:  09-Feb-03
 * 
 * (c) Copyright 2003, Hewlett-Packard Company, all rights reserved.
 * [See end of file]
 * $Id: Graph.java,v 1.8 2002/11/29 23:21:13 jjc Exp $
 *****************************************************************/
package com.hp.hpl.jena.graph.dt;

import com.hp.hpl.jena.graph.LiteralLabel;

/**
 * Base implementation for all string datatypes derinved from
 * xsd:string. The only purpose of this place holder is
 * to support the isValidLiteral tests across string types.
 * 
 * @author <a href="mailto:der@hplb.hpl.hp.com">Dave Reynolds</a>
 * @version $Revision$ on $Date: 2000/06/22 16:03:33 $
 */
public class XSDBaseStringType extends XSDDatatype {

    /**
     * Constructor. 
     * @param typeName the name of the XSD type to be instantiated, this is 
     * used to lookup a type definition from the Xerces schema factory.
     */
    public XSDBaseStringType(String typeName) {
        super(typeName);
    }
    
    /**
     * Constructor. 
     * @param typeName the name of the XSD type to be instantiated, this is 
     * used to lookup a type definition from the Xerces schema factory.
     * @param javaClass the java class for which this xsd type is to be
     * treated as the cannonical representation
     */
    public XSDBaseStringType(String typeName, Class javaClass) {
        super(typeName, javaClass);
    }

    
    /**
     * Test whether the given LiteralLabel is a valid instance
     * of this datatype. This takes into accound typing information
     * as well as lexical form - for example an xsd:string is
     * never considered valid as an xsd:integer (even if it is
     * lexically legal like "1").
     */
    public boolean isValidLiteral(LiteralLabel lit) {
        RDFDatatype dt = lit.getDatatype();
        if (dt == null || this.equals(dt)) return true;
        if (dt instanceof XSDBaseStringType) {
            return isValid(lit.toString());
        } else {
            return false;
        }
    }
    
    /**
     * Compares two instances of values of the given datatype. 
     * This ignores lang tags and optionally allows plain literals to
     * equate to strings. The latter option is currently set by a static
     * global flag in LiteralLabel.
     */
    public boolean isEqual(LiteralLabel value1, LiteralLabel value2) {
        // value1 will have been used to dispatch here so we know value1 is an xsdstring or extension
        if ((value2.getDatatype() == null && LiteralLabel.enablePlainSameAsString) ||
             (value2.getDatatype() instanceof XSDBaseStringType)) {
                return value1.getValue().equals(value2.getValue());
        } else {
                return false;
        }
    }

 }

/*
    (c) Copyright Hewlett-Packard Company 2002
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
