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

import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.util.FmtUtils;

/**
 * A {@link NodeValue} that supports collation value for a string. This allows query values
 * to be sorted following rules for a specific collation.
 */
public class NodeValueSortKey extends NodeValue {

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
    public String getString() {
        return string;
    }

    @Override
    public String asString() {
        return string;
    }

    @Override
    public String getCollation() {
        return collation;
    }

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

}
