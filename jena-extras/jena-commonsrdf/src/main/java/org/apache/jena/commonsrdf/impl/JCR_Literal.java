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

package org.apache.jena.commonsrdf.impl;

import java.util.Objects;
import java.util.Optional;

import org.apache.commons.rdf.api.IRI;
import org.apache.commons.rdf.api.Literal;
import org.apache.jena.graph.Node;

public class JCR_Literal extends JCR_Term implements Literal {

    /*package*/ JCR_Literal(Node node) {
        super(node);
    }

    @Override
    public String getLexicalForm() {
        return getNode().getLiteralLexicalForm();
    }

    @Override
    public IRI getDatatype() {
        return JCR_Factory.createIRI(getNode().getLiteralDatatype().getURI());
    }

    @Override
    public Optional<String> getLanguageTag() {
        String x = getNode().getLiteralLanguage();
        if ( x == null || x.isEmpty() )
            return Optional.empty();
        return Optional.of(x);
    }

    @Override
    public int hashCode() {
        return Objects.hash(getLexicalForm(), getDatatype(), getLanguageTag());
    }

    private static boolean equalsIgnoreCase(Optional<String> s1, Optional<String> s2) {
        if ( Objects.equals(s1, s2) )
            return true;
        if ( ! s1.isPresent() || ! s2.isPresent() )
            return false;
        return s1.get().equalsIgnoreCase(s2.get());
    }

    @Override
    public boolean equals(Object other) {
        if ( other == this ) return true;
        if ( other == null ) return false;
        if ( ! ( other instanceof Literal ) ) return false;
        Literal literal = (Literal)other;
        return  getLexicalForm().equals(literal.getLexicalForm()) &&
                equalsIgnoreCase(getLanguageTag(), literal.getLanguageTag()) &&
                getDatatype().equals(literal.getDatatype());
    }
}

