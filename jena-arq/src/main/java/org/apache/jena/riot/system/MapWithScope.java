/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.jena.riot.system;

import java.util.Map ;

import org.apache.jena.atlas.lib.InternalErrorException ;

/** Maps A's to B's, based on a scope S */
public class MapWithScope<A, B, S>
{
    // ======== Interfaces
    
    public interface ScopePolicy<A, B, S>
    {
        Map<A, B> getScope(S scope) ;
        void clear() ;
    }

    /** Allocate a B, given some A.
     *  Only called once per instance of B if the ScopePolicy map is non-null.
     */ 
    public interface Allocator<A,B>
    {
        /** Allocate - return the same B for a given A.
         * "same" means .equals, not == 
         */
        public B alloc(A item) ;
        
        /** Create a fresh, unique (to within policy) B */  
        public B create() ;
        
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
        if ( item == null )
            throw new InternalErrorException("null in MapWithScope.get(,null)") ;
        
        Map<A, B> map = scopePolicy.getScope(scope) ;
        if ( map == null )
            // No map - no item->allocation tracking.
            return allocator.alloc(item) ;

        B mappedItem = map.get(item) ;
        if ( mappedItem == null )
        {
            mappedItem = allocator.alloc(item) ;
            map.put(item, mappedItem) ;
        }
        return mappedItem ;
    }
    
    /** Create a label that is guaranteed to be fresh */ 
    public B create() { return allocator.create() ; }
    
    /** Clear scope and allocation */
    public void clear() { scopePolicy.clear() ; allocator.reset() ; }
}
