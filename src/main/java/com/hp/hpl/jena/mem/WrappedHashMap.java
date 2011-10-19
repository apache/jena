/*
    (c) Copyright 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
    All rights reserved - see end of file.
    $Id: WrappedHashMap.java,v 1.1 2009-06-29 08:55:55 castagna Exp $
*/
package com.hp.hpl.jena.mem;

import java.util.Map;

import com.hp.hpl.jena.util.CollectionFactory;
import com.hp.hpl.jena.util.iterator.*;

/**
    An implementation of BunchMap that delegates to a [Hashed]Map.
*/
public class WrappedHashMap implements BunchMap
    {
    protected final Map<Object, TripleBunch> map = CollectionFactory.createHashedMap();
    
    @Override
    public void clear()
        { map.clear(); }
    
    @Override
    public long size()
        { return map.size(); }

    @Override
    public TripleBunch get( Object key )
        { return map.get( key ); }

    @Override
    public void put( Object key, TripleBunch value )
        { map.put( key, value ); }

    @Override
    public void remove( Object key )
        { map.remove( key ); }

    @Override
    public ExtendedIterator<Object> keyIterator()
        { return WrappedIterator.create( map.keySet().iterator() ); }
    }

/*
 * (c) Copyright 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
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