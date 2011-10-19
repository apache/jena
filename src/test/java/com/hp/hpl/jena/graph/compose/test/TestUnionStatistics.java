/*
 	(c) Copyright 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 	All rights reserved.
 	$Id: TestUnionStatistics.java,v 1.1 2009-06-29 08:55:42 castagna Exp $
*/

package com.hp.hpl.jena.graph.compose.test;

import com.hp.hpl.jena.graph.*;
import com.hp.hpl.jena.graph.compose.MultiUnion;
import com.hp.hpl.jena.graph.impl.GraphBase;
import com.hp.hpl.jena.graph.test.GraphTestBase;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;

public class TestUnionStatistics extends GraphTestBase
    {
    public TestUnionStatistics( String name )
        { super( name ); }

    static class AnInteger
        {
        public int value = 0;
        
        public AnInteger( int value )
            { this.value = value; }
        }
    
    public void testUnionValues() 
        {
        testUnion( 0, 0, 0, 0 );
        }

    public void testCopiesSingleNonZeroResult()
        {
        testUnion( 1, 1, 0, 0 );
        testUnion( 1, 0, 1, 0 );
        testUnion( 1, 0, 0, 1 );
        testUnion( 1, 1, 0, 0 );
        testUnion( 2, 0, 2, 0 );
        testUnion( 4, 0, 0, 4 );
        }

    public void testResultIsSumOfBaseResults()
        {
        testUnion( 3, 1, 2, 0 );
        testUnion( 5, 1, 0, 4 );
        testUnion( 6, 0, 2, 4 );
        testUnion( 7, 1, 2, 4 );
        testUnion( 3, 0, 2, 1 );
        testUnion( 5, 4, 1, 0 );
        testUnion( 6, 2, 2, 2 );
        testUnion( 7, 6, 0, 1 );
        }

    public void testUnknownOverrulesAll()
        {
        testUnion( -1, -1, 0, 0 );
        testUnion( -1, 0, -1, 0 );
        testUnion( -1, 0, 0, -1 );
        testUnion( -1, -1, 1, 1 );
        testUnion( -1, 1, -1, 1 );
        testUnion( -1, 1, 1, -1 );
        }

    /**
        Asserts that the statistic obtained by probing the three-element union
        with statistics <code>av</code>, <code>bv</code>, and 
        <code>cv</code> is <code>expected</code>.
    */
    private void testUnion( int expected, int av, int bv, int cv )
        {
        AnInteger a = new AnInteger( av ), b = new AnInteger( bv ), c = new AnInteger( cv );
        Graph g1 = graphWithGivenStatistic( a );
        Graph g2 = graphWithGivenStatistic( b );
        Graph g3 = graphWithGivenStatistic( c );
        Graph [] graphs = new Graph[]{g1, g2, g3};
        MultiUnion mu = new MultiUnion( graphs );
        GraphStatisticsHandler gs = new MultiUnion.MultiUnionStatisticsHandler( mu );
        assertEquals( expected, gs.getStatistic( Node.ANY, Node.ANY, Node.ANY ) );
        }
    
    static Graph graphWithGivenStatistic( final AnInteger x )
        {
        return new GraphBase() 
            {
            @Override protected ExtendedIterator<Triple> graphBaseFind( TripleMatch m )
                {    
                throw new RuntimeException( "should never be called" );
                }
            
            @Override protected GraphStatisticsHandler createStatisticsHandler()
                {
                return new GraphStatisticsHandler()
                    {
                    @Override
                    public long getStatistic( Node S, Node P, Node O )
                        { return x.value; }
                    };
                }
            };
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