/*
 *  (c) Copyright 2003 Hewlett-Packard Development Company, LP
 *  All rights reserved.
 *
 *
 */


//=======================================================================
// Package
package com.hp.hpl.jena.db.impl;

//=======================================================================
// Imports
import java.util.*;


//=======================================================================
/**
* Trivial implementation of the generic cache interface used to cache
* literals and resources. This implementation simple flushes the cache
* when the threshold limit is exceeded.
*
* @author <a href="mailto:der@hplb.hpl.hp.com">Dave Reynolds</a>
* @version $Revision: 1.2 $ on $Date: 2003-08-27 12:56:40 $
*/

public class SimpleCache implements ICache {

    /** The cache itself */
    protected Map cache = new HashMap();

    /** The current size limit */
    protected int threshold;

    /** The current number of entries (probably redundant, just use cache.size) */
    protected int count = 0;

    /**
     * Create an empty cache with the given threshold limit.
     *
     * @param threshold the cache size limit, use 0 for no cache, -1 for
     * unlimited cache growth; any other number indicates the number of cache entries
     */
    public SimpleCache(int threshold) {
        this.threshold = threshold;
    }

    /**
     * Add an entry to the cache
     * @param id the database ID to be used as an index
     * @param val the literal or resources to be stored
     */
    public void put(IDBID id, Object val) {
        if (threshold == 0) return;
        if (threshold > 0 && count >= threshold) {
            cache = new HashMap();
            count = 0;
        }
        count++;
        cache.put(id, val);
    }

    /**
     * Retreive an object from the cache
     * @param id the database ID of the object to be retrieved
     * @return the object or null if it is not in the cache
     */
    public Object get(IDBID id) {
        return cache.get(id);
    }

    /**
     * Set a threshold for the cache size in terms of the count of cache entries.
     * For literals a storage limit rather than a count might be more useful but
     * counts are easier, more general and sufficient for the current use.
     *
     * @param threshold the cache size limit, use 0 for no cache, -1 for
     * unlimited cache growth; any other number indicates the number of cache entries
     */
    public void setLimit(int threshold) {
        this.threshold = threshold;
        if (threshold >= 0 && count > threshold) {
            cache = new HashMap();
            count = 0;
        }
    }

    /**
     * Return the current threshold limit for the cache size.
     */
    public int getLimit() {
        return threshold;
    }
}
/*
 *  (c) Copyright 2003 Hewlett-Packard Development Company, LP
 *  All rights reserved.
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


