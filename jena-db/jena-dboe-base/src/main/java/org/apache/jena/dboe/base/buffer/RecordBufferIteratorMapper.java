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

package org.apache.jena.dboe.base.buffer;

import static org.apache.jena.atlas.lib.Alg.decodeIndex ;

import java.util.Iterator;
import java.util.NoSuchElementException;

import org.apache.jena.atlas.lib.Bytes;
import org.apache.jena.dboe.base.record.Record;
import org.apache.jena.dboe.base.record.RecordMapper;

// Iterate over one RecordBuffer
public class RecordBufferIteratorMapper<X> implements Iterator<X>
{
    private static final int END = -99;
    private RecordBuffer rBuff ;
    private int nextIdx ;
    private X slot = null ;
    private final byte[] keySlot ;
    private final Record maxRec ;
    private final Record minRec ;
    private final RecordMapper<X> mapper;
    
//    RecordBufferIteratorMapper(RecordBuffer rBuff)
//    { this(rBuff, null, null); }
    
    RecordBufferIteratorMapper(RecordBuffer rBuff, Record minRecord, Record maxRecord, int keyLen, RecordMapper<X> mapper)
    {
        this.rBuff = rBuff ;
        this.mapper = mapper ;
        this.keySlot = (maxRecord==null) ? null : new byte[keyLen];
        nextIdx = 0 ;
        minRec = minRecord ;
        if ( minRec != null )
        {
            nextIdx = rBuff.find(minRec) ;
            if ( nextIdx < 0 )
                nextIdx = decodeIndex(nextIdx) ;
        }
        
        maxRec = maxRecord ; 
    }

    private void finish()
    {
        rBuff = null ;
        nextIdx = END ;
        slot = null ;
    }
    
    @Override
    public boolean hasNext()
    {
        if ( slot != null )
            return true ;
        if ( nextIdx < 0 )
            return false ;
        if ( nextIdx >= rBuff.size() )
        {
            finish() ;
            return false ;
        }
        
        slot = rBuff.access(nextIdx, keySlot, mapper);
        if ( maxRec != null && Bytes.compare(keySlot, maxRec.getKey()) >= 0 ) 
        {
            // Finished - now to large
            finish() ;
            return false ;
        }
        nextIdx ++ ;
        return true ;
    }

    @Override
    public X next()
    {
        if ( ! hasNext() )
            throw new NoSuchElementException("RecordBufferIterator") ;
        X r = slot ;
        slot = null ;
        return r ;
    }

    @Override
    public void remove()
    { throw new UnsupportedOperationException("RecordBufferIterator.remove") ; }
}
