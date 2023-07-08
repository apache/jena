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

package org.apache.jena.graph.test;

import java.util.Set;

import junit.framework.TestSuite;
import org.apache.jena.JenaRuntime;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.graph.Triple;
import org.apache.jena.graph.impl.LiteralLabelFactory;

public class TestFindLiterals extends GraphTestBase {
    public TestFindLiterals( String name )
        { super( name ); }


    public static junit.framework.Test suite() {
        return new TestSuite(TestFindLiterals.class);
    }

    private void runTest(String graph, int size, String search, String results ) {
        Graph g = graphWith( graph );
        Node literal = NodeCreateUtils.create( search );
        assertEquals( "graph has wrong size", size, g.size() );
        Set<Node> got = g.find( Node.ANY, Node.ANY, literal ).mapWith( t -> t.getObject() ).toSet();
        assertEquals( nodeSet( results ), got );
    }

    public void test01() {
        runTest("a P 'simple'", 1, "'simple'", "'simple'");
    }

    public void test02() {
        runTest("a P 'simple'xsd:string", 1, "'simple'", "'simple'xsd:string");
    }

    public void test03() {
        runTest("a P 'simple'", 1, "'simple'xsd:string", "'simple'");
    }

    public void test04() {
        runTest("a P 'simple'xsd:string", 1, "'simple'xsd:string", "'simple'xsd:string");
    }

    int expected = JenaRuntime.isRDF11 ? 1 : 2;
    public void test05() {
        runTest("a P 'simple'; a P 'simple'xsd:string", expected, "'simple'", "'simple' 'simple'xsd:string");
    }

    public void test06() {
        runTest("a P 'simple'; a P 'simple'xsd:string", expected, "'simple'xsd:string", "'simple' 'simple'xsd:string");
    }

    public void test07() {
        runTest("a P 1", 1, "1", "1");
    }

    public void test08() {
        runTest("a P '1'xsd:float", 1, "'1'xsd:float", "'1'xsd:float");
    }

    public void test09() {
        runTest("a P '1'xsd:double", 1, "'1'xsd:double", "'1'xsd:double");
    }

    public void test10() {
        runTest("a P '1'xsd:float", 1, "'1'xsd:float", "'1'xsd:float");
    }

    public void test11() {
        runTest("a P '1.1'xsd:float", 1, "'1'xsd:float", "");
    }

    public void test12() {
        runTest("a P '1'xsd:double", 1, "'1'xsd:int", "");

    }

    public void test13() {
        runTest("a P 'abc'rdf:XMLLiteral", 1, "'abc'", "");
    }

    public void test14() {
        runTest("a P 'abc'", 1, "'abc'rdf:XMLLiteral", "");
    }

    //
    // floats & doubles are not compatible
    //
    public void test15() {
        runTest("a P '1'xsd:float", 1, "'1'xsd:double", "");
    }

    public void test16() {
        runTest("a P '1'xsd:double", 1, "'1'xsd:float", "");
    }

    public void test17() {
        runTest("a P 1", 1, "'1'", "");
    }

    public void test18() {
        runTest("a P 1", 1, "'1'xsd:integer", "'1'xsd:integer");
    }

    public void test19() {
        runTest("a P 1", 1, "'1'", "");
    }

    public void test20() {
        runTest("a P '1'xsd:short", 1, "'1'xsd:integer", "'1'xsd:short");
    }

    public void test21() {
        runTest("a P '1'xsd:int", 1, "'1'xsd:integer", "'1'xsd:int");
    }

    public void testFloatVsDouble()
        {
        Node A = NodeCreateUtils.create( "'1'xsd:float" );
        Node B = NodeCreateUtils.create( "'1'xsd:double" );
        assertFalse( A.equals( B ) );
        assertFalse( A.sameValueAs( B ) );
        assertFalse( B.sameValueAs( A ) );
        assertFalse( A.matches( B ) );
        assertFalse( B.matches( A ) );
        }

    @SuppressWarnings("deprecation")
    public void testProgrammaticValues()
        {
        Node ab = NodeFactory.createLiteral( LiteralLabelFactory.createTypedLiteral( (byte) 42 ) );
        Node as = NodeFactory.createLiteral( LiteralLabelFactory.createTypedLiteral( (short) 42 ) );
        Node ai = NodeFactory.createLiteral( LiteralLabelFactory.createTypedLiteral( 42 ) );
        Node al = NodeFactory.createLiteral( LiteralLabelFactory.createTypedLiteral( (long) 42 ) );
        Graph g = graphWith( "" );
        Node SB = NodeCreateUtils.create( "SB" );
        Node SS = NodeCreateUtils.create( "SS" );
        Node SI = NodeCreateUtils.create( "SI" );
        Node SL = NodeCreateUtils.create( "SL" );
        Node P = NodeCreateUtils.create( "P" );
        g.add( Triple.create( SB, P, ab ) );
        g.add( Triple.create( SS, P, as ) );
        g.add( Triple.create( SI, P, ai ) );
        g.add( Triple.create( SL, P, al ) );
        assertEquals( 4, iteratorToSet( g.find( Node.ANY, P, NodeCreateUtils.create( "42" ) ) ).size() );
        }
    }
