/*
 * (c) Copyright 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.tdb.base.recordbuffer;


import java.nio.ByteBuffer;

import org.openjena.atlas.io.IndentedWriter ;


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
                                   RecordFactory factory,
                                   int count)
    {   // This code knows the alignment of the records in the ByteBuffer.
        super(block) ;
        this.headerLength = FIELD_LENGTH+offset ;        // NB +4 for the count field
        ByteBuffer bb = block.getByteBuffer() ;
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