/*
 * (c) Copyright 2011 Epimorphics Ltd.
 * All rights reserved.
 * [See end of file]
 */

package org.openjena.riot.system;

import java.util.Map ;

/** Maps A's to B's, based on a scope S */
public class MapWithScope<A, B, S>
{
    // ======== Interfaces
    
    protected interface ScopePolicy<A, B, S>
    {
        Map<A, B> getScope(S scope) ;
        void clear() ;
    }
    
    protected interface Allocator<A,B>
    {
        public B create(A item) ;
        public void reset() ;
    }
    
    // ======== The Object

    private final ScopePolicy<A,B,S> scopePolicy ;
    private final Allocator<A,B> allocator ;

    protected MapWithScope(ScopePolicy<A,B,S> scopePolicy, Allocator<A,B> allocator)
    {
        this.scopePolicy = scopePolicy ;
        this.allocator = allocator ;
    }
    
    /** Get a B object for an A object in scope S object */
    public B get(S scope, A item)
    {
        Map<A, B> map = scopePolicy.getScope(scope) ;
        B mappedItem = map.get(item) ;
        if ( mappedItem == null )
        {
            mappedItem = allocator.create(item) ;
            map.put(item, mappedItem) ;
        }
        return mappedItem ;
    }
    
    /** Create a label that is guaranteed to be fresh */ 
    public B create() { return allocator.create(null) ; }
    
    /** Clear scope and allocation */
    public void clear() { scopePolicy.clear() ; allocator.reset() ; }
}

/*
 * (c) Copyright 2011 Epimorphics Ltd.
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