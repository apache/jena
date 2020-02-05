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
import org.apache.jena.dboe.storage.simple.StoragePrefixesMem;
import org.apache.jena.graph.Node;
import org.apache.jena.shared.PrefixMapping;

/** Factory for some in-memory prefix impls */
public class PrefixesFactory
{
    public static PrefixMapI createMem() { return newPrefixMap(newPrefixMapStorageMem() ); }

    /** Create a memory-backed {@link StoragePrefixes} */
    public static StoragePrefixes newDatasetPrefixesMem()
    { return new StoragePrefixesMem(); }

    /** Create {@link PrefixMapping} using a {@link PrefixMapI}. */
    public static PrefixMapping newPrefixMappingOverPrefixMapI(PrefixMapI pmap)
    { return new PrefixMappingOverPrefixMapI(pmap); }

    /** Create {@link StoragePrefixMap} for the default graph of a {@link StoragePrefixes}. */
    public static StoragePrefixMap storagePrefixMapDft(StoragePrefixes storage)
    { return StoragePrefixesView.viewDefaultGraph(storage); }

    /** Create {@link StoragePrefixMap} for the named graph of a {@link StoragePrefixes}. */
    public static StoragePrefixMap storagePrefixMapGraph(StoragePrefixes storage, Node graphName)
    { return StoragePrefixesView.viewGraph(storage, graphName); }

    /** Create a memory-backed {@link StoragePrefixMap} */
    public static StoragePrefixMap newPrefixMapStorageMem()
    { return new PrefixMapStorageSimple(); }

    /** Create a {@link PrefixMapI} over a {@link StoragePrefixMap}. */
    public static PrefixMapI newPrefixMap(StoragePrefixMap storage)
    { return new PrefixMapIOverStorage(storage); }

    /** Return an empty immutable {@link PrefixMapI}. */
    public static PrefixMapI empty() { return emptyPrefixMap; }

    private static StoragePrefixes emptyDatasetPrefixes = new StoragePrefixesEmpty();
    private static PrefixMapI emptyPrefixMap = newPrefixMap(storagePrefixMapDft(emptyDatasetPrefixes));
}

