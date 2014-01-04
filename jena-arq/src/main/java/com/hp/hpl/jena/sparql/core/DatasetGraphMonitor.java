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

package com.hp.hpl.jena.sparql.core ;

import static org.apache.jena.atlas.iterator.Iter.take ;

import java.util.Iterator ;
import java.util.List ;

import com.hp.hpl.jena.graph.Graph ;
import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.graph.Triple ;
import com.hp.hpl.jena.sparql.SystemARQ ;
import com.hp.hpl.jena.util.iterator.ExtendedIterator ;

/** Connect a DatasetGraph to a DatasetChanges monitor.
 *  Any add or delete to the DatasetGraph is notified to the
 *  monitoring object with a {@linkplain QuadAction} to indicate
 *  the change made.   
 */

public class DatasetGraphMonitor extends DatasetGraphWrapper
{
    /** Whether to see if a quad action will change the dataset - test before add for existence, test before delete for absence */   
    private boolean CheckFirst = true ;
    /** Whether to record a no-op (maybe as a comment) */   
    private boolean RecordNoAction = true ;
    /** Where to send the notifications */  
    private final DatasetChanges monitor ;

    /**
     * Create a DatasetGraph wrapper that monitors the dataset for changes (add or delete quads).
     * Use this DatasetGraph for all operations in order to record changes.
     * Note whether additions of deletions cause an actual change to the dataset or not.
     * @param dsg       The DatasetGraph to monitor
     * @param monitor   The handler for a change
     *         
     * @see DatasetChanges
     * @see QuadAction
     */
    public DatasetGraphMonitor(DatasetGraph dsg, DatasetChanges monitor) 
    {
        super(dsg) ;
        this.monitor = monitor ;
    }

    /**
     * Create a DatasetGraph wrapper that monitors the dataset for changes (add or delete quads).
     * Use this DatasetGraph for all operations in order to record changes.  
     * @param dsg       The DatasetGraph to monitor
     * @param monitor   The handler for a change
     * @param recordOnlyIfRealChange
     *         If true, check to see if the chnage would have an effect (e.g. add is a new quad).
     *         If false, log changes as ADD/DELETE regardless of whether the dataset actually changes.
     *         
     * @see DatasetChanges
     * @see QuadAction
     */
    public DatasetGraphMonitor(DatasetGraph dsg, DatasetChanges monitor, boolean recordOnlyIfRealChange) 
    {
        super(dsg) ;
        CheckFirst = recordOnlyIfRealChange ;
        this.monitor = monitor ;
    }

    /** Return the monitor */ 
    public DatasetChanges getMonitor()      { return monitor ; }
    
    /** Return the monitored DatasetGraph */
    public DatasetGraph   monitored()       { return getWrapped() ; }

    @Override public void add(Quad quad)
    {
        if ( CheckFirst && contains(quad) )
        {
            if ( RecordNoAction )
                record(QuadAction.NO_ADD, quad.getGraph(), quad.getSubject(), quad.getPredicate(), quad.getObject()) ;
            return ;
        }
        add$(quad) ;
    }
    
    @Override public void add(Node g, Node s, Node p, Node o)
    {
        if ( CheckFirst && contains(g,s,p,o) )
        {
            if ( RecordNoAction )
                record(QuadAction.NO_ADD,g,s,p,o) ; 
            return ;
        }
        
        add$(g,s,p,o) ;
    }
    
    private void add$(Node g, Node s, Node p, Node o)
    {
        super.add(g,s,p,o) ;
        record(QuadAction.ADD,g,s,p,o) ; 
    }
    
    private void add$(Quad quad)
    {
        super.add(quad) ;
        record(QuadAction.ADD, quad.getGraph(), quad.getSubject(), quad.getPredicate(), quad.getObject()) ;
    }

    @Override public void delete(Quad quad)
    {
        if ( CheckFirst && ! contains(quad) )
        {
            if ( RecordNoAction )
                record(QuadAction.NO_DELETE, quad.getGraph(), quad.getSubject(), quad.getPredicate(), quad.getObject()) ;
            return ;
        }
        delete$(quad) ;
    }
    
    @Override public void delete(Node g, Node s, Node p, Node o)
    {
        if ( CheckFirst && ! contains(g,s,p,o) )
        {
            if ( RecordNoAction )
                record(QuadAction.NO_DELETE, g,s,p,o) ;
            return ;
        }
        delete$(g,s,p,o) ;
    }
    
    private void delete$(Quad quad)
    {
        super.delete(quad) ;
        record(QuadAction.DELETE, quad.getGraph(), quad.getSubject(), quad.getPredicate(), quad.getObject()) ;
    }
    
    private void delete$(Node g, Node s, Node p, Node o)
    {
        super.delete(g,s,p,o) ;
        record(QuadAction.DELETE,g,s,p,o) ; 
    }
    

    private static int SLICE = 1000 ;
    
    @Override
    public void deleteAny(Node g, Node s, Node p, Node o)
    {
        while (true)
        {
            Iterator<Quad> iter = find(g, s, p, o) ;
            // Materialize - stops possible ConcurrentModificationExceptions 
            List<Quad> some = take(iter, SLICE) ;
            for (Quad q : some)
                delete$(q) ;
            if (some.size() < SLICE) break ;
        }
    }
    
    @Override public void addGraph(Node gn, Graph g)
    {
        // Convert to quads.
        //super.addGraph(gn, g) ;
        ExtendedIterator<Triple> iter = g.find(Node.ANY, Node.ANY, Node.ANY) ;
        for ( ; iter.hasNext(); )
        {
            Triple t = iter.next() ;
            add(gn, t.getSubject(), t.getPredicate(), t.getObject()) ;
        }
    }
    
    @Override public void removeGraph(Node gn)
    {
        //super.removeGraph(gn) ;
        deleteAny(gn, Node.ANY, Node.ANY, Node.ANY) ;
    }
    
    private void record(QuadAction action, Node g, Node s, Node p, Node o)
    {
        monitor.change(action, g, s, p, o) ;
    }
    
    @Override
    public void sync() {
        SystemARQ.syncObject(monitor) ;
        super.sync() ;
    }
}

