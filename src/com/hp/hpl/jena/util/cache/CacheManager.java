/*
     (c) Copyright 2002, 2004 Hewlett-Packard Development Company, LP
     All rights reserved.
     See end of file.
     
     $Id: CacheManager.java,v 1.5 2004-12-01 14:51:04 chris-dollin Exp $
*/
package com.hp.hpl.jena.util.cache;

/** A factory for creating cache objects
 *
 * @author bwm
 */

public class CacheManager {

    public static final String RAND = "RAND";
    
    public static final String ENHNODECACHE = "ENHNODECACHE";

    /** Creates new Manager */
    private CacheManager() {
    }

    /** Create a new cache
     * @param type The type of cache to create.  This should be one
     * of the standard cache types defined in this class.
     * @param name A name for the cache.  This should be unique and
     * may be used to identify the cache in logging and
     * other operations.  To ensure uniqueness it is
     * suggested that cache's be given names similar to
     * full java names such as
     * com.hp.hpl.jena.graph.Node.NodeCache.
     * @param size Teh size of the cache in terms of the number of
     * objects it can store.
     * @return a newly created cache object
     *
     */
    public static Cache createCache(String type, String name, int size) {
        // for now we just have one type
        if (type.equals(RAND)) return new RandCache( name, size );
        if (type.equals(ENHNODECACHE)) return new EnhancedNodeCache( name, size );
        throw new Error( "Bad cache type: " + type );
    }
}
/*
     (c) Copyright 2002, 2004 Hewlett-Packard Development Company, LP
     
     All rights reserved.
     
     
     Redistribution and use in source and binary forms, with or without
     modification, are permitted provided that the following conditions
     are met:
     1. Redistributions of source code must retain the above copyright
        notice, this list of conditions and the following disclaimer.
     2. Redistributions in binary form must reproduce the above copyright
        notice, this list of conditions and the following disclaimer in the
        documentation and/or other materials provided with the distribution.
     3. The name of the author may not be used to endorse or promote products
        derived from this software without specific prior written permission.
    
     THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
     IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
     OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
     IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
     INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
     NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
     DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
     THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
     (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
     THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
     
     $Id: CacheManager.java,v 1.5 2004-12-01 14:51:04 chris-dollin Exp $
*/
