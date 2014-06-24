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

import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.graph.NodeFactory ;
import com.hp.hpl.jena.graph.Triple ;
import com.hp.hpl.jena.graph.impl.LiteralLabel ;
import com.hp.hpl.jena.graph.impl.LiteralLabelFactory ;
import com.hp.hpl.jena.rdf.model.AnonId ;
import com.hp.hpl.jena.shared.PrefixMapping ;

public class TestTriple extends GraphTestBase
    {    
        
    public TestTriple(String name)
        { super( name ); }
    
    public static TestSuite suite()
        { return new TestSuite( TestTriple.class ); }   
                
    private static final String U = "http://some.domain.name/magic/spells.incant";
    private static final String N = "Alice";
    private static final LiteralLabel L = LiteralLabelFactory.create( "ashes are burning", "en", false );
        
    public void testTripleEquals() {
        // create some nodes to test
        AnonId id = AnonId.create();
        LiteralLabel L2 = LiteralLabelFactory.create(id.toString(), "", false);
        String U2 = id.toString();
        String N2 = id.toString();

        Node[] nodes = new Node[] {
            Node.ANY,
            NodeFactory.createAnon(id),    NodeFactory.createAnon(),
            NodeFactory.createLiteral(L),  NodeFactory.createLiteral(L2),
            NodeFactory.createURI(U),      NodeFactory.createURI(U2),
            NodeFactory.createVariable(N), NodeFactory.createVariable(N2)
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
    }
    
    public void testTripleCreate()
        {
        Node S = NodeCreateUtils.create( "s" ), P = NodeCreateUtils.create( "p" ), O = NodeCreateUtils.create( "o" );
        assertEquals( new Triple( S, P, O ), Triple.create( S, P, O ) );
        }
        
    public void testTripleCreateFromString()
        {
        Node S = NodeCreateUtils.create( "a" ), P = NodeCreateUtils.create( "_P" ), O = NodeCreateUtils.create( "?c" );
        assertEquals( new Triple( S, P, O ), NodeCreateUtils.createTriple( "a _P ?c") );
        }
        
    /**
        Test that triple-creation respects prefixes, assuming that node creation
        does.
    */
    public void testTriplePrefixes()
        {
        Node S = NodeCreateUtils.create( "rdf:alpha" ), P = NodeCreateUtils.create( "dc:creator" );
        Node O = NodeCreateUtils.create( "spoo:notmapped" );
        Triple t = NodeCreateUtils.createTriple( "rdf:alpha dc:creator spoo:notmapped" );
        assertEquals( new Triple( S, P, O ), t );
        }
        
    public void testTripleCreationMapped()
        {
        PrefixMapping pm = PrefixMapping.Factory.create()
            .setNsPrefix( "a", "ftp://foo/" )
            .setNsPrefix( "b", "http://spoo/" )
            ;
        Triple wanted = NodeCreateUtils.createTriple( "ftp://foo/x http://spoo/y c:z" );
        Triple got = NodeCreateUtils.createTriple( pm, "a:x b:y c:z" );
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
        { assertTrue( NodeCreateUtils.createTriple( pattern ).matches( NodeCreateUtils.createTriple( triple ) ) ); }
        
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
        { assertFalse( NodeCreateUtils.createTriple( pattern ).matches( NodeCreateUtils.createTriple( triple ) ) ); }
        
    public void testMatchesNodes()
        {
        assertTrue( NodeCreateUtils.createTriple( "S P O" ).matches( node("S" ), node( "P" ), node( "O" ) ) );
        assertTrue( NodeCreateUtils.createTriple( "?? P O" ).matches( node("Z" ), node( "P" ), node( "O" ) ) );
        assertTrue( NodeCreateUtils.createTriple( "S ?? O" ).matches( node("S" ), node( "Q" ), node( "O" ) ) );
        assertTrue( NodeCreateUtils.createTriple( "S P ??" ).matches( node("S" ), node( "P" ), node( "I" ) ) );
    /* */
        assertFalse( NodeCreateUtils.createTriple( "S P O" ).matches( node("Z" ), node( "P" ), node( "O" ) ) );
        assertFalse( NodeCreateUtils.createTriple( "S P O" ).matches( node("S" ), node( "Q" ), node( "O" ) ) );
        assertFalse( NodeCreateUtils.createTriple( "S P O" ).matches( node("Z" ), node( "P" ), node( "I" ) ) );        
        }
        
    public void testElementMatches()
        {
        assertTrue( NodeCreateUtils.createTriple( "S P O" ).subjectMatches( node( "S" ) ) );
        assertTrue( NodeCreateUtils.createTriple( "S P O" ).predicateMatches( node( "P" ) ) );
        assertTrue( NodeCreateUtils.createTriple( "S P O" ).objectMatches( node( "O" ) ) );
    /* */
        assertFalse( NodeCreateUtils.createTriple( "S P O" ).subjectMatches( node( "Z" ) ) );
        assertFalse( NodeCreateUtils.createTriple( "S P O" ).predicateMatches( node( "Q" ) ) );
        assertFalse( NodeCreateUtils.createTriple( "S P O" ).objectMatches( node( "I" ) ) );        
    /* */
        assertTrue( NodeCreateUtils.createTriple( "?? P O" ).subjectMatches( node( "SUB" ) ) );
        assertTrue( NodeCreateUtils.createTriple( "S ?? O" ).predicateMatches( node( "PRED" ) ) );
        assertTrue( NodeCreateUtils.createTriple( "S P ??" ).objectMatches( node( "OBJ" ) ) );    
        }
        
    public void testConcrete()
        {
        assertTrue( NodeCreateUtils.createTriple( "S P O" ).isConcrete() );
        assertTrue( NodeCreateUtils.createTriple( "S P 11").isConcrete() );
        assertTrue( NodeCreateUtils.createTriple( "S P _X").isConcrete() );
        assertTrue( NodeCreateUtils.createTriple( "S _P 11").isConcrete() );
        assertTrue( NodeCreateUtils.createTriple( "_S _P _O").isConcrete() );
        assertTrue( NodeCreateUtils.createTriple( "10 11 12").isConcrete() );
        assertTrue( NodeCreateUtils.createTriple( "S P 11").isConcrete() );
        assertFalse( NodeCreateUtils.createTriple( "?? P 11").isConcrete() );
        assertFalse( NodeCreateUtils.createTriple( "S ?? 11").isConcrete() );
        assertFalse( NodeCreateUtils.createTriple( "S P ??").isConcrete() );
        assertFalse( NodeCreateUtils.createTriple( "?S P 11").isConcrete() );
        assertFalse( NodeCreateUtils.createTriple( "S ?P 11").isConcrete() );
        assertFalse( NodeCreateUtils.createTriple( "S P ?O").isConcrete() );
        }
        
    /**
        Primarily to make sure that literals get quoted and stuff comes out in some
        kind of coherent order.
    */
    public void testTripleToStringOrdering()
        {
        Triple t1 = NodeCreateUtils.createTriple( "subject predicate object" );
        assertTrue( "subject must be present", t1.toString().contains( "subject" ) );
        assertTrue( "subject must preceed predicate", t1.toString().indexOf( "subject" ) < t1.toString().indexOf( "predicate" ) );
        assertTrue( "predicate must preceed object", t1.toString().indexOf( "predicate" ) < t1.toString().indexOf( "object" ) );
        }
        
    public void testTripleToStringQuoting()
        {
        Triple t1 = NodeCreateUtils.createTriple( "subject predicate 'object'" );
        assertTrue( t1.toString().indexOf( "\"object\"") > 0 );
        }
        
    public void testTripleToStringWithPrefixing()
        {
        PrefixMapping pm = PrefixMapping.Factory.create();
        pm.setNsPrefix( "spoo", "eg://domain.dom/spoo#" );
        Triple t1 = NodeCreateUtils.createTriple( "eg://domain.dom/spoo#a b c" );
        assertEquals( "spoo:a @eh:/b eh:/c", t1.toString( pm ) );
        }
    
    public void testTripleMaps()
        {
        assertEquals( node( "x" ), Triple.getSubject.map1( NodeCreateUtils.createTriple( "x P z" ) ) );
        assertEquals( node( "P" ), Triple.getPredicate.map1( NodeCreateUtils.createTriple( "x P z" ) ) );
        assertEquals( node( "z" ), Triple.getObject.map1( NodeCreateUtils.createTriple( "x P z" ) ) );
        }
    

    }
