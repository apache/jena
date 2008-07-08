/*
 * (c) Copyright 2008 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.tdb.index;

import com.hp.hpl.jena.sparql.util.ALog;
import com.hp.hpl.jena.tdb.Const;
import com.hp.hpl.jena.tdb.TDB;
import com.hp.hpl.jena.tdb.TDBException;
import com.hp.hpl.jena.tdb.base.file.Location;
import com.hp.hpl.jena.tdb.base.record.RecordFactory;

/** A policy holder for making indexes, range indexes, and data files. 
 */   

public class IndexBuilder
{
    static IndexBuilder builder = new IndexBuilder(new IndexBuilderBTree(), new IndexBuilderBTree()) ;
    public static IndexBuilder get() { return builder ; }
    
    public static IndexBuilder mem()
    { 
        //XXX BTree
        IndexFactoryBTreeMem idxFactory = new IndexFactoryBTreeMem(Const.BlockSizeMem) ;
        return new IndexBuilder(idxFactory,idxFactory) ;
    }

    // Instance
    IndexFactory factoryIndex = null ;
    IndexRangeFactory builderRangeIndex = null ;

    public IndexBuilder(IndexFactory indexBuilder, IndexRangeFactory rangeIndexBuilder)
    {
        factoryIndex = indexBuilder ;
        builderRangeIndex = rangeIndexBuilder ;
        
    }
//    static public FactoryIndex getBuilderIndex() { return factoryIndex ; }
//    static public BuilderRange getBuilderIndexRange() { return builderRangeIndex ; }
    
    // Set from context
    static { init() ; }
    static private void init()
    {
        final String defaultIndexType = "btree" ;
        
        IndexFactory factoryIndex = null ;
        IndexRangeFactory builderRangeIndex = null  ;
        
        String indexType = TDB.getContext().getAsString(TDB.symIndexType, defaultIndexType) ;

        if (indexType.equalsIgnoreCase("BPlusTree"))
        {
            ALog.warn(IndexBuilder.class,"BPlusTree turned off currently") ;
            //builder = new IndexBuilder(new IndexBuilderBPlusTree(), new IndexBuilderBPlusTree()) ;
            return ;
        }            
        
        if (indexType.equalsIgnoreCase("BTree")) 
        {
            builder = new IndexBuilder(new IndexBuilderBTree(), new IndexBuilderBTree()) ;
            return ;
        }            

        throw new TDBException("Unrecognized index type: " + indexType) ;
    }
    
    // Statics.
    
    /** Create an index at the specified place
     * @param location  Place to put the file or files needed
     * @param name      Name of index within the location
     * @return Index
     */ 
    static public Index createIndex(Location location, String name, RecordFactory recordFactory)
    {
        return builder.newIndex(location, recordFactory, name) ;
    }

    /** Create a range index at the specified place
     * @param location  Place to put the file or files needed
     * @param name      Name of index within the location
     * @return RangeIndex
     */ 
    static public RangeIndex createRangeIndex(Location location, String name, RecordFactory recordFactory)
    {
        return builder.newRangeIndex(location, recordFactory, name) ;
    }

    //---- The class
    
    public Index newIndex(Location location, RecordFactory factory, String name)
    {
        return factoryIndex.createIndex(location, name, factory) ;
    }
    
    public RangeIndex newRangeIndex(Location location, RecordFactory factory, String name)
    {
        return builderRangeIndex.createRangeIndex(location, name, factory) ;
    }
    
    // ----
    
//    private static class IndexBuilderBPlusTree implements IndexFactory, IndexRangeFactory
//    {
//
//        public Index createIndex(Location location, String name, RecordFactory recordFactory)
//        {
//            IndexFactory idxFactory = new IndexFactoryBPlusTree(Const.BlockSize) ;
//            return idxFactory.createIndex(location, name, recordFactory) ;
//        }
//
//        public RangeIndex createRangeIndex(Location location, String name, RecordFactory recordFactory)
//        {
//            IndexRangeFactory idxFactory = new IndexFactoryBPlusTree(Const.BlockSize) ;
//            return idxFactory.createRangeIndex(location, name, recordFactory) ;
//        }
//    }
    
    // ----
    
    private static class IndexBuilderBTree implements IndexFactory, IndexRangeFactory
    {
        public Index createIndex(Location location, String name, RecordFactory recordFactory)
        {
            IndexFactory idxFactory = new IndexFactoryBTree(Const.BlockSize) ;
            return idxFactory.createIndex(location, name, recordFactory) ;
        }

        public RangeIndex createRangeIndex(Location location, String name, RecordFactory recordFactory)
        {
            IndexRangeFactory idxFactory = new IndexFactoryBTree(Const.BlockSize) ;
            return idxFactory.createRangeIndex(location, name, recordFactory) ;
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