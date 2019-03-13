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

package org.apache.jena.dboe.base.recordbuffer;

import static org.apache.jena.atlas.lib.Alg.decodeIndex;

import java.util.Iterator;
import java.util.NoSuchElementException;

import org.apache.jena.atlas.lib.Bytes;
import org.apache.jena.atlas.lib.Closeable;
import org.apache.jena.dboe.base.StorageException;
import org.apache.jena.dboe.base.block.BlockException;
import org.apache.jena.dboe.base.record.Record;
import org.apache.jena.dboe.base.record.RecordMapper;

final public
class RecordRangeIterator<X> implements Iterator<X>, Closeable
{
    /** Iterate over a range of fromRec (inclusive) to toRec (exclusive) */
    public static <X> Iterator<X> iterator(int pageId, Record fromRec, Record toRec, RecordBufferPageMgr pageMgr, RecordMapper<X> mapper) {
        if ( ! pageMgr.valid(pageId) ) {
            String msg = "RecordRangeIterator.iterator -- No such block (pageId="+pageId+", fromRec="+fromRec+", toRec="+toRec+ ")";
//            System.err.println(msg);
//            System.exit(0);
            throw new BlockException(msg);
        }
        return new RecordRangeIterator<>(pageId, fromRec, toRec, pageMgr, mapper);
    }

    private final RecordMapper<X> mapper;

    private RecordBufferPage currentPage;      // Set null when finished.

    private int currentIdx;
    private final RecordBufferPageMgr pageMgr;
    private final Record maxRec;
    private final Record minRec;

    private X slot = null;
    private final byte[] keySlot;

    private long countRecords = 0;
    private long countBlocks = 0;

    private RecordRangeIterator(int id, Record fromRec, Record toRec, RecordBufferPageMgr pageMgr, RecordMapper<X> mapper) {
        currentIdx = 0;
        this.pageMgr = pageMgr;
        this.minRec = fromRec;
        this.maxRec = toRec;
        this.mapper = mapper;
        this.keySlot = new byte[pageMgr.getRecordFactory().keyLength()];

        if ( toRec != null && fromRec != null && Record.keyLE(toRec, fromRec) ) {
            currentPage = null;
            return;
        }

        pageMgr.startRead();
        currentPage = pageMgr.getRead(id);

        if ( currentPage.getCount() == 0 ) {
            // Empty page.
            close();
            return;
        }

        if ( fromRec != null ) {
            currentIdx = currentPage.getRecordBuffer().find(fromRec);
            if ( currentIdx < 0 )
                currentIdx = decodeIndex(currentIdx);
        }
    }

    @Override
    public boolean hasNext() {
        if ( slot != null )
            return true;
        if ( currentPage == null )
            return false;
        // Set slot.
        while (currentIdx >= currentPage.getCount()) {
            // Move to next.
            int link = currentPage.getLink();
            if ( link < 0 ) {
                close();
                return false;
            }

            if ( currentPage != null )
                pageMgr.release(currentPage);

            RecordBufferPage nextPage = pageMgr.getRead(link);
            // Check currentPage -> nextPage is strictly increasing keys.
            if ( false ) {
                Record r1 = currentPage.getRecordBuffer().getHigh();
                Record r2 = nextPage.getRecordBuffer().getLow();
                if ( Record.keyGE(r1, r2) )
                    throw new StorageException("RecordRangeIterator: records not strictly increasing: " + r1 + " // " + r2);
            }
            currentPage = nextPage;
            countBlocks++;
            currentIdx = 0;
        }

        slot = currentPage.getRecordBuffer().access(currentIdx, keySlot, mapper);
        currentIdx++;

        if ( maxRec != null && Bytes.compare(keySlot, maxRec.getKey()) >= 0 ) {
            close();
            return false;
        }

        if ( slot == null ) {
            close();
            return false;
        }
        countRecords++;
        return true;
    }

    @Override
    public void close() {
        if ( currentPage == null )
            return;
        pageMgr.release(currentPage);
        pageMgr.finishRead();
        currentPage = null;
        currentIdx = -99;
        slot = null;
    }

    @Override
    public X next() {
        if ( !hasNext() )
            throw new NoSuchElementException();
        X x = slot;
        slot = null;
        return x;
    }

    final public long getCountRecords()     { return countRecords; }

    final public long getCountBlocks()      { return countBlocks; }
}
