/*
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *  See the NOTICE file distributed with this work for additional
 *  information regarding copyright ownership.
 */

package org.seaborne.tdb2.repack;

import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.jena.sparql.core.DatasetGraph ;
import org.apache.jena.sparql.core.DatasetGraphWrapper ;

final // Encourage the JIT!
public class DatasetGraphSwitchable extends DatasetGraphWrapper
{
    private final AtomicReference<DatasetGraph> dsgx = new AtomicReference<>();
    // Null for in-memory datasets.
    private final Path basePath; 
    
    public DatasetGraphSwitchable(Path base, DatasetGraph dsg) {
        // Don't use the slot in datasetGraphWrapper - use the AtomicReference
        super(null) ;
        dsgx.set(dsg);
        this.basePath = base;  
    }

    public Path getContainerPath() { return basePath; }
    
    /** The dataset to use for redirection - can be overridden.
     *  It is also guaranteed that this is called only once per
     *  delegated call.  Changes to the wrapped object can be
     *  made based on that contract.
     */
    @Override
    public DatasetGraph get() { return dsgx.get(); }

    /** Set the base {@link DatasetGraph}.
     * Returns the old value.
     */ 
    public DatasetGraph set(DatasetGraph dsg) { 
        return dsgx.getAndSet(dsg);
    }
}

