/******************************************************************
 * File:        XMLLiteralType.java
 * Created by:  Dave Reynolds
 * Created on:  08-Dec-02
 * 
 * (c) Copyright 2002, Hewlett-Packard Company, all rights reserved.
 * [See end of file]
 * $Id: XMLLiteralType.java,v 1.1.1.1 2002-12-19 19:13:41 bwm Exp $
 *****************************************************************/
package com.hp.hpl.jena.graph.dt;

import com.hp.hpl.jena.vocabulary.RDF;

/**
 * Builtin data type to represent XMLLiteral (i.e. items created
 * by use of <code>rdf:parsetype='literal'</code>.
 * 
 * @TODO implement parsing - this is just a dummy implementation at present.
 * 
 * @author <a href="mailto:der@hplb.hpl.hp.com">Dave Reynolds</a>
 * @version $Revision: 1.1.1.1 $ on $Date: 2002-12-19 19:13:41 $
 */
public class XMLLiteralType extends BaseDatatype implements RDFDatatype {
    /** Singleton instance */
    public static final RDFDatatype theXMLLiteralType = new XMLLiteralType(RDF.getURI() + "XMLLiteral");
    
    /**
     * Private constructor.
     */
    private XMLLiteralType(String uri) {
        super(uri);
    }
    
    /**
     * Convert a serialize a value of this datatype out
     * to lexical form.
     */
    public String unparse(Object value) {
        return value.toString();
    }
    
    /**
     * Parse a lexical form of this datatype to a value
     * @throws DatatypeFormatException if the lexical form is not legal
     */
    public Object parse(String lexicalForm) throws DatatypeFormatException {
        return lexicalForm;
    }
    
    /**
     * Test whether the given string is a legal lexical form
     * of this datatype.
     */
    public boolean isValid(String lexicalForm) {
        return true;
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
