/**
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

package org.apache.jena.riot.lang;

import java.util.HashMap ;
import java.util.Map ;

import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.graph.NodeFactory ;

/** Allocate blank ndoes according to the traditional policy (up to jena 2.10.0)
 *  This allocator has arbitrary sized state. 
 *  Create a fresh one for each parser run.
 */

public class BlankNodeAllocatorTraditional implements BlankNodeAllocator
{
    Map<String, Node> map = new HashMap<>() ;
    
    public BlankNodeAllocatorTraditional()  {}

    @Override
    public void reset()         { map.clear() ; }

    @Override
    public Node alloc(String label)
    {
        Node b = map.get(label) ;
        if ( b == null )
        {
            b = create() ;
            map.put(label, b) ;
        }
        return b ;
    }
    
    
    @Override
    public Node create()
    {
        return NodeFactory.createAnon() ;
    }
}
