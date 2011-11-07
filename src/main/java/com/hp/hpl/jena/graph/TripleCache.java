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

package com.hp.hpl.jena.graph;

/**
	TripleCache caches triples according to their SPO members, to reduce store
    turnover at the expense of some added computation. The cache is implemented
    as an array indexed by the (reduced) hashCode of the triples it stores. 
    Each slot is treated independantly and only the most recent stored triple is
    remembered - there is no weighting, LRU, or anything like that.

	@author kers
*/
public class TripleCache
    {
    /**
         The size of the cache array. 1000 gets 80% hits when running the Jena
         test suite and all the slots get used. 10000 gets about 83% and *almost* all
         the slots get used. Absent real performance indicators, 1000 will do for
         now.
    */
    public static int SIZE = 1000;
    
    /**
         The array holding the cached triples.
    */
    private Triple [] triples = new Triple[SIZE];
    
    /**
         Cache the triple <code>t</code> by storing it in the slot with the its reduced 
         hash. Any triple already in that slot vanishes. Answer that triple.
    */
    public Triple put( Triple t )
        { triples[(t.hashCode() & 0x7fffffff) % SIZE] = t; return t; }
    
    private static int count = 0;
    private int id = ++count;
    private int hits = 0;
    private int misses = 0;
        
    /**
         Answer the number of occupied slots in the cache array.
    */
    private int count()
        {
        int result = 0;
        for (int i = 0; i < SIZE; i += 1) if (triples[i] != null) result += 1;
        return result;
        }
    
    /**
         Answer any triple in the cache with subject <code>s</code>, predicate
         <code>p</code>, and object <code>o</code>, or <code>null</code> if
         no such triple exists.
     <p>
         The implementation looks in the slot with the same reduced hashCode as
         the SPO combination would have. If the triple there has the same SPO,
         it is returned; otherwise <code>null</code> is returned.
    */
    public Triple get( Node s, Node p, Node o )
        { 
        Triple already = triples[(Triple.hashCode( s, p, o ) & 0x7fffffff) % SIZE]; 
        if (false)
            {
            if (already == null || !already.sameAs( s, p, o )) misses += 1; else hits += 1;
            if ((hits + misses) % 1000 == 0) System.err.println( ">> cache [" + id + "] hits: " + hits + ", misses: " + misses + ", occ: " + count() + "/" + SIZE );
            }
        return already == null || !already.sameAs( s, p, o ) ? null : already;
        }
    }
