/*
 *  (c) Copyright 2002, 2003, 2004, 2005 Hewlett-Packard Development Company, LP
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
import com.hp.hpl.jena.util.CollectionFactory;


//=======================================================================
/**
* As simple LRU cache based on LinkedHashMap. otherwise, pretty much
* the same as SimpleCache.
*
* @author <a href="mailto:der@hplb.hpl.hp.com">Dave Reynolds</a>
* @version $Revision: 1.7 $ on $Date: 2005-02-21 12:03:06 $
*/

public class LRUCache implements ICache {
	
	/* don't use until jena moves to jre 1.4
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
	protected myLinkedHashMap cache;
	
    public LRUCache(int max) {
    	maxCount = max;
		cache = new myLinkedHashMap(max);
    }
    
	*/
	
	protected Map keyCache;
	protected Map valCache;

	protected IDBID Keys[];
	protected Random rand;

	public LRUCache(int max) {
		rand = new Random();
		resize(max);
	}
	
	protected void resize ( int max ) {
		maxCount = max;
		keyCache = CollectionFactory.createHashedMap(max);
		valCache = CollectionFactory.createHashedMap(max);
		Keys = new IDBID[max];
		count = 0;
	}

	protected int maxCount;
	protected int count;

	public Object get(IDBID id) {
		return keyCache.get(id);
	}
	
	public Object getByValue(String val) {
		return valCache.get(val);
	}


	public void put(IDBID id, Object val) {
		synchronized (this) {
			int curSize = keyCache.size();
			keyCache.put(id, val);
			valCache.put(val,id);
			if (keyCache.size() > curSize) {
				int ix = count++;
				if (count > maxCount) {
					// pick an entry at random and remove it.
					// not exactly LRU
					ix = rand.nextInt(maxCount);
					Object keyval = keyCache.get(Keys[ix]);
					if ( (keyval == null) || (keyCache.remove(Keys[ix]) == null) )
						throw new JenaException("LRUCache keyCache corrupted");
					if ( valCache.remove(keyval) == null )
						throw new JenaException("LRUCache valCache corrupted");
					count--;
					Keys[ix] = id;
					if (keyCache.size() > maxCount)
						throw new JenaException("LRUCache exceeds threshold");
				}
				Keys[ix] = id;
			}
		}
	}

	/* save for java 1.4
    public void put(IDBID id, Object val) {
        cache.put(id, val);
        if ( cache.size() > maxCount ) {
			throw new JenaException("LRUCache exceeds threshold");
		}
    }
    */
    
    public void clear() {
    	keyCache.clear();
    	valCache.clear();
    	count = 0;
    }
    
	/*
	public void setLimit(int max) {
		maxCount = max;
		cache = new myLinkedHashMap(max);
	}
	*/
	public void setLimit(int max) {
		resize(max);
	}

	public int getLimit() {
		return maxCount;
	}

}
/*
 *  (c) Copyright 2002, 2003, 2004, 2005 Hewlett-Packard Development Company, LP
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


