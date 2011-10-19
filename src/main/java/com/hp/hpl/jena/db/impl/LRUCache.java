/*
 *  (c) Copyright 2002, 2003, 2004, 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
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
* @version $Revision: 1.1 $ on $Date: 2009-06-29 08:55:37 $
*/

public class LRUCache<K,T> implements ICache<K,T>
{
	// Predates LinkedHashMap
	protected Map<K, T> keyCache;
	protected Map<T, K> valCache;

	protected K Keys[];
	protected Random rand;

	public LRUCache(int max) {
		rand = new Random();
		resize(max);
	}
	
	@SuppressWarnings("unchecked")
    protected void resize ( int max ) {
		maxCount = max;
		keyCache = CollectionFactory.createHashedMap(max);
		valCache = CollectionFactory.createHashedMap(max);
		Keys = (K[]) new Object[max];
		count = 0;
	}

	protected int maxCount;
	protected int count;

	@Override
    public T get(K id) {
		return keyCache.get(id);
	}
	
	public Object getByValue(String val) {
		return valCache.get(val);
	}


	@Override
    public void put(K id, T val) {
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

    public void clear() {
    	keyCache.clear();
    	valCache.clear();
    	count = 0;
    }
    
	@Override
    public void setLimit(int max) {
		resize(max);
	}

	@Override
    public int getLimit() {
		return maxCount;
	}

}
/*
 *  (c) Copyright 2002, 2003, 2004, 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
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


