/**
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

package com.hp.hpl.jena.datatypes.xsd.impl;

import com.hp.hpl.jena.datatypes.BaseDatatype ;
import com.hp.hpl.jena.datatypes.RDFDatatype ;
import com.hp.hpl.jena.graph.impl.LiteralLabel ;
import com.hp.hpl.jena.vocabulary.RDF ;

/** rdf:langString.
 * This covers the unusual case of "foo"^^"rdf:langString"
 * When there is a language tag, there is a lexcial form but it is in two parts lex@lang
 * This is not rdf:plainLiteral!
 */

public class RDFLangString extends BaseDatatype implements RDFDatatype {
    /** Singleton instance */
    public static final RDFDatatype rdfLangString = new RDFLangString(RDF.getURI() + "langString");
    
    /**
     * Private constructor.
     */
    private RDFLangString(String uri) {
        super(uri);
    }

    /**
     * Compares two instances of values of the given datatype. 
     */
    @Override
    public boolean isEqual(LiteralLabel value1, LiteralLabel value2) {
        if ( value2 == null )
            return false ;
        if ( ! rdfLangString.equals(value2.getDatatype()) )
            return false ;
        
        return value1.getLexicalForm().equals(value2.getLexicalForm()) && 
            value1.language().equalsIgnoreCase(value2.language()) ;
    }
    
    // This covers the unusual case of "foo"^^"rdf:langString"
    // When there is a language tag, there is a lexcial form but it is in two parts lex@lang
    // This is not rdf:plainLiteral!
    @Override
    public Object parse(String lexicalForm) { return lexicalForm ; }
    
    @Override
    public String unparse(Object value) { return value.toString(); }
}

