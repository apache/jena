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

package com.hp.hpl.jena.tdb.base.file;

import static java.lang.String.format ;

import java.nio.ByteBuffer ;
import java.util.concurrent.atomic.AtomicLong ;

import org.apache.jena.atlas.lib.FileOps ;
import org.slf4j.Logger ;

import com.hp.hpl.jena.tdb.base.block.Block ;
import com.hp.hpl.jena.tdb.base.block.BlockException ;
import com.hp.hpl.jena.tdb.sys.SystemTDB ;

/** Support for a disk file backed FileAccess */
public abstract class BlockAccessBase implements BlockAccess 
{
    protected final int blockSize ;
    protected final FileBase file ; 
    
    protected final String label ;
    protected long numFileBlocks = -1 ;             // Don't overload use of this!
    protected final AtomicLong seq ;                // Id (future)
    protected boolean isEmpty = false ;

    public BlockAccessBase(String filename, int blockSize)
    {
        file = FileBase.create(filename) ;
        this.blockSize = blockSize ;
        this.label = FileOps.basename(filename) ;
        long filesize = file.size() ;               // This is not related to used file length in mapped mode.
        long longBlockSize = blockSize ;
            
        numFileBlocks = filesize/longBlockSize ;    // This is not related to used file length in mapped mode.
        seq = new AtomicLong(numFileBlocks) ;

        if ( numFileBlocks > Integer.MAX_VALUE )
            getLog().warn(format("File size (%d) exceeds tested block number limits (%d)", filesize, blockSize)) ;

        if ( filesize%longBlockSize != 0 )
            throw new BlockException(format("File size (%d) not a multiple of blocksize (%d)", filesize, blockSize)) ;

        if ( filesize == 0 )
            isEmpty = true ;
    }

    protected abstract Logger getLog()  ;
    @Override
    final public boolean isEmpty() { return isEmpty ; }
    
    final protected void writeNotification(Block block) { isEmpty = false ; }
    
    final protected void overwriteNotification(Block block)
    {
        // Write at end => extend
        if ( block.getId() >= numFileBlocks )
        {
            numFileBlocks = block.getId()+1 ;
            seq.set(numFileBlocks) ;
        }
    }

    
    //@Override 
    final
    //public 
    protected int allocateId()
    {
        checkIfClosed() ;
        int id = (int)seq.getAndIncrement() ;
        numFileBlocks ++ ;  // TODO Fix this when proper freeblock management is introduced.
        return id ;
    }
    
    @Override
    final synchronized
    public boolean valid(long id)
    {
        // Access to numFileBlocks not synchronized - it's only a check
        if ( id >= numFileBlocks )
            return false ;
        if ( id < 0 )
            return false ;
        return true ; 
    }

    final
    protected void check(long id)
    {
        if ( id > Integer.MAX_VALUE )
            throw new BlockException(format("BlockAccessBase: Id (%d) too large", id )) ;
        
        // Access to numFileBlocks not synchronized - it's only a check
        if ( id < 0 || id >= numFileBlocks )
        {
            // Do it properly!
            synchronized(this)
            {
                if ( id < 0 || id >= numFileBlocks )
                    throw new BlockException(format("BlockAccessBase: Bounds exception: %s: (%d,%d)", file.filename, id, numFileBlocks)) ;
            }
        }
    }
    
    final protected void check(Block block)
    {
        check(block.getId()) ;
        ByteBuffer bb = block.getByteBuffer() ;
        if ( bb.capacity() != blockSize )
            throw new BlockException(format("BlockMgrFile: Wrong size block.  Expected=%d : actual=%d", blockSize, bb.capacity())) ;
        if ( bb.order() != SystemTDB.NetworkOrder )
            throw new BlockException("BlockMgrFile: Wrong byte order") ;
    }

    protected void force()
    {
        file.sync() ;
    }
    
    //@Override
    final public boolean isClosed() { return file.channel() == null ; }  
    
    protected final void checkIfClosed() 
    { 
        if ( isClosed() ) 
            getLog().error("File has been closed") ;
    }
    
    protected abstract void _close() ; 

    @Override
    final public void close()
    {
        _close() ;
        file.close() ;
    }
    
    @Override
    public String getLabel()
    {
        return label ;
    }
}
