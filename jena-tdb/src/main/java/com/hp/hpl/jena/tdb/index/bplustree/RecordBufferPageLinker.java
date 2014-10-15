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

import java.util.Iterator;
import java.util.NoSuchElementException;

import org.apache.jena.atlas.iterator.PeekIterator ;

import com.hp.hpl.jena.tdb.base.recordbuffer.RecordBufferPage ;

/** From a stream of RecordBufferPage,  manage the link fields.
 * That is, be a one slot delay so that the "link" field can point to the next page.
 * Be careful about the last block.   
 *
 */
class RecordBufferPageLinker implements Iterator<RecordBufferPage>
{
    PeekIterator<RecordBufferPage> peekIter ;
    
    RecordBufferPage slot = null ;
    
    RecordBufferPageLinker(Iterator<RecordBufferPage> iter)
    {
        if ( ! iter.hasNext() )
        {
            peekIter = null ;
            return ;
        }
        
        peekIter = new PeekIterator<>(iter) ;
    }
    
    @Override
    public boolean hasNext()
    {
        if ( slot != null )
            return true ;
        
        if ( peekIter == null )
            return false ;

        if ( ! peekIter.hasNext() )
        {
            peekIter = null ;
            return false ;
        }
        
        slot = peekIter.next() ;
        RecordBufferPage nextSlot = peekIter.peek() ;
        // If null, no slot ahead so no linkage field to set.
        if ( nextSlot != null )
            // Set the slot to the id of the next one
            slot.setLink(nextSlot.getId()) ;
        return true ;
    }
    
    @Override
    public RecordBufferPage next()
    {
        if ( ! hasNext() ) throw new NoSuchElementException() ;
        RecordBufferPage rbp = slot ;
        slot = null ;
        return rbp ;
    }
    
    @Override
    public void remove()
    { throw new UnsupportedOperationException() ; }
}
