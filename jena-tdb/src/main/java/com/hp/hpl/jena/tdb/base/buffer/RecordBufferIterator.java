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

package com.hp.hpl.jena.tdb.base.buffer;

import static org.apache.jena.atlas.lib.Alg.decodeIndex ;

import java.util.Iterator;
import java.util.NoSuchElementException;

import com.hp.hpl.jena.tdb.base.record.Record;

public class RecordBufferIterator implements Iterator<Record>
{
    private RecordBuffer rBuff ;
    private int nextIdx ;
    private Record slot = null ;
    private final Record maxRec ;
    private final Record minRec ;
    
    RecordBufferIterator(RecordBuffer rBuff)
    { this(rBuff, null, null); }
    
    RecordBufferIterator(RecordBuffer rBuff, Record minRecord, Record maxRecord)
    {
        this.rBuff = rBuff ;
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
        nextIdx = -99 ;
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
        
        slot = rBuff.get(nextIdx) ;
        if ( maxRec != null && Record.keyGE(slot, maxRec) )
        {
            // Finished - now to large
            finish() ;
            return false ;
        }
        nextIdx ++ ;
        return true ;
    }

    @Override
    public Record next()
    {
        if ( ! hasNext() )
            throw new NoSuchElementException("RecordBufferIterator") ;
        Record r = slot ;
        slot = null ;
        return r ;
    }

    @Override
    public void remove()
    { throw new UnsupportedOperationException("RecordBufferIterator.remove") ; }
    
}
