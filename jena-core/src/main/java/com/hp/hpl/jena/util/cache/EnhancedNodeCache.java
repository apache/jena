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

package com.hp.hpl.jena.util.cache;

import com.hp.hpl.jena.enhanced.EnhNode;
import com.hp.hpl.jena.graph.Node;

/**
     EnhancedNodeCache - the cache to use for enhanced nodes (mapping a Node
     to one of the enhanced nodes it can represent).
     
     <p>The cache methods do not need to be synchronised. Java guarantees that
     access/update of reference values is atomic. The <code>get</code> method
     does a single read operation of the cache, and then checks that the
     returned element matches the key, only returning legal objects; changes
     to the cache subsequently don't affect the correctness of the result.
     
     <p>The <code>put</code> method updates the appropriate cache entry as
     a one-shot deal. gets on different slots don't matter. Gets on the same
     slot have either completed (and thus don't care about the change) or
     are about to happen (and will be equally happy with the old or new
     value).
     
     <p>Synchronisation *is* required when updating the EnhNode sibling ring,
     but that doesn't happen here.
*/
public class EnhancedNodeCache implements Cache
    {
    protected String name;
    
    protected EnhNode [] elements;
    
    protected boolean enabled = true;
    
    protected long gets, puts, hits;
    
    public EnhancedNodeCache( String name, int size )
        { this.name = name;
        this.elements = new EnhNode[size]; }

    @Override
    public Object get( Object key )
        {
        if (enabled)
            {
            gets += 1;
            Node n = (Node) key;
            int i = hashNode( n );
            EnhNode result = elements[i];
            if (result != null && result.asNode().equals( key ))
                {
                hits += 1;
                return result;
                }
            }
        return null;
        }

    @Override
    public void put( Object key, Object value )
        {
        if (enabled)
            {
            puts += 1;
            Node n = (Node) key;
            int i = hashNode( n ) ;
            elements[i] = (EnhNode) value;
            }
        }

    /**
     * @param n
     * @return
    */
    protected int hashNode( Node n )
        { return (n.hashCode() & 0x7fffffff) % elements.length; }

    @Override
    public boolean getEnabled()
        { return enabled; }

    @Override
    public boolean setEnabled( boolean enabled )
        { boolean result = this.enabled;
        this.enabled = enabled;
        return result; }
    
    @Override
    public void clear()
        { for (int i = 0; i < elements.length; i += 1) elements[i] = null; }

    @Override
    public long getGets()
        { return gets; }

    @Override
    public long getPuts()
        { return puts; }

    @Override
    public long getHits()
        { return hits; }

    }
