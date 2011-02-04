/*
 * (c) Copyright 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package org.openjena.atlas.lib;

import java.util.Iterator ;

/** A cache */
public interface Cache<Key, Value>
{
    /** Does the cache contain the key? */
    public boolean containsKey(Key key) ;
    
    /** Get from cache - or return null.  
     * Implementations should state whether
     * they are thread-safe or not. */ 
    public Value get(Key key) ;
    
    /** Insert into from cache and return old value (or null if none) */
    public Value put(Key key, Value thing) ;

    /** Remove from cache - return true if key referenecd an entry */
    public boolean remove(Key key) ;
    
    /** Iterate over all keys. Iteratering over the keys requires the caller be thread-safe. */ 
    public Iterator<Key> keys() ;
    
    public boolean isEmpty() ;
    public void clear() ;
    
    /** Current size of cache */
    public long size() ;
    
    /** Register a callback - called when an object is dropped from the cache (optional operation) */ 
    public void setDropHandler(ActionKeyValue<Key,Value> dropHandler) ;
}

/*
 * (c) Copyright 2008, 2009 Hewlett-Packard Development Company, LP
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