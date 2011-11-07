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

package com.hp.hpl.jena.tdb.transaction;

import java.util.HashMap ;
import java.util.HashSet ;
import java.util.Iterator ;
import java.util.Map ;
import java.util.Set ;

import org.openjena.atlas.logging.Log ;
import org.slf4j.Logger ;
import org.slf4j.LoggerFactory ;

import com.hp.hpl.jena.tdb.base.block.Block ;
import com.hp.hpl.jena.tdb.base.block.BlockException ;
import com.hp.hpl.jena.tdb.base.block.BlockMgr ;
import com.hp.hpl.jena.tdb.sys.FileRef ;

public class BlockMgrJournal implements BlockMgr, TransactionLifecycle
{
    private static Logger log = LoggerFactory.getLogger(BlockMgrJournal.class) ;
    private BlockMgr blockMgr ; // read-only except during journal checkpoint.
    private Transaction transaction ;
    private FileRef fileRef ;
    
    final private Set<Long> readBlocks = new HashSet<Long>() ;
    final private Set<Long> iteratorBlocks = new HashSet<Long>() ;
    final private Map<Long, Block> writeBlocks = new HashMap<Long, Block>() ;
    final private Map<Long, Block> freedBlocks = new HashMap<Long, Block>() ;
    private boolean closed = false ;
    
    public BlockMgrJournal(Transaction txn, FileRef fileRef, BlockMgr underlyingBlockMgr)
    {
        reset(txn, fileRef, underlyingBlockMgr) ;
    }

    @Override
    public void begin(Transaction txn)
    {
        reset(txn, fileRef, blockMgr) ;
    }
    
    @Override
    public void commitPrepare(Transaction txn)
    {
        for ( Block blk : writeBlocks.values() )
            writeJournalEntry(blk) ;
    }

    @Override
    public void commitEnact(Transaction txn)
    {
        // No-op : this is done by playing the master journal.
    }

    @Override
    public void abort(Transaction txn)
    {
        // Do clearup of in-memory structures in clearup().
    }
    
    @Override
    public void commitClearup(Transaction txn)
    {
        // Persistent state is in the system journal.
        reset(txn) ;
    }
    
//    public Iterator<Block> updatedBlocks()  { return writeBlocks.values().iterator() ; }
//    public Iterator<Block> freedBlocks()    { return freedBlocks.values().iterator() ; }

    /** Set, or reset, this BlockMgr.
     *  Enables it to be reused when already part of a datastructure. 
     */
    private void reset(Transaction txn, FileRef fileRef, BlockMgr underlyingBlockMgr)
    {
        this.fileRef = fileRef ;
        this.blockMgr = underlyingBlockMgr ;
        reset(txn) ;
    }
    
    private void reset(Transaction txn)
    {
        this.transaction = txn ;
        this.readBlocks.clear() ;
        iteratorBlocks.clear() ;
        this.writeBlocks.clear() ;
        this.freedBlocks.clear() ;
    }
                       
    @Override
    public Block allocate(int blockSize)
    {
        checkIfClosed() ;
        // Might as well allocate now. 
        // This allocates the id.
        Block block = blockMgr.allocate(blockSize) ;
        // [TxTDB:TODO]
        // But we "copy" it by allocating ByteBuffer space.
        block = block.replicate( ) ;
        writeBlocks.put(block.getId(), block) ;
        return block ;
    }

    @Override
    public Block getRead(long id)
    {
        checkIfClosed() ;
        Block block = localBlock(id) ;
        if ( block != null )
            return block ;
        
        block = blockMgr.getRead(id) ;
        readBlocks.add(block.getId()) ;
        return block ;
    }

    @Override
    public Block getReadIterator(long id)
    {
        //logState() ;
        checkIfClosed() ;
        Block block = localBlock(id) ;
        if ( block == null )
            block = blockMgr.getReadIterator(id) ;
        if ( block == null )
            throw new BlockException("No such block: "+getLabel()+" "+id) ;
        iteratorBlocks.add(block.getId()) ;
        return block ;
    }

    @Override
    public Block getWrite(long id)
    {
        checkIfClosed() ;
        Block block = localBlock(id) ;
        if ( block != null )
            return block ;
        
        // Get-as-read.
        block = blockMgr.getRead(id) ;
        // If most blocks get modified, then a copy is needed
        // anyway so now is as good a time as any.
        block = _promote(block) ;
        return block ;
    }

    private Block localBlock(long id)
    {
        checkIfClosed() ;
        return writeBlocks.get(id) ;
    }
    
    @Override
    public Block promote(Block block)
    {
        checkIfClosed() ;
        if ( writeBlocks.containsKey(block.getId()) )
            return block ;
        return _promote(block) ;
    }

    private Block _promote(Block block)
    {
        block = block.replicate() ;
        writeBlocks.put(block.getId(), block) ;
        return block ;
    }

    @Override
    public void release(Block block)
    {
        checkIfClosed() ;
        Long id = block.getId() ;
        // Only release unchanged blocks.
        if ( ! writeBlocks.containsKey(id))
            blockMgr.release(block) ;
    }

    @Override
    public void write(Block block)
    {
        checkIfClosed() ;
        if ( ! block.isModified() )
            Log.warn(this, "Page for block "+fileRef+"/"+block.getId()+" not modified") ;
        
        if ( ! writeBlocks.containsKey(block.getId()) )
        {
            Log.warn(this, "Block not recognized: "+block.getId()) ;
            // Probably corruption by writing in-place.
            // but at least when this transaction commits,
            // the update data is written,
            writeBlocks.put(block.getId(), block) ;
        }
    }
    
    @Override
    public void overwrite(Block block)
    {
        // We are in a chain of BlockMgrs - pass down to the base.
        blockMgr.overwrite(block) ;
    }

    @Override
    public void free(Block block)
    {
        checkIfClosed() ;
        freedBlocks.put(block.getId(), block) ;
    }

    @Override
    public boolean isEmpty()
    {
        checkIfClosed() ;
        return writeBlocks.isEmpty() && blockMgr.isEmpty() ;
    }

    @Override
    public boolean valid(int id)
    {
        checkIfClosed() ;
        if ( writeBlocks.containsKey(id) ) return true ;
        return blockMgr.valid(id) ; 
    }

    @Override
    public void close()
    {
        closed = true ;
    }

    @Override
    public boolean isClosed()
    {
        return closed ;
    }
    
    private void checkIfClosed()
    {
        if ( closed )
            Log.fatal(this, "Already closed: "+transaction.getTxnId()) ;
    }

    @Override
    public void sync()
    {
        checkIfClosed() ;
    }

    // we only use the underlying blockMgr in read-mode - we don't write back blocks.  
    @Override
    public void beginUpdate()           { checkIfClosed() ; blockMgr.beginRead() ; }

    @Override
    public void endUpdate()
    {
        checkIfClosed() ;
        blockMgr.endRead() ;
    }

    private void writeJournalEntry(Block blk)
    {
        blk.getByteBuffer().rewind() ;
        transaction.getJournal().write(JournalEntryType.Block, fileRef, blk) ;
    }
    
    private void logState()
    {
        Log.info(this, "state: "+getLabel()) ;
        Log.info(this, "  readBlocks:      "+readBlocks) ;
        Log.info(this, "  writeBlocks:     "+writeBlocks) ;
        Log.info(this, "  iteratorBlocks:  "+iteratorBlocks) ;
        Log.info(this, "  freedBlocks:     "+freedBlocks) ;
    }
    
    @Override
    public void beginRead()             { checkIfClosed() ; blockMgr.beginRead() ; }

    @Override
    public void endRead()               { checkIfClosed() ; blockMgr.endRead() ; }

    @Override
    public void beginIterator(Iterator<?> iterator)
    {
        checkIfClosed() ; 
        transaction.addIterator(iterator) ;
        blockMgr.beginIterator(iterator) ;
    }

    @Override
    public void endIterator(Iterator<?> iterator)
    {
        checkIfClosed() ; 
        transaction.removeIterator(iterator) ;
        blockMgr.endIterator(iterator) ;
    }

    @Override
    public String toString() { return "Journal:"+fileRef.getFilename()+" ("+blockMgr.getClass().getSimpleName()+")" ; }

    @Override
    public String getLabel() { return fileRef.getFilename() ; }
}
