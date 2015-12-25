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

package org.seaborne.tdb2.loader;

import org.apache.jena.atlas.logging.ProgressLogger ;
import org.apache.jena.graph.Triple ;
import org.apache.jena.riot.system.StreamRDF ;
import org.apache.jena.riot.system.StreamRDFWrapper ;
import org.apache.jena.sparql.core.DatasetChanges ;
import org.apache.jena.sparql.core.DatasetGraph ;
import org.apache.jena.sparql.core.DatasetGraphMonitor ;
import org.apache.jena.sparql.core.Quad ;

/**
 * Attach a {@link ProgressLogger} to an {@link StreamRDF}. The
 * {@link ProgressLogger} is called for each triple and quad that goes through
 * the stream. {@link #startMonitor} and {@link #finishMonitor} signal the
 * beginning and end of stream operations, and allow for compound
 * {@link StreamRDF} {@link #start} and {@link #finish} calls.
 * <p>
 * {@link DatasetChanges} is a related interface for notification of changes to
 * a DatasetGraph. {@link DatasetGraphMonitor} adds that functionality to a
 * {@link DatasetGraph}.
 * 
 * @see DatasetChanges
 * @see DatasetGraphMonitor
 */

public class StreamRDFMonitor extends StreamRDFWrapper {
    //DatasetChanges - version for add only? 
    
    private final ProgressLogger monitor ;
    
    public StreamRDFMonitor(StreamRDF other, ProgressLogger monitor) {
        super(other) ;
        this.monitor = monitor ;
    }

    public void startMonitor() {
        //monitor.startMessage();
        monitor.start();
    }
    
    public void finishMonitor() {
        monitor.finish();
        monitor.finishMessage();
    }

    @Override
    public void triple(Triple triple) {
        tick() ;
        super.triple(triple) ;
    }

    @Override
    public void quad(Quad quad) {
        tick() ;
        super.quad(quad);
    }

    private void tick() { monitor.tick() ; }
}
