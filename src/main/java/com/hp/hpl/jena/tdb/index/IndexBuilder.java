/*
 * (c) Copyright 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
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

/*
 * (c) Copyright 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. The name of the author may not be used to endorse or promote products
 *    derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */