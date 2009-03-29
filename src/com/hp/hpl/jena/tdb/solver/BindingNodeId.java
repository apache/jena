/*
 * (c) Copyright 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.tdb.solver;

import java.util.HashMap;
import java.util.Map;

import atlas.lib.Map2;


import com.hp.hpl.jena.sparql.core.Var;

import com.hp.hpl.jena.tdb.store.NodeId;

/** Class for a Binding-like structure excep tit works on NodeIds, not on Nodes */  
public class BindingNodeId extends Map2<Var, NodeId>
{
    // Possible optimization: there are at most 3 possible values so HashMap is overkill.
    // Use a chain of small objects.
    
    private BindingNodeId(Map<Var, NodeId> map1, Map2<Var, NodeId> map2)
    {
        super(map1, map2) ;
    }
    
    public BindingNodeId(Map2<Var, NodeId> map2)
    {
        super(new HashMap<Var, NodeId>(), map2) ;
    }
    
    public BindingNodeId()
    {
        super(new HashMap<Var, NodeId>(), null) ;
    }
    
    //@Override public NodeId get(Var v)    { return super.get(v) ; } 
    
    @Override public void put(Var v, NodeId n)
    {
        if ( v == null || n == null )
            throw new IllegalArgumentException("("+v+","+n+")") ;
        super.put(v, n) ;
        
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
/*
 * (c) Copyright 2008, 2009 Hewlett-Packard Development Company, LP
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