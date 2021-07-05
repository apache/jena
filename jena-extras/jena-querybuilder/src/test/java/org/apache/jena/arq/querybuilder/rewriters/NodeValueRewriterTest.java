/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.jena.arq.querybuilder.rewriters;

import static org.junit.Assert.assertEquals;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.GregorianCalendar;
import java.util.HashMap;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.Duration;

import org.apache.jena.datatypes.xsd.XSDDatatype;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.expr.nodevalue.NodeValueBoolean;
import org.apache.jena.sparql.expr.nodevalue.NodeValueDateTime;
import org.apache.jena.sparql.expr.nodevalue.NodeValueDecimal;
import org.apache.jena.sparql.expr.nodevalue.NodeValueDouble;
import org.apache.jena.sparql.expr.nodevalue.NodeValueDuration;
import org.apache.jena.sparql.expr.nodevalue.NodeValueFloat;
import org.apache.jena.sparql.expr.nodevalue.NodeValueInteger;
import org.apache.jena.sparql.expr.nodevalue.NodeValueLang;
import org.apache.jena.sparql.expr.nodevalue.NodeValueNode;
import org.apache.jena.sparql.expr.nodevalue.NodeValueSortKey;
import org.apache.jena.sparql.expr.nodevalue.NodeValueString;
import org.apache.jena.sparql.expr.nodevalue.XSDFuncOp;
import org.apache.jena.sparql.graph.NodeConst;
import org.apache.jena.sparql.util.Utils;
import org.junit.Test;

public class NodeValueRewriterTest {

    NodeValueRewriter rewriter = new NodeValueRewriter(new HashMap<Var, Node>());

    @Test
    public void visitNodeValueBooleanTest() {
        NodeValue nv = new NodeValueBoolean(true);
        nv.visit(rewriter);
        NodeValue result = rewriter.getResult();
        assertEquals(nv, result);
        assertEquals(nv.getClass(), result.getClass());
    }

    @Test
    public void visitNodeValueBooleanNodeTest() {
        NodeValue nv = new NodeValueBoolean(true, NodeConst.nodeTrue);
        nv.visit(rewriter);
        NodeValue result = rewriter.getResult();
        assertEquals(nv, result);
        assertEquals(nv.getClass(), result.getClass());
    }

    @Test
    public void visitNodeValueDecimalTest() {
        NodeValue nv = new NodeValueDecimal(new BigDecimal(3.14));
        nv.visit(rewriter);
        NodeValue result = rewriter.getResult();
        assertEquals(nv, result);
        assertEquals(nv.getClass(), result.getClass());
    }

    @Test
    public void visitNodeValueDecimalNodeTest() {
        Node n = NodeFactory.createLiteral(XSDFuncOp.canonicalDecimalStr(new BigDecimal(3.14)), XSDDatatype.XSDdecimal);
        NodeValue nv = new NodeValueDecimal(new BigDecimal(3.14), n);
        nv.visit(rewriter);
        NodeValue result = rewriter.getResult();
        assertEquals(nv, result);
        assertEquals(nv.getClass(), result.getClass());
    }

    @Test
    public void visitNodeValueDoubleTest() {
        NodeValue nv = new NodeValueDouble(3.14);
        nv.visit(rewriter);
        NodeValue result = rewriter.getResult();
        assertEquals(nv, result);
        assertEquals(nv.getClass(), result.getClass());
    }

    @Test
    public void visitNodeValueDoubleNodeTest() {
        Node n = NodeFactory.createLiteral(Utils.stringForm(3.14), XSDDatatype.XSDdouble);
        NodeValue nv = new NodeValueDouble(3.14, n);
        nv.visit(rewriter);
        NodeValue result = rewriter.getResult();
        assertEquals(nv, result);
        assertEquals(nv.getClass(), result.getClass());
    }

    @Test
    public void visitNodeValueFloatTest() {
        NodeValue nv = new NodeValueFloat(3.14F);
        nv.visit(rewriter);
        NodeValue result = rewriter.getResult();
        assertEquals(nv, result);
        assertEquals(nv.getClass(), result.getClass());
    }

    @Test
    public void visitNodeValueFloatNodeTest() {
        Node n = NodeFactory.createLiteral(Utils.stringForm(3.14F), XSDDatatype.XSDfloat);
        NodeValue nv = new NodeValueFloat(3.14F, n);
        nv.visit(rewriter);
        NodeValue result = rewriter.getResult();
        assertEquals(nv, result);
        assertEquals(nv.getClass(), result.getClass());
    }

    @Test
    public void visitNodeValueIntegerTest() {
        NodeValue nv = new NodeValueInteger(BigInteger.ONE);
        nv.visit(rewriter);
        NodeValue result = rewriter.getResult();
        assertEquals(nv, result);
        assertEquals(nv.getClass(), result.getClass());
    }

    @Test
    public void visitNodeValueIntegerNodeTest() {
        Node n = NodeFactory.createLiteral(BigInteger.ONE.toString(), XSDDatatype.XSDinteger);
        NodeValue nv = new NodeValueInteger(BigInteger.ONE, n);
        nv.visit(rewriter);
        NodeValue result = rewriter.getResult();
        assertEquals(nv, result);
        assertEquals(nv.getClass(), result.getClass());
    }

    @Test
    public void visitNodeValueNodeTest() {
        NodeValue nv = new NodeValueNode(Node.ANY);
        nv.visit(rewriter);
        NodeValue result = rewriter.getResult();
        assertEquals(nv, result);
        assertEquals(nv.getClass(), result.getClass());
    }

    @Test
    public void visitNodeValueStringTest() {
        NodeValue nv = new NodeValueString("Hello");
        nv.visit(rewriter);
        NodeValue result = rewriter.getResult();
        assertEquals(nv, result);
        assertEquals(nv.getClass(), result.getClass());
    }

    @Test
    public void visitNodeValueStringNodeTest() {
        Node n = NodeFactory.createLiteral("Hello");
        NodeValue nv = new NodeValueString("Hello", n);
        nv.visit(rewriter);
        NodeValue result = rewriter.getResult();
        assertEquals(nv, result);
        assertEquals(nv.getClass(), result.getClass());
    }

    @Test
    public void visitNodeValueSortKeyTest() {
        NodeValue nv = new NodeValueSortKey("Hello", "fi");
        nv.visit(rewriter);
        NodeValue result = rewriter.getResult();
        assertEquals(nv, result);
        assertEquals(nv.getClass(), result.getClass());
    }

    @Test
    public void visitNodeValueSortKeyNodeTest() {
        Node n = NodeFactory.createLiteral("Hello");
        NodeValue nv = new NodeValueSortKey("Hello", "fi", n);
        nv.visit(rewriter);
        NodeValue result = rewriter.getResult();
        assertEquals(nv, result);
        assertEquals(nv.getClass(), result.getClass());
    }

    @Test
    public void visitNodeValueDTTest() {
        // 2001-10-26T21:32:52
        GregorianCalendar cal = new GregorianCalendar(2001, 10, 26, 21, 32, 52);
        Node n = NodeFactory.createLiteralByValue(cal, XSDDatatype.XSDdateTime);
        NodeValue nv = new NodeValueDateTime("2001-10-26T21:32:52", n);
        nv.visit(rewriter);
        NodeValue result = rewriter.getResult();
        assertEquals(nv, result);
        assertEquals(nv.getClass(), result.getClass());
    }

    @Test
    public void visitNodeValueDurationTest() throws DatatypeConfigurationException {
        Duration dur = DatatypeFactory.newInstance().newDuration(true, 1, 2, 3, 4, 5, 6);
        NodeValue nv = new NodeValueDuration(dur);
        nv.visit(rewriter);
        NodeValue result = rewriter.getResult();
        assertEquals(nv, result);
        assertEquals(nv.getClass(), result.getClass());
    }

    @Test
    public void visitNodeValueDurationNodeTest() throws DatatypeConfigurationException {
        Duration dur = DatatypeFactory.newInstance().newDuration(true, 1, 2, 3, 4, 5, 6);
        Node n = NodeFactory.createLiteral(dur.toString(), XSDDatatype.XSDduration);
        NodeValue nv = new NodeValueDuration(dur, n);
        nv.visit(rewriter);
        NodeValue result = rewriter.getResult();
        assertEquals(nv, result);
        assertEquals(nv.getClass(), result.getClass());
    }

    @Test
    public void visitNodeValueLangTest() {
        NodeValue nv = new NodeValueLang("Hello", "fi");
        nv.visit(rewriter);
        NodeValue result = rewriter.getResult();
        assertEquals(nv, result);
        assertEquals(nv.getClass(), result.getClass());
    }

    @Test
    public void visitNodeValueLangNodeTest() {
        Node n = NodeFactory.createLiteral("Hello", "fi");
        NodeValue nv = new NodeValueLang(n);
        nv.visit(rewriter);
        NodeValue result = rewriter.getResult();
        assertEquals(nv, result);
        assertEquals(nv.getClass(), result.getClass());
    }

}
