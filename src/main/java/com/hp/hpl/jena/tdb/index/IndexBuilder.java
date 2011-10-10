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

package com.hp.hpl.jena.tdb.index;

import com.hp.hpl.jena.tdb.TDBException ;
import com.hp.hpl.jena.tdb.base.file.FileSet ;
import com.hp.hpl.jena.tdb.base.file.Location ;
import com.hp.hpl.jena.tdb.base.record.RecordFactory ;
import com.hp.hpl.jena.tdb.index.factories.IndexFactoryBPlusTree ;
import com.hp.hpl.jena.tdb.index.factories.IndexFactoryExtHash ;
import com.hp.hpl.jena.tdb.sys.SystemTDB ;

/** A policy holder for making indexes and range indexes.  */
 
public class IndexBuilder
{
    // Migrate to be a general policy place for files.
    
    private static IndexBuilder builder         = chooseIndexBuilder() ;
    public static IndexBuilder get()            { return builder ; }
    
    public static IndexBuilder getBPlusTree()   { return createIndexBuilder(IndexType.BPlusTree) ; }
    public static IndexBuilder getExtHash()     { return createIndexBuilder(IndexType.ExtHash) ; }
    
    private static IndexBuilder builderMem = null ;
    
    public static IndexBuilder mem()
    { 
        return createIndexBuilderMem(SystemTDB.getIndexType()) ;
    }

    /** Convert the index name to a file name */
    public static String filenameForIndex(String indexName) { return indexName ; }

    /** Convert the index name to a file name */
    public static FileSet filesetForIndex(Location location, String indexName) { return new FileSet(location, filenameForIndex(indexName)) ; }
    
    private static synchronized IndexBuilder chooseIndexBuilder()
    {
        return createIndexBuilder(SystemTDB.getIndexType()) ;
    }
    
    private static IndexBuilder createIndexBuilderMem(IndexType indexType)
    {
        return createIndexBuilder(indexType) ;
    }
    
    private static IndexBuilder createIndexBuilder(IndexType indexType)
    {
        switch (indexType)
        {
            case BPlusTree:
            {
                IndexFactoryBPlusTree idx = new IndexFactoryBPlusTree() ;
                return new IndexBuilder(idx, idx) ;
            }
            case ExtHash:
            {
                IndexFactoryExtHash idxFactory = new IndexFactoryExtHash() ;
                IndexFactoryBPlusTree idx = new IndexFactoryBPlusTree() ;
                return new IndexBuilder(idxFactory, idx) ;
            }
        }
        throw new TDBException("Unrecognized index type: " + indexType) ;
    }

    /** Create an index at the specified place
     * @param fileset   Place and basename where the file or files needed are found/created.
     * @return Index
     */ 
    static public Index createIndex(FileSet fileset, RecordFactory recordFactory)
    {
        return builder.newIndex(fileset, recordFactory) ;
    }

    /** Create a range index at the specified place
     * @param fileset   Place and basename where the file or files needed are found/created.
     * @return RangeIndex
     */ 
    static public RangeIndex createRangeIndex(FileSet fileset, RecordFactory recordFactory)
    {
        return builder.newRangeIndex(fileset, recordFactory) ;
    }

    // ---- The class .... a pairing of an index builder and a range index builder.
    IndexFactory factoryIndex = null ;
    IndexRangeFactory builderRangeIndex = null ;

    public IndexBuilder(IndexFactory indexBuilder, IndexRangeFactory rangeIndexBuilder)
    {
        factoryIndex = indexBuilder ;
        builderRangeIndex = rangeIndexBuilder ;
    }
    
    public Index newIndex(FileSet fileset, RecordFactory factory)
    {
        return factoryIndex.createIndex(fileset, factory) ;
    }
    
    public RangeIndex newRangeIndex(FileSet fileset , RecordFactory factory)
    {
        return builderRangeIndex.createRangeIndex(fileset, factory) ;
    }
}
