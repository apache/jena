/*
  (c) Copyright 2004, Hewlett-Packard Development Company, LP, all rights reserved.
  [See end of file]
  $Id: TripleCache.java,v 1.1 2004-04-22 12:42:27 chris-dollin Exp $
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
//        if (already == null || !already.sameAs( s, p, o )) misses += 1; else hits += 1;
//        if ((hits + misses) % 1000 == 0) System.err.println( ">> cache [" + id + "] hits: " + hits + ", misses: " + misses + ", occ: " + count() + "/" + SIZE );
        return already == null || !already.sameAs( s, p, o ) ? null : already;
        }
    }
/*
    (c) Copyright 2004, Hewlett-Packard Development Company, LP
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