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

package com.hp.hpl.jena.tdb.store.bulkloader2;

import org.apache.jena.atlas.lib.ColumnMap ;

import com.hp.hpl.jena.tdb.base.file.FileSet ;
import com.hp.hpl.jena.tdb.base.file.Location ;
import com.hp.hpl.jena.tdb.index.RangeIndex ;
import com.hp.hpl.jena.tdb.index.TupleIndex ;
import com.hp.hpl.jena.tdb.index.TupleIndexRecord ;
import com.hp.hpl.jena.tdb.sys.SetupTDB ;

public class IndexFactory
{

    public static TupleIndex openTupleIndex(Location location, String indexName, String primary, String indexOrder, int readCacheSize, int writeCacheSize, int dftKeyLength, int dftValueLength)
    {
        // This should work but requires properties.
        //return SetupTDB.makeTupleIndex(location, properties, indexName, primary, indexOrder, dftKeyLength)
        // Migrate to TDB proper.
        // Value part is null (zero length)
        RangeIndex rIndex = openBPT(location, indexName, readCacheSize, writeCacheSize, dftKeyLength, dftValueLength) ;
        TupleIndex tupleIndex = new TupleIndexRecord(primary.length(), new ColumnMap(primary, indexOrder), indexOrder, rIndex.getRecordFactory(), rIndex) ;
        return tupleIndex ;
    }

    public static RangeIndex openBPT(Location location, String indexName, int readCacheSize, int writeCacheSize, int dftKeyLength, int dftValueLength)
    {
        FileSet fileset = new FileSet(location, indexName) ;
        return SetupTDB.makeBPlusTree(fileset, readCacheSize, writeCacheSize, dftKeyLength, dftValueLength) ;
    }

}
