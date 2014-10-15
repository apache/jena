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

import com.hp.hpl.jena.tdb.base.block.Block ;
import com.hp.hpl.jena.tdb.base.page.Page ;
import com.hp.hpl.jena.tdb.base.record.RecordFactory ;
import com.hp.hpl.jena.tdb.sys.SystemTDB ;

/**
 * B+Tree records nodes and hash buckets.
 * Add link field to a RecordBufferPageBase
 */

public final class RecordBufferPage extends RecordBufferPageBase
{
    // Why not straight to BPlusTreeRecords?
    // 1 - this may be useful in its own right as a seqeunce of records on-disk.
    // 2 - BPlusTreeRecords inherits from BPlusTreePage
    //
    // Could combine with HashBuck and just have an unused link area in a HashBucket
    
    
    // To Constants
    // Offsets
//    final public static int COUNT      = 0 ;
    final public static int LINK            = 4 ;
    final private static int FIELD_LENGTH   = SystemTDB.SizeOfInt ; // Length of the space needed here (not count)

    private int link = Page.NO_ID ;
    
    public final int getLink() { return link ; }
    
    public void setLink(int link)
    { 
        this.link = link ;
        getBackingBlock().getByteBuffer().putInt(LINK, link) ;
    }
    
    @Override
    protected void _reset(Block block)
    { 
        super.reset(block, this.getCount()) ;
        this.link = block.getByteBuffer().getInt(LINK) ;
    }

    public static int calcRecordSize(RecordFactory factory, int blkSize)
    { return RecordBufferPageBase.calcRecordSize(factory, blkSize, FIELD_LENGTH) ; }
    
    public static int calcBlockSize(RecordFactory factory, int maxRec)
    { return RecordBufferPageBase.calcBlockSize(factory, maxRec, FIELD_LENGTH) ; }
    
    /** The construction methods */
    public static RecordBufferPage createBlank(Block block,RecordFactory factory)
    {
        int count = 0 ;
        int linkId = NO_ID ;
        return new RecordBufferPage(block, factory, count, linkId) ;
    }

    public static RecordBufferPage format(Block block, RecordFactory factory)
    {
        int count = block.getByteBuffer().getInt(COUNT) ;
        int linkId = block.getByteBuffer().getInt(LINK) ;
        return new RecordBufferPage(block, factory, count, linkId) ;
    } 
        
    
    private RecordBufferPage(Block block, RecordFactory factory, int count, int linkId)  
    {
        super(block, FIELD_LENGTH, factory, count) ;
        this.link = linkId ;
    }
    
    @Override
    public String toString()
    { return String.format("RecordBufferPage[id=%d,link=%d]: %s", getBackingBlock().getId(), getLink(), recBuff) ; }

}
