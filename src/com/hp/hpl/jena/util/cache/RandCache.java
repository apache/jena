/*
 *  (c) Copyright 2002 Hewlett-Packard Development Company, LP
 *
 *  All rights reserved.
 *
 * See end of file.
 */

package com.hp.hpl.jena.util.cache;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 *
 * @author  bwm
 */
public class RandCache implements Cache, CacheControl {
    int size;
    int threshhold;
    boolean enabled = true;
                                               // so we can identify caches
    String name;                               // e.g. when logging

    HashMap map;
    Collection collection;

    protected static Log logger = LogFactory.getLog(RandCache.class);
    
    long gets = 0;
    long puts = 0;
    long hits = 0;

    /** Creates new RandCache */
    RandCache(String name, int size) {
        this.size = size;
        try {
            map = new HashMap(size * 100 / 75);  // based on .75 loadfactor
        } catch (IllegalArgumentException e) {
            if ("Illegal load factor: NaN".equals(e.getMessage())) {
                logger.warn("Detected a NaN anomaly believed to be due to use of JDK 1.4.1");
                map = new HashMap(size*100/75, 0.75f);
            } else {
                throw e;
            }
        }
        threshhold = size;
        if (threshhold < 2) {
            throw new Error("Cache size too small: " + size);
        }
        collection = map.values();
    }

    public synchronized Object get(Object key) {
        if (enabled) {
            if (gets == Long.MAX_VALUE) {
                forgetStats();
            }
            gets++;
            Object result = map.get(key);
            if (result != null) {
                hits++;
            }
            return result;
        } else {
            return null;
        }
    }

    public synchronized void put(Object key, Object value) {

        // don't allow null values
        if (value == null) {
            throw new NullPointerException();
        }

        if (enabled) {
            if (puts == Long.MAX_VALUE) {
                forgetStats();
            }
            puts++;
            if (map.size() >= threshhold) {
                makeSpace();
            }
            map.put(key, value);
        }
    }

    protected void makeSpace() {
        Iterator iter = collection.iterator();

        // we are going to remove every 3rd member of the cache
        int size = map.size();
        int i = 3;
        while (i < size ) {
            iter.next();
            iter.remove();
            iter.next();
            iter.next();
            i = i + 3;
        }
    }

    public synchronized boolean getEnabled() {
        return enabled;
    }

    public synchronized boolean setEnabled(boolean enabled) {
        boolean result = enabled;
        this.enabled = enabled;
        return result;
    }

    public synchronized void clear() {
        map.clear();
    }

    public synchronized long getHits() {
        return hits;
    }

    public synchronized long getGets() {
        return gets;
    }

    public synchronized long getPuts() {
        return puts;
    }

    protected void forgetStats() {
        gets = gets/2;
        puts = puts/2;
        hits = hits/2;
    }

}
/*
 *  (c) Copyright 2002 Hewlett-Packard Development Company, LP
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
 * $Id: RandCache.java,v 1.6 2004-05-04 15:26:22 der Exp $
 */
