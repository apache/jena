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

import org.apache.jena.tdb2.loader.base.LoaderOps;
import org.apache.jena.system.progress.MonitorOutput;
import org.apache.jena.system.progress.ProgressMonitor;
import org.apache.jena.system.progress.ProgressMonitorOutput;
import org.apache.jena.tdb2.store.tupletable.TupleIndex;

/**
 * This interface is the mechanism for building indexes given that at least one index
 * already exists (the "primary", which normally is SPO or GSPO).
 */
public class BuilderSecondaryIndexes
{
    public static void createSecondaryIndexes(MonitorOutput output, TupleIndex primaryIndex, TupleIndex[] secondaryIndexes) {
        boolean printTiming = true;
        for ( TupleIndex index : secondaryIndexes ) {
            String msg = primaryIndex.getName()+"->"+index.getName();
            if ( index != null ) {
                ProgressMonitor monitor = ProgressMonitorOutput.create(output, msg,
                                                                       LoaderSequential.IndexTickPoint,
                                                                       LoaderSequential.IndexSuperTick);
                monitor.startMessage(msg);
                monitor.start();

                LoaderOps.copyIndex(primaryIndex.all(), new TupleIndex[]{index}, monitor);

                monitor.finish();
                monitor.finishMessage(index.getName()+" indexing: ");
            }
        }
    }
}
