/******************************************************************
 * File:        XSDGenericType.java
 * Created by:  Dave Reynolds
 * Created on:  13-Dec-2002
 * 
 * (c) Copyright 2002, Hewlett-Packard Company, all rights reserved.
 * [See end of file]
 * $Id: XSDGenericType.java,v 1.4 2003-04-15 21:06:17 jeremy_carroll Exp $
 *****************************************************************/
package com.hp.hpl.jena.datatypes.xsd.impl;

import org.apache.xerces.impl.dv.XSSimpleType;
import org.apache.xerces.impl.dv.xs.DecimalDV;
import org.apache.xerces.impl.dv.xs.XSSimpleTypeDecl;

import com.hp.hpl.jena.datatypes.*;
import com.hp.hpl.jena.datatypes.xsd.*;

import java.math.BigDecimal;
import java.math.BigInteger;

/**
 * Datatype template that adapts any response back from Xerces type parsing
 * to an appropriate java representation. This is primarily used in creating
 * user defined types - the built in types have a fixed mapping.
 * 
 * @author <a href="mailto:der@hplb.hpl.hp.com">Dave Reynolds</a>
 * @version $Revision: 1.4 $ on $Date: 2003-04-15 21:06:17 $
 */
public class XSDGenericType extends XSDDatatype {

    /**
     * Hidden constructor used when loading in external user defined XSD types
     * @param xstype the XSSimpleType definition to be wrapped
     * @param namespace the namespace for the type (used because the grammar loading doesn't seem to keep that)
     */
    public XSDGenericType(XSSimpleType xstype, String namespace) {
        super(xstype, namespace);
    }

    /**
     * Parse a lexical form of this datatype to a value
     * @throws DatatypeFormatException if the lexical form is not legal
     */
    public Object parse(String lexicalForm) throws DatatypeFormatException {
        Object xsdValue = super.parse(lexicalForm);
        if (xsdValue instanceof String || xsdValue instanceof Number) {
            return xsdValue;
        } else {
            XSSimpleTypeDecl decl = (XSSimpleTypeDecl) typeDeclaration;
            if (decl.getIsNumeric()) {
                return convertNumeric(xsdValue);
            } else if (decl.getVariety() == XSSimpleTypeDecl.VARIETY_UNION) {
                try {
                    // Just try it as a number
                    return convertNumeric(xsdValue);
                } catch (ClassCastException e) {
                    // Rats assume its usable java already
                    // TODO add date/time format conversion
                    return xsdValue;
                }
            } else {
                // This will be a date/time type - leave unprocessed for now
                // @TODO add date/time format conversion
                return xsdValue;
            }
        }
    }
    
    /**
     * Helper method.
     * Attempts to convert a type to a number, with throw a ClassCastException if this is not a number
     */
    private Object convertNumeric(Object xsdValue) {
        if (xsdValue instanceof Number) {
            return xsdValue;
        }
        DecimalDV dv = XSDBigNumberType.decimalDV;
        if (dv.getFractionDigits(xsdValue) > 1) {
            return new BigDecimal(xsdValue.toString());
        } else {
            // Pick a small representation, this just works on string length so it
            // will not be the minimal representation, just an adequate one
            int digits = dv.getTotalDigits(xsdValue);
            if (digits > 18) {
                return new BigInteger(xsdValue.toString());
            } else if (digits > 9) {
                return new Long(xsdValue.toString());
            } else {
                return new Integer(xsdValue.toString());
            }
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
