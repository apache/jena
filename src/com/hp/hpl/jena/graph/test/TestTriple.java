/*
  (c) Copyright 2002, Hewlett-Packard Company, all rights reserved.
  [See end of file]
  $Id: TestTriple.java,v 1.4 2003-02-11 15:17:02 chris-dollin Exp $
*/

package com.hp.hpl.jena.graph.test;

/**
	@author bwm out of kers
*/

import com.hp.hpl.jena.graph.LiteralLabel;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.rdf.model.AnonId;

import junit.framework.TestCase;
import junit.framework.TestSuite;


public class TestTriple extends TestCase
    {    
        
        public TestTriple(String name)
        { super( name ); }
    
    public static TestSuite suite()
        { return new TestSuite( TestTriple.class ); }   
            
    public void assertFalse( String name, boolean b )
        { assertTrue( name, !b ); }
    
    private void assertDiffer( String title, Object x, Object y )
        { assertFalse( title, x.equals( y ) ); }
                
    private static final String U = "http://some.domain.name/magic/spells.incant";
    private static final String N = "Alice";
    private static final LiteralLabel L = new LiteralLabel( "ashes are burning", "en", false );
    private static final AnonId A = new AnonId();
        
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
            
            String[] types= {
                "ANY",
                "blank", "blank", "blank",
                "literal", "literal", "literal",
                "uri", "uri", "uri",
                "variable", "variable", "variable"
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
