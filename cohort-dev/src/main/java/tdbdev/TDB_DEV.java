/**
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

public class TDB_DEV {
    // ComponentId management.
    
    // Names.* "obj", "-data", "bdf state file.
    
    // Quack clean / split into general and TDB
    
    // DatasetGraph.exec(op)
    //   Interface ExecuteOp + generic registration.
    // DatasetGraph.getBaseDatasetGraph
    
    // Merge:
    // public class DatasetGraphTxn extends DatasetGraphTrackActive
    // public class DatasetGraphTDB extends DatasetGraphCaching
    //    implements /*DatasetGraph,*/ Sync, Closeable
    
    // TL => ?
    
    // Switch Lizard to TDB2 technology.
    //   Hunt down adapters and remove.

    // Component id management.
    //   Fixed base id and component in set.
    //   Simplify with "per journal" ids
    //   Component id == UUID + integer
    
    // Fixed table of component offset.
    //   The return of FileRef!
    
    // Journal and recovery.
}
