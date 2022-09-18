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

package org.apache.jena.tdb.transaction;

import java.nio.ByteBuffer ;
import java.util.Iterator ;

import org.apache.jena.atlas.iterator.Iter ;
import org.apache.jena.atlas.lib.Pair ;
import org.apache.jena.atlas.lib.StrUtils ;
import org.apache.jena.atlas.logging.Log ;
import org.apache.jena.tdb.base.file.FileException ;
import org.apache.jena.tdb.base.objectfile.ObjectFile ;

/**
 * This version works with two files - the main, base file and a per-transaction journal
 * file. It writes to the journal side.
 * 
 * To flush, it writes the journal to the base file.
 * 
 * It is relatively untested. This is not the normal implementation of a transactional
 * ObjectFile.
 * 
 * @see ObjectFileTrans
 */
public class ObjectFileTransComplex implements ObjectFile, TransactionLifecycle
{
    private final ObjectFile transObjects ;
    private long otherAllocOffset ;           // record where we start allocating
    private boolean passthrough = false ;
    private boolean inTransaction = false ;
    private final ObjectFile base ;
    
    // For recovery replay, we need to truncate "base" first. 
    
    public ObjectFileTransComplex(Transaction txn, ObjectFile base, ObjectFile other)
    {
        // The "other" object file must use the same allocation policy.
        this.base = base ;
        this.transObjects = other ;
        inTransaction = false ;
        this.otherAllocOffset = base.length() ;
        //Log.info(this, getLabel()+": otherAllocOffset = "+otherAllocOffset) ;
    }

    // Begin read ==> passthrough.
    
    @Override
    public void begin(Transaction txn)
    {
        passthrough = false ;
        inTransaction = true ;
        transObjects.reposition(0) ;
        this.otherAllocOffset = base.length() ;
    }
    
    @Override
    public void commitPrepare(Transaction txn)
    {
        if ( ! inTransaction )
            throw new TDBTransactionException("Not in a transaction for a commit to happen") ; 
        transObjects.sync() ;
    }
    
    @Override
    public void committed(Transaction txn)
    {}
    
    @Override
    public void enactCommitted(Transaction txn)
    {
        if ( ! inTransaction )
            throw new TDBTransactionException("Not in a transaction for a commit to happen") ; 
        append() ;
        base.sync() ;
        transObjects.reposition(0) ;
    }

    @Override
    public void clearupCommitted(Transaction txn)
    {
        transObjects.truncate(0) ;
        passthrough = true ;
    }

    @Override
    public void abort(Transaction txn)
    {
        transObjects.reposition(0) ;
    }
    
    /** Copy from the temporary file to the real file */
    private void append()
    {
        // We could write directly to the real file if:
        //    we record the truncate point needed for an abort
        //    manage partial final writes
        //    deny the existence of nodes after the transaction mark.
        // Later - stay simple for now.
        
        // Truncate/position the ObjectFile.
        base.reposition(otherAllocOffset) ;
        
        Iterator<Pair<Long, ByteBuffer>> iter = transObjects.all() ;
        for ( ; iter.hasNext() ; )
        {
            Pair<Long, ByteBuffer> p = iter.next() ;
            String s = StrUtils.fromUTF8bytes(p.getRight().array()) ;
            
            long x = base.write(p.getRight()) ;
            
            if ( p.getLeft()+otherAllocOffset != x )
                throw new FileException("Expected id of "+(p.getLeft()+otherAllocOffset)+", got an id of "+x) ;
        }
    }
    
    //public void setPassthrough(boolean v) { passthrough = v ; }
    
    @Override
    public void reposition(long id)
    {
        if ( passthrough ) { base.reposition(id) ; return ; }
        if ( id > otherAllocOffset )
        {
            transObjects.reposition(mapToOther(id)) ;
            return ;
        }
        
        Log.warn(this, "Unexpected: Attempt to reposition over base file") ;
        base.reposition(id) ;
        transObjects.reposition(0) ;
        otherAllocOffset = base.length() ;
    }
    
    @Override
    public void truncate(long id)
    {
        if ( passthrough ) { base.truncate(id) ; return ; }
        if ( id > otherAllocOffset )
        {
            transObjects.truncate(mapToOther(id)) ;
            return ;
        }
        base.truncate(id) ;
        transObjects.truncate(0) ;
        otherAllocOffset = base.length() ;
    }

    /** Convert from a id to the id in the "other" file */ 
    private long mapToOther(long x) { return x-otherAllocOffset ; }
    /** Convert from a id in other to an external id  */ 
    private long mapFromOther(long x) { return x+otherAllocOffset ; }
    
    @Override
    public long write(ByteBuffer buffer)
    {
        if ( passthrough ) { return base.write(buffer) ; } 
        // Write to auxillary
        long x = transObjects.write(buffer) ;
        return mapFromOther(x) ;
    }

    @Override
    public ByteBuffer read(long id)
    {
        if ( passthrough ) { return base.read(id) ; } 
        if ( id < otherAllocOffset )
            return base.read(id) ;
        long x = mapToOther(id) ; 
        return transObjects.read(id-otherAllocOffset) ;
    }

    @Override
    public long length()
    {
        if ( passthrough ) { return base.length() ; } 
        return otherAllocOffset+transObjects.length() ;
    }
    
    @Override
    public boolean isEmpty()
    {
        if ( passthrough ) { return base.isEmpty() ; } 
        return transObjects.isEmpty() && base.isEmpty() ;
    }

    @Override
    public Iterator<Pair<Long, ByteBuffer>> all()
    {
        if ( passthrough ) { return base.all() ; } 
        return Iter.concat(base.all(), transObjects.all()) ;
    }

    @Override
    public void sync()
    { 
        if ( passthrough ) { base.sync() ; return ; } 
    }

    @Override
    public void close()
    {
        if ( passthrough ) { base.close() ; return ; }
    }

    @Override
    public String getLabel()
    {
        return "("+base.getLabel()+":"+transObjects.getLabel()+")" ;
    }
}
