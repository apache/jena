/*
  (c) Copyright 2004, Hewlett-Packard Development Company, LP, all rights reserved.
  [See end of file]
  $Id: NodeCache.java,v 1.1 2004-04-22 12:42:27 chris-dollin Exp $
*/

package com.hp.hpl.jena.graph;

/**
    A NodeCache caches nodes according to their labels, to reduce store turnover
    at the expense of some additional computation. The cache is represented as an
    array indexed by the reduced hashcode of the labels of the nodes it contains.
    Only the most recent node with any given reduced hash is kept. This tactic
    means that we don't need to have any explicit cache-clearing code in normal
    oepration.
     
	@author kers
 */
public class NodeCache
    {
    /**
        The defined size of the cache; 5000 is mostly guesswork. (It didn't *quite*
        fill up when running the tests and had about an 85% hit-rate).
    */
    private static int SIZE = 5000;
    
    /**
        The cache nodes, indexed by their label's reduced hash.
    */
    private Node [] nodes = new Node [SIZE];
    
    /**
        Wipe the cache of all entries.
    */
    public void clear()
        { for (int i = 0; i < SIZE; i += 1) nodes[i] = null; }
    
    public int size()
        { return 0; }
    
    private int hits = 0;
    private int misses = 0;
    
    /**
        Answer the number of used slots in the cache
    */
    private int count()
        {
        int result = 0;
        for (int i = 0; i < SIZE; i += 1) if (nodes[i] != null) result += 1;
        return result;
        }
    
    /**
        Answer the node with the given <code>label</code> in the cache, or 
        <code>null</code> if there isn't one. Selects the slot in the cache by the
        reduced hash of the label, and confirms that the Node is the right one using
        .equals() on this label and that node's label.
    */
    public Node get( Object label )
        {
        Node present = nodes[(label.hashCode() & 0x7fffffff) % SIZE]; 
//        if (present == null || !label.equals( present.label )) misses+= 1; else hits += 1;
//        if ((misses + hits) %100 == 0) System.err.println( ">> hits: " + hits + ", misses: " + misses + ", occ: " + count() + "/" + SIZE );
        return present == null || !label.equals( present.label ) ? null : present;
        }
    
    /**
         Record in the cache the designated Node, using the given label (which must
         be .equals() to the Node's label).
    */
    public void put( Object label, Node cached )
        {
        nodes[(label.hashCode() & 0x7fffffff) % SIZE] = cached;
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