/*
  (c) Copyright 2002, 2003, Hewlett-Packard Company, all rights reserved.
  [See end of file]
  $Id: TestTriple.java,v 1.13 2003-08-01 13:25:41 chris-dollin Exp $
*/

package com.hp.hpl.jena.graph.test;

/**
	@author bwm out of kers, then kers updates
*/

import com.hp.hpl.jena.graph.*;
import com.hp.hpl.jena.graph.impl.LiteralLabel;
import com.hp.hpl.jena.rdf.model.AnonId;
import com.hp.hpl.jena.shared.*;

import junit.framework.*;

public class TestTriple extends GraphTestBase
    {    
        
    public TestTriple(String name)
        { super( name ); }
    
    public static TestSuite suite()
        { return new TestSuite( TestTriple.class ); }   
                
    private static final String U = "http://some.domain.name/magic/spells.incant";
    private static final String N = "Alice";
    private static final LiteralLabel L = new LiteralLabel( "ashes are burning", "en", false );
        
    public void testTripleEquals() {
        try {
            Node.cache(false);
            
            // create some nodes to test
            AnonId id = new AnonId();
            LiteralLabel L2 = new LiteralLabel(id.toString(), "", false);
            String U2 = id.toString();
            String N2 = id.toString();
            
            Node[] nodes = new Node[] {
              Node.ANY,
              Node.createAnon(id),    Node.createAnon(),
              Node.createLiteral(L),  Node.createLiteral(L2),
              Node.createURI(U),      Node.createURI(U2),
              Node.createVariable(N), Node.createVariable(N2)
            };
            
            Triple[] triples = 
               new Triple [nodes.length * nodes.length * nodes.length];
            for (int i=0; i<nodes.length; i++) {
                for (int j=0; j<nodes.length; j++) {
                    for (int k=0; k<nodes.length; k++) {
                        triples[i*nodes.length*nodes.length +
                                j*nodes.length +
                                k] = new Triple(nodes[i], nodes[j], nodes[k]);
                    }
                }
            }
            
            // set up the expected results matrix
            // a expected[i][j] is true if triples[i] equals triples[j]
            // triples are expected to be equals if there components are equal
            boolean[][] expected = new boolean[triples.length][triples.length];
            for (int i1=0; i1<nodes.length; i1++) {
                for (int j1=0; j1<nodes.length; j1++) {
                    for (int k1=0; k1<nodes.length; k1++) {
                        for (int i2=0; i2<nodes.length; i2++) {
                            for (int j2=0; j2<nodes.length; j2++) {
                                for (int k2=0; k2<nodes.length; k2++) {
                                    expected[i1*nodes.length*nodes.length +
                                             j1*nodes.length +
                                             k1]
                                            [i2*nodes.length*nodes.length +
                                             j2*nodes.length +
                                             k2] =
                                             nodes[i1].equals(nodes[i2]) &&
                                             nodes[j1].equals(nodes[j2]) &&
                                             nodes[k1].equals(nodes[k2]);
                                }
                            }
                        }
                    }
                }
            }

            assertEquals("triple, null",   triples[0].equals(null), false);
            assertDiffer("triple, string", triples[0], "string");
            
            // now compare each triple with each other triple
            for (int i=0; i<triples.length; i++) {
                for (int j=0; j<triples.length; j++) {
                    if (expected[i][j]) {
                        assertEquals("triples " + i + ", " + j ,
                                                       triples[i], triples[j]);
                    } else {
                        assertDiffer("triples" + i + ", " + j,
                                                       triples[i], triples[j]);
                    }
                }
            }
                
        } finally {
            Node.cache(true);
        }
    }
    
    public void testTripleCreate()
        {
        Node S = Node.create( "s" ), P = Node.create( "p" ), O = Node.create( "o" );
        assertEquals( new Triple( S, P, O ), Triple.create( S, P, O ) );
        }
        
    public void testTripleCreateFromString()
        {
        Node S = Node.create( "a" ), P = Node.create( "_P" ), O = Node.create( "?c" );
        assertEquals( new Triple( S, P, O ), Triple.create( "a _P ?c") );
        }
        
    /**
        Test that triple-creation respects prefixes, assuming that node creation
        does.
    */
    public void testTriplePrefixes()
        {
        Node S = Node.create( "rdf:alpha" ), P = Node.create( "dc:creator" );
        Node O = Node.create( "spoo:notmapped" );
        Triple t = Triple.create( "rdf:alpha dc:creator spoo:notmapped" );
        assertEquals( new Triple( S, P, O ), t );
        }
        
    public void testTripleCreationMapped()
        {
        PrefixMapping pm = PrefixMapping.Factory.create()
            .setNsPrefix( "a", "ftp://foo/" )
            .setNsPrefix( "b", "http://spoo/" )
            ;
        Triple wanted = Triple.create( "ftp://foo/x http://spoo/y c:z" );
        Triple got = Triple.create( pm, "a:x b:y c:z" );
        assertEquals( wanted, got );
        }
        
    public void testPlainTripleMatches()
        {
        testMatches( "S P O" );
        testMatches( "_S _P _O" );
        testMatches( "1 2 3" );
        }
        
    public void testAnyTripleMatches()
        {
        testMatches( "?? P O", "Z P O" );
        testMatches( "S ?? O", "S Q O" );
        testMatches( "S P ??", "S P oh" );
        testMatches( "?? ?? ??", "X Y Z" );
        testMatches( "?? ?? ??", "X Y 1" );
        testMatches( "?? ?? ??", "_X Y Z" );
        testMatches( "?? ?? ??", "X _Y Z" );
        }
        
    private void testMatches( String triple )
        { testMatches( triple, triple ); }
        
    private void testMatches( String pattern, String triple )
        { assertTrue( Triple.create( pattern ).matches( Triple.create( triple ) ) ); }
        
    public void testPlainTripleDoesntMatch()
        {
        testMatchFails( "S P O", "Z P O" );
        testMatchFails( "S P O", "S Q O" );
        testMatchFails( "S P O", "S P oh" );
        }
        
    public void testAnyTripleDoesntMatch()
        {
        testMatchFails( "?? P O", "S P oh" );
        testMatchFails( "S ?? O", "Z R O" );
        testMatchFails( "S P ??", "Z P oh" );
        }
        
    public void testMatchFails( String pattern, String triple )
        { assertFalse( Triple.create( pattern ).matches( Triple.create( triple ) ) ); }
        
    public void testMatchesNodes()
        {
        assertTrue( Triple.create( "S P O" ).matches( node("S" ), node( "P" ), node( "O" ) ) );
        assertTrue( Triple.create( "?? P O" ).matches( node("Z" ), node( "P" ), node( "O" ) ) );
        assertTrue( Triple.create( "S ?? O" ).matches( node("S" ), node( "Q" ), node( "O" ) ) );
        assertTrue( Triple.create( "S P ??" ).matches( node("S" ), node( "P" ), node( "I" ) ) );
    /* */
        assertFalse( Triple.create( "S P O" ).matches( node("Z" ), node( "P" ), node( "O" ) ) );
        assertFalse( Triple.create( "S P O" ).matches( node("S" ), node( "Q" ), node( "O" ) ) );
        assertFalse( Triple.create( "S P O" ).matches( node("Z" ), node( "P" ), node( "I" ) ) );        
        }
        
    public void testElementMatches()
        {
        assertTrue( Triple.create( "S P O" ).subjectMatches( node( "S" ) ) );
        assertTrue( Triple.create( "S P O" ).predicateMatches( node( "P" ) ) );
        assertTrue( Triple.create( "S P O" ).objectMatches( node( "O" ) ) );
    /* */
        assertFalse( Triple.create( "S P O" ).subjectMatches( node( "Z" ) ) );
        assertFalse( Triple.create( "S P O" ).predicateMatches( node( "Q" ) ) );
        assertFalse( Triple.create( "S P O" ).objectMatches( node( "I" ) ) );        
    /* */
        assertTrue( Triple.create( "?? P O" ).subjectMatches( node( "SUB" ) ) );
        assertTrue( Triple.create( "S ?? O" ).predicateMatches( node( "PRED" ) ) );
        assertTrue( Triple.create( "S P ??" ).objectMatches( node( "OBJ" ) ) );    
        }
        
    public void testConcrete()
        {
        assertTrue( Triple.create( "S P O" ).isConcrete() );
        assertTrue( Triple.create( "S P 11").isConcrete() );
        assertTrue( Triple.create( "S P _X").isConcrete() );
        assertTrue( Triple.create( "S _P 11").isConcrete() );
        assertTrue( Triple.create( "_S _P _O").isConcrete() );
        assertTrue( Triple.create( "10 11 12").isConcrete() );
        assertTrue( Triple.create( "S P 11").isConcrete() );
        assertFalse( Triple.create( "?? P 11").isConcrete() );
        assertFalse( Triple.create( "S ?? 11").isConcrete() );
        assertFalse( Triple.create( "S P ??").isConcrete() );
        assertFalse( Triple.create( "?S P 11").isConcrete() );
        assertFalse( Triple.create( "S ?P 11").isConcrete() );
        assertFalse( Triple.create( "S P ?O").isConcrete() );
        }
        
    /**
        Primarily to make sure that literals get quoted and stuff comes out in some
        kind of coherent order.
    */
    public void testTripleToStringOrdering()
        {
        Triple t1 = Triple.create( "subject predicate object" );
        assertTrue( "subject must be present",  t1.toString().indexOf( "subject" ) >= 0 );    
        assertTrue( "subject must preceed predicate", t1.toString().indexOf( "subject" ) < t1.toString().indexOf( "predicate" ) );
        assertTrue( "predicate must preceed object", t1.toString().indexOf( "predicate" ) < t1.toString().indexOf( "object" ) );
        }
        
    public void testTripleToStringQuoting()
        {
        Triple t1 = Triple.create( "subject predicate 'object'" );
        assertTrue( t1.toString().indexOf( "\"object\"") > 0 );
        }
        
    public void testTripleToStringWithPrefixing()
        {
        PrefixMapping pm = PrefixMapping.Factory.create();
        pm.setNsPrefix( "spoo", "eg://domain.dom/spoo#" );
        Triple t1 = Triple.create( "eg://domain.dom/spoo#a b c" );
        assertEquals( "spoo:a @eh:b eh:c", t1.toString( pm ) );
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
