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
    // ** Append-write-only, transactional byte file c.f. Object file.
    
    // TDBLib
    //   Better name.
    //   Enable AdpaterTransactionalDBOEToJena
    
    // Union graph not working?
    
    // Test
    // Centralize making a in-memory, non-transactional, datasetTDB for unit tests.
    // TestFactoryTDB.createRawDatasetGraphTDB() ;
    
    // Ensure sync called.
    // BinaryDataFile
    //   .isEmpty
    
    // Switch Lizard to TDB2 technology.
    // Hunt down adapter and remove.

    // Sort out transactional Object
    // Transactional wiring.
    // Component id management.
    //   Read from disk.
    //   Fixed base id and component in set.
    //   Simplify with "per journal" ids
    //   Component id == UUID + integer
    
    // Fixed table of component offset.
    //   The return of FileRef!
    
    // Tests
    
    // Journal and recovery.
    
    // Collapse DatasetGraphTDB and DatasetGraphTxn
    
    // Port TDB tests
    //   BuildTestLib
    //   TestTransactions
    //    Just need TestDataset?
}

