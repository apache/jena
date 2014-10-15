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

import java.util.Iterator ;
import java.util.NoSuchElementException ;

import com.hp.hpl.jena.tdb.base.buffer.RecordBuffer ;
import com.hp.hpl.jena.tdb.base.record.Record ;
import com.hp.hpl.jena.tdb.base.recordbuffer.RecordBufferPage ;
import com.hp.hpl.jena.tdb.base.recordbuffer.RecordBufferPageMgr ;

/** Iterate over a stream of records, packing them into RecordBufferPage -- the leaf of a B+Tree 
 *  This class does not write the blocks back to the block manager.
 *  This cleass does allocate block ids and blocks.
 *  @see RecordBufferPageLinker
 */

class RecordBufferPagePacker implements Iterator<RecordBufferPage>
{
    Iterator<Record> records = null ;
    RecordBufferPage recordBufferPage = null ;
    RecordBufferPageMgr rbMgr = null ;
    
    RecordBufferPagePacker(Iterator<Record> records, RecordBufferPageMgr rbMgr)
    {
        this.records = records ;
        this.rbMgr = rbMgr ;
    }
    
    @Override
    public boolean hasNext()
    {
        if ( recordBufferPage == null )
        {
            if ( records == null )
                return false ;
            
            if ( !records.hasNext() )
            {
                records = null ;
                return false ;
            }
            // At least one record to be processed.
            // No pending RecordBufferPage
            // ==> There will be a RecordBufferPage to yield.

//            int id = rbMgr.allocateId() ;
//            //System.out.println("Allocate : "+id) ;
            recordBufferPage = rbMgr.create() ;
            
            RecordBuffer rb = recordBufferPage.getRecordBuffer() ;
            while ( !rb.isFull() && records.hasNext() )
            {
                Record r = records.next();
                rb.add(r) ;
            }
            if ( ! records.hasNext() )
                records = null ;
            return true ;
        }
        return true ;
        
    }
    
    @Override
    public RecordBufferPage next()
    {
        if ( ! hasNext() ) throw new NoSuchElementException() ;
        RecordBufferPage rbp = recordBufferPage ;
        recordBufferPage = null ;
        return rbp ;
    }
    
    @Override
    public void remove()
    { throw new UnsupportedOperationException() ; }
}
