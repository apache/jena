/*
 *  (c) Copyright Hewlett-Packard Company 2003
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

import com.hp.hpl.jena.shared.JenaException;


//=======================================================================
/**
* As simple LRU cache based on LinkedHashMap. otherwise, pretty much
* the same as SimpleCache.
*
* @author <a href="mailto:der@hplb.hpl.hp.com">Dave Reynolds</a>
* @version $Revision: 1.1 $ on $Date: 2003-08-25 02:14:02 $
*/

public class LRUCache implements ICache {
	
	public class myLinkedHashMap extends LinkedHashMap {
		
		int threshold;
		
		public myLinkedHashMap ( int max ) {
			super(max);
			threshold = max;
		}
		
		protected boolean removeEldestEntry ( Map.Entry eldest ) {
			return size() > threshold;
		}
	}
	
	protected int maxCount;
	
	protected myLinkedHashMap cache;

    public LRUCache(int max) {
    	maxCount = max;
		cache = new myLinkedHashMap(max);
    }
    
	public Object get(IDBID id) {
		return cache.get(id);
	}

    public void put(IDBID id, Object val) {
        cache.put(id, val);
        if ( cache.size() > maxCount ) {
			throw new JenaException("LRUCache exceeds threshold");
		}
    }
    
    public void clear() {
    	cache.clear();
    }
    
	public void setLimit(int max) {
		maxCount = max;
		cache = new myLinkedHashMap(max);
	}

	public int getLimit() {
		return maxCount;
	}

}
/*
 *  (c) Copyright Hewlett-Packard Company 2003.
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


