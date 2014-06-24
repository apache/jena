/**
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

package com.hp.hpl.jena.sparql.core;

import java.util.Collections ;
import java.util.LinkedList ;
import java.util.List ;

import org.apache.jena.atlas.lib.Pair ;

import com.hp.hpl.jena.graph.Node ;

/** Capture a record of quad actions */ 
public class DatasetChangesCapture implements DatasetChanges
{
    // ArrayLists have an annoying issue that they grow by copying the internal []-array.
    // This growth is by a fixed factor of adding 50% which for an array
    // with little guidance as to likely size, can lead to undesirable GC
    // and copy-time issues.
    // Using a LinkedList avoids this although it adds overhead for list entries.  
    final private List<Pair<QuadAction, Quad>> actions = new LinkedList<>() ;
        //new ArrayList<Pair<QuadAction, Quad>>() ;
        
    final private boolean captureAdd ;
    final private boolean captureDelete ;
    final private boolean captureNoAdd ;
    final private boolean captureNoDelete ;
    
    /** Capture quad actions, excluding no-ops */
    public DatasetChangesCapture()
    { this(true, true, false, false) ; }

    /** Capture quad actions, either including or excluding the "no ops"
     * @param recordNoOps   Whether to record {@linkplain QuadAction#NO_ADD} and {@linkplain QuadAction#NO_DELETE}  
     */
    public DatasetChangesCapture(boolean recordNoOps)
    { this(true, true, recordNoOps, recordNoOps) ; }

    
    /** Capture quad actions, selectively by category */
    public DatasetChangesCapture(boolean captureAdd, boolean captureDelete,  boolean captureNoAdd, boolean captureNoDelete)
    { 
        this.captureAdd = captureAdd ; 
        this.captureDelete = captureDelete ; 
        this.captureNoAdd = captureNoAdd ; 
        this.captureNoDelete = captureNoDelete ; 
    }

    public List<Pair<QuadAction, Quad>> getActions()
    {
        return Collections.unmodifiableList(actions) ; 
    }
    
    @Override public void start() { }

    @Override
    public void change(QuadAction qaction, Node g, Node s, Node p, Node o)
    {
        Quad q = new Quad(g,s,p,o) ;
        Pair<QuadAction, Quad> pair = Pair.create(qaction, q) ;  
            
        switch(qaction) {
            case ADD : 
                if ( captureAdd ) actions.add(pair) ;
                break ;
            case DELETE :
                if ( captureDelete ) actions.add(pair) ;
                break ;
            case NO_ADD :
                if ( captureNoAdd ) actions.add(pair) ;
                break ;
            case NO_DELETE :
                if ( captureNoDelete ) actions.add(pair) ;
                break ;
        }
    }

    @Override public void finish() { }
}
