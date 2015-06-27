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

import org.apache.jena.JenaRuntime ;
import org.apache.jena.datatypes.DatatypeFormatException ;
import org.apache.jena.datatypes.RDFDatatype ;
import org.apache.jena.datatypes.xsd.XSDDatatype ;
import org.apache.jena.graph.NodeFactory ;
import org.apache.jena.vocabulary.RDF ;

public class LiteralLabelFactory
{
    // This code works for RDF 1.0 and RDF 1.1
    //
    // In RDF 1.0, "abc" has no datatype and is a different term to "abc"^^xsd:string
    // In RDF 1.0, "abc"@en has no datatype.
    //
    // In RDF 1.1, "abc" has no datatype xsd:string and is the same term as "abc"^^xsd:string
    // In RDF 1.1, "abc"@en has datatype rdf:langString.
    
    private static final RDFDatatype dtSLangString = NodeFactory.getType(RDF.Nodes.langString.getURI()) ;
    
    private static RDFDatatype fixDatatype(RDFDatatype dtype, String lang) {
        if ( dtype != null ) 
            return dtype ;
        if ( JenaRuntime.isRDF11 )
            dtype = (lang == null || lang.equals("")) ? XSDDatatype.XSDstring : dtSLangString  ;
        return dtype ;
    }
    
    /** Create a literal with a dataype. */ 
    public static LiteralLabel create( String lex, RDFDatatype dtype) {
        return new LiteralLabelImpl( lex, "", dtype );
    }

    /** Using {@link #create(String, String)} or {@link #create(String, RDFDatatype)}
     * where possible is preferred.
     */
    public static LiteralLabel createLiteralLabel( String lex, String lang, RDFDatatype dtype ) 
        throws DatatypeFormatException
    { 
        dtype = fixDatatype(dtype, lang) ;
        return new LiteralLabelImpl( lex, lang, dtype ); }

    /**
     * Build a plain literal label from its lexical form and language tag.
     * @param lex the lexical form of the literal
     * @param lang the optional language tag, only relevant for plain literals
     */
    public static LiteralLabel create(String lex, String lang) {
        RDFDatatype dt = fixDatatype(null, lang) ;
        return new LiteralLabelImpl(lex, lang, dt);
    }

    /**
     * Build a typed literal label from its value form. If the value is a string we
     * assume this is inteded to be a lexical form after all.
     * 
     * @param value the value of the literal
     * @param lang the optional language tag, only relevant for plain literals
     * @param dtype the type of the literal, null for old style "plain" literals (which become xsd:string in RDF 1.1)
     */
    public static LiteralLabel createByValue(Object value, String lang, RDFDatatype dtype) throws DatatypeFormatException {
        dtype = fixDatatype(dtype, lang) ;
        return new LiteralLabelImpl(value, lang, dtype) ; 
    }

    /** Use {@link #createTypedLiteral} */
    @Deprecated
    public static LiteralLabel create(Object value) {
        return createTypedLiteral(value) ;
    }
    
    /**
     * Build a typed literal label from its value form using
     * whatever datatype is currently registered as the the default
     * representation for this java class. No language tag is supplied.
     * A plain string becomes an xsd:string.
     * @param value the literal value to encapsulate
     */
    public static LiteralLabel createTypedLiteral(Object value) {
        return new LiteralLabelImpl(value) ;
    }

    /**
     * Creates either a plain literal or an XMLLiteral.
     *       @param xml If true then s is exclusive canonical XML of type rdf:XMLLiteral, and no checking will be invoked.
     */
    public static LiteralLabel create(String s, String lang, boolean xml) {
        if ( xml )
            return new LiteralLabelImpl(s, lang, xml) ;
        return create(s, lang) ;
    }

    
}
