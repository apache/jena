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
import org.apache.jena.JenaRuntime ;
import org.apache.jena.graph.Graph ;
import org.apache.jena.graph.Node ;
import org.apache.jena.graph.NodeFactory ;
import org.apache.jena.graph.Triple ;
import org.apache.jena.graph.impl.LiteralLabelFactory ;

public class TestFindLiterals extends GraphTestBase
    {
    public TestFindLiterals( String name )
        { super( name ); }
    
    public static TestFindLiterals aTest
        ( final String graph, final int size, final String search, final String results )
        {
        return new TestFindLiterals
            ( "TestFindLiterals: graph {" + graph 
            + "} size " + size 
            + " search " + search 
            + " expecting {" + results + "}" )
            {
            @Override
            public void runBare()
                { 
                Graph g = graphWith( graph );
                Node literal = NodeCreateUtils.create( search );
            //
                assertEquals( "graph has wrong size", size, g.size() );
                Set<Node> got = g.find( Node.ANY, Node.ANY, literal ).mapWith( t -> t.getObject() ).toSet();
                assertEquals( nodeSet( results ), got );
                }
            };
        }
    
    public static TestSuite suite()
        { 
        TestSuite result = new TestSuite( TestFindLiterals.class ); 
    //
        result.addTest( aTest( "a P 'simple'", 1, "'simple'", "'simple'" ) );
        result.addTest( aTest( "a P 'simple'xsd:string", 1, "'simple'", "'simple'xsd:string" ) );
        result.addTest( aTest( "a P 'simple'", 1, "'simple'xsd:string", "'simple'" ) );
        result.addTest( aTest( "a P 'simple'xsd:string", 1, "'simple'xsd:string", "'simple'xsd:string" ) );
    //
        int expected = JenaRuntime.isRDF11 ? 1 : 2 ;
        result.addTest( aTest( "a P 'simple'; a P 'simple'xsd:string", expected, "'simple'", "'simple' 'simple'xsd:string" ) );
        result.addTest( aTest( "a P 'simple'; a P 'simple'xsd:string", expected, "'simple'xsd:string", "'simple' 'simple'xsd:string" ) );
    //
        result.addTest( aTest( "a P 1", 1, "1", "1" ) );
        result.addTest( aTest( "a P '1'xsd:float", 1, "'1'xsd:float", "'1'xsd:float" ) );
        result.addTest( aTest( "a P '1'xsd:double", 1, "'1'xsd:double", "'1'xsd:double" ) );
        result.addTest( aTest( "a P '1'xsd:float", 1, "'1'xsd:float", "'1'xsd:float" ) );
        result.addTest( aTest( "a P '1.1'xsd:float", 1, "'1'xsd:float", "" ) );
        result.addTest( aTest( "a P '1'xsd:double", 1, "'1'xsd:int", "" ) );
    //
        result.addTest( aTest( "a P 'abc'rdf:XMLLiteral", 1, "'abc'", "" ) );
        result.addTest( aTest( "a P 'abc'", 1, "'abc'rdf:XMLLiteral", "" ) );
    //    
    // floats & doubles are not compatible
    //
        result.addTest( aTest( "a P '1'xsd:float", 1, "'1'xsd:double", "" ) );
        result.addTest( aTest( "a P '1'xsd:double", 1, "'1'xsd:float", "" ) );
    //
        result.addTest( aTest( "a P 1", 1, "'1'", "" ) );
        result.addTest( aTest( "a P 1", 1, "'1'xsd:integer", "'1'xsd:integer" ) );
        result.addTest( aTest( "a P 1", 1, "'1'", "" ) );
        result.addTest( aTest( "a P '1'xsd:short", 1, "'1'xsd:integer", "'1'xsd:short" ) );
        result.addTest( aTest( "a P '1'xsd:int", 1, "'1'xsd:integer", "'1'xsd:int" ) );
        return result;
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
