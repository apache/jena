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

package org.apache.jena.tdb2.loader.sequential;

import org.apache.jena.atlas.lib.Timer ;
import org.apache.jena.tdb2.loader.base.LoaderOps;
import org.apache.jena.tdb2.loader.base.MonitorOutput;
import org.apache.jena.tdb2.loader.base.ProgressMonitor;
import org.apache.jena.tdb2.loader.base.ProgressMonitorOutput;
import org.apache.jena.tdb2.store.tupletable.TupleIndex ;

public class BuilderSecondaryIndexesSequential implements BuilderSecondaryIndexes
{
    public BuilderSecondaryIndexesSequential() {} 
    
    // Create each secondary indexes, doing one at a time.
    @Override
    public void createSecondaryIndexes(MonitorOutput output, TupleIndex primaryIndex, TupleIndex[] secondaryIndexes)
    {
        Timer timer = new Timer() ;
        timer.startTimer() ;
        boolean printTiming = true;
        for ( TupleIndex index : secondaryIndexes ) {
            if ( index != null ) {
                ProgressMonitor monitor = ProgressMonitorOutput.create(output, index.getName(), 
                                                                   LoaderSequential.IndexTickPoint,
                                                                   LoaderSequential.IndexSuperTick);
                monitor.startMessage();
                monitor.start();

                long time1 = timer.readTimer() ;
                LoaderOps.copyIndex(primaryIndex.all(), new TupleIndex[]{index}, monitor) ;
                long time2 = timer.readTimer() ;
                monitor.finish();
                monitor.finishMessage(index.getName()+" indexing: ");
//                if ( printTiming )
//                    output.print("Time for %s indexing: %.2fs", index.getName(), (time2-time1)/1000.0) ;
            }  
        }   
    }
}
