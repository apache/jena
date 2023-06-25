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

import org.apache.jena.datatypes.RDFDatatype ;
import org.apache.jena.graph.impl.* ;
import org.apache.jena.shared.* ;

/**
    An RDF node holding a literal value. Literals may have datatypes.
*/
public class Node_Literal extends Node
{
    private final LiteralLabel label;

    /* package */ Node_Literal( LiteralLabel label )
    { this.label = Objects.requireNonNull(label); }

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
    public final String getLiteralDatatypeURI()
    { return getLiteral().getDatatypeURI(); }

    @Override
    public final RDFDatatype getLiteralDatatype()
    { return getLiteral().getDatatype(); }

    @Override
    public final boolean getLiteralIsXML()
    { return getLiteral().isXML(); }

    @Override
    public boolean isLiteral()
    { return true; }

    /**
     * Literal nodes defer their indexing value to the component literal.
     *
     * @see org.apache.jena.graph.Node#getIndexingValue()
     */
    @Override
    public Object getIndexingValue()
    { return getLiteral().getIndexingValue(); }

    @Override
    public Object visitWith( NodeVisitor v )
    { return v.visitLiteral( this, getLiteral() ); }

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
