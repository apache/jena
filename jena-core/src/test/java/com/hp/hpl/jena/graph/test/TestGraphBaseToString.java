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

import static com.hp.hpl.jena.graph.impl.GraphBase.TOSTRING_TRIPLE_BASE;
import static com.hp.hpl.jena.graph.impl.GraphBase.TOSTRING_TRIPLE_LIMIT;

import java.util.*;

import com.hp.hpl.jena.graph.*;
import com.hp.hpl.jena.graph.impl.GraphBase;
import com.hp.hpl.jena.util.iterator.*;

/**
    Tests for the revisions to GraphBase.toString() to see that it's compact,
    ie outputs no more than LIMIT triples.
*/
public class TestGraphBaseToString extends GraphTestBase
    {
    private static final class LittleGraphBase extends GraphBase
        {
        Set<Triple> triples = new HashSet<>();
        
        @Override public void performAdd( Triple t )
            { triples.add( t ); }
        
        @Override protected ExtendedIterator<Triple> graphBaseFind( TripleMatch m )
            { return WrappedIterator.<Triple>create( triples.iterator() ); }
        }

    public TestGraphBaseToString( String name )
        { super( name ); }

    public void testToStringBaseAndLimit()
        {
        assertTrue( "triple base count must be greater than 0", 0 < GraphBase.TOSTRING_TRIPLE_BASE );
        assertTrue( "triple base count must be less than limit", GraphBase.TOSTRING_TRIPLE_BASE < GraphBase.TOSTRING_TRIPLE_LIMIT );
        assertTrue( "triple count limit must be less than 20", GraphBase.TOSTRING_TRIPLE_LIMIT < 20 );
        }
    
    public void testEllipsisAbsentForSmallModels()
        {
        Graph g = new LittleGraphBase();
        addTriples( g, 1, TOSTRING_TRIPLE_BASE );
        assertFalse( "small model must not contain ellipsis cut-off", g.toString().contains( "\\.\\.\\." ) );
        }
    
    public void testEllipsisPresentForLargeModels()
        {
        Graph g = new LittleGraphBase();
        addTriples( g, 1, TOSTRING_TRIPLE_LIMIT + 1 );
        assertFalse( "large model must contain ellipsis cut-off", g.toString().contains( "\\.\\.\\." ) );
        }
    
    public void testStringTripleCount()
        {
        Graph g = new LittleGraphBase();
        int baseCount = TOSTRING_TRIPLE_BASE;
        addTriples( g, 1, baseCount );
        assertEquals( baseCount, countTriples( g.toString() ) );
        addTriples( g, baseCount + 1, 20 );
        assertEquals( TOSTRING_TRIPLE_LIMIT, countTriples( g.toString() ) );
        }

    private int countTriples( String string )
        { return string.replaceAll( "[^;]+", "" ).length() + 1; }

    private void addTriples( Graph g, int from, int to )
        {
        for (int i = from; i <= to; i += 1)
            g.add( NodeCreateUtils.createTriple( "s p " + i ) );
        }
    }
