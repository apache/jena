/*
 *  (c) Copyright Hewlett-Packard Company 2002
 *  
 *  All rights reserved.
 * 
 * See end of file.
 */

package com.hp.hpl.jena.util.cache;

/** A caching store for objects.
 *
 * <p>A caching store will hold on to some objects for some
 * time, but may fail to store them.  It is used as an
 * optimization, so that objects that have already been
 * constructed, need not be made again.  The null object
 * should not be stored under a key as there is no way
 * to distingish this from a missing object.</p>
 *
 * <p>Cache objects are usually created using the {@link CacheManager }.</p>
 *
 * <p>An object is associated with a key which is used to
 * identify the object on retrieval.  Only one object may be
 * associated with a key.</p>
 *
 * @author bwm
 * @version
 */
public interface Cache extends CacheControl {
    /** Get and object from the cache, if it is there.
     * @param key the key for the object sought
     * @return the object associated with the key, or null if
     * the key is not found in the cache
     */    
    public Object get(Object key);
    /** Store an object in the cache
     * @param key the key for the object being stored
     * @param value the object stored under the key
     *
     */    
    public void put(Object key, Object value);
}

/*
 *  (c) Copyright Hewlett-Packard Company 2002
 *  
 *  All rights reserved.
 * 
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
 *
 * $Id: Cache.java,v 1.1.1.1 2002-12-19 19:21:07 bwm Exp $
 */

