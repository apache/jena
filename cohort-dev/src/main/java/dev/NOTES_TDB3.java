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

package dev;

import org.apache.jena.sparql.core.DatasetPrefixStorage ;
import org.seaborne.dboe.base.file.Location ;
import org.seaborne.tdb2.store.QuadTable ;
import org.seaborne.tdb2.store.TripleTable ;

public class NOTES_TDB3 {

    // Check index find code for efficences
    // Revisit NodeLib.hash
    // StoreParamns - split into static and dynamic.
    
    // Pros and cons of hash ids.
    
    // NodeID changes. Adjustable NodeId length
    // Reaper::
    // Needs to work with the switching DatasetGraphTDB.
    //   Abstract DatasetGraphTDB as an interface!
    //     Storage unit to have indexes and node table.
    // At least oprepearet NodeId - 63 bit pointers.
    
    static class StorageTDB {
        private TripleTable tripleTable ;
        private QuadTable quadTable ;
        private DatasetPrefixStorage prefixes ;
        private Location location ;
    }
    
    // Cache (txn sensitive):
    //   Contains -> add
    //   Repeat find() ->[xeno]
    
    // StorageRDF

    // DatasetGraph.exec(op)
    //   Interface ExecuteOp + generic registration.
    // DatasetGraph.getBaseDatasetGraph
    
    // NodeId2
    //  One byte type.
    //  High byte 1 for ptr then all bytes (79bits) 
    //  2^79 = 2^15(=32k) * 2^64
    //  96 bits = 12 bytes. (4+8) 8 bytes hash + 4 byte hash -1 one bit.
    // Hash functions http://aras-p.info/blog/2016/08/09/More-Hash-Function-Tests/
    // Guava Hashing
}