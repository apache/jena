/*
 * (c) Copyright 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.graph.impl;

import com.hp.hpl.jena.datatypes.DatatypeFormatException;
import com.hp.hpl.jena.datatypes.RDFDatatype;

public class LiteralLabelFactory
{
    public static LiteralLabel createLiteralLabel( String lex, String lang, RDFDatatype dtype ) 
    throws DatatypeFormatException
    { return new LiteralLabelImpl( lex, lang, dtype ); }

    /**
     * Build a plain literal label from its lexical form. 
     * @param lex the lexical form of the literal
     * @param lang the optional language tag, only relevant for plain literals
     */
    public static LiteralLabel create(String lex, String lang) 
    {
        return new LiteralLabelImpl(lex, lang, null);
    }

    /**
     * Build a typed literal label from its value form. If the value is a string we
     * assume this is inteded to be a lexical form after all.
     * 
     * @param value the value of the literal
     * @param lang the optional language tag, only relevant for plain literals
     * @param dtype the type of the literal, null for old style "plain" literals
     */
    public static LiteralLabel create(Object value, String lang, RDFDatatype dtype) throws DatatypeFormatException {
        return new LiteralLabelImpl(value, lang, dtype) ; 
    }


//    /**
//     * Build a typed literal label supplying both value and lexical form.
//     * The caller guarantees that the lexical form is legal, 
//     * and the value corresponds. 
//     * 
//     * @param lex the lexical form of the literal
//     * @param value the value of the literal
//     * @param lang the optional language tag, only relevant for plain literals
//     * @param dtype the type of the literal, null for old style "plain" literals
//     */
//    public static LiteralLabel create(String lex,
//                                      Object value,
//                                      String lang,
//                                      RDFDatatype dtype) {
//        return new LiteralLabelImpl(lex, value, lang, dtype) ; 
//    }

    /**
     * Build a typed literal label from its value form using
     * whatever datatype is currently registered as the the default
     * representation for this java class. No language tag is supplied.
     * @param value the literal value to encapsulate
     */
    public static LiteralLabel create(Object value) {
        return new LiteralLabelImpl(value) ;
    }

    /**
     * Old style constructor. Creates either a plain literal or an
     * XMLLiteral.
     *       @param xml If true then s is exclusive canonical XML of type rdf:XMLLiteral, and no checking will be invoked.

     */
    public static LiteralLabel create(String s, String lg, boolean xml) {
        return new LiteralLabelImpl(s, lg, xml) ;
    }

    
}

/*
 * (c) Copyright 2009 Hewlett-Packard Development Company, LP
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