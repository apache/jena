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

package com.hp.hpl.jena.graph.test;


import junit.framework.TestSuite ;

import com.hp.hpl.jena.datatypes.RDFDatatype ;
import com.hp.hpl.jena.datatypes.TypeMapper ;
import com.hp.hpl.jena.graph.* ;
import com.hp.hpl.jena.graph.impl.LiteralLabel ;
import com.hp.hpl.jena.graph.impl.LiteralLabelFactory ;
import com.hp.hpl.jena.rdf.model.AnonId ;
import com.hp.hpl.jena.rdf.model.impl.Util ;
import com.hp.hpl.jena.shared.JenaException ;
import com.hp.hpl.jena.shared.PrefixMapping ;
import com.hp.hpl.jena.vocabulary.DC ;
import com.hp.hpl.jena.vocabulary.OWL ;
import com.hp.hpl.jena.vocabulary.RSS ;
import com.hp.hpl.jena.vocabulary.VCARD ;

/**
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
    private static final LiteralLabel L = LiteralLabelFactory.create( "ashes are burning", "en", false );
    private static final AnonId A = AnonId.create();

    public void testBlanks()
        {
        assertTrue( "anonymous nodes are blank", NodeFactory.createAnon().isBlank() );
        assertFalse( "anonymous nodes aren't literal", NodeFactory.createAnon().isLiteral() );
        assertFalse( "anonymous nodes aren't URIs", NodeFactory.createAnon().isURI() );
        assertFalse( "anonymous nodes aren't variables", NodeFactory.createAnon().isVariable() );
        assertEquals( "anonymous nodes have the right id", NodeFactory.createAnon(A).getBlankNodeId(), A );
        }
        
    public void testLiterals()
        {
        assertFalse( "literal nodes aren't blank", NodeFactory.createLiteral( L ).isBlank() );
        assertTrue( "literal nodes are literal", NodeFactory.createLiteral( L ).isLiteral() );
        assertFalse( "literal nodes aren't variables", NodeFactory.createLiteral( L ).isVariable() );
        assertFalse( "literal nodes aren't URIs", NodeFactory.createLiteral( L ).isURI() );
        assertEquals( "literal nodes preserve value", NodeFactory.createLiteral( L ).getLiteral(), L );
        }
        
    public void testURIs()
        {
        assertFalse( "URI nodes aren't blank", NodeFactory.createURI( U ).isBlank() );
        assertFalse( "URI nodes aren't literal", NodeFactory.createURI( U ).isLiteral() );
        assertFalse( "URI nodes aren't variables", NodeFactory.createURI( U ).isVariable() );
        assertTrue( "URI nodes are URIs", NodeFactory.createURI( U ).isURI() );
        assertEquals( "URI nodes preserve URI", NodeFactory.createURI( U ).getURI(), U );
        }
        
    public void testVariables()
        {
        assertFalse( "variable nodes aren't blank", NodeFactory.createVariable( N ).isBlank() );
        assertFalse( "variable nodes aren't literal", NodeFactory.createVariable( N ).isLiteral() );        
        assertFalse( "variable nodes aren't URIs", NodeFactory.createVariable( N ).isURI() );
        assertTrue( "variable nodes are variable", NodeFactory.createVariable( N ).isVariable() );
        assertEquals( "variable nodes keep their name", N, NodeFactory.createVariable( N ).getName() );
        assertEquals( "variable nodes keep their name", N + "x", NodeFactory.createVariable( N + "x" ).getName() );
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
    
    public void testNodeVariableConstructor()
        {
        assertEquals( NodeFactory.createVariable( "hello" ), new Node_Variable( "hello" ) );
        assertEquals( NodeFactory.createVariable( "world" ), new Node_Variable( "world" ) );
        assertDiffer( NodeFactory.createVariable( "hello" ), new Node_Variable( "world" ) );
        assertEquals( "myName", new Node_Variable( "myName" ).getName() );
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
        AnonId id = AnonId.create();
        LiteralLabel L2 = LiteralLabelFactory.create( id.toString(), "", false );

        LiteralLabel LLang1 = LiteralLabelFactory.create( "xyz", "en", null) ;
        LiteralLabel LLang2 = LiteralLabelFactory.create( "xyz", "EN", null) ;

        String U2 = id.toString();
        String N2 = id.toString();
        return new Object [][]
            {
            { Node.ANY, "0" },
            { NodeFactory.createAnon( id ), "1" },
            { NodeFactory.createAnon(), "2" },
            { NodeFactory.createAnon( id ), "1" },
            { NodeFactory.createLiteral( L ), "3" },

            { NodeFactory.createLiteral( L2 ), "4" },
            { NodeFactory.createLiteral( L ), "3" },
            { NodeFactory.createURI( U ), "5" },
            { NodeFactory.createURI( U2 ), "6" },
            { NodeFactory.createURI( U ), "5" },
            { NodeFactory.createVariable( N ), "7" },
            { NodeFactory.createVariable( N2 ), "8" },
            { NodeFactory.createVariable( N ), "7" } ,

            { NodeFactory.createLiteral( LLang1 ), "9" },
            { NodeFactory.createLiteral( LLang2 ), "10" },
            };
    }
        
    public void testNodeEquals() 
        {
        Object [][] tests = eqTestCases();
            for ( Object[] I : tests )
            {
                assertFalse( I[0] + " should not equal null", I[0].equals( null ) );
                assertFalse( I[0] + "should not equal 'String'", I[0].equals( "String" ) );
                for ( Object[] J : tests )
                {
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
        assertDiffer( "different variables", NodeFactory.createVariable( "xx" ), NodeFactory.createVariable( "yy" ) );
        assertEquals( "same vars", NodeFactory.createVariable( "aa" ), NodeFactory.createVariable( "aa" ) );
        assertEquals( "same URI", NodeFactory.createURI( U ), NodeFactory.createURI( U ) );
        assertEquals( "same anon", NodeFactory.createAnon( A ), NodeFactory.createAnon( A ) );
        assertEquals( "same literal", NodeFactory.createLiteral( L ), NodeFactory.createLiteral( L ) );
        assertFalse( "distinct URIs", NodeFactory.createURI( U ) == NodeFactory.createURI( U ) );
        assertFalse( "distinct hyphens", NodeFactory.createAnon( A ) == NodeFactory.createAnon( A ) );
        assertFalse( "distinct literals", NodeFactory.createLiteral( L ) == NodeFactory.createLiteral( L ) );
        assertFalse( "distinct vars", NodeFactory.createVariable( "aa" ) == NodeFactory.createVariable( "aa" ) );
    }

    /**
        test that the label of a Node can be retrieved from that Node in
        a way appropriate to that Node.
    */
    public void testLabels()
        {
        AnonId id = AnonId.create();
        assertEquals( "get URI value", U, NodeFactory.createURI( U ).getURI() );
        assertEquals( "get blank value", id, NodeFactory.createAnon( id ).getBlankNodeId() );
        assertEquals( "get literal value", L, NodeFactory.createLiteral( L ).getLiteral() );
        assertEquals( "get variable name", N, NodeFactory.createVariable( N ).getName() );
        }
        
    /**
        this is where we test that using the wrong accessor on a Node gets you
        an exception. 
    */
    public void testFailingLabels()
        {
        Node u = NodeFactory.createURI( U ), b = NodeFactory.createAnon();
        Node l = NodeFactory.createLiteral( L ), v = NodeFactory.createVariable( N );
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
        
    
    public void testGetBlankNodeLabelString()
        {
        Node n = NodeFactory.createAnon();
        assertEquals( n.getBlankNodeId().getLabelString(), n.getBlankNodeLabel() );
        }
    
    public void testVariableSupport()
        {
        assertEquals( Node_Variable.variable( "xxx" ), Node_Variable.variable( "xxx" ) );
        assertDiffer( Node_Variable.variable( "xxx" ), Node_Variable.variable( "yyy" ) );
        assertEquals( Node_Variable.variable( "aaa" ), Node_Variable.variable( "aaa" ) );
        assertDiffer( Node_Variable.variable( "aaa" ), Node_Variable.variable( "yyy" ) );
        }
    
    /** 
        Test that the create method does sensible things on null and ""
    */
    public void testCreateBadString()
        {
        try { NodeCreateUtils.create( null ); fail( "must catch null argument" ); }
        catch (NullPointerException e) {}
        catch (JenaException e) {}
        try { NodeCreateUtils.create( "" ); fail("must catch empty argument" ); }
        catch (JenaException e) {}
        }
        
    /**
        Test that anonymous nodes are created with the correct labels
    */
    public void testCreateAnon()
        {
        String idA = "_xxx", idB = "_yyy";
        Node a = NodeCreateUtils.create( idA ), b = NodeCreateUtils.create( idB );
        assertTrue( "both must be bnodes", a.isBlank() && b.isBlank() );
        assertEquals( new AnonId( idA ), a.getBlankNodeId() );
        assertEquals( new AnonId( idB ), b.getBlankNodeId() );
        }
        
    public void testCreateVariable()
        {
        String V = "wobbly";
        Node v = NodeCreateUtils.create( "?" + V );
        assertTrue( "must be a variable", v.isVariable() );
        assertEquals( "name must be correct", V, v.getName() );
        }
        
    public void testCreateANY()
        {
        assertEquals( "?? must denote ANY", Node.ANY, NodeCreateUtils.create( "??" ) );
        }
    
    public void testCreatePlainLiteralSingleQuotes()
        {
        Node n = NodeCreateUtils.create( "'xxx'" );
        assertEquals( "xxx", n.getLiteralLexicalForm() );
        assertEquals( "", n.getLiteralLanguage() );
        assertEquals( null, n.getLiteralDatatypeURI() );
        }
    
    public void testCreatePlainLiteralDoubleQuotes()
        {
        Node n = NodeCreateUtils.create( "\"xxx\"" );
        assertEquals( "xxx", n.getLiteralLexicalForm() );
        assertEquals( "", n.getLiteralLanguage() );
        assertEquals( null, n.getLiteralDatatypeURI() );
        }
    
    public void testCreateLiteralBackslashEscape()
        {
        testStringConversion( "xx\\x", "'xx\\\\x'" );
        testStringConversion( "xx\\x\\y", "'xx\\\\x\\\\y'" );
        testStringConversion( "\\xyz\\", "'\\\\xyz\\\\'" );
        }
    
    public void testCreateLiteralQuoteEscapes()
        {
        testStringConversion( "x\'y", "'x\\'y'" );
        testStringConversion( "x\"y", "'x\\\"y'" );
        testStringConversion( "x\'y\"z", "'x\\\'y\\\"z'" );
        }
    
    public void testCreateLiteralOtherEscapes()
        {
        testStringConversion( " ", "'\\s'" );
        testStringConversion( "\t", "'\\t'" );
        testStringConversion( "\n", "'\\n'" );
        }
    
    protected void testStringConversion( String wanted, String template )
        {
        Node n = NodeCreateUtils.create( template );
        assertEquals( wanted, n.getLiteralLexicalForm() );
        assertEquals( "", n.getLiteralLanguage() );
        assertEquals( null, n.getLiteralDatatypeURI() );
        }

    public void testCreateLanguagedLiteralEN1()
        {
        Node n = NodeCreateUtils.create( "'chat'en-UK" );
        assertEquals( "chat", n.getLiteralLexicalForm() );
        assertEquals( "en-UK", n.getLiteralLanguage() );
        assertEquals( null, n.getLiteralDatatypeURI() );
        }    

    public void testCreateLanguagedLiteralEN2()
        {
        Node n1 = NodeCreateUtils.create( "'chat'en-UK" );
        Node n2 = NodeCreateUtils.create( "'chat'EN-UK" );
        assertTrue( n1.sameValueAs(n2) ) ;
        assertFalse( n1.equals(n2) ) ;
        }    
    
    public void testCreateLanguagedLiteralXY()
        {
        Node n = NodeCreateUtils.create( "\"chat\"xy-AB" );
        assertEquals( "chat", n.getLiteralLexicalForm() );
        assertEquals( "xy-AB", n.getLiteralLanguage() );
        assertEquals( null, n.getLiteralDatatypeURI() );
        }
    
    public void testCreateTypedLiteralInteger()
        {
        Node n = NodeCreateUtils.create( "'42'xsd:integer" );
        assertEquals( "42", n.getLiteralLexicalForm() );
        assertEquals( "", n.getLiteralLanguage() );
        assertEquals( expand( "xsd:integer" ), n.getLiteralDatatypeURI() );
        }
    
    public void testCreateTypedLiteralBoolean()
        {
        Node n = NodeCreateUtils.create( "\"true\"xsd:boolean" );
        assertEquals( "true", n.getLiteralLexicalForm() );
        assertEquals( "", n.getLiteralLanguage() );
        assertEquals( expand( "xsd:boolean" ), n.getLiteralDatatypeURI() );
        }
        
    public void testGetPlainLiteralLexicalForm()
        {
        Node n = NodeCreateUtils.create( "'stuff'" );
        assertEquals( "stuff", n.getLiteralLexicalForm() );
        }
    
    public void testGetNumericLiteralLexicalForm()
        {
        Node n = NodeCreateUtils.create( "17" );
        assertEquals( "17", n.getLiteralLexicalForm() );
        }
    
    public void testTypesExpandPrefix()
        {
        testTypeExpandsPrefix( "rdf:spoo" );
        testTypeExpandsPrefix( "rdfs:bar" );
        testTypeExpandsPrefix( "owl:henry" );
        testTypeExpandsPrefix( "xsd:bool" );
        testTypeExpandsPrefix( "unknown:spoo" );
        }
    
    private void testTypeExpandsPrefix( String type )
        {
        Node n = NodeCreateUtils.create( "'stuff'" + type );
        String wanted = PrefixMapping.Extended.expandPrefix( type );
        assertEquals( wanted, n.getLiteralDatatypeURI() );
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
        Node n = NodeCreateUtils.create( mine, "mine:" + suffix );
        assertEquals( myNS + suffix, n.getURI() );
        }
        
    private void testCreateURI( String inOut )
        { testCreateURI( inOut, inOut ); }
        
    private void testCreateURI( String in, String wanted )
        {
        String got = NodeCreateUtils.create( in ).getURI();
        if (!wanted.equals( got ))
            {
            if (in.equals( wanted )) fail( "should preserve " + in );
            else fail( "should translate " + in + " to " + wanted + " not " + got );
            }
        }
        
    public void testCreatePrefixed()
        {
        PrefixMapping pm = PrefixMapping.Factory.create();
        /* TODO Node n = */ NodeCreateUtils.create( pm, "xyz" );
        }
        
    public void testToStringWithPrefixMapping()
        {
        PrefixMapping pm = PrefixMapping.Factory.create();
        String prefix = "spoo", ns = "abc:def/ghi#";
        pm.setNsPrefix( prefix, ns );
        String suffix = "bamboozle";
        assertEquals( prefix + ":" + suffix, NodeCreateUtils.create( ns + suffix ).toString( pm ) );    
        }
        
    public void testNodeHelp()
        {
        assertTrue( "node() making URIs", node( "hello" ).isURI() );
        assertTrue( "node() making literals", node( "123" ).isLiteral() );
        assertTrue( "node() making literals", node( "'hello'" ).isLiteral() );
        assertTrue( "node() making hyphens", node( "_x" ).isBlank() );
        assertTrue( "node() making variables", node( "?x" ).isVariable() );
        }
        
    public void testVisitorPatternNode()
        {
       NodeVisitor returnNode = new NodeVisitor() 
            {
            @Override
            public Object visitAny( Node_ANY it ) { return it; }
            @Override
            public Object visitBlank( Node_Blank it, AnonId id ) { return it; }
            @Override
            public Object visitLiteral( Node_Literal it, LiteralLabel lit ) { return it; }
            @Override
            public Object visitURI( Node_URI it, String uri ) { return it; }
            @Override
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
            @Override
            public Object visitAny( Node_ANY it ) 
                { return null; }
            @Override
            public Object visitBlank( Node_Blank it, AnonId id ) 
                { assertTrue( it.getBlankNodeId() == id ); return null; }
            @Override
            public Object visitLiteral( Node_Literal it, LiteralLabel lit ) 
                { assertTrue( it.getLiteral() == lit ); return null; }
            @Override
            public Object visitURI( Node_URI it, String uri ) 
                { assertTrue( it.getURI() == uri ); return null; }
            @Override
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
            @Override
            public Object visitAny( Node_ANY it ) 
                { strings[0] += " any"; return null; }
            @Override
            public Object visitBlank( Node_Blank it, AnonId id ) 
                { strings[0] += " blank"; return null; }
            @Override
            public Object visitLiteral( Node_Literal it, LiteralLabel lit ) 
                { strings[0] += " literal"; return null; }
            @Override
            public Object visitURI( Node_URI it, String uri ) 
                { strings[0] += " uri"; return null; }
            @Override
            public Object visitVariable( Node_Variable it, String name ) 
                { strings[0] += " variable"; return null; }
            };
        String desired = " uri variable blank literal any";        
        visitExamples( checkCalled );
        assertEquals( "all vists must have been made", desired, strings[0] );
        }
        
    public void testSimpleMatches()
        {
        assertTrue( NodeCreateUtils.create( "S").matches( NodeCreateUtils.create( "S" ) ) );
        assertFalse(  "", NodeCreateUtils.create( "S").matches( NodeCreateUtils.create( "T" ) ) );
        assertFalse( "", NodeCreateUtils.create( "S" ).matches( null ) );
        assertTrue( NodeCreateUtils.create( "_X").matches( NodeCreateUtils.create( "_X" ) ) );
        assertFalse( "", NodeCreateUtils.create( "_X").matches( NodeCreateUtils.create( "_Y" ) ) );
        assertFalse( "", NodeCreateUtils.create( "_X").matches( null ) );
        assertTrue( NodeCreateUtils.create( "10" ).matches( NodeCreateUtils.create( "10" ) ) );
        assertFalse( "", NodeCreateUtils.create( "10" ).matches( NodeCreateUtils.create( "11" ) ) );
        assertFalse( "", NodeCreateUtils.create( "10" ).matches( null ) );
        assertTrue( Node.ANY.matches( NodeCreateUtils.create( "S" ) ) );
        assertTrue( Node.ANY.matches( NodeCreateUtils.create( "_X" ) ) );
        assertTrue( Node.ANY.matches( NodeCreateUtils.create( "10" ) ) );
        assertFalse( "", Node.ANY.matches( null ) );
        }
        
    public void testDataMatches()
        {
        TypeMapper tm = TypeMapper.getInstance();
        RDFDatatype dt1 = tm.getTypeByValue( new Integer( 10 ) );
        RDFDatatype dt2 = tm.getTypeByValue( new Short( (short) 10 ) );
        Node a = NodeFactory.createLiteral( "10", "", dt1 );
        Node b = NodeFactory.createLiteral( "10", "", dt2 );
        assertDiffer( "types must make a difference", a, b );
        assertTrue( "A and B must express the same value", a.sameValueAs( b ) );
        assertTrue( "matching literals must respect sameValueAs", a.matches( b ) );
        }
        
    public void testLiteralToString()
        {
        TypeMapper tm = TypeMapper.getInstance();
        RDFDatatype dtInt = tm.getTypeByValue( new Integer( 10 ) );
        Node plain = NodeFactory.createLiteral( "rhubarb", "", false );    
        Node english = NodeFactory.createLiteral( "eccentric", "en_UK", false );
        Node typed = NodeFactory.createLiteral( "10", "", dtInt );
        assertEquals( "\"rhubarb\"", plain.toString() );
        assertEquals( "rhubarb", plain.toString( false ) );
        assertEquals( "\"eccentric\"@en_UK", english.toString() );
        assertEquals( "10^^http://www.w3.org/2001/XMLSchema#int", typed.toString( false ) );
        }
    
    public void testGetIndexingValueURI()
        {
        Node u = NodeCreateUtils.create( "eh:/telephone" );
        assertSame( u, u.getIndexingValue() );
        }
    
    public void testGetIndexingValueBlank()
        {
        Node b = NodeCreateUtils.create( "_television" );
        assertSame( b, b.getIndexingValue() );
        }
    
    public void testGetIndexingValuePlainString()
        { testIndexingValueLiteral( NodeCreateUtils.create( "'literally'" ) ); }
    
    public void testGetIndexingValueLanguagedString()
        { testIndexingValueLiteral( NodeCreateUtils.create( "'chat'fr" ) ); }
    
    public void testGetIndexingValueXSDString()
        { testIndexingValueLiteral( NodeCreateUtils.create( "'string'xsd:string" ) ); }
    
    private void testIndexingValueLiteral( Node s )
        { assertEquals( s.getLiteral().getIndexingValue(), s.getIndexingValue() ); }
    
    // TODO should have more of these
    public void  testGetLiteralValuePlainString()
        {
        Node s = NodeCreateUtils.create( "'aString'" );
        assertSame( s.getLiteral().getValue(), s.getLiteralValue() );
        }
    
    public void testGetLiteralDatatypeNull()
        {
        assertEquals( null, NodeCreateUtils.create( "'plain'" ).getLiteralDatatype() );
        }
    
    public void testLiteralIsXML()
        {
        assertFalse( NodeCreateUtils.create( "'notXML'" ).getLiteralIsXML() );
        assertFalse( NodeCreateUtils.create( "17" ).getLiteralIsXML() );
        assertFalse( NodeCreateUtils.create( "'joke'xsd:Joke" ).getLiteralIsXML() );
        assertTrue( NodeFactory.createLiteral( "lit", "lang", true ).getLiteralIsXML() );
        assertFalse( NodeFactory.createLiteral( "lit", "lang", false ).getLiteralIsXML() );
        }
   
    public void testConcrete()
        {
        assertTrue( NodeCreateUtils.create( "S" ).isConcrete() );
        assertTrue( NodeCreateUtils.create( "_P" ).isConcrete() );
        assertTrue( NodeCreateUtils.create( "11" ).isConcrete() );
        assertTrue( NodeCreateUtils.create( "'hello'" ).isConcrete() );
        assertFalse( NodeCreateUtils.create( "??" ).isConcrete() );
        assertFalse( NodeCreateUtils.create( "?x" ).isConcrete() );
        }

    static String [] someURIs = new String [] 
        {
    		"http://domainy.thing/stuff/henry",
            "http://whatever.com/stingy-beast/bee",
            "ftp://erewhon/12345",
            "potatoe:rhubarb"
        };
    
    /**
        test that URI nodes have namespace/localname splits which are consistent
        with Util.splitNamepace.
    */
    public void testNamespace()
        {
            for ( String uri : someURIs )
            {
                int split = Util.splitNamespace( uri );
                Node n = NodeCreateUtils.create( uri );
                assertEquals( "check namespace", uri.substring( 0, split ), n.getNameSpace() );
                assertEquals( "check localname", uri.substring( split ), n.getLocalName() );
            }
        }
    
    protected static String [] someNodes =
        {
            "42",
            "'hello'",
            "_anon",
            "'robotic'tick",
            "'teriffic'abc:def"
        };
    
    public void testHasURI()
        {
            for ( String someURI : someURIs )
            {
                testHasURI( someURI );
            }
            for ( String someNode : someNodes )
            {
                testHasURI( someNode );
            }
        }

	protected void testHasURI( String uri ) 
        {
		Node n = NodeCreateUtils.create( uri );
		assertTrue( uri, !n.isURI() || n.hasURI( uri ) );
		assertFalse( uri, n.hasURI( uri + "x" ) );
        }

    /**
     	Answer the string <code>s</code> prefix-expanded using the built-in
     	PrefixMapping.Extended.
    */
    private String expand( String s )
        { return PrefixMapping.Extended.expandPrefix( s ); }
    }
