/*
  (c) Copyright 2002, Hewlett-Packard Development Company, LP
  [See end of file]
  $Id: TestNode.java,v 1.25 2003-08-27 13:00:36 andy_seaborne Exp $
*/

package com.hp.hpl.jena.graph.test;


import com.hp.hpl.jena.graph.*;
import com.hp.hpl.jena.graph.impl.*;
import com.hp.hpl.jena.rdf.model.AnonId;
import com.hp.hpl.jena.shared.*;
import com.hp.hpl.jena.datatypes.*;
import com.hp.hpl.jena.vocabulary.*;

import junit.framework.*;

/**
    @author bwm out of kers
    Exercise nodes. Make sure that the different node types do not overlap
    and that the test predicates work properly on the different node kinds.
*/

public class TestNode extends GraphTestBase
    {    
    public TestNode( String name )
        { super( name ); }
    
    public static TestSuite suite()
        { return new TestSuite( TestNode.class ); }   
            
    private static final String U = "http://some.domain.name/magic/spells.incant";
    private static final String N = "Alice";
    private static final LiteralLabel L = new LiteralLabel( "ashes are burning", "en", false );
    private static final AnonId A = new AnonId();

    public void testBlanks()
        {
        assertTrue( "anonymous nodes are blank", Node.createAnon().isBlank() );
        assertFalse( "anonymous nodes aren't literal", Node.createAnon().isLiteral() );
        assertFalse( "anonymous nodes aren't URIs", Node.createAnon().isURI() );
        assertFalse( "anonymous nodes aren't variables", Node.createAnon().isVariable() );
        assertEquals( "anonymous nodes have the right id", Node.createAnon(A).getBlankNodeId(), A );
        }
        
    public void testLiterals()
        {
        assertFalse( "literal nodes aren't blank", Node.createLiteral( L ).isBlank() );
        assertTrue( "literal nodes are literal", Node.createLiteral( L ).isLiteral() );
        assertFalse( "literal nodes aren't variables", Node.createLiteral( L ).isVariable() );
        assertFalse( "literal nodes aren't URIs", Node.createLiteral( L ).isURI() );
        assertEquals( "literal nodes preserve value", Node.createLiteral( L ).getLiteral(), L );
        }
        
    public void testURIs()
        {
        assertFalse( "URI nodes aren't blank", Node.createURI( U ).isBlank() );
        assertFalse( "URI nodes aren't literal", Node.createURI( U ).isLiteral() );
        assertFalse( "URI nodes aren't variables", Node.createURI( U ).isVariable() );
        assertTrue( "URI nodes are URIs", Node.createURI( U ).isURI() );
        assertEquals( "URI nodes preserve URI", Node.createURI( U ).getURI(), U );
        }
        
    public void testVariables()
        {
        assertFalse( "variable nodes aren't blank", Node.createVariable( N ).isBlank() );
        assertFalse( "variable nodes aren't literal", Node.createVariable( N ).isLiteral() );        
        assertFalse( "variable nodes aren't URIs", Node.createVariable( N ).isURI() );
        assertTrue( "variable nodes are variable", Node.createVariable( N ).isVariable() );
        assertEquals( "variable nodes keep their name", N, Node.createVariable( N ).getName() );
        assertEquals( "variable nodes keep their name", N + "x", Node.createVariable( N + "x" ).getName() );
        }
        
    public void testANY()
        {
        assertFalse( "ANY nodes aren't blank", Node.ANY.isBlank() );
        assertFalse( "ANY nodes aren't literals", Node.ANY.isLiteral() );
        assertFalse( "ANY nodes aren't URIs", Node.ANY.isURI() );
        assertFalse( "ANY nodes aren't variables", Node.ANY.isVariable() );
        assertFalse( "ANY nodes aren't blank", Node.ANY.isBlank() );
        assertFalse( "ANY nodes aren't blank", Node.ANY.isBlank() );
        }
        
    /**
        test cases for equality: an array of (Node, String) pairs. [It's not worth
        making a special class for these pairs.] The nodes are created with caching
        off, to make sure that caching effects don't hide the effect of using .equals().
        The strings are "equality groups": the nodes should test equal iff their
        associated strings test equal. 
    */
    private Object [][] eqTestCases()
        {
        try
            {
            Node.cache( false );           
            AnonId id = new AnonId();
            LiteralLabel L2 = new LiteralLabel( id.toString(), "", false );
            String U2 = id.toString();
            String N2 = id.toString();
            return new Object [][]
                {
                    { Node.ANY, "0" },
                    { Node.createAnon( id ), "1" },
                    { Node.createAnon(), "2" },
                    { Node.createAnon( id ), "1" },
                    { Node.createLiteral( L ), "3" },
                    { Node.createLiteral( L2 ), "4" },
                    { Node.createLiteral( L ), "3" },
                    { Node.createURI( U ), "5" },
                    { Node.createURI( U2 ), "6" },
                    { Node.createURI( U ), "5" },
                    { Node.createVariable( N ), "7" },
                    { Node.createVariable( N2 ), "8" },
                    { Node.createVariable( N ), "7" }
                };
            }
        finally
            { Node.cache( true ); }
        }
        
    public void testNodeEquals() 
        {
        Object [][] tests = eqTestCases();
        for (int i = 0; i < tests.length; i += 1)
            {
            Object [] I = tests[i];
            assertFalse( I[0] + " should not equal null", I[0].equals( null ) );
            assertFalse( I[0] + "should not equal 'String'", I[0].equals( "String" ) );
            for (int j = 0; j < tests.length; j += 1)
                {
                Object [] J = tests[j];
                testEquality( I[1].equals( J[1] ), I[0], J[0] );
                }
            }
        }
        
    private void testEquality( boolean testEq, Object x, Object y )
        {
        String testName = getType( x ) + " " + x + " and " + getType( y ) + " " + y;
        if (testEq)
            assertEquals( testName + "should be equal", x, y );
        else
            assertDiffer( testName + " should differ", x, y );
        }
        
    private String getType( Object x )
        {
        String fullName = x.getClass().getName();
        return fullName.substring( fullName.lastIndexOf( '.' ) + 1 );
        }
        
    public void testEquals()
        {
        try
            {
            Node.cache( false );
            assertDiffer( "different variables", Node.createVariable( "xx" ), Node.createVariable( "yy" ) );
            assertEquals( "same vars", Node.createVariable( "aa" ), Node.createVariable( "aa" ) );
            assertEquals( "same URI", Node.createURI( U ), Node.createURI( U ) );
            assertEquals( "same anon", Node.createAnon( A ), Node.createAnon( A ) );
            assertEquals( "same literal", Node.createLiteral( L ), Node.createLiteral( L ) );
            assertFalse( "distinct URIs", Node.createURI( U ) == Node.createURI( U ) );
            assertFalse( "distinct blanks", Node.createAnon( A ) == Node.createAnon( A ) );
            assertFalse( "distinct literals", Node.createLiteral( L ) == Node.createLiteral( L ) );
            assertFalse( "distinct vars", Node.createVariable( "aa" ) == Node.createVariable( "aa" ) );
            }
        finally
            { Node.cache( true ); }
        }
        
    /**
        test that the label of a Node can be retrieved from that Node in
        a way appropriate to that Node.
    */
    public void testLabels()
        {
        AnonId id = new AnonId();
        assertEquals( "get URI value", U, Node.createURI( U ).getURI() );
        assertEquals( "get blank value", id, Node.createAnon( id ).getBlankNodeId() );
        assertEquals( "get literal value", L, Node.createLiteral( L ).getLiteral() );
        assertEquals( "get variable name", N, Node.createVariable( N ).getName() );
        }
        
    /**
        this is where we test that using the wrong accessor on a Node gets you
        an exception. 
    */
    public void testFailingLabels()
        {
        Node u = Node.createURI( U ), b = Node.createAnon();
        Node l = Node.createLiteral( L ), v = Node.createVariable( N );
        Node a = Node.ANY;
    /* */
        testGetURIFails( a );
        testGetURIFails( b );
        testGetURIFails( l );
        testGetURIFails( v );
    /* */
        testGetLiteralFails( a );
        testGetLiteralFails( u );
        testGetLiteralFails( b );
        testGetLiteralFails( v );
    /* */
        testGetNameFails( a );
        testGetNameFails( u );
        testGetNameFails( b );
        testGetNameFails( l );
    /* */
        testGetBlankNodeIdFails( a );
        testGetBlankNodeIdFails( u );
        testGetBlankNodeIdFails( l );
        testGetBlankNodeIdFails( v );
        }
        
    public void testGetBlankNodeIdFails( Node n )
        { try { n.getBlankNodeId(); fail( n.getClass() + " should fail getName()" ); } catch (UnsupportedOperationException e) {} }

    public void testGetURIFails( Node n )
        { try { n.getURI(); fail( n.getClass() + " should fail getURI()" ); } catch (UnsupportedOperationException e) {} }
        
    public void testGetNameFails( Node n )
        { try { n.getName(); fail( n.getClass() + " should fail getName()" ); } catch (UnsupportedOperationException e) {} }
    
    public void testGetLiteralFails( Node n )
        { try { n.getLiteral(); fail( n.getClass() + " should fail getLiteral()" ); } catch (UnsupportedOperationException e) {} }
        
    public void testCache()
        {
        assertTrue( "remembers URI", Node.createURI( U ) == Node.createURI( U ) );   
        assertTrue( "remembers literal", Node.createLiteral( L ) == Node.createLiteral( L ) );
        assertTrue( "remembers blanks", Node.createAnon( A ) == Node.createAnon( A ) );
        assertTrue( "remembers variables", Node.createVariable( N ) == Node.createVariable( N ) );
        // assertTrue( "remembers valued", Node.createValued( voidTriple ) == Node.createValued( voidTriple ) );
    /* */
        assertFalse( "is not confused", Node.createVariable( N ) == Node.createURI( N ) );
        }
        
    /** 
        Test that the create method does sensible things on null and ""
    */
    public void testCreateBadString()
        {
        try { Node.create( null ); fail( "must catch null argument" ); }
        catch (NullPointerException e) {}
        catch (JenaException e) {}
        try { Node.create( "" ); fail("must catch empty argument" ); }
        catch (JenaException e) {}
        }
        
    /**
        Test that anonymous nodes are created with the correct labels
    */
    public void testCreateAnon()
        {
        String idA = "_xxx", idB = "_yyy";
        Node a = Node.create( idA ), b = Node.create( idB );
        assertTrue( "both must be bnodes", a.isBlank() && b.isBlank() );
        assertEquals( new AnonId( idA ), a.getBlankNodeId() );
        assertEquals( new AnonId( idB ), b.getBlankNodeId() );
        }
        
    public void testCreateVariable()
        {
        String V = "wobbly";
        Node v = Node.create( "?" + V );
        assertTrue( "must be a variable", v.isVariable() );
        assertEquals( "name must be correct", V, v.getName() );
        }
        
    public void testCreateANY()
        {
        assertEquals( "?? must denote ANY", Node.ANY, Node.create( "??" ) );
        }
        
    public void testCreateURI()
        {
        String uri = "http://www.electric-hedgehog.net/";
        testCreateURI( uri );
        testCreateURI( "rdf:trinket", "http://www.w3.org/1999/02/22-rdf-syntax-ns#trinket" );
        testCreateURI( "rdfs:device", "http://www.w3.org/2000/01/rdf-schema#device" );
        testCreateURI( "dc:creator", DC.getURI() + "creator" );
        testCreateURI( "rss:something", RSS.getURI() + "something" );
        testCreateURI( "vcard:TITLE", VCARD.getURI() + "TITLE" );
        testCreateURI( "owl:wol", OWL.NAMESPACE + "wol" );
        }
        
    public void testCreateURIOtherMap()
        {
        String myNS = "eh:foo/bar#", suffix = "something";
        PrefixMapping mine = PrefixMapping.Factory.create().setNsPrefix( "mine", myNS );
        Node n = Node.create( mine, "mine:" + suffix );
        assertEquals( myNS + suffix, n.getURI() );
        }
        
    private void testCreateURI( String inOut )
        { testCreateURI( inOut, inOut ); }
        
    private void testCreateURI( String in, String wanted )
        {
        String got = Node.create( in ).getURI();
        if (!wanted.equals( got ))
            {
            if (in.equals( wanted )) fail( "should preserve " + in );
            else fail( "should translate " + in + " to " + wanted + " not " + got );
            }
        }
        
    public void testCreatePrefixed()
        {
        PrefixMapping pm = PrefixMapping.Factory.create();
        /* TODO Node n = */ Node.create( pm, "xyz" );
        }
        
    public void testToStringWithPrefixMapping()
        {
        PrefixMapping pm = PrefixMapping.Factory.create();
        String prefix = "spoo", ns = "abc:def/ghi#";
        pm.setNsPrefix( prefix, ns );
        String suffix = "bamboozle";
        assertEquals( prefix + ":" + suffix, Node.create( ns + suffix ).toString( pm ) );    
        }
        
    public void testNodeHelp()
        {
        assertTrue( "node() making URIs", node( "hello" ).isURI() );
        assertTrue( "node() making literals", node( "123" ).isLiteral() );
        assertTrue( "node() making literals", node( "'hello'" ).isLiteral() );
        assertTrue( "node() making blanks", node( "_x" ).isBlank() );
        assertTrue( "node() making variables", node( "?x" ).isVariable() );
        }
        
    public void testVisitorPatternNode()
        {
       NodeVisitor returnNode = new NodeVisitor() 
            {
            public Object visitAny( Node_ANY it ) { return it; }
            public Object visitBlank( Node_Blank it, AnonId id ) { return it; }
            public Object visitLiteral( Node_Literal it, LiteralLabel lit ) { return it; }
            public Object visitURI( Node_URI it, String uri ) { return it; }
            public Object visitVariable( Node_Variable it, String name ) { return it; }
            };
        testVisitorPatternNode( "sortOfURI", returnNode );
        testVisitorPatternNode( "?variable", returnNode );
        testVisitorPatternNode( "_anon", returnNode );
        testVisitorPatternNode( "11", returnNode );
        testVisitorPatternNode( "??", returnNode );
        }
        
    private void testVisitorPatternNode( String ns, NodeVisitor v )
        {
        Node n = node( ns ); 
        assertEquals( n, n.visitWith( v ) ); 
        }
        
    private void visitExamples( NodeVisitor nv )
        {        
        node( "sortOfURI" ).visitWith( nv );        
        node( "?variableI" ).visitWith( nv );        
        node( "_anon" ).visitWith( nv );        
        node( "11" ).visitWith( nv );        
        node( "??" ).visitWith( nv );
        }
        
    public void testVisitorPatternValue()
        {
        NodeVisitor checkValue = new NodeVisitor() 
            {
            public Object visitAny( Node_ANY it ) 
                { return null; }
            public Object visitBlank( Node_Blank it, AnonId id ) 
                { assertTrue( it.getBlankNodeId() == id ); return null; }
            public Object visitLiteral( Node_Literal it, LiteralLabel lit ) 
                { assertTrue( it.getLiteral() == lit ); return null; }
            public Object visitURI( Node_URI it, String uri ) 
                { assertTrue( it.getURI() == uri ); return null; }
            public Object visitVariable( Node_Variable it, String name ) 
                { assertEquals( it.getName(), name ); return null; }
            };
        visitExamples( checkValue );
        }
        
    /**
        Test that the appropriate elements of the visitor are called exactly once;
        this relies on the order of the visits in visitExamples.
    */
    public void testVisitorPatternCalled()
        {
        final String [] strings = new String [] { "" };
        NodeVisitor checkCalled = new NodeVisitor() 
            {
            public Object visitAny( Node_ANY it ) 
                { strings[0] += " any"; return null; }
            public Object visitBlank( Node_Blank it, AnonId id ) 
                { strings[0] += " blank"; return null; }
            public Object visitLiteral( Node_Literal it, LiteralLabel lit ) 
                { strings[0] += " literal"; return null; }
            public Object visitURI( Node_URI it, String uri ) 
                { strings[0] += " uri"; return null; }
            public Object visitVariable( Node_Variable it, String name ) 
                { strings[0] += " variable"; return null; }
            };
        String desired = " uri variable blank literal any";        
        visitExamples( checkCalled );
        assertEquals( "all vists must have been made", desired, strings[0] );
        }
        
    public void testSimpleMatches()
        {
        assertTrue( Node.create( "S").matches( Node.create( "S" ) ) );
        assertFalse(  "", Node.create( "S").matches( Node.create( "T" ) ) );
        assertFalse( "", Node.create( "S" ).matches( null ) );
        assertTrue( Node.create( "_X").matches( Node.create( "_X" ) ) );
        assertFalse( "", Node.create( "_X").matches( Node.create( "_Y" ) ) );
        assertFalse( "", Node.create( "_X").matches( null ) );
        assertTrue( Node.create( "10" ).matches( Node.create( "10" ) ) );
        assertFalse( "", Node.create( "10" ).matches( Node.create( "11" ) ) );
        assertFalse( "", Node.create( "10" ).matches( null ) );
        assertTrue( Node.ANY.matches( Node.create( "S" ) ) );
        assertTrue( Node.ANY.matches( Node.create( "_X" ) ) );
        assertTrue( Node.ANY.matches( Node.create( "10" ) ) );
        assertFalse( "", Node.ANY.matches( null ) );
        }
        
    public void testDataMatches()
        {
        TypeMapper tm = TypeMapper.getInstance();
        RDFDatatype dt1 = tm.getTypeByValue( new Integer( 10 ) );
        RDFDatatype dt2 = tm.getTypeByValue( new Short( (short) 10 ) );
        Node a = Node.createLiteral( "10", "", dt1 );
        Node b = Node.createLiteral( "10", "", dt2 );
        assertDiffer( "types must make a difference", a, b );
        assertTrue( "A and B must express the same value", a.sameValueAs( b ) );
        assertTrue( "matching literals must respect sameValueAs", a.matches( b ) );
        }
        
    public void testLiteralToString()
        {
        TypeMapper tm = TypeMapper.getInstance();
        RDFDatatype dtInt = tm.getTypeByValue( new Integer( 10 ) );
        Node plain = Node.createLiteral( "rhubarb", "", false );    
        Node english = Node.createLiteral( "eccentric", "en_UK", false );
        Node typed = Node.createLiteral( "10", "", dtInt );
        assertEquals( "rhubarb", plain.toString() );
        assertEquals( "eccentric~en_UK", english.toString() );
        assertEquals( "10:http://www.w3.org/2001/XMLSchema#int", typed.toString() );
        }
        
    public void testConcrete()
        {
        assertTrue( Node.create( "S" ).isConcrete() );
        assertTrue( Node.create( "_P" ).isConcrete() );
        assertTrue( Node.create( "11" ).isConcrete() );
        assertTrue( Node.create( "'hello'" ).isConcrete() );
        assertFalse( Node.create( "??" ).isConcrete() );
        assertFalse( Node.create( "?x" ).isConcrete() );
        }
    }

/*
    (c) Copyright 2002, 2003 Hewlett-Packard Development Company, LP
    All rights reserved.

    Redistribution and use in source and binary forms, with or without
    modification, are permitted provided that the following conditions
    are met:

    1. Redistributions of source code must retain the above copyright
       notice, this list of conditions and the following disclaimer.

    2. Redistributions in binary form must reproduce the above copyright
       notice, this list of conditions and the following disclaimer in the
       documentation and/or other materials provided with the distribution.

    3. The name of the author may not be used to endorse or promote products
       derived from this software without specific prior written permission.

    THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
    IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
    OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
    IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
    INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
    NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
    DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
    THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
    (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
    THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/
