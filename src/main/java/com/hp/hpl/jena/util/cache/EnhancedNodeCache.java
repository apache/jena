/*
  (c) Copyright 2004, 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP, all rights reserved.
  [See end of file]
  $Id: EnhancedNodeCache.java,v 1.1 2009-06-29 08:55:53 castagna Exp $
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
     
     @author kers
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


/*
	(c) Copyright 2004, 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
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