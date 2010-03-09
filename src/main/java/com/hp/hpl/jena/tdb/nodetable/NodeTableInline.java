/*
 * (c) Copyright 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.tdb.nodetable;


import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.tdb.nodetable.NodeTable ;
import com.hp.hpl.jena.tdb.store.NodeId ;

/** NodeTable wrapper to hanlde inline node ids.
 * If a node can be made inline, then the underlying table never sees it.
 * If an inline Nodeid is seen, itis decoded and returned without
 * the underlying table being called. 
 */

public class NodeTableInline extends NodeTableWrapper
{
    // Stack order: Inline > Cache > Actual
    
    public static NodeTable create(NodeTable nodeTable)
    {
        return new NodeTableInline(nodeTable) ;
    }
    
    private NodeTableInline(NodeTable nodeTable)
    {
        super(nodeTable) ;
    }
    
    @Override
    public final NodeId getAllocateNodeId(Node node)
    {
        NodeId nid = NodeId.inline(node) ;
        if ( nid != null ) return nid ;
        return super.getAllocateNodeId(node) ;
    }

    @Override
    public final NodeId getNodeIdForNode(Node node)
    {
        NodeId nid = NodeId.inline(node) ;
        if ( nid != null ) return nid ;
        return super.getNodeIdForNode(node) ;
    }
    @Override
    public final Node getNodeForNodeId(NodeId id)
    {
        Node n = NodeId.extract(id) ;
        if ( n != null )
            return n ;
        return super.getNodeForNodeId(id) ;
    }
}

/*
 * (c) Copyright 2009 Hewlett-Packard Development Company, LP
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
