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

package com.hp.hpl.jena.tdb.base.block;

import java.util.Iterator ;

import org.apache.jena.atlas.lib.Closeable ;
import org.apache.jena.atlas.lib.Sync ;


public interface BlockMgr extends Sync, Closeable /*UnitMgr<Block>*/
{
    /** Allocate an uninitialized block - writable - call only inside a update sequence. 
     *  If blockSize is -1, means "default/fixed size" for this BlockMgr
     */
    public Block allocate(int blockSize) ;
    
    /** Answer whether there are any blocks in the collection being managed */
    public boolean isEmpty() ; 
    
    /** Fetch a block, use for read only */
    public Block getRead(long id);
    
    /** Fetch a block, use for read only in an iterator */
    public Block getReadIterator(long id);

    /** Fetch a block, use for write and read - only inside "update" */
    public Block getWrite(long id);

    /** Release a block, unmodified. */
    public void release(Block block) ;

    /** Promote to writeable : it's OK to promote an already writeable block */ 
    public Block promote(Block block);

    // Bad name?  "endWrite", "put" -- for a mapped block, the changes are made directly, not on the write() */   
    /** Write a block back - it still needs releasing. */ 
    public void write(Block block) ;
    
    /** Replace the contents of a block slot with new contents. Block does not need releasing.
     * The write() operation may not do real work if the block is mapped - this operation
     * really does replace the contents with the new contents.  
     */ 
    public void overwrite(Block blk) ;
    
   /** Announce a block is no longer in use (i.e it's now freed) */ 
    public void free(Block block);
  
    /** Is this a valid block id? (may be a free block)*/
    public boolean valid(int id) ;
    
    /** Close the block manager */
    @Override
    public void close() ;
    
    /** Is this block manager still usable?  Closed block managers can not perform any operations except this one. */  
    public boolean isClosed() ; 
    
    /** Sync the block manager */
    @Override
    public void sync() ;
    
    /** Sync the block manager : system operation to ensure sync() is passed down */
    public void syncForce() ;

    // This is not Session interface which si more an application facing
    // coarser granularity interface.  We also add iterator tracking.
    
    /** Start of update */
    public void beginUpdate() ;
    
    /** Completion of update */
    public void endUpdate() ;

    /** Start of read */
    public void beginRead() ;

    /** Completion of read */
    public void endRead() ;

    /** Start of iterator */
    public void beginIterator(Iterator<?> iterator) ;

    /** Completion of iterator */
    public void endIterator(Iterator<?> iterator) ;

    /* Label for helping trace which BlockMgr is which */
    public String getLabel() ;
}
