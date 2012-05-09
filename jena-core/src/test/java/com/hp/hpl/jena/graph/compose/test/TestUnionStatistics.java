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
