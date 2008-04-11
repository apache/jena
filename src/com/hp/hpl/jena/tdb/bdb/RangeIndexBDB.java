/*
 * (c) Copyright 2008 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.tdb.bdb;

import iterator.ClosableIterator;
import iterator.NullIterator;

import java.util.Iterator;
import java.util.NoSuchElementException;

import com.sleepycat.je.Cursor;
import com.sleepycat.je.DatabaseEntry;
import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.OperationStatus;

import com.hp.hpl.jena.shared.JenaException;
import com.hp.hpl.jena.tdb.base.record.Record;
import com.hp.hpl.jena.tdb.base.record.RecordFactory;
import com.hp.hpl.jena.tdb.index.RangeIndex;

public class RangeIndexBDB extends IndexBDB implements RangeIndex 
{

    public RangeIndexBDB(String name, SetupBDB setup, RecordFactory recordFactory)
    {
        super(name, setup, recordFactory) ;
    }

    @Override
    public Iterator<Record> iterator()
    {
        try {
            Cursor cursor = db.openCursor(txn, setup.cursorConfig);
            return new IteratorRangeBDB(cursor, null, null, null) ;     // No start, no end.
        } catch (DatabaseException dbe)
        { throw new JenaException("RangeIndexBDB", dbe) ; }
    }

    @Override
    public Iterator<Record> iterator(Record recordMin, Record recordMax)
    {
        try {
            Cursor cursor = db.openCursor(txn, setup.cursorConfig);
            DatabaseEntry key = entryKey(recordMin) ;
            DatabaseEntry data = genBlank() ;
            DatabaseEntry end = entryKey(recordMax) ;
            OperationStatus status = cursor.getSearchKeyRange(key, data, setup.lockMode) ;
            if ( status != OperationStatus.SUCCESS )
                return new NullIterator<Record>() ;
            return new IteratorRangeBDB(cursor, key, data, end) ;
        } catch (DatabaseException dbe)
        { throw new JenaException("RangeIndexBDB", dbe) ; }
    }
    
    class IteratorRangeBDB implements Iterator<Record>, ClosableIterator
    {
        private Cursor cursor ;
        private DatabaseEntry end ;
        private boolean finished = false ;
        private Record slot = null ;

        // End is exclusive.
        IteratorRangeBDB(Cursor cursor, DatabaseEntry firstKey, DatabaseEntry firstValue, DatabaseEntry end) 
        {
            this.cursor = cursor ;
            this.end = end ;
            // This was a positioned cursor. 
            if ( firstKey != null )
                this.slot = record(firstKey, firstValue) ;
        }
        
        public void close()
        {
            try
            {
                if ( ! finished ) 
                    endIterator() ;
            } catch (DatabaseException ex)
            {
                ex.printStackTrace();
            }
        }
        
        @Override
        public boolean hasNext()
        {
            if ( finished ) return false ;
            if ( slot != null ) return true ;
            
            try {
                DatabaseEntry key = new DatabaseEntry(new byte[recordFactory.recordLength()]) ;
                DatabaseEntry value = genBlank() ;
                OperationStatus status = cursor.getNext(key, value, null) ;
                if ( status == OperationStatus.NOTFOUND )
                    return endIterator() ;
                    
                if ( status != OperationStatus.SUCCESS )
                    throw new JenaException("GraphBDB.Mapper: cursor get failed") ;
                // Compare.
                if ( end != null && compare(key, end) >= 0) 
                    return endIterator() ; 
                slot = record(key, value) ;
                return true ;
            } catch (DatabaseException dbe)
            { throw new JenaException("GraphBDB.Mapper", dbe) ; }
        }
        
        private boolean endIterator() throws DatabaseException 
        {
            cursor.close() ;
            finished = true ;
            return false ;
        }
        
        // Use DB comparator instead?
        private int compare(DatabaseEntry x, DatabaseEntry y)
        {
            byte[] xBytes = x.getData() ;
            byte[] yBytes = y.getData() ;
            
            for ( int i = 0 ; i < xBytes.length ; i++ )
            {
                byte b1 = xBytes[i] ;
                byte b2 = yBytes[i] ;
                if ( b1 == b2 )
                    continue ;
                // Treat as unsigned values in the bytes. 
                return (b1&0xFF) - (b2&0xFF) ;  
            }
            return  0 ;
        }

        @Override
        public Record next() {
            if ( ! hasNext() )
                throw new NoSuchElementException() ;
            Record x = slot ;
            slot = null ;
            return x ;
        }

        @Override
        public void remove() { throw new UnsupportedOperationException("remove") ; }
    }

}

/*
 * (c) Copyright 2008 Hewlett-Packard Development Company, LP
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