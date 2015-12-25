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

package tdbdev;

import org.apache.jena.riot.system.StreamRDF ;
import org.apache.jena.riot.system.StreamRDFWrapper ;

/** Merge a number of start-finish calls into one.
 * This is done by passing on start and finiish only when the call depth is zero.
 * When this class is used, call {@link #start} to start a sequence 
 * and {@link #finish} to end it.  Then, provided the
 * <p>
 * Example of usage : loading a number of files and havig a single, 
 * overall progress counter. 
 * <pre>
 *    StreamRDF dest = ...
 *    ProgressMonitor progress = ...
 *    StreamRDF monitor = new StreamRDFMonitor(dest, progress) ;
 *    StreamRDF merged = new StreamRDFMerge(monitor) ;
 *    merged.start() ;
 *    RDFDataMgr.parse(merged, datafile1) ;
 *    RDFDataMgr.parse(merged, datafile2) ;
 *    RDFDataMgr.parse(merged, datafile3) ;
 *    merged.finish() ;
 * </pre>    
 */
public class StreamRDFMerge extends StreamRDFWrapper {
    private int depth = 0 ; 
    
    public StreamRDFMerge(StreamRDF other) {
        super(other) ;
    }
    
    @Override
    public void start() {
        if ( depth == 0 )
            super.start() ;
        depth++ ;
    }

    @Override
    public void finish() { 
        depth-- ;
        if ( depth == 0 )
            super.finish() ;
    }
}
