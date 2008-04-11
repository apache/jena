/*
 * (c) Copyright 2008 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.tdb.bdb;

import com.sleepycat.je.*;

import com.hp.hpl.jena.tdb.TDBException;
import com.hp.hpl.jena.tdb.base.record.Record;
import com.hp.hpl.jena.tdb.base.record.RecordFactory;
import com.hp.hpl.jena.tdb.index.Index;

// General bridge between BDB and TDB indexes
// See also direct implementations such as TripleIndex.

public class IndexBDB implements Index
{
    protected RecordFactory recordFactory ;
    protected Database db ;
    protected SetupBDB setup ;
    protected Transaction txn = null ;;

    public IndexBDB(String name, SetupBDB setup, RecordFactory recordFactory)
    {
        try
        {
            this.recordFactory = recordFactory ;
            this.setup = setup ;
            db = setup.dbEnv.openDatabase(txn, name, setup.dbConfig) ;
        } catch (DatabaseException ex)
        { throw new TDBException(ex) ; }
    }
    
    @Override
    public RecordFactory getRecordFactory()
    {
        return recordFactory  ;
    }

    @Override
    public boolean add(Record record)
    {
        try {
            DatabaseEntry entry = entryKey(record) ;
            DatabaseEntry value = entryValue(record) ;
            
            OperationStatus status = db.putNoOverwrite(txn, entry, value) ;
            if ( status == OperationStatus.KEYEXIST )
                // Duplicate
                return false ;
            return true ;
        } catch (DatabaseException ex)
        {
            throw new TDBException("IndexBDB",ex) ;
        }
    }

    @Override
    public void close()
    {
        try {
            db.close() ;
        } catch (DatabaseException ex)
        {
            throw new TDBException("IndexBDB",ex) ;
        }
    }

    @Override
    public boolean contains(Record record)
    {
        return find(record) != null ;
    }

    @Override
    public boolean delete(Record record)
    {
        try {
            DatabaseEntry key = entryKey(record) ;
            OperationStatus status = db.delete(txn, key) ;
            if ( status == OperationStatus.NOTFOUND )
                return false ;
            return true ;
        } catch (DatabaseException ex)
        {
            throw new TDBException("IndexBDB",ex) ;
        }
    }

    @Override
    public Record find(Record record)
    {
        try {
            DatabaseEntry key = entryKey(record) ;
            DatabaseEntry retValue = new DatabaseEntry() ;
            OperationStatus status = db.get(txn, key, retValue, setup.lockMode) ;
            if ( status == OperationStatus.NOTFOUND )
                return null ;
            return record(key, retValue) ;
        } catch (DatabaseException ex)
        {
            throw new TDBException("IndexBDB",ex) ;
        }
    }

    @Override
    public void sync(boolean force)
    {}
    
    // Don't use in a .get() or .getNext()
    private static DatabaseEntry empty = new DatabaseEntry(new byte[0]) ;
    protected static DatabaseEntry genBlank() { return new DatabaseEntry() ; }
    
    protected DatabaseEntry entryKey(Record record)
    {
        byte b[] = new byte[recordFactory.keyLength()] ;
        System.arraycopy(b, 0, record.getKey(), 0, recordFactory.keyLength()) ;
        return new DatabaseEntry(b) ;
    }
    
    protected DatabaseEntry entryValue(Record record)
    {
        if ( ! recordFactory.hasValue() )
            return new DatabaseEntry(new byte[0]) ;
        byte b[] = new byte[recordFactory.valueLength()] ;
        System.arraycopy(b, 0, record.getValue(), 0, recordFactory.valueLength()) ;
        return new DatabaseEntry(b) ;
    }
    
    protected Record record(DatabaseEntry key, DatabaseEntry value)
    {
        if ( false ) return recordFactory.create(key.getData(), value.getData()) ;
        
        // Avoid copy if key-only?
        byte k[] = new byte[recordFactory.keyLength()] ;
        System.arraycopy(key.getData(), 0, k, 0, recordFactory.keyLength()) ;
        
        byte v[] = null ;
        if ( recordFactory.hasValue() )
        {
            v = new byte[recordFactory.valueLength()] ;
            System.arraycopy(value.getData(), 0, v, 0, recordFactory.valueLength()) ;
        }
        return recordFactory.create(k, v) ;
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