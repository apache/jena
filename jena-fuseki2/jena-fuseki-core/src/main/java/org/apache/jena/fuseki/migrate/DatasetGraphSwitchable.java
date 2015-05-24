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

package org.apache.jena.fuseki.migrate;

import java.util.concurrent.atomic.AtomicReference ;

import org.apache.jena.fuseki.FusekiException ;
import org.apache.jena.sparql.core.DatasetGraph ;
import org.apache.jena.sparql.core.DatasetGraphWrapper ;

public class DatasetGraphSwitchable extends DatasetGraphWrapper {
    // **** Associated query engine factory - QueryEngineFactoryWrapper
    // which executes on the unwrapped DSG.
    
    // *** Modify DatasetGraphWrapper to use a get().
    
    // Time to have DatasetGraph.getQueryDataset
    private final DatasetGraph dsg1 ;
    private final DatasetGraph dsg2 ;
    private final AtomicReference<DatasetGraph> current = new AtomicReference<DatasetGraph>() ;
    
    // Change DatasetGraphWrapper to use protected get() 

    public DatasetGraphSwitchable(DatasetGraph dsg1, DatasetGraph dsg2) {
        super(null) ;
        if ( dsg1 == null )
            // Personally I think IllegalArgumentException is more
            // appropriate, with NPE for unexpected use of null 
            // but convention says .... 
            throw new NullPointerException("First argument is null") ;
        if ( dsg2 == null )
            throw new NullPointerException("Second argument is null") ;
        this.dsg1 = dsg1 ;
        this.dsg2 = dsg2 ;
        set(dsg1) ;
    }

    private void set(DatasetGraph dsg) { current.set(dsg) ; }
    
    /** Change to using the other dataset */ 
    public void flip() {
        // Don't worry about concurrent calls to flip()
        // The outcome will be that one call wins (the actual second caller)
        // and not corrupted data. Noet that get() is only called once per
        // redirection. 
        
        // if dsg1 -- (expected, update)
        if ( current.compareAndSet(dsg1, dsg2) )
            return ;
        // if dsg2 
        if ( current.compareAndSet(dsg2, dsg1) )
            return ;
        throw new FusekiException() ;
    }
    
    /** Current dataset of the switchable pair */
    public final DatasetGraph getCurrent()  { return get() ; }
    
    /** Return dataset1 of the switchable pair */
    public final DatasetGraph getDataset1() { return dsg1 ; }
    
    /** Return dataset2 of the switchable pair */
    public final DatasetGraph getDataset2() { return dsg2 ; }
    
    /** Use dataset1 */
    public final void useDataset1()         { set(dsg1) ; }

    /** Use dataset2 */
    public final void useDataset2()         { set(dsg2) ; }
}

