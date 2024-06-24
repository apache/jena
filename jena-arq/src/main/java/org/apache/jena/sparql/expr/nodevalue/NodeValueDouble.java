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

//import java.math.BigDecimal;

import java.math.BigDecimal;

import org.apache.jena.datatypes.xsd.XSDDatatype;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.util.XSDNumUtils;


public class NodeValueDouble extends NodeValue
{
    double value = Double.NaN;

    public NodeValueDouble(double d)         { super(); value = d; }
    public NodeValueDouble(double d, Node n) { super(n); value = d; }

    @Override
    public boolean isNumber() { return true; }
    @Override
    public boolean isDouble() { return true; }

    // Does not provide getFloat
    @Override
    public double getDouble()  { return value; }

    @Override
    public BigDecimal getDecimal()  { return BigDecimal.valueOf(value); }

    @Override
    protected Node makeNode()
    { return NodeFactory.createLiteralDT(XSDNumUtils.stringForm(value), XSDDatatype.XSDdouble); }

    @Override
    public String asString() { return toString(); }

    @Override
    public String toString() {
        // Preserve lexical form
        if ( getNode() != null )
            return super.asString();
        return XSDNumUtils.stringForm(value);
    }

    @Override
    public void visit(NodeValueVisitor visitor) { visitor.visit(this); }
}
