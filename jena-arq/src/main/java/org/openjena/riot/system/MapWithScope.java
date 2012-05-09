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
