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

package com.hp.hpl.jena.tdb.index.bplustree;

import static com.hp.hpl.jena.tdb.index.bplustree.BPlusTreeParams.CheckingNode ;
import static java.lang.String.format ;
import static org.apache.jena.atlas.lib.Alg.decodeIndex ;
import org.apache.jena.atlas.io.IndentedWriter ;
import org.slf4j.Logger ;
import org.slf4j.LoggerFactory ;

import com.hp.hpl.jena.tdb.base.StorageException ;
import com.hp.hpl.jena.tdb.base.block.Block ;
import com.hp.hpl.jena.tdb.base.buffer.RecordBuffer ;
import com.hp.hpl.jena.tdb.base.record.Record ;
import com.hp.hpl.jena.tdb.base.recordbuffer.RecordBufferPage ;

/** B+Tree wrapper over a block of records in a RecordBufferPage.
 * This class adds no persistent state to a RecordBufferPage */
public final class BPTreeRecords extends BPTreePage
{
    private static Logger log = LoggerFactory.getLogger(BPTreeRecords.class) ;
    private final RecordBufferPage  rBuffPage ;
    private RecordBuffer            rBuff ;         // Used heavily.
    
    BPTreeRecords(BPlusTree bpTree, RecordBufferPage rbp)
    {
        super(bpTree) ;
        rBuffPage = rbp ;
        rBuff = rBuffPage.getRecordBuffer() ;
    }
    
    RecordBufferPage getRecordBufferPage()
    { return rBuffPage ; }
    
    /*TEMP*/ public RecordBuffer getRecordBuffer()
    { return rBuff ; }

    @Override
    public final Block getBackingBlock()
    {
        return rBuffPage.getBackingBlock() ;
    }
    
    @Override
    public void reset(Block block)
    { 
        rBuffPage.reset(block) ;
        rBuff = rBuffPage.getRecordBuffer() ;
    }

    int getLink()
    { return rBuffPage.getLink() ; }

    @Override
    public boolean isFull()
    {
        return ( rBuff.size() >= rBuff.maxSize() ) ;
    }
    
    @Override
    public boolean hasAnyKeys()
    {
        return rBuff.size() > 0 ;
    }

    @Override
    public boolean isMinSize()
    {
        // 50% packing minimum.
        // If of max length 5 (i.e. odd), min size is 2.  Integer division works.  
        return ( rBuff.size() <= rBuff.maxSize()/2 ) ;
   }

    @Override
    public Record internalSearch(Record rec)
    {
        int i = rBuff.find(rec) ;
        if ( i < 0 )
            return null ;
        return rBuff.get(i) ;
    }

//    @Override
//    public BPTreeRecords findPage(Record record)
//    {
//        if ( rBuff.size() == 0 )
//            return this ;
//        
//        // Not true if above the last record.
//        if ( this.getLink() != RecordBufferPage.NO_ID && Record.keyGT(record, maxRecord()) ) 
//            error("Record [%s] not in this page: %s", record , this) ;
//        return this ;
//    }
//    
//    @Override
//    public BPTreeRecords findFirstPage() { return this ; }

    @Override final
    public void write()     { bpTree.getRecordsMgr().write(this) ; } 
    
    @Override final
    public void promote()   { bpTree.getRecordsMgr().promote(this) ; } 
    
    @Override final
    public void release()   { bpTree.getRecordsMgr().release(this) ; }
    
    @Override final
    public void free()      { bpTree.getRecordsMgr().free(this) ; }

    @Override
    public Record internalInsert(Record record)
    {
        // [TxTDB:PATCH-UP]
        promote() ;
        int i = rBuff.find(record) ;
        Record r2 = null ;
        if ( i < 0 )
        {
            i = decodeIndex(i) ;
            if ( rBuff.size() >= rBuff.maxSize())  
                throw new StorageException("RecordBlock.put overflow") ; 
            rBuff.add(i, record) ;
        }
        else
        {
            r2 = rBuff.get(i) ;
            if ( Record.compareByKeyValue(record, r2) != 0 )
                // Replace : return old
                rBuff.set(i, record) ;
        }
        write() ;
        return r2 ;
    }

    @Override
    public Record internalDelete(Record record)
    {
        promote() ;
        int i = rBuff.find(record) ;
        if ( i < 0 )
            return null ;
        Record r2 = rBuff.get(i) ;
        rBuff.remove(i) ;
        write() ;
        return r2 ;       
    }
    
    @Override final
    public Record getSplitKey()
    {
        int splitIdx = rBuff.size()/2-1 ;
        Record r = rBuff.get(splitIdx) ;
        return r ;
    }

    /** Split: place old high half in 'other'. Return the new (upper) BPTreeRecords(BPTreePage).
     * Split is the high end of the low page.
     */
    @Override final
    public BPTreePage split() 
    {
        // LinkIn
        // Create a new BPTreeRecords.
        
        BPTreeRecords other = create(rBuffPage.getLink()) ;
        rBuffPage.setLink(other.getId()) ;
        
        int splitIdx = rBuff.size()/2-1 ;
        Record r = rBuff.get(splitIdx) ;                // Only need key for checking later.
        
        int moveLen =  rBuff.size()-(splitIdx+1) ;      // Number to move.
        // Copy high end to new.  
        rBuff.copy(splitIdx+1, other.getRecordBufferPage().getRecordBuffer(), 0, moveLen) ;
        rBuff.clear(splitIdx+1, moveLen) ;
        rBuff.setSize(splitIdx+1) ;
        
        if ( CheckingNode )
        {
            if ( ! Record.keyEQ(r, maxRecord()) )
            {
                System.err.println(rBuff) ;
                System.err.println(other.rBuff) ;
                error("BPTreeRecords.split: Not returning expected record") ;
            }
        }
        return other ;
    }

    private BPTreeRecords create(int linkId)
    {
        BPTreeRecords newPage = bpTree.getRecordsMgr().create() ;
        newPage.getRecordBufferPage().setLink(linkId) ;
        return newPage ;
    }

    @Override
    public Record shiftRight(BPTreePage other, Record splitKey)
    {
        // Error checking by RecordBuffer
        BPTreeRecords page = cast(other) ;
        rBuff.shiftRight(page.rBuff) ;
        if ( rBuff.size() == 0 )
            return null ;
        return rBuff.getHigh() ;
    }
    
    @Override
    public Record shiftLeft(BPTreePage other, Record splitKey)
    {
        // Error checking by RecordBuffer
        BPTreeRecords page = cast(other) ;
        rBuff.shiftLeft(page.rBuff) ;
        if ( rBuff.size() == 0 )
            return null ;
        return rBuff.getHigh() ;
    }

    @Override
    public BPTreePage merge(BPTreePage right, Record splitKey)
    {
        // Split key ignored - it's for the B+Tree case of pushing down a key
        // Records blocks have all the key/values in them anyway.
        return merge(this, cast(right)) ;
    }

    private static BPTreeRecords merge(BPTreeRecords left, BPTreeRecords right)
    {
        // Copy right to top of left.
        // The other way round needs a shift as well.
        right.rBuff.copyToTop(left.rBuff) ;
        // Same as: right.rBuff.copy(0, left.rBuff, left.rBuff.size(), right.rBuff.size()) ;
        right.rBuff.clear() ;
        
        //The right page is released by the caller.  left is still in use.
        // So the test code can poke around in the right block after merge. 
        //left.bpTree.getRecordsMgr().release(left.getId()) ;
        
        // Fix up the link chain.
        left.rBuffPage.setLink(right.rBuffPage.getLink()) ;
        return left ;
    }
    
    private static BPTreeRecords cast(BPTreePage page)
    {
        try { return (BPTreeRecords)page  ; }
        catch (ClassCastException ex) { error("Wrong type: "+page) ; return null ; }
    }
    
    @Override final
    public Record minRecord()
    {
        return getLowRecord() ;
    }

    @Override final
    public Record maxRecord()
    {
        return getHighRecord() ;
    }

    private static void error(String msg, Object... args)
    {
        msg = format(msg, args) ;
        System.out.println(msg) ;
        System.out.flush();
        throw new BPTreeException(msg) ;
    }

    @Override
    public final Record getLowRecord()
    {
        if ( rBuff.size() == 0 )
            return null ;
        return rBuff.getLow() ;
    }

    @Override
    public final Record getHighRecord()
    {
        if ( rBuff.size() == 0 )
            return null ;
        return rBuff.getHigh() ;
    }

    @Override
    public final int getMaxSize()             { return rBuff.maxSize() ; }
    
    @Override
    public final int getCount()             { return rBuff.size() ; }
 
    @Override
    public final void setCount(int count)   { rBuff.setSize(count) ; }
    
    @Override
    public String toString()
    { return String.format("BPTreeRecords[id=%d, link=%d]: %s", getId(), getLink(), rBuff.toString()); }
    
    @Override
    public final void checkNode()
    {
        if ( ! CheckingNode ) return ;
        if ( rBuff.size() < 0 || rBuff.size() > rBuff.maxSize() )
            error("Misized: %s", this) ;

        for ( int i = 1 ; i < getCount() ; i++ )
        {
            Record r1 = rBuff.get(i-1) ;
            Record r2 = rBuff.get(i) ;
            if ( Record.keyGT(r1, r2) )
                error("Not sorted: %s", this) ;
        }
    }
    
    @Override
    public final void checkNodeDeep()
    { checkNode() ; }

//    @Override
//    public ByteBuffer getBackingByteBuffer()   { return rBuffPage.getBackingByteBuffer() ; }

    @Override
    public int getId()                  { return rBuffPage.getId() ; } 

//    @Override
//    public void setId(int id)           { rBuffPage.setId(id) ; }

    @Override
    public void output(IndentedWriter out)
    {
        out.print(toString()) ;
    }
}
