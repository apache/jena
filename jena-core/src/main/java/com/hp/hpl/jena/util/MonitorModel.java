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

import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.rdf.model.impl.ModelCom;

/**
 * Model wrapper which provides normal access to an underlying model but
 * also maintains a snapshot of the triples it was last known to contain. 
 * A snapshot action
 * causes the set of changes between this and the previous snapshot to
 * be calculated and the cache updated. The snapshot process will also 
 * fire change notification.
 */

public class MonitorModel extends ModelCom {

    /**
     * Create a monitor over the given underlying base model.
     */
    public MonitorModel(Model base) {
        super(new MonitorGraph(base.getGraph()));
    }
    
    /**
     * Compute the differences between the current monitored graph and the last
     * snapshot. The changes will also be forwarded to any listeners.
     * Then take a new snapshot.
     * @param additions a place in which the set of newly added statements should be noted, can be null
     * @param deletions a place in which the set of newly deleted statements should be noted, can be null
     */
    public void snapshot(List<Statement> additions, List<Statement> deletions) {
        List<Triple> additionsTemp = (additions != null) ? new ArrayList<Triple>() : null;
        List<Triple> deletionsTemp = (deletions != null) ? new ArrayList<Triple>() : null;
        ((MonitorGraph)getGraph()).snapshot(additionsTemp, deletionsTemp);
        if (additions != null) {
            for ( Triple anAdditionsTemp : additionsTemp )
            {
                additions.add( this.asStatement( anAdditionsTemp ) );
            }
        }
        if (deletions != null) {
            for ( Triple aDeletionsTemp : deletionsTemp )
            {
                deletions.add( this.asStatement( aDeletionsTemp ) );
            }
        }
    }
    
    /**
     * Compute the differences between the current monitored graph and the last
     * snapshot, forward any changes to registered listeners, then take a new snapshot.
     */
    public void snapshot() {
        snapshot(null, null);
    }
    
}
