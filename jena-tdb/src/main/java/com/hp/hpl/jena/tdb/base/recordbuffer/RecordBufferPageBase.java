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

package com.hp.hpl.jena.tdb.base.recordbuffer;


import java.nio.ByteBuffer;

import org.apache.jena.atlas.io.IndentedWriter ;


import com.hp.hpl.jena.tdb.base.block.Block ;
import com.hp.hpl.jena.tdb.base.buffer.RecordBuffer;
import com.hp.hpl.jena.tdb.base.page.PageBase;
import com.hp.hpl.jena.tdb.base.record.RecordFactory;
import com.hp.hpl.jena.tdb.sys.SystemTDB;

/** The on-disk form of a block of a single RecordBuffer
 * (i.e. this is not part of a BTree/BPlusTree branch node).
 * This must be compatible with B+Tree records nodes and hashbuckets.
 */

public abstract class RecordBufferPageBase extends PageBase //implements Page
{
    // Field offsets
    final public static int COUNT           = 0 ;
    // Length due to this class - subclasses may use more overhead.
    final private static int FIELD_LENGTH   = SystemTDB.SizeOfInt ;     
    
    protected final int headerLength ;

    // Interface: "Page" - id, byteBuffer, count
    protected RecordBuffer recBuff ;
    private final RecordFactory factory ;
    
    //private int offset ;                // Bytes of overhead.
    
    public static int calcRecordSize(RecordFactory factory, int blkSize, int headerOffset)
    { 
        // Length = X*recordLength + HEADER
        int x = blkSize-totalOffset(headerOffset) ; 
        return x / factory.recordLength() ;
    }
    
    public static int calcBlockSize(RecordFactory factory, int maxRec, int headerOffset)
    { 
        return totalOffset(headerOffset) + factory.recordLength()*maxRec ;
    }
    
    private static int totalOffset(int headerOffset)
    {
        return FIELD_LENGTH+headerOffset ;
    }

    protected RecordBufferPageBase(Block block, int offset, 
                                   RecordFactory factory, int count)
    {   // This code knows the alignment of the records in the ByteBuffer.
        super(block) ;
        this.headerLength = FIELD_LENGTH+offset ;        // NB +4 for the count field
        this.factory = factory ;
        reset(block, count) ;
    }
    
    protected void reset(Block block, int count)
    {
        ByteBuffer bb = block.getByteBuffer() ;
        bb.clear() ;
        bb.position(headerLength) ;
        bb = bb.slice();
        this.recBuff = new RecordBuffer(bb, factory, count) ;
    }

    public final RecordBuffer getRecordBuffer()
    {
        return recBuff ;
    }
    
    public final int getCount()
    {
        return recBuff.size() ;
    }

    public final int getMaxSize()
    {
        return recBuff.maxSize() ;
    }

    public void setCount(int count)
    { recBuff.setSize(count) ; }

    @Override
    public String toString()
    { return String.format("RecordBufferPageBase[id=%d]: %s", getBackingBlock().getId(), recBuff) ; }

    @Override
    public void output(IndentedWriter out)
    { out.print(toString()) ; }
}
