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

package org.apache.jena.dboe.storage.prefixes;

import org.apache.jena.dboe.storage.StoragePrefixes;
import org.apache.jena.dboe.storage.simple.StoragePrefixesSimpleMem;
import org.apache.jena.riot.system.PrefixMap;

/** Factory for DBOE impls */
public class PrefixesDboeFactory
{
    /** Create a memory-backed {@link StoragePrefixes} */
    public static StoragePrefixes newDatasetPrefixesMem()
    { return new StoragePrefixesSimpleMem(); }

//    /** Create {@link StoragePrefixMap} for the default graph of a {@link StoragePrefixes}. */
//    public static StoragePrefixMap storagePrefixMapDataset(StoragePrefixes storage)
//    { return StoragePrefixesView.viewDataset(storage); }

    // Not needed for dataset prefixes.
//    /** Create {@link StoragePrefixMap} for the named graph of a {@link StoragePrefixes}. */
//    public static StoragePrefixMap storagePrefixMapGraph(StoragePrefixes storage, Node graphName)
//    { return StoragePrefixesView.viewGraph(storage, graphName); }

//    /** Create a memory-backed {@link StoragePrefixMap} */
//    public static StoragePrefixMap newPrefixMapStorageMem()
//    { return new PrefixMapStorageSimple(); }
//
    /** Create a {@link PrefixMap} over a {@link StoragePrefixMap}. */
    public static PrefixMap newPrefixMap(StoragePrefixMap storage)
    { return new PrefixMapOverStorage(storage); }
}

