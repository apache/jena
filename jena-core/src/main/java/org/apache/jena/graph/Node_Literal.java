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

package org.apache.jena.graph;

import java.util.Objects;

import org.apache.jena.datatypes.RDFDatatype;
import org.apache.jena.graph.impl.LiteralLabel;
import org.apache.jena.graph.impl.LiteralLabelFactory;
import org.apache.jena.shared.PrefixMapping;

/**
    An RDF node holding a literal value. Literals may have datatypes.
*/
public class Node_Literal extends Node
{
    private final LiteralLabel label;

    /* package */ Node_Literal( LiteralLabel label )
    { this.label = Objects.requireNonNull(label); }

    /* package */ Node_Literal(String string) {
        Objects.requireNonNull(string, "Argument to NodeFactory.createLiteral is null");
        this.label = LiteralLabelFactory.createString(string);
    }

    /* package */ Node_Literal(String string, String lang) {
        Objects.requireNonNull(string, "null lexical form for literal");
        Objects.requireNonNull(lang, "null language");
        this.label = LiteralLabelFactory.createLang(string, lang);
    }

    /*package*/ Node_Literal(String lex, String lang, TextDirection textDir) {
        Objects.requireNonNull(lex, "null lexical form for literal");
        Objects.requireNonNull(lang, "null language");
        Objects.requireNonNull(textDir, "null text direction");
        this.label = LiteralLabelFactory.createDirLang(lex, lang, textDir);
    }

    /* package */ Node_Literal(String lex, RDFDatatype dtype) {
        Objects.requireNonNull(lex, "null lexical form for literal");
        Objects.requireNonNull(dtype, "null datatype");
        this.label = LiteralLabelFactory.create(lex, dtype);
    }

    @Override
    public boolean isConcrete()
    { return true; }

    @Override
    public LiteralLabel getLiteral()
    { return label; }

    @Override
    public final Object getLiteralValue()
    { return getLiteral().getValue(); }

    @Override
    public final String getLiteralLexicalForm()
    { return getLiteral().getLexicalForm(); }

    @Override
    public final String getLiteralLanguage()
    { return getLiteral().language(); }

    @Override
    public final TextDirection getLiteralTextDirection()
    { return getLiteral().initialTextDirection(); }


    @Override
    public final String getLiteralDatatypeURI()
    { return getLiteral().getDatatypeURI(); }

    @Override
    public final RDFDatatype getLiteralDatatype()
    { return getLiteral().getDatatype(); }

    @Override
    public boolean isLiteral()
    { return true; }

    /**
     * Indexing object for literals.
     * Literal nodes defer their indexing value to the component literal.
     *
     * @see org.apache.jena.graph.Node#getIndexingValue()
     */
    @Override
    public Object getIndexingValue()
    { return getLiteral().getIndexingValue(); }

    @Override
    public Object visitWith(NodeVisitor v)
    { return v.visitLiteral(this, getLiteralLexicalForm(), getLiteralLanguage(), getLiteralDatatype()); }

    @Override
    public int hashCode()
    { return label.hashCode(); }

    @Override
    public boolean equals(Object obj) {
        if ( this == obj )
            return true;
        if ( obj == null )
            return false;
        if ( getClass() != obj.getClass() )
            return false;
        Node_Literal other = (Node_Literal)obj;
        return label.equals(other.label);
    }

    /**
     * Test that two nodes are equivalent as values.
     * In some cases this may be the same as "same term", in others
     * equals is stricter. For example, two xsd:int literals with
     * the same value if they are "01" and "1".
     * <p>Default implementation is to use equals, subclasses should
     * override this.</p>
     */
    @Override
    public boolean sameValueAs(Object o) {
        return o instanceof Node_Literal
              && label.sameValueAs( ((Node_Literal) o).getLiteral() );
    }

    @Override
    public boolean matches(Node x) {
        return sameValueAs(x);
    }

    @Override
    public String toString(PrefixMapping pm) {
        return label.toString(pm, true);
    }

    @Override
    public String toString() {
        return label.toString(PrefixMapping.Standard, true);
    }
}
