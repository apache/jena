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

package org.apache.jena.datatypes.xsd.impl;

import java.util.Objects;

import org.apache.jena.datatypes.BaseDatatype ;
import org.apache.jena.datatypes.RDFDatatype ;
import org.apache.jena.graph.impl.LiteralLabel ;
import org.apache.jena.vocabulary.RDF;

/**
 * {@code rdf:dirLangString} - a literal with language and initial text direction.
 * <p>
 * This covers the unusual case of {@code "foo"^^rdf:dirLangString}.
 */

public class RDFDirLangString extends BaseDatatype implements RDFDatatype {

    public static final String rdfDirLangStringURI = "http://www.w3.org/1999/02/22-rdf-syntax-ns#dirLangString";

    /**
     * Singleton instance
     * <p>
     * Prefer {@link RDF#dtDirLangString} in applications.
     */
    public static final RDFDatatype rdfDirLangString = new RDFDirLangString();

    /**
     * Test where an {@link RDFDatatype} is that for {@code rdf:dirLangString}.
     */
    public static boolean isRDFDirLangString(RDFDatatype rdfDatatype) {
        Objects.requireNonNull(rdfDatatype);
        return rdfDirLangStringURI.equals(rdfDatatype.getURI());
    }

    private RDFDirLangString() {
        super(rdfDirLangStringURI);
    }

    /**
     * Compares two instances of values of the given datatype.
     */
    @Override
    public boolean isEqual(LiteralLabel value1, LiteralLabel value2) {
        return isEqualByTerm(value1, value2) ;
    }

    /** This covers the unusual case of "foo"^^rdf:dirLangString. */
    @Override
    public Object parse(String lexicalForm) { return lexicalForm ; }

    @Override
    public String unparse(Object value) { return value.toString(); }
}

