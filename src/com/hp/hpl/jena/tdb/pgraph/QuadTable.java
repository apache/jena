/*
 * (c) Copyright 2008 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.tdb.pgraph;

import iterator.Iter;
import lib.Tuple;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.sparql.core.Closeable;
import com.hp.hpl.jena.tdb.TDBException;
import com.hp.hpl.jena.tdb.base.file.Location;
import com.hp.hpl.jena.tdb.index.QuadIndex;
import com.hp.hpl.jena.tdb.lib.Sync;
import com.hp.hpl.jena.tdb.solver.reorder.ReorderTransformation;

public class QuadTable implements Sync, Closeable
{
    private Location location ;
    private QuadIndex indexSPOG ;
    private QuadIndex indexPOSG ;
    private QuadIndex indexOSPG ;
    private QuadIndex indexGSPO ;
    private QuadIndex indexGPOS ;
    private QuadIndex indexGOSP ;
    private NodeTable nodeTable ;
    private ReorderTransformation reorderTransform ;
    
    
    public QuadTable(QuadIndex spog, QuadIndex posg, QuadIndex ospg, 
                     QuadIndex gspo, QuadIndex gpos, QuadIndex gosp,
                     NodeTable nodeTable, 
                     ReorderTransformation reorderTransform, Location location)
    {
        if ( spog == null )
            throw new TDBException("SPOG index is required") ;
        
        this.indexSPOG = spog ;
        this.indexPOSG = posg ;
        this.indexOSPG = ospg ;
        this.indexGSPO = gspo ;
        this.indexGPOS = gpos ;
        this.indexGOSP = gosp ;
        this.nodeTable = nodeTable ;
        this.reorderTransform = reorderTransform ;
        this.location = location ;
        
//        int syncPoint = SystemTDB.SyncTick ;
//        if ( syncPoint > 0 )
//            this.getEventManager().register(new GraphSyncListener(this, syncPoint)) ;
//        this.getEventManager().register(new UpdateListener(this)) ;
    }
    

    public void add(Node graphNode, Triple triple) {} ;
    public void add(Node graphNode, Node subject, Node predicate, Node object){}
    
    public void delete(Node graphNode, Triple triple) {} ;
    public void delete(Node graphNode, Node subject, Node predicate, Node object){}
    
    public Iter<Tuple<Node>> find(Node graphNode, Node subject, Node predicate, Node object) { return null ;}
    
    public Iter<Tuple<Node>> find(NodeId graphNode, NodeId subject, NodeId predicate, NodeId object) { return null ;}

    @Override public void close() {}
    @Override public void sync(boolean force) {}
    
    public Location getLocation()                           { return location ; }
    
}

/*
 * (c) Copyright 2008 Hewlett-Packard Development Company, LP
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