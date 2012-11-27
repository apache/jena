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

package com.hp.hpl.jena.tdb.solver;

import java.util.HashMap;
import java.util.Iterator ;
import java.util.Map;

import org.apache.jena.atlas.lib.Map2 ;



import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.sparql.engine.binding.Binding ;

import com.hp.hpl.jena.tdb.store.NodeId;

/** Class for a Binding-like structure except it works on NodeIds, not on Nodes */  
public class BindingNodeId extends Map2<Var, NodeId>
{
    // This is the parent binding - which may be several steps up the chain. 
    // This just carried around for later use when we go BindingNodeId back to Binding.
    private final Binding parentBinding ;

    // Possible optimization: there are at most 3 possible values so HashMap is overkill.
    // Use a chain of small objects.
    
    private BindingNodeId(Map<Var, NodeId> map1, Map2<Var, NodeId> map2, Binding parentBinding)
    {
        super(map1, map2) ;
        this.parentBinding = parentBinding ;
    }

    // Make from an existing BindingNodeId 
    public BindingNodeId(BindingNodeId other)
    {
        this(new HashMap<Var, NodeId>(), other, other.getParentBinding()) ;
    }
    
    // Make from an existing Binding 
    public BindingNodeId(Binding binding)
    {
        this(new HashMap<Var, NodeId>(), null, binding) ;
    }

    public BindingNodeId()
    {
        this(new HashMap<Var, NodeId>(), null, null) ;
    }
    
    public Binding getParentBinding()    { return parentBinding ; } 
    
    //@Override public NodeId get(Var v)    { return super.get(v) ; } 
    
    @Override public void put(Var v, NodeId n)
    {
        if ( v == null || n == null )
            throw new IllegalArgumentException("("+v+","+n+")") ;
        super.put(v, n) ;
    }
    
    public void putAll(BindingNodeId other)
    {
        Iterator<Var> vIter = other.iterator() ;
        
        for ( ; vIter.hasNext() ; )
        {
            Var v = vIter.next() ;
            if ( v == null )
                throw new IllegalArgumentException("Null key") ;
            NodeId n = other.get(v) ;
            if ( n == null )
                throw new IllegalArgumentException("("+v+","+n+")") ;
            super.put(v, n) ;
        }
    }
    
    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder() ;
        
        boolean first = true ;
        for ( Var v : this )
        {
            if ( ! first )
                sb.append(" ") ;
            first = false ;
            NodeId x = get(v) ;
            sb.append(v) ;
            sb.append(" = ") ;
            sb.append(x) ;
        }
            
        return sb.toString() ;
        
    }
}
