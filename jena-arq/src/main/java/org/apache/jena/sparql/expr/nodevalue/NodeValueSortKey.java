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

package org.apache.jena.sparql.expr.nodevalue;

import java.text.Collator;
import java.util.Locale;
import java.util.Objects;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.graph.Node_Literal;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.util.FmtUtils;

/**
 * A {@link NodeValue} that supports collation value for a string. This allows query values
 * to be sorted following rules for a specific collation.
 */
public final class NodeValueSortKey extends NodeValue implements Comparable<NodeValueSortKey> {

    /**
     * Node value text.
     */
    private final String string;
    /**
     * Node value collation language tag (e.g. fi, pt-BR, en, en-CA, etc).
     */
    private final String collation;

    public NodeValueSortKey(final String string, final String collation) {
        this.string = string;
        this.collation = collation;
    }

    public NodeValueSortKey(final String string, final String collation, Node n) {
        super(n);
        this.string = string;
        this.collation = collation;
    }

    @Override
    public boolean isSortKey() {
        return Boolean.TRUE;
    }
    
    @Override
    public NodeValueSortKey getSortKey() {
        return this;
    }

    @Override
    public String getString() {
        return string;
    }

    @Override
    public String asString() {
        return string;
    }

    public String getCollation() {
        return collation;
    }

    /**
     * The node created by a NodeValueSortKey is a {@link Node_Literal}. This is used to represent
     * the node value internally for comparison, and should no be expected to work in other cases.
     * Users are not expected to extend it, or use in other functions.
     */
    @Override
    protected Node makeNode() {
        return NodeFactory.createLiteral(string);
    }

    @Override
    public void visit(NodeValueVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public String toString()
    { 
        if (getNode() != null) {
            return FmtUtils.stringForNode(getNode()) ;
        }
        return "'"+getString()+"'";
    }

    @Override
    public int compareTo(NodeValueSortKey other) {
        Objects.requireNonNull(other);
        if ( this == other )
            return Expr.CMP_EQUAL;
        String c1 = this.getCollation();
        String c2 = other.getCollation();
        if (c1 == null || c2 == null || ! c1.equals(c2))
            return XSDFuncOp.compareString(this, other) ;
        // locales are parsed. Here we could think about caching if necessary
        Locale desiredLocale = Locale.forLanguageTag(c1);
        // collators are already stored in a concurrent map by the JVM, with <locale, softref<collator>>
        Collator collator = Collator.getInstance(desiredLocale);
        return collator.compare(this.getString(), other.getString());
    }

}
