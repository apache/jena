/*
  (c) Copyright 2002, Hewlett-Packard Company, all rights reserved.
  [See end of file]
  $Id: TestNode.java,v 1.2 2003-01-28 16:20:48 chris-dollin Exp $
*/

package com.hp.hpl.jena.graph.test;

/**
	@author bwm out of kers
*/

import com.hp.hpl.jena.graph.LiteralLabel;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.rdf.model.AnonId;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;


public class TestNode extends TestCase
    {    
        
    public TestNode( String name )
        { super( name ); }
    
    public static TestSuite suite()
        { return new TestSuite( TestNode.class ); }   
            
    public void assertFalse( String name, boolean b )
        { assertTrue( name, !b ); }
    
    private void assertDiffer( String title, Object x, Object y )
        { assertFalse( title, x.equals( y ) ); }
                
    private static final String U = "http://some.domain.name/magic/spells.incant";
    private static final String N = "Alice";
    private static final LiteralLabel L = new LiteralLabel( "ashes are burning", "en", false );
    private static final AnonId A = new AnonId();

    public void testBlanks()
        {
        assertTrue( "anonymous nodes are blank", Node.makeAnon().isBlank() );
        assertFalse( "anonymous nodes aren't literal", Node.makeAnon().isLiteral() );
        assertFalse( "anonymous nodes aren't URIs", Node.makeAnon().isURI() );
        assertFalse( "anonymous nodes aren't variables", Node.makeAnon().isVariable() );
        assertEquals( "anonymous nodes have the right id", Node.makeAnon(A).getBlankNodeId(), A );
        }
        
    public void testLiterals()
        {
        assertFalse( "literal nodes aren't blank", Node.makeLiteral( L ).isBlank() );
        assertTrue( "literal nodes are literal", Node.makeLiteral( L ).isLiteral() );
        assertFalse( "literal nodes aren't variables", Node.makeLiteral( L ).isVariable() );
        assertFalse( "literal nodes aren't URIs", Node.makeLiteral( L ).isURI() );
        assertEquals( "literal nodes preserve value", Node.makeLiteral( L ).getLiteral(), L );
        }
        
    public void testURIs()
        {
        assertFalse( "URI nodes aren't blank", Node.makeURI( U ).isBlank() );
        assertFalse( "URI nodes aren't literal", Node.makeURI( U ).isLiteral() );
        assertFalse( "URI nodes aren't variables", Node.makeURI( U ).isVariable() );
        assertTrue( "URI nodes are URIs", Node.makeURI( U ).isURI() );
        assertEquals( "URI nodes preserve URI", Node.makeURI( U ).getURI(), U );
        }
        
    public void testVariables()
        {
        assertFalse( "variable nodes aren't blank", Node.makeVariable( N ).isBlank() );
        assertFalse( "variable nodes aren't literal", Node.makeVariable( N ).isLiteral() );        
        assertFalse( "variable nodes aren't URIs", Node.makeVariable( N ).isURI() );
        assertTrue( "variable nodes are variable", Node.makeVariable( N ).isVariable() );
        assertEquals( "variable nodes keep their name", N, Node.makeVariable( N ).getName() );
        assertEquals( "variable nodes keep their name", N + "x", Node.makeVariable( N + "x" ).getName() );
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
        
    public void testNodeEquals() {
        try {
            Node.cache(false);
            
            // create some nodes to test
            AnonId id = new AnonId();
            LiteralLabel L2 = new LiteralLabel(id.toString(), "", false);
            String U2 = id.toString();
            String N2 = id.toString();
            
            Node[] nodes = new Node[] {
              Node.ANY,
              Node.makeAnon(id),    Node.makeAnon(),       Node.makeAnon(id),
              Node.makeLiteral(L),  Node.makeLiteral(L2),  Node.makeLiteral(L),
              Node.makeURI(U),      Node.makeURI(U2),      Node.makeURI(U),
              Node.makeVariable(N), Node.makeVariable(N2), Node.makeVariable(N)
            };
            
            String[] types= {
                "ANY",
                "blank", "blank", "blank",
                "literal", "literal", "literal",
                "uri", "uri", "uri",
                "variable", "variable", "variable"
            };
            
            boolean[][] expected = new boolean[nodes.length][nodes.length];
            
            for (int i=0; i<nodes.length; i++) {
                for (int j=0; j<nodes.length; j++) {
                    expected[i][j] = (i==j);
                }
            }
            expected[1][3] = true;
            expected[3][1] = true;
            expected[4][6] = true;
            expected[6][4] = true;
            expected[7][9] = true;
            expected[9][7] = true;
            expected[10][12] = true;
            expected[12][10] = true;                                 
            
            for (int i=0; i<nodes.length; i++) {
                Node n1 = nodes[i];
                String m1 = 
                      "testNodeEquals: " 
                    + types[i] + "(" + n1.toString() + ") ";
                assertEquals(m1 + "String(String)", n1.equals("String"), false);
                assertEquals(m1 + "null", n1.equals(null), false);
                for (int j=0; j<nodes.length; j++) {
                    Node n2 = nodes[j];
                    String m2 = 
                        m1 + types[j] + "(" + n2.toString() + ")";
                    if (expected[i][j]) {
                        assertEquals(m2, n1, n2);
                    } else {
                        assertDiffer(m2, n1, n2);
                    }
                }
            }
                
        } finally {
            Node.cache(true);
        }
    }
    
    public void testEquals()
        {
        try
            {
            Node.cache( false );
            assertDiffer( "different variables", Node.makeVariable( "xx" ), Node.makeVariable( "yy" ) );
            assertEquals( "same vars", Node.makeVariable( "aa" ), Node.makeVariable( "aa" ) );
            assertEquals( "same URI", Node.makeURI( U ), Node.makeURI( U ) );
            assertEquals( "same anon", Node.makeAnon( A ), Node.makeAnon( A ) );
            assertEquals( "same literal", Node.makeLiteral( L ), Node.makeLiteral( L ) );
            assertFalse( "distinct URIs", Node.makeURI( U ) == Node.makeURI( U ) );
            assertFalse( "distinct blanks", Node.makeAnon( A ) == Node.makeAnon( A ) );
            assertFalse( "distinct literals", Node.makeLiteral( L ) == Node.makeLiteral( L ) );
            assertFalse( "distinct vars", Node.makeVariable( "aa" ) == Node.makeVariable( "aa" ) );
            }
        finally
            { Node.cache( true ); }
        }
        
    public void testGetLabel()
        {
            AnonId id = new AnonId();
            String[] types = {
                "ANY",
                "Blank",
                "Literal",
                "URI",
                "VAR"
            };
            Object[] labels = {
                null,
                id,
                L,
                U,
                N
            };
            Node[] nodes = {
                Node.ANY,
                Node.makeAnon((AnonId) labels[1]),
                Node.makeLiteral((LiteralLabel) labels[2]),
                Node.makeURI((String) labels[3]),
                Node.makeVariable((String) labels[4])
            };
            boolean[] expectedBlk = new boolean[nodes.length];
            boolean[] expectedLit = new boolean[nodes.length];
            boolean[] expectedURI = new boolean[nodes.length];
            boolean[] expectedVar = new boolean[nodes.length];
            for (int i=0; i<nodes.length; i++) {
                expectedBlk[i] = false;
                expectedLit[i] = false;
                expectedURI[i] = false;
                expectedVar[i] = false;
            }
            expectedBlk[1] = true;
            expectedLit[2] = true;
            expectedURI[3] = true;
            expectedVar[4] = true;
            
            for (int i=0; i<nodes.length; i++) {
                try {
                    Object label = nodes[i].getBlankNodeId();
                    assertTrue(expectedBlk[i]);
                    assertEquals("getBlankNodeId: " + types[i], 
                                 label,
                                 labels[i]);
                } catch (UnsupportedOperationException e) {
                    assertTrue(!expectedBlk[i]);
                }
                try {
                    Object label = nodes[i].getLiteral();
                    assertTrue(expectedLit[i]);
                    assertEquals("getLiteral: " + types[i], 
                                 label,
                                 labels[i]);
                } catch (UnsupportedOperationException e) {
                    assertTrue(!expectedLit[i]);
                }
                try {
                    Object label = nodes[i].getURI();
                    assertTrue(expectedURI[i]);
                    assertEquals("getURI: " + types[i], 
                                 label,
                                 labels[i]);
                } catch (UnsupportedOperationException e) {
                    assertTrue(!expectedURI[i]);
                }
                try {
                    Object label = nodes[i].getName();
                    assertTrue(expectedVar[i]);
                    assertEquals("getBlankNodeId: " + types[i], 
                                 label,
                                 labels[i]);
                } catch (UnsupportedOperationException e) {
                    assertTrue(!expectedVar[i]);
                }
                
            }

        }
        
    public void testCache()
        {
        assertTrue( "remembers URI", Node.makeURI( U ) == Node.makeURI( U ) );   
        assertTrue( "remembers literal", Node.makeLiteral( L ) == Node.makeLiteral( L ) );
        assertTrue( "remembers blanks", Node.makeAnon( A ) == Node.makeAnon( A ) );
        assertTrue( "remembers variables", Node.makeVariable( N ) == Node.makeVariable( N ) );
    /* */
        assertFalse( "is not confused", Node.makeVariable( N ) == Node.makeURI( N ) );
        }
    }

/*
    (c) Copyright Hewlett-Packard Company 2002
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
