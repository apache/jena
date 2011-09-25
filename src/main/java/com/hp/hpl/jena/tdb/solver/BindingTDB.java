/*
 * (c) Copyright 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.tdb.solver;

import java.util.HashMap ;
import java.util.Iterator ;
import java.util.Map ;

import org.openjena.atlas.logging.Log ;


import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.sparql.core.Var ;
import com.hp.hpl.jena.sparql.engine.binding.BindingBase ;
import com.hp.hpl.jena.tdb.lib.NodeFmtLib ;
import com.hp.hpl.jena.tdb.nodetable.NodeTable ;
import com.hp.hpl.jena.tdb.store.NodeId ;

/** Bind that delays turning a NodeId into a Node until explicitly needed by get() */

public class BindingTDB extends BindingBase
{
    private final NodeTable nodeTable ;
    private final BindingNodeId idBinding ;
    
    private static final boolean caching = false ;
    // Whether the cache is worthwhile is unclear - the NodeTable keeps a cache. 
    private final Map<Var,Node> cache = ( caching ? new HashMap<Var, Node>() : null ) ;

    public BindingTDB(BindingNodeId idBinding, NodeTable nodeTable)
    {
        super(idBinding.getParentBinding()) ;
        this.idBinding = idBinding ;
        this.nodeTable = nodeTable ;
    }

    @Override
    protected int size1() { return idBinding.size(); }
    
    /** Iterate over all the names of variables. */
    @Override
    protected Iterator<Var> vars1() 
    {
        return idBinding.iterator() ;
    }

    @Override
    protected boolean isEmpty1()
    {
        return size1() == 0 ;
    }

    @Override
    public boolean contains1(Var var)
    {
        return idBinding.containsKey(var) ;
    }
    
    public BindingNodeId getBindingId() { return idBinding ; }
    
    public NodeId getNodeId(Var var)
    {
        
        NodeId id = idBinding.get(var) ;
        if ( id != null )
            return id ;
        
        if ( parent == null )
            return null ; 

        // Maybe in the parent.
        if ( parent instanceof BindingTDB )
            return ((BindingTDB)parent).getNodeId(var) ;
        return null ;
    }
    
    @Override
    public Node get1(Var var)
    {
        try {
            Node n = cacheGet(var) ;
            if ( n != null )
                return n ;
            
            NodeId id = idBinding.get(var) ;
            if ( id == null )
                return null ; 
            n = nodeTable.getNodeForNodeId(id) ;
            // Update cache.
            cachePut(var, n) ;
            return n ;
        } catch (Exception ex)
        {
            Log.fatal(this, String.format("get1(%s)", var), ex) ;
            return null ;
        }
    }

    private void cachePut(Var var, Node n)
    {
        if ( cache != null ) cache.put(var, n) ; 
    }

    private Node cacheGet(Var var)
    { 
        if ( cache == null ) return null ;
        return cache.get(var) ;
    }
    
    @Override
    protected void format(StringBuffer sbuff, Var var)
    {
        NodeId id = idBinding.get(var) ;
        String extra = "" ;
        if ( id != null )
            extra = "/"+id ;
        Node node = get(var) ;
        String tmp = NodeFmtLib.displayStr(node) ;
        sbuff.append("( ?"+var.getVarName()+extra+" = "+tmp+" )") ;
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