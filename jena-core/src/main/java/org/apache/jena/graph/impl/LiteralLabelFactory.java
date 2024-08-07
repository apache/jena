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

import static org.apache.jena.atlas.lib.Lib.isEmpty;

import org.apache.jena.datatypes.DatatypeFormatException;
import org.apache.jena.datatypes.RDFDatatype;
import org.apache.jena.datatypes.xsd.XSDDatatype;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.graph.TextDirection;
import org.apache.jena.vocabulary.RDF;

/**
 * This class is not in the public API.
 */
public class LiteralLabelFactory
{
    private static final RDFDatatype dtLangString = NodeFactory.getType(RDF.Nodes.langString.getURI());
    private static final RDFDatatype dtDirLangString = NodeFactory.getType(RDF.Nodes.dirLangString.getURI());

//    /*package*/ static String noLang = "";
//    /*package*/ static TextDirection noTextDirection = null;
//
    private static RDFDatatype fixDatatype(RDFDatatype dtype, String lang, TextDirection textDir) {
        // lang is assumed to have correct case.
        if ( dtype != null )
            return dtype;
        if (isEmpty(lang) && textDir == null )
            return XSDDatatype.XSDstring;
        if ( textDir == null )
            return dtLangString;
        return dtDirLangString;
    }

    /** Create a string literal */
    public static LiteralLabel createString(String lex) {
        return new LiteralLabel(lex, Node.noLangTag, Node.noTextDirection, XSDDatatype.XSDstring);
    }

    /**
     * Build a literal label from its lexical form and language tag.
     * @param lex the lexical form of the literal
     * @param lang the optional language tag
     */
    public static LiteralLabel createLang(String lex, String lang) {
        RDFDatatype dt = fixDatatype(null, lang, Node.noTextDirection);
        return new LiteralLabel(lex, lang, Node.noTextDirection, dt);
    }

    /**
     * Build a literal label from its lexical form,language tag and initial text direction
     * @param lex the lexical form of the literal
     * @param lang the optional language tag
     * @param textDir the optional initial text direction (lang required)
     */
    public static LiteralLabel createDirLang(String lex, String lang, TextDirection textDir) {
        RDFDatatype dt = fixDatatype(null, lang, textDir);
        return new LiteralLabel(lex, lang, textDir, dt);
    }

    /** Create a literal with a dtype. */
    public static LiteralLabel create(String lex, RDFDatatype dtype) {
        return new LiteralLabel( lex, dtype );
    }

    public static LiteralLabel createLiteralLabel( String lex, String lang, TextDirection textDir, RDFDatatype dtype ) {
        dtype = fixDatatype(dtype, lang, textDir);
        return new LiteralLabel(lex, lang, textDir, dtype);
    }

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
        dtype = fixDatatype(dtype, null, null);
        return new LiteralLabel(value, dtype);
    }

    /**
     * Create a typed literal for which both the lexical form and the value
     * form are already available. Use with care!
     *
     * @param lex the lexical form of the literal (assumed to be well-formed
     *            for the given datatype)
     * @param value the value of the literal (assumed to be the value obtained
     *              when applying the lexical-to-value mapping of the the given
     *              datatype to the given lexical form)
     * @param dtype the datatype of the literal
     */
    public static LiteralLabel createIncludingValue(String lex, Object value, RDFDatatype dtype) {
        return new LiteralLabel(lex, value, dtype);
    }

    /**
     * Build a typed literal label from its value form using
     * whatever datatype is currently registered as the default
     * representation for this java class. No language tag is supplied.
     * A Java string becomes an xsd:string.
     * @param value the literal value to encapsulate
     */
    public static LiteralLabel createTypedLiteral(Object value) {
        return new LiteralLabel(value);
    }
}
