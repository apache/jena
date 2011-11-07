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
    protected static final int SIZE = 5000;
    
    /**
        The cache nodes, indexed by their label's reduced hash.
    */
    protected final Node [] nodes = new Node [SIZE];
    
    protected static final boolean counting = false;
    
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
        if (counting)
            {
            if (present == null || !label.equals( present.label )) misses += 1; else hits += 1;
            if ((misses + hits) % 100 == 0) 
                System.err.println( ">> hits: " + hits + ", misses: " + misses + ", occ: " + count() + "/" + SIZE );
            }
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
