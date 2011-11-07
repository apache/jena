/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
