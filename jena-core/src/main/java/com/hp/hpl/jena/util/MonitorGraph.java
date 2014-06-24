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
 */

public class MonitorGraph extends WrappedGraph {
    
    /** The last known snapshot, a set of triples */
    protected Set<Triple> snapshot = new HashSet<>();

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
    public void snapshot(List<Triple> additions, List<Triple> deletions) {
        boolean listening = getEventManager().listening();
        boolean wantAdditions = listening || additions != null;
        boolean wantDeletions = listening || deletions != null;
        
        List<Triple> additionsTemp = (additions != null) ? additions : new ArrayList<Triple>();
        List<Triple> deletionsTemp = (deletions != null) ? deletions : new ArrayList<Triple>();
        Set<Triple>  deletionsTempSet = (wantDeletions) ? new HashSet<Triple>() : null;
        
        if (wantAdditions || wantDeletions) {
            if (wantDeletions) {
                deletionsTempSet.addAll(snapshot);
            }
            for (Iterator<Triple> i = base.find(Node.ANY, Node.ANY, Node.ANY); i.hasNext(); ) {
                Triple triple = i.next();
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
        for (Iterator<Triple> i = base.find(Node.ANY, Node.ANY, Node.ANY); i.hasNext(); ) {
            snapshot.add(i.next());
        }

    }
    
}
