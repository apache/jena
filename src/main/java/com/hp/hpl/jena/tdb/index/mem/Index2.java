/*
 * (c) Copyright 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.tdb.index.mem;


import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.openjena.atlas.iterator.IteratorConcat ;
import org.openjena.atlas.lib.DS ;



import com.hp.hpl.jena.graph.Node;

/** Index of (K1, K2) -> List of V */

public class Index2<K1, K2, V>
{
    private Map<K1, Index<K2, List<V>>> map = DS.map() ;
    
    public Index2() {}
    
    public Index<K2, List<V>> get(Node key1) { return map.get(key1) ; }
    
    public List<V> get(K1 key1, K2 key2) { return map.get(key1).get(key2) ; }
    
    public void put(K1 key1, K2 key2, V value)
    { 
        Index<K2, List<V>> x = map.get(key1) ;
        if ( x == null )
        {
            x = new Index<K2, List<V>>() ;
            map.put(key1, x) ;
        }
        
        List<V> z = x.get(key2) ;
        if ( z == null )
        {
            z = DS.list() ;
            x.put(key2, z) ;
        }
        z.add(value) ;
    }
    
    public void remove(K1 key1, K2 key2)
    {
        Index<K2, List<V>> x = map.get(key1) ;
        if ( x == null )
            return ;
        x.remove(key2) ;
    }
    
    public Iterator<V> flatten()
    {
        IteratorConcat<V> all = new IteratorConcat<V>() ;
        for ( K1 k1 : map.keySet() )
        {
            Index<K2, List<V>> x =  map.get(k1) ;
            for ( K2 k2 : x.keys() )
            {
                List<V> y = x.get(k2) ;
                all.add(y.iterator()) ;
            }
        }
        return all ;
    }
    
    public int size() { return map.size() ; }
    public boolean isEmpty() { return map.isEmpty() ; }
}

/*
 * (c) Copyright 2007, 2008, 2009 Hewlett-Packard Development Company, LP
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