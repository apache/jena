/*
 *  (c) Copyright 2002 Hewlett-Packard Development Company, LP
 *  
 *  All rights reserved.
 * 
 * See end of file.
 */

package com.hp.hpl.jena.util.cache;

/** An interface for controlling the behaviour of a cache.
 *
 * <p>This is separated from the main {@link Cache } interface
 * so that methods return an object that can set control
 * parameters on a cache, without granting read/write access
 * to the cache itself.</p>
 *
 * <p>Cache's may be enabled or disabled.  A disabled cache
 * is a silent cache; it will silently not return objects
 * from its store and not update its store.  It will operate
 * as if the cache always missed.</p>
 *
 * <p>Cache's keep statistics on their accesses.  On a long
 * running cache the numbers may exceeed the size of the
 * variables counting the statistics, in which case, the
 * fields counting gets hits and puts are reduced
 * proportionately.</p>
 *
 * @author bwm
 * @version $Version$
 */
public interface CacheControl {
    
    /** Get the enabled state of the cache
     * @return The enabled state of the cache
     */    
    public boolean getEnabled();
    
    /** Set the enabled state of a cache
     * @param enabled the new enabled state of the cache
     * @return the previous enabled state of the cache
     */    
    public boolean setEnabled(boolean enabled);
    
    /** Clear the cache's store
     */    
    public void clear();
    
    /** Return number of gets on this cache.
     *
     *
     * @return The number of gets on this cache.
     */    
    public long getGets();
    /** Get the number of puts on this cache
     * @return the number of puts
     */    
    public long getPuts();
    /** Get the number of hits on this cache
     * @return the number of hits
     */    
    public long getHits();
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
 * $Id: CacheControl.java,v 1.2 2003-08-27 13:07:54 andy_seaborne Exp $
 */

