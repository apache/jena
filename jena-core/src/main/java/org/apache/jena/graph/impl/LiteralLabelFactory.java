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

package org.apache.jena.graph.impl;

import org.apache.jena.datatypes.DatatypeFormatException;
import org.apache.jena.datatypes.RDFDatatype;
import org.apache.jena.datatypes.xsd.XSDDatatype;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.vocabulary.RDF;

public class LiteralLabelFactory
{
    private static final RDFDatatype dtSLangString = NodeFactory.getType(RDF.Nodes.langString.getURI());

    private static RDFDatatype fixDatatype(RDFDatatype dtype, String lang) {
        if ( dtype != null )
            return dtype;
        if ( lang != null && !lang.equals("") )
            return dtSLangString;
        return XSDDatatype.XSDstring;
    }

    /** Create a string literal */
    public static LiteralLabel createString(String lex) {
        return new LiteralLabel( lex, "", XSDDatatype.XSDstring);
    }

    /**
     * Build a plain literal label from its lexical form and language tag.
     * @param lex the lexical form of the literal
     * @param lang the optional language tag, only relevant for plain literals
     */
    public static LiteralLabel createLang(String lex, String lang) {
        RDFDatatype dt = fixDatatype(null, lang);
        return new LiteralLabel(lex, lang, dt);
    }

    /** Create a literal with a dtype. */
    public static LiteralLabel create(String lex, RDFDatatype dtype) {
        return new LiteralLabel( lex, "", dtype );
    }

    /** Using {@link #createLang(String, String)} or {@link #create(String, RDFDatatype)}
     * where possible is preferred.
     */
    public static LiteralLabel createLiteralLabel( String lex, String lang, RDFDatatype dtype )
        throws DatatypeFormatException
    {
        dtype = fixDatatype(dtype, lang);
        return new LiteralLabel( lex, lang, dtype ); }

    /**
     * Build a typed literal label from its value form. If the value is a string we
     * assume this is intended to be a lexical form after all.
     *
     * @param value the value of the literal
     */
    public static LiteralLabel createByValue(Object value) throws DatatypeFormatException {
        return new LiteralLabel(value);
    }

    /**
     * Build a typed literal label from its value form. If the value is a string we
     * assume this is intended to be a lexical form after all.
     *
     * @param value the value of the literal
     * @param dtype the type of the literal, null for old style "plain" literals (which become xsd:string in RDF 1.1)
     */
    public static LiteralLabel createByValue(Object value, RDFDatatype dtype) throws DatatypeFormatException {
        dtype = fixDatatype(dtype, null);
        return new LiteralLabel(value, dtype);
    }

    /**
     * Build a typed literal label from its value form using
     * whatever datatype is currently registered as the default
     * representation for this java class. No language tag is supplied.
     * A plain string becomes an xsd:string.
     * @param value the literal value to encapsulate
     */
    public static LiteralLabel createTypedLiteral(Object value) {
        return new LiteralLabel(value);
    }
}
