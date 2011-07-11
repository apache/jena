/*
 * (c) Copyright 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.tdb.store;

import org.openjena.atlas.lib.Closeable ;
import org.openjena.atlas.lib.Sync ;
import org.openjena.atlas.lib.Tuple ;

import com.hp.hpl.jena.graph.Graph ;
import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.graph.Triple ;
import com.hp.hpl.jena.shared.Lock ;
import com.hp.hpl.jena.sparql.engine.optimizer.reorder.Reorderable ;
import com.hp.hpl.jena.tdb.base.file.Location ;
import com.hp.hpl.jena.tdb.nodetable.NodeTupleTable ;
import com.hp.hpl.jena.tdb.sys.Session ;

public interface GraphTDB extends Graph, Closeable, Sync, Reorderable, Session
{
    public NodeTupleTable getNodeTupleTable() ;
    public Tuple<Node> asTuple(Triple triple) ;
    
    /** Get a lock that is shared for all graphs from the same dataset (it is the dataset lock) */
    public Lock getLock() ;
    
    /**
     * Return the graph node for this graph if it's in a quad table, else return
     * null for a triple table based (e.g. the default graph of a dataset)
     */ 
    public Node getGraphNode() ;
    
    /** Return the TDB-backed daatset for this graph.
     *  Maybe null - indicating it's a simple graph backed by TDB
     *  (and also the concrete default graph) 
     */
    public DatasetGraphTDB getDataset() ;
    
    public Location getLocation() ; 
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