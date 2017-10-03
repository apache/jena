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

package org.apache.jena.tdb2.setup;

import org.apache.jena.atlas.lib.ArrayUtils ;
import org.apache.jena.atlas.logging.Log ;
import org.apache.jena.tdb2.store.DatasetGraphTDB;
import org.apache.jena.tdb2.store.nodetable.NodeTable;
import org.apache.jena.tdb2.store.nodetable.NodeTableCache;
import org.apache.jena.tdb2.store.nodetable.NodeTableInline;
import org.apache.jena.tdb2.store.tupletable.TupleIndex;

public class TDBDatasetDetails {
    public final TupleIndex[] tripleIndexes ;
    public final TupleIndex[] quadIndexes ;
    public final NodeTable ntTop ;
    public NodeTableInline ntInline ;
    public NodeTableCache ntCache ;
    public NodeTable ntBase ;
    
    
    public TDBDatasetDetails(DatasetGraphTDB dsg) {
        ntTop = dsg.getTripleTable().getNodeTupleTable().getNodeTable() ;
        tripleIndexes = ArrayUtils.copy(dsg.getTripleTable().getNodeTupleTable().getTupleTable().getIndexes()) ;
        quadIndexes = ArrayUtils.copy(dsg.getQuadTable().getNodeTupleTable().getTupleTable().getIndexes()) ;
        
        fillInNodeTableDetails() ;
        fillInIndexDetails() ;
    }

    private void fillInNodeTableDetails() {
        // Nodetable.
        NodeTable ntx = ntTop ;
        while(ntx.wrapped() != null ) {
            if ( ntx instanceof NodeTableInline ) {
                if ( ntInline != null )
                    Log.warn(this, "Multiple NodeTableInline") ;
                ntInline = (NodeTableInline)ntx ;
            }
            else if ( ntx instanceof NodeTableCache ) {
                if ( ntCache != null )
                    Log.warn(this, "Multiple NodeTableCache") ;
                ntCache = (NodeTableCache)ntx ;
            }
            ntx = ntx.wrapped() ;
        } 
        
        ntBase = ntx ;
        
        if ( ntInline == null )
            Log.warn(this, "No NodeTableInline") ;
        if ( ntCache == null )
            Log.warn(this, "No NodeTableCache") ;
        if ( ntBase == null )
            Log.warn(this, "No base NodeTable") ;
    }
    
    private void fillInIndexDetails() {
    }
}
