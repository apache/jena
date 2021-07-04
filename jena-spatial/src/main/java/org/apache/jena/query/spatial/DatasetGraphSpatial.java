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

package org.apache.jena.query.spatial;

import org.apache.jena.graph.Graph ;
import org.apache.jena.graph.Node ;
import org.apache.jena.query.ReadWrite ;
import org.apache.jena.sparql.core.* ;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DatasetGraphSpatial extends DatasetGraphMonitor implements Transactional 
{
    private static Logger log = LoggerFactory.getLogger(DatasetGraphSpatial.class) ;
    private final SpatialIndex spatialIndex ;

    public DatasetGraphSpatial(DatasetGraph dsg, SpatialIndex index, SpatialDocProducer producer)
    {
        super(dsg, producer) ;
        this.spatialIndex = index ;
    }

//    public DatasetGraph getBase() { return getWrapped() ; }
    
    // ---- Intecept these and force the use of views.
    @Override
    public Graph getDefaultGraph()
    { return GraphView.createDefaultGraph(this) ; }

    @Override
    public Graph getGraph(Node graphNode)
    { return GraphView.createNamedGraph(this, graphNode) ; }
    // ----    
    
    public SpatialIndex getSpatialIndex()
    {
        return spatialIndex;
    }

    // Imperfect.
    private boolean needFinish = false ;
    
    @Override
    public void begin(ReadWrite readWrite)
    {
        super.begin(readWrite) ;
        //textIndex.begin(readWrite) ;
        if ( readWrite == ReadWrite.WRITE )
        {
            // WRONG design
            super.getMonitor().start() ;
            // Right design.
            //textIndex.startIndexing() ;
            needFinish = true ;
        }
    }

    @Override
    public void commit()
    {
        try {
            if ( needFinish )
            {
                super.getMonitor().finish() ;
                //spatialIndex.finishIndexing() ;
            }
            needFinish = false ;
            //spatialIndex.commit() ;
            super.commit() ;
        } catch (Throwable ex) { 
            log.warn("Exception in commit: "+ex.getMessage(), ex) ;
            super.abort() ; 
        }
    }

    @Override
    public void abort()
    {
        try {
            if ( needFinish )
                spatialIndex.abortIndexing() ;
            //spatialIndex.abort() ;
            super.abort() ;
        } catch (Throwable ex) { log.warn("Exception in abort: "+ex.getMessage(), ex) ; }
    }

    @Override
    public boolean isInTransaction()
    {
        return super.isInTransaction() ;
    }

    @Override
    public void end()
    {
        try {
            //spatialIndex.end() ;
            super.end() ;
        } catch (Throwable ex) { log.warn("Exception in end: "+ex.getMessage(), ex) ; }
    }
    
    @Override
    public boolean supportsTransactions() {
        return super.supportsTransactions() ;
    }
    
    /** Declare whether {@link #abort} is supported.
     *  This goes along with clearing up after exceptions inside application transaction code.
     */
    @Override
    public boolean supportsTransactionAbort() {
        return super.supportsTransactionAbort() ;
    }
}

