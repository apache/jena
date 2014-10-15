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

import com.hp.hpl.jena.sparql.util.Timer ;
import com.hp.hpl.jena.tdb.store.tupletable.TupleIndex ;

public class BuilderSecondaryIndexesInterleaved implements BuilderSecondaryIndexes
{
    private LoadMonitor monitor ;

    public BuilderSecondaryIndexesInterleaved(LoadMonitor monitor) { this.monitor = monitor ; } 
    
    // Do as one pass over the SPO index, creating both other indexes at the same time.
    // Can be hugely costly in system resources.
    @Override
    public void createSecondaryIndexes(TupleIndex   primaryIndex ,
                                       TupleIndex[] secondaryIndexes)
    {
        Timer timer = new Timer() ;
        timer.startTimer() ;

        long time1 = timer.readTimer() ;

        LoaderNodeTupleTable.copyIndex(primaryIndex.all(), secondaryIndexes, "All", monitor) ;

        long time2 = timer.readTimer() ;
        monitor.print("Time for all indexes: %.2fs\n", (time2-time1)/1000.0) ;
    }
}
