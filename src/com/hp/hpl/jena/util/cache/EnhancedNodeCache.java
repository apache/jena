/*
  (c) Copyright 2004, 2005 Hewlett-Packard Development Company, LP, all rights reserved.
  [See end of file]
  $Id: EnhancedNodeCache.java,v 1.2 2005-02-21 12:19:12 andy_seaborne Exp $
*/
package com.hp.hpl.jena.util.cache;

import com.hp.hpl.jena.enhanced.EnhNode;
import com.hp.hpl.jena.graph.Node;

/**
 EnhancedNodeCache
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

    public boolean getEnabled()
        { return enabled; }

    public boolean setEnabled( boolean enabled )
        { boolean result = this.enabled;
        this.enabled = enabled;
        return result; }
    
    public void clear()
        { for (int i = 0; i < elements.length; i += 1) elements[i] = null; }

    public long getGets()
        { return gets; }

    public long getPuts()
        { return puts; }

    public long getHits()
        { return hits; }

    }


/*
	(c) Copyright 2004, 2005 Hewlett-Packard Development Company, LP
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