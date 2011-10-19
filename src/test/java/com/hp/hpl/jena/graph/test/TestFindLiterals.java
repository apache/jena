/*
 	(c) Copyright 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 	All rights reserved - see end of file.
<<<<<<< TestFindLiterals.java
 	$Id: TestFindLiterals.java,v 1.2 2009-07-27 09:13:36 andy_seaborne Exp $
=======
 	$Id: TestFindLiterals.java,v 1.2 2009-07-27 09:13:36 andy_seaborne Exp $
>>>>>>> 1.4
*/

package com.hp.hpl.jena.graph.test;

import java.util.Set;

import junit.framework.TestSuite;

import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.graph.impl.LiteralLabelFactory;
import com.hp.hpl.jena.util.iterator.Map1;

public class TestFindLiterals extends GraphTestBase
    {
    public TestFindLiterals( String name )
        { super( name ); }

    static final Map1<Triple, Node> getObject = new Map1<Triple, Node>() 
        {
        @Override
        public Node map1( Triple o ) { return o.getObject(); }
        };
    
    public static TestFindLiterals aTest
        ( final String graph, final String size, final String search, final String results )
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
                int n = Integer.parseInt( size );
                Node literal = NodeCreateUtils.create( search );
            //
                assertEquals( "graph has wrong size", n, g.size() );
                Set<Node> got = g.find( Node.ANY, Node.ANY, literal ).mapWith( getObject ).toSet();
                assertEquals( nodeSet( results ), got );
                }
            };
        }
    
    public static TestSuite suite()
        { 
        TestSuite result = new TestSuite( TestFindLiterals.class ); 
    //
        result.addTest( aTest( "a P 'simple'", "1", "'simple'", "'simple'" ) );
        result.addTest( aTest( "a P 'simple'xsd:string", "1", "'simple'", "'simple'xsd:string" ) );
        result.addTest( aTest( "a P 'simple'", "1", "'simple'xsd:string", "'simple'" ) );
        result.addTest( aTest( "a P 'simple'xsd:string", "1", "'simple'xsd:string", "'simple'xsd:string" ) );
    //
        result.addTest( aTest( "a P 'simple'; a P 'simple'xsd:string", "2", "'simple'", "'simple' 'simple'xsd:string" ) );
        result.addTest( aTest( "a P 'simple'; a P 'simple'xsd:string", "2", "'simple'xsd:string", "'simple' 'simple'xsd:string" ) );
    //
        result.addTest( aTest( "a P 1", "1", "1", "1" ) );
        result.addTest( aTest( "a P '1'xsd:float", "1", "'1'xsd:float", "'1'xsd:float" ) );
        result.addTest( aTest( "a P '1'xsd:double", "1", "'1'xsd:double", "'1'xsd:double" ) );
        result.addTest( aTest( "a P '1'xsd:float", "1", "'1'xsd:float", "'1'xsd:float" ) );
        result.addTest( aTest( "a P '1.1'xsd:float", "1", "'1'xsd:float", "" ) );
        result.addTest( aTest( "a P '1'xsd:double", "1", "'1'xsd:int", "" ) );
    //
        result.addTest( aTest( "a P 'abc'rdf:XMLLiteral", "1", "'abc'", "" ) );
        result.addTest( aTest( "a P 'abc'", "1", "'abc'rdf:XMLLiteral", "" ) );
    //    
    // floats & doubles are not compatible
    //
        result.addTest( aTest( "a P '1'xsd:float", "1", "'1'xsd:double", "" ) );
        result.addTest( aTest( "a P '1'xsd:double", "1", "'1'xsd:float", "" ) );
    //
        result.addTest( aTest( "a P 1", "1", "'1'", "" ) );
        result.addTest( aTest( "a P 1", "1", "'1'xsd:integer", "'1'xsd:integer" ) );
        result.addTest( aTest( "a P 1", "1", "'1'", "" ) );
        result.addTest( aTest( "a P '1'xsd:short", "1", "'1'xsd:integer", "'1'xsd:short" ) );
        result.addTest( aTest( "a P '1'xsd:int", "1", "'1'xsd:integer", "'1'xsd:int" ) );
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
        Node ab = Node.createLiteral( LiteralLabelFactory.create( new Byte((byte)42) ) );
        Node as = Node.createLiteral( LiteralLabelFactory.create( new Short((short)42) ) );
        Node ai = Node.createLiteral( LiteralLabelFactory.create( new Integer(42) ) );
        Node al = Node.createLiteral( LiteralLabelFactory.create( new Long(42) ) );
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


/*
 * (c) Copyright 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. The name of the author may not be used to endorse or promote products
 *    derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/