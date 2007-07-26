/*
    (c) Copyright 2005, 2006, 2007 Hewlett-Packard Development Company, LP
    All rights reserved - see end of file.
    $Id: BunchMap.java,v 1.7 2007-07-26 13:05:01 chris-dollin Exp $
*/
package com.hp.hpl.jena.mem;

import java.util.Iterator;

import com.hp.hpl.jena.util.iterator.ExtendedIterator;

/**
    A pruned (and slightly stewed) version of Map, containing just those operations
    required by NodeToTriplesMaps. BunchMaps contain only TripleBunch's.
    
    @author kers
*/
public interface BunchMap
    {
    /**
        Clear this map: all entries are removed.
    */
    public void clear();
    
    /**
        The number of items in the bunch.
    */
    public long size();

    /**
        Answer the TripleBunch associated with <code>key</code>, or 
        <code>null</code> if there isn't one.
    */
    public TripleBunch get( Object key );

    /**
        Associate <code>key</code> and <code>value</code>. Any existing
        association of <code>key</code> is lost. <code>get</code> on this key
        will now deliver this value.
    */
    public void put( Object key, TripleBunch value );

    /**
        Remove any association for <code>key</code>; <code>get</code> on this
        key will now deliver <code>null</code>.
    */
    public void remove( Object key );

    /**
        Answer an iterator over all the keys in this map.
    */
    public ExtendedIterator keyIterator();
    }

/*
 * (c) Copyright 2005, 2006, 2007 Hewlett-Packard Development Company, LP
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. The name of the author may not be used to endorse or promote products
 *    derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/