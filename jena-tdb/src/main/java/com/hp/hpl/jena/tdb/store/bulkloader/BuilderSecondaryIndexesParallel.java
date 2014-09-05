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

package com.hp.hpl.jena.tdb.store.bulkloader;

import java.util.concurrent.Semaphore ;

import com.hp.hpl.jena.sparql.util.Timer ;
import com.hp.hpl.jena.tdb.store.tupletable.TupleIndex ;

public class BuilderSecondaryIndexesParallel implements BuilderSecondaryIndexes
{
    private LoadMonitor monitor ;

    public BuilderSecondaryIndexesParallel(LoadMonitor monitor) { this.monitor = monitor ; } 
    
    @Override
    public void createSecondaryIndexes(TupleIndex   primaryIndex ,
                                       TupleIndex[] secondaryIndexes)
    {
        monitor.print("** Parallel index building") ;
        Timer timer = new Timer() ;
        timer.startTimer() ;

        int semaCount = 0 ;
        Semaphore sema = new Semaphore(0) ;

        for ( TupleIndex index : secondaryIndexes )
        {
            if ( index != null )
            {
                Runnable builder = setup(sema, primaryIndex, index, index.getMapping()) ;
                new Thread(builder).start() ;
                semaCount++ ;
            }
        }

        try {  sema.acquire(semaCount) ; } catch (InterruptedException ex) { ex.printStackTrace(); }

        long time = timer.readTimer() ;
        timer.endTimer() ;
        monitor.print("Time for parallel indexing: %.2fs\n", time/1000.0) ;
    }

    private Runnable setup(final Semaphore sema, final TupleIndex srcIndex, final TupleIndex destIndex, final String label)
    {
        Runnable builder = new Runnable(){
            @Override
            public void run()
            {
                LoaderNodeTupleTable.copyIndex(srcIndex.all(), new TupleIndex[]{destIndex}, label, monitor) ;
                sema.release() ;
            }} ;

            return builder ;
    }
}
