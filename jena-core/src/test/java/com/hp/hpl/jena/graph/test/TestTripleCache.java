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

import junit.framework.*;

import com.hp.hpl.jena.graph.*;

/**
 TestTripleCache

 @author kers
*/
public class TestTripleCache extends GraphTestBase 
    {
	public TestTripleCache( String name ) 
        { super( name ); }
    
    public static TestSuite suite()
        { return new TestSuite( TestTripleCache.class ); }
    
    protected static Triple [] someTriples = makeTriples();
    
    protected static Triple [] makeTriples()
        {
        Node [] S = { node( "x" ), node( "gooseberry" ), node( "_who" ) };
        Node [] P = { node( "p" ), node( "ramshackle" ), node( "_what" ) };
        Node [] O = { node( "o" ), node( "42" ), node( "'alpha'greek" ), node( "_o" ) };
        int here = 0;
        Triple [] result = new Triple[S.length * P.length * O.length];
        for (int i = 0; i < S.length; i += 1)
            for (int j = 0; j < P.length; j += 1)
                for (int k = 0; k < O.length; k += 1)
                    result[here++] = new Triple( S[i], P[j], O[k] );
        return result;
        }
    
    public void testEmptyTripleCache()
        {
        TripleCache tc = new TripleCache();
        for (int i = 0; i < someTriples.length; i += 1)
            {
            Triple t = someTriples[i];
            assertEquals( null, tc.get( t.getSubject(), t.getPredicate(), t.getObject() ) );
            }
        }
    
    public void testPutReturnsTriple()
        {
        TripleCache tc = new TripleCache();
        for (int i = 0; i < someTriples.length; i += 1)
            assertSame( someTriples[i], tc.put( someTriples[i] ) );
        }
    
    public void testPutinTripleCache()
        {
        TripleCache tc = new TripleCache();
        for (int i = 0; i < someTriples.length; i += 1)
            {
            Triple t = someTriples[i];
            tc.put( t );
            assertEquals( t, tc.get( t.getSubject(), t.getPredicate(), t.getObject() ) );
            }
        }
    
    public void testCacheClash()
        {
        TripleCache tc = new TripleCache();
        Triple A = new Triple( node( "eg:Yx" ), node( "p" ), node( "o" ) );
        Triple B = new Triple( node( "eg:ZY" ), node( "p" ), node( "o" ) );
        assertEquals( A.hashCode(), B.hashCode() );
        tc.put( A );
        assertEquals( null, tc.get( B.getSubject(), B.getPredicate(), B.getObject() ) );
        }
    }
