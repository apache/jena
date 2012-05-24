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

package com.hp.hpl.jena.graph.query.test;

import java.util.*;

import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.graph.query.GraphQuery;

public class TestQuery extends QueryTestBase
    {

    public TestQuery( String name )
        { super( name ); }

    public void testEmptyQueryPattern()
        {
        assertEquals( new ArrayList<Triple>(), new GraphQuery().getPattern() );
        }
    
    public void testIOneTriple()
        {
        GraphQuery q = new GraphQuery();
        Triple spo = triple( "S P O" );
        q.addMatch( spo );
        assertEquals( listOfOne( spo ), q.getPattern() );
        }
    
    public void testSeveralTriples()
        {
        Triple [] triples = tripleArray( "a P b; c Q ?d; ?e R '17'" );
        List<Triple> expected = new ArrayList<Triple>();
        GraphQuery q = new GraphQuery();
        for (int i = 0; i < triples.length; i += 1)
            {
            expected.add( triples[i] );
            q.addMatch( triples[i] );
            assertEquals( expected, q.getPattern() );            
            }
        }
    }
