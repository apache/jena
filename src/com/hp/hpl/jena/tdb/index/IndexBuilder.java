/*
 * (c) Copyright 2008 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.tdb.index;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.tdb.Const;
import com.hp.hpl.jena.tdb.TDB;
import com.hp.hpl.jena.tdb.TDBException;
import com.hp.hpl.jena.tdb.base.file.Location;
import com.hp.hpl.jena.tdb.base.record.RecordFactory;

/** A policy holder for making indexes, range indexes, and data files. 
 */   

public class IndexBuilder
{
    private static Logger log = LoggerFactory.getLogger(IndexBuilder.class) ;
    
    // Migrate to be a general policy place for files.
    final static String indexTypeBTree          = "BTree" ;
    final static String indexTypeBPlusTree      = "BPlusTree" ;
    final static String defaultIndexType        = indexTypeBTree ; // CHANGE ME!
    
    private static IndexBuilder builder         = chooseIndexBuilder() ;
    public static IndexBuilder get()            { return builder ; }
    
    public static IndexBuilder getBTree()       { return createIndexBuilder(IndexType.BTree) ; }
    public static IndexBuilder getBPlusTree()   { return createIndexBuilder(IndexType.BPlusTree) ; }
    
    private static IndexBuilder builderMem = null ;
    
    public static IndexBuilder mem()
    { 
        IndexType indexType = IndexType.get() ;
        return createIndexBuilderMem(indexType) ;
    }

    private static synchronized IndexBuilder chooseIndexBuilder()
    {
        IndexType indexType = IndexType.get() ;
        return createIndexBuilder(indexType) ;
    }
    
    private static IndexBuilder createIndexBuilderMem(IndexType indexType)
    {
        switch (indexType)
        {
            case BTree:
            {
                IndexFactoryBTreeMem idxFactory = new IndexFactoryBTreeMem(Const.BlockSizeMem) ;
                return new IndexBuilder(idxFactory,idxFactory) ;
            }
            case BPlusTree:
            {
                IndexFactoryBPlusTreeMem idxFactory = new IndexFactoryBPlusTreeMem(Const.BlockSizeMem) ;
                return new IndexBuilder(idxFactory,idxFactory) ;
            }
        }
        throw new TDBException("Memory index builder: Unrecognized index type: " + indexType) ;
    }
    
    private static IndexBuilder createIndexBuilder(IndexType indexType)
    {
        switch (indexType)
        {
            case BTree:
            {
                IndexFactoryBTree idx = new IndexFactoryBTree(Const.BlockSize) ;
                return new IndexBuilder(idx, idx) ;
            }
            case BPlusTree:
            {
                IndexFactoryBPlusTree idx = new IndexFactoryBPlusTree(Const.BlockSize) ;
                return new IndexBuilder(idx, idx) ;
            }
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

    // ---- The class ....
    IndexFactory factoryIndex = null ;
    IndexRangeFactory builderRangeIndex = null ;

    public IndexBuilder(IndexFactory indexBuilder, IndexRangeFactory rangeIndexBuilder)
    {
        factoryIndex = indexBuilder ;
        builderRangeIndex = rangeIndexBuilder ;
        
    }
    
    public Index newIndex(Location location, RecordFactory factory, String name)
    {
        return factoryIndex.createIndex(location, name, factory) ;
    }
    
    public RangeIndex newRangeIndex(Location location, RecordFactory factory, String name)
    {
        return builderRangeIndex.createRangeIndex(location, name, factory) ;
    }
    
    enum IndexType
    {
        BTree 
        { @Override public String getName() { return "BTree" ; } } ,
        BPlusTree
        { @Override public String getName() { return "BPlusTree" ; } } ,
        ;
        
        abstract public String getName() ;

        static IndexType get()
        {
            boolean defaultSetting = false ;
            String x = TDB.getContext().getAsString(TDB.symIndexType) ;
            if ( x == null )
            {
                defaultSetting = true ;
                x = defaultIndexType ;
            }
            IndexType iType = get(x) ;
            if ( !defaultSetting )
                log.info("Index type: "+iType) ;
            
            return iType ;
        }
        
        static IndexType get(String name)
        {
            if ( name.equalsIgnoreCase(indexTypeBTree) ) return BTree ;
            if ( name.equalsIgnoreCase(indexTypeBPlusTree) ) return BPlusTree ;
            return null ;
        }
        @Override public String toString() { return getName() ; }
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