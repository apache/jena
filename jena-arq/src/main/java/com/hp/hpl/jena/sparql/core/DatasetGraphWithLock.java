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

package com.hp.hpl.jena.sparql.core ;

import org.apache.jena.atlas.lib.Lib ;
import org.apache.jena.atlas.lib.Sync ;

import com.hp.hpl.jena.query.ReadWrite ;
import com.hp.hpl.jena.sparql.JenaTransactionException ;
import com.hp.hpl.jena.sparql.SystemARQ ;
import com.hp.hpl.jena.sparql.util.Context ;

/**
 * A DatasetGraph that uses the dataset lock to give weak transactional
 * behaviour, that is the application see transaction but they are not durable.
 * Only provides multiple-reader OR single-writer, and no write-transction
 * abort.
 */
public class DatasetGraphWithLock extends DatasetGraphTrackActive implements Sync {
    static class ThreadLocalBoolean extends ThreadLocal<Boolean> {
        @Override
        protected Boolean initialValue() {
            return false ;
        }
    }

    static class ThreadLocalReadWrite extends ThreadLocal<ReadWrite> {
        @Override
        protected ReadWrite initialValue() {
            return null ;
        }
    }

    private final ThreadLocalReadWrite readWrite     = new ThreadLocalReadWrite() ;
    private final ThreadLocalBoolean   inTransaction = new ThreadLocalBoolean() ;
    private DatasetGraph dsg ;

    public DatasetGraphWithLock(DatasetGraph dsg) {
        this.dsg = dsg ;
    }

    @Override
    protected DatasetGraph get() {
        return dsg ;
    }

    @Override
    protected void checkActive() {
        if ( !isInTransaction() )
            throw new JenaTransactionException("Not in a transaction") ;
    }

    @Override
    protected void checkNotActive() {
        if ( isInTransaction() )
            throw new JenaTransactionException("Currently in a transaction") ;
    }

    @Override
    public boolean isInTransaction() {
        return inTransaction.get() ;
    }

    protected boolean isTransactionType(ReadWrite readWriteType) {
        return readWrite.get() == readWriteType ;
    }

    @Override
    protected void _begin(ReadWrite readWrite) {
        this.readWrite.set(readWrite) ;
        boolean b = isTransactionType(ReadWrite.READ) ;
        get().getLock().enterCriticalSection(b) ;
        inTransaction.set(true) ;
    }

    @Override
    protected void _commit() {
        if ( isTransactionType(ReadWrite.WRITE) )
            sync() ;
        _end() ;
    }

    @Override
    protected void _abort() {
        if ( isTransactionType(ReadWrite.WRITE) && ! abortImplemented() ) {
            // Still clean up.
            _end() ; // This clears the transaction type.  
            throw new JenaTransactionException("Can't abort a write lock-transaction") ;
        }
        _end() ;
    }

    /** Return whether abort is provided.
     *  Just by locking, a transaction can not write-abort (the chnages have been made and not recorded).
     *  Subclasses may do better and still rely on this locking class.  
     */
    protected boolean abortImplemented() { return false ; }

    @Override
    protected void _end() {
        if ( isInTransaction() ) {
            get().getLock().leaveCriticalSection() ;
            clearState() ;
        }
    }

    @Override
    protected void _close() {
        if ( get() != null )
            get().close() ;
    }
    
    private void clearState() {
        inTransaction.set(false) ;
        readWrite.set(null) ;
    }

    @Override
    public Context getContext() {
        return get().getContext() ;
    }

    @Override
    public void sync() {
        SystemARQ.sync(get()) ;
    }

    @Override
    public String toString() {
        try {
            return get().toString() ;
        } catch (Exception ex) {
            return Lib.className(this) ;
        }
    }
}
