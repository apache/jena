/*
 * (c) Copyright 2002, 2003, 2004, 2005 Hewlett-Packard Development Company, LP  
 * [see end of file]
 */

package com.hp.hpl.jena.ontology.tidy.impl;

import java.math.*;
import com.hp.hpl.jena.graph.*;
import com.hp.hpl.jena.graph.impl.*;
import com.hp.hpl.jena.datatypes.xsd.XSDDatatype;
import com.hp.hpl.jena.vocabulary.*;
import com.hp.hpl.jena.shared.*;
import java.util.*;

/**
 * @author jjc
 *
 */
class CLit extends CBuiltin {
	static private Set allSchemaTypes = new HashSet();
	static {  
		allSchemaTypes.add("float");
		allSchemaTypes.add("double");
		allSchemaTypes.add("int");
		allSchemaTypes.add("long");
		allSchemaTypes.add("short");
		allSchemaTypes.add("byte");
		allSchemaTypes.add("boolean");
		allSchemaTypes.add("string");
		allSchemaTypes.add("unsignedByte");
		allSchemaTypes.add("unsignedShort");
		allSchemaTypes.add("unsignedInt");
		allSchemaTypes.add("unsignedLong");
		allSchemaTypes.add("decimal");
		allSchemaTypes.add("integer");
		allSchemaTypes.add("nonPositiveInteger");
		allSchemaTypes.add("nonNegativeInteger");
		allSchemaTypes.add("positiveInteger");
		allSchemaTypes.add("negativeInteger");
		allSchemaTypes.add("normalizedString");
		allSchemaTypes.add("anyURI");
		allSchemaTypes.add("token");
		allSchemaTypes.add("Name");
		allSchemaTypes.add("QName");
		allSchemaTypes.add("language");
		allSchemaTypes.add("NMTOKEN");
		allSchemaTypes.add("ENTITIES");
		allSchemaTypes.add("NMTOKENS");
		allSchemaTypes.add("ENTITY");
		allSchemaTypes.add("ID");
		allSchemaTypes.add("NCName");
		allSchemaTypes.add("IDREF");
		allSchemaTypes.add("IDREFS");
		allSchemaTypes.add("NOTATION");
		allSchemaTypes.add("hexBinary");
		allSchemaTypes.add("base64Binary");
		allSchemaTypes.add("date");
		allSchemaTypes.add("time");
		allSchemaTypes.add("dateTime");
		allSchemaTypes.add("duration");
		allSchemaTypes.add("gDay");
		allSchemaTypes.add("gMonth");
		allSchemaTypes.add("gYear");
		allSchemaTypes.add("gYearMonth");
		allSchemaTypes.add("gMonthDay");
	}
    static final Integer zero = new Integer(0);
    static final Integer one = new Integer(1);
	CLit(Node n, AbsChecker eg) {
		super(n, eg, literalCategory(n,eg));
	}
	static private String rdfXMLLiteral = RDF.getURI()+"XMLLiteral";

	static final int noTypeAndDatatypeID = CategorySet.find(
	        new int[]{Grammar.notype,Grammar.datatypeID},
	  true
	);

    /** 
     * Decide whether this literal node is a
     * nonNegativeInteger, (or compatible),
     * and if so is it 0 or 1.
     * @param n    Must be a Literal node.
     * @return int
     */
    static int literalCategory(Node n,AbsChecker eg) {
        LiteralLabel l = n.getLiteral();
        Object v = l.getValue();
        if (XSDDatatype.XSDnonNegativeInteger.isValidValue(v)) {
            // v must be a java.Number at this point so...
        if (!(v instanceof BigInteger) && 
            !(v instanceof BigDecimal) &&
            ((Number)v).longValue() >= 0 &&
        ((Number)v).longValue() <= 1) {
                return Grammar.liteInteger;
            }
            return Grammar.dlInteger;
        }
        String dt = l.getDatatypeURI();
        if ( dt != null ){
        	if ( dt.startsWith("http://www.w3.org/2001/XMLSchema#")) {
        		if ( allSchemaTypes.contains(dt.substring(33)))
        		  return Grammar.literal;
        	}
        	if ( dt.equals( rdfXMLLiteral))
        	  return Grammar.literal;
        CNodeI dtURI =        	eg.getCNode(Node.createURI(n.getLiteral().getDatatypeURI()));
        int cat = dtURI.getCategories();
        if ( cat != Grammar.datatypeID &&
                cat != noTypeAndDatatypeID)
            throw new BrokenException("Precondition that datatype URI has already been registered as such has been violated"); 
        
        	return Grammar.userTypedLiteral;
        }
        return Grammar.literal;
    }


}


/*
 * (c) Copyright 2002, 2003, 2004, 2005 Hewlett-Packard Development Company, LP
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

