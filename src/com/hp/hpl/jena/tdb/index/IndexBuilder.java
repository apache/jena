/*
 * (c) Copyright 2008 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.tdb.index;

import com.hp.hpl.jena.tdb.Const;
import com.hp.hpl.jena.tdb.TDB;
import com.hp.hpl.jena.tdb.TDBException;
import com.hp.hpl.jena.tdb.base.file.Location;
import com.hp.hpl.jena.tdb.base.record.RecordFactory;

public class IndexBuilder
{
    // Global choices.
    static IndexBuilderIndex builderIndex = new IndexBuilderBTree() ;
    static IndexBuilderRange builderRangeIndex = new IndexBuilderBTree() ;

    // Set from context
    static { init() ; }
    static private void init()
    {
        String indexType = TDB.getContext().getAsString(TDB.symIndexType, "BPlusTree") ;

        if (indexType.equalsIgnoreCase("BPlusTree"))
        {
            builderIndex = new IndexBuilderBPlusTree() ;
            builderRangeIndex = new IndexBuilderBPlusTree() ;
            return ;
        }            
        
        if (indexType.equalsIgnoreCase("BTree")) 
        {
            builderIndex = new IndexBuilderBTree() ;
            builderRangeIndex = new IndexBuilderBTree() ;
            return ;
        }            

        throw new TDBException("Unrecognized index type: " + indexType) ;
    }
    
    /** Create an index at the specified place
     * @param location  Place to put the file or files needed
     * @param name      Name of index within the location
     * @return Index
     */ 
    static public Index createIndex(Location location, String name, RecordFactory recordFactory)
    {
        return builderIndex.createIndex(location, name, recordFactory) ;
    }

    /** Create a range index at the specified place
     * @param location  Place to put the file or files needed
     * @param name      Name of index within the location
     * @return RangeIndex
     */ 
    static public RangeIndex createRangeIndex(Location location, String name, RecordFactory recordFactory)
    {
        return builderRangeIndex.createRangeIndex(location, name, recordFactory) ;
    }

    /** Create an index in-memory (mostly for testing) */ 
    static public Index createIndexMem(RecordFactory recordFactory)
    {
        return builderIndex.createIndexMem(recordFactory) ;
    }

    /** Create a range index in-memory (mostly for testing) */ 
    static public RangeIndex createRangeIndexMem(RecordFactory recordFactory)
    {
        return builderRangeIndex.createRangeIndexMem(recordFactory) ;
    }
    
    // ---- Packaged combinations
    
    private interface IndexBuilderIndex
    {
        Index createIndex(Location location, String name, RecordFactory recordFactory) ;
        Index createIndexMem(RecordFactory recordFactory) ;
    }
    
    private interface IndexBuilderRange
    {
        RangeIndex createRangeIndex(Location location, String name, RecordFactory recordFactory) ;
        RangeIndex createRangeIndexMem(RecordFactory recordFactory) ;
    }

    // ----
    
    private static class IndexBuilderBPlusTree implements IndexBuilderIndex, IndexBuilderRange
    {

        public Index createIndex(Location location, String name, RecordFactory recordFactory)
        {
            IndexFactory idxFactory = new IndexFactoryBPlusTree(location, Const.BlockSize) ;
            return idxFactory.createIndex(recordFactory, name) ;
        }

        public RangeIndex createRangeIndex(Location location, String name, RecordFactory recordFactory)
        {
            IndexFactory idxFactory = new IndexFactoryBPlusTree(location, Const.BlockSize) ;
            return idxFactory.createRangeIndex(recordFactory, name) ;
        }

        public Index createIndexMem(RecordFactory recordFactory)
        {
            IndexFactory idxFactory = new IndexFactoryBPlusTreeMem(Const.BlockSize) ;
            return idxFactory.createIndex(recordFactory, null) ;
        }

        public RangeIndex createRangeIndexMem(RecordFactory recordFactory)
        {
            IndexFactory idxFactory = new IndexFactoryBPlusTreeMem(Const.BlockSize) ;
            return idxFactory.createRangeIndex(recordFactory, null) ;
        }
    }
    
    // ----
    
    private static class IndexBuilderBTree implements IndexBuilderIndex, IndexBuilderRange
    {
        public Index createIndex(Location location, String name, RecordFactory recordFactory)
        {
            IndexFactory idxFactory = new IndexFactoryBTree(location, Const.BlockSize) ;
            return idxFactory.createIndex(recordFactory, name) ;
        }

        public RangeIndex createRangeIndex(Location location, String name, RecordFactory recordFactory)
        {
            IndexFactory idxFactory = new IndexFactoryBTree(location, Const.BlockSize) ;
            return idxFactory.createRangeIndex(recordFactory, name) ;
        }

        public Index createIndexMem(RecordFactory recordFactory)
        {
            IndexFactory idxFactory = new IndexFactoryBTreeMem(Const.BlockSize) ;
            return idxFactory.createIndex(recordFactory, null) ;
        }

        public RangeIndex createRangeIndexMem(RecordFactory recordFactory)
        {
            IndexFactory idxFactory = new IndexFactoryBTreeMem(Const.BlockSize) ;
            return idxFactory.createRangeIndex(recordFactory, null) ;
        }
    }
}

/*
 * (c) Copyright 2008 Hewlett-Packard Development Company, LP
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