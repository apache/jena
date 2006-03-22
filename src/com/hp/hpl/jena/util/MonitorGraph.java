/******************************************************************
 * File:        MonitorGraph.java
 * Created by:  Dave Reynolds
 * Created on:  12-May-2005
 * 
 * (c) Copyright 2005, Hewlett-Packard Development Company, LP
 * [See end of file]
 * $Id: MonitorGraph.java,v 1.2 2006-03-22 13:52:49 andy_seaborne Exp $
 *****************************************************************/

package com.hp.hpl.jena.util;

import java.util.*;
import com.hp.hpl.jena.graph.*;
import com.hp.hpl.jena.graph.impl.*;

/**
 * Graph wrapper which provides normal access to an underlying graph but
 * also maintains a snapshot of the triples it was last known to contain. 
 * A snapshot action
 * causes the set of changes between this and the previous snapshot to
 * be calculated and the cache updated. The snapshot process will also 
 * fire change notification.
 * 
 * @author <a href="mailto:der@hplb.hpl.hp.com">Dave Reynolds</a>
 * @version $Revision: 1.2 $
 */

public class MonitorGraph extends WrappedGraph {
    
    /** The last known snapshot, a set of triples */
    protected Set snapshot = new HashSet();

    /** Constructor, wrap the given graph with a state monitor */
    public MonitorGraph(Graph g) {
        super(g);
    }
    
    /**
     * Compute the differences between the current monitored graph and the last
     * snapshot. The changes will also be forwarded to any listeners.
     * Then take a new snapshot.
     * @param additions a place in which the set of newly added triples should be noted, can be null
     * @param deletions a place in which the set of newly deleted triples should be noted, can be null
     */
    public void snapshot(List additions, List deletions) {
        boolean listening = getEventManager().listening();
        boolean wantAdditions = listening || additions != null;
        boolean wantDeletions = listening || deletions != null;
        
        List additionsTemp = (additions != null) ? additions : new ArrayList();
        List deletionsTemp = (deletions != null) ? deletions : new ArrayList();
        Set  deletionsTempSet = (wantDeletions) ? new HashSet() : null;
        
        if (wantAdditions || wantDeletions) {
            if (wantDeletions) {
                deletionsTempSet.addAll(snapshot);
            }
            for (Iterator i = base.find(Node.ANY, Node.ANY, Node.ANY); i.hasNext(); ) {
                Object triple = i.next();
                if (wantAdditions && ! snapshot.contains(triple)) {
                    additionsTemp.add(triple);
                }
                if (wantDeletions) {
                    deletionsTempSet.remove(triple);
                }
            }
        }
        if (deletions != null) {
            // We use a set for performance computing in deletions but specify a list
            // for the method signature for compatibility with listeners
            deletionsTemp.addAll(deletionsTempSet);
        }
        
        if (listening) {
            getEventManager().notifyAddList(this, additionsTemp);
            getEventManager().notifyDeleteList(this, deletionsTemp);
        }
        
        // Update shapshot
        // In somecases applying the already computed changes may be cheaper, could optmize
        // this based on relative sizes if it becomes an issue.
        snapshot.clear();
        for (Iterator i = base.find(Node.ANY, Node.ANY, Node.ANY); i.hasNext(); ) {
            snapshot.add(i.next());
        }

    }
    
}


/*
    (c) Copyright 2005, 2006 Hewlett-Packard Development Company, LP
    All rights reserved.

    Redistribution and use in source and binary forms, with or without
    modification, are permitted provided that the following conditions
    are met:

    1. Redistributions of source code must retain the above copyright
       notice, this list of conditions and the following disclaimer.

    2. Redistributions in binary form must reproduce the above copyright
       notice, this list of conditions and the following disclaimer in the
       documentation and/or other materials provided with the distribution.

    3. The name of the author may not be used to endorse or promote products
       derived from this software without specific prior written permission.

    THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
    IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
    OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
    IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
    INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
    NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
    DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
    THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
    (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
    THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/
