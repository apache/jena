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
 * <a href="https://www.w3.org/TR/rdf-concepts/#section-json">rdf:JSON</a>.
 * <p>
 * This only implements syntactic equality, not value equality.
 */
public class RDFjson extends BaseDatatype implements RDFDatatype {

    public static final String rdfJsonURI = "http://www.w3.org/1999/02/22-rdf-syntax-ns#JSON";

    /**
     * Singleton instance.
     * <p>
     * Prefer {@link RDF#dtRDFJSON} in applications.
     */
    public static final RDFDatatype rdfJSON = new RDFjson();

    /**
     * @deprecated Prefer {@link #rdfJsonURI}.
     */
    @Deprecated
    public static String RDFjson = rdfJsonURI;

    /**
     * Test where an {@link RDFDatatype} is that for {@code rdf:XMLLiteral}.
     */
    public static boolean isRDFjson(RDFDatatype rdfDatatype) {
        Objects.requireNonNull(rdfDatatype);
        return rdfJsonURI.equals(rdfDatatype.getURI());
    }

    private RDFjson() {
        // Include the string for the RDF namespace, not use RDF.getURI(), to avoid an initializer circularity
        super(rdfJsonURI);
    }

    /**
     * Compares two instances of values of the given datatype.
     */
    @Override
    public boolean isEqual(LiteralLabel value1, LiteralLabel value2) {
        return isEqualByTerm(value1, value2) ;
    }

    @Override
    public Object parse(String lexicalForm) { return lexicalForm ; }

    @Override
    public String unparse(Object value) { return value.toString(); }
}

