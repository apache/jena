/*
 * (c) Copyright 2011 Epimorphics Ltd.
 * All rights reserved.
 * [See end of file]
 */

package tx;

import java.util.HashMap ;
import java.util.Iterator ;
import java.util.Map ;
import java.util.Map.Entry ;

import org.openjena.atlas.iterator.Iter ;
import org.openjena.atlas.iterator.IteratorConcat ;
import org.openjena.atlas.iterator.Transform ;
import org.openjena.atlas.lib.Pair ;
import tx.journal.Journal ;

import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.tdb.nodetable.NodeTable ;
import com.hp.hpl.jena.tdb.store.NodeId ;

public class NodeTableJournal implements NodeTable
{
    // Becomes   extends NodeTableNative.
    // Uses a second ObjectFile as the journal for write ahead.
    // Write name of this to Journal.
    // Transaction commit is now two sync's, not one.
    
    
    private final Journal journal ;
    private final NodeTable other ;
    
    // Node we have.
    private Map<NodeId, Node> nodeId2Node = new HashMap<NodeId, Node>()  ;
    private Map<Node, NodeId> node2NodeId = new HashMap<Node, NodeId>()  ;

    public NodeTableJournal(Journal journal, NodeTable sub)
    {
        this.journal = journal ;
        this.other = sub ;
    }

    // Low level interface for "pre allocation"
    // Life would be easier if ids were increments.
    
    // OR do everything but write - return (id,bytes) that would be written (sans length)
    
//  @Override
//  public NodeId preallocate(Node node, NodeId last)
//  {
//      return null ;
//  }
    
    @Override
    public NodeId getAllocateNodeId(Node node)
    {
        // ????????????
        return null ;
    }

    @Override
    public NodeId getNodeIdForNode(Node node)
    {
        NodeId nodeId = node2NodeId.get(node) ;
        if ( nodeId != null )
            return nodeId ;    
        return other.getNodeIdForNode(node) ;
    }

    @Override
    public Node getNodeForNodeId(NodeId id)
    {
        Node node = nodeId2Node.get(id) ;
        if ( node != null )
            return node ;
        return other.getNodeForNodeId(id) ;
    }

    @Override
    public Iterator<Pair<NodeId, Node>> all()
    {
        Iterator<Pair<NodeId, Node>> iter1 =
            Iter.map(nodeId2Node.entrySet().iterator(), new Transform<Map.Entry<NodeId, Node>,Pair<NodeId, Node>>(){
                @Override
                public Pair<NodeId, Node> convert(Entry<NodeId, Node> item)
                {
                    return new Pair<NodeId, Node>(item.getKey(), item.getValue()) ;
                }}) ;
        return IteratorConcat.concat(iter1, other.all()) ;
    }

    // Read-only with repect to the underlying NodeTable

    @Override
    public void sync()  { }

    @Override
    public void close() { }


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