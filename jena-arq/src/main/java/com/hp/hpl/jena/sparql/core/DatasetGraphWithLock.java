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
 * behaviour. Only supports multiple-reader OR single-writer, and no write-transction
 * abort. Transactions are not durable.
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

    private final DatasetGraph         dsg ;
    private final ThreadLocalReadWrite readWrite     = new ThreadLocalReadWrite() ;
    private final ThreadLocalBoolean   inTransaction = new ThreadLocalBoolean() ;

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

    @Override
    protected void _begin(ReadWrite readWrite) {
        this.readWrite.set(readWrite) ;
        boolean b = (readWrite == ReadWrite.READ) ;
        dsg.getLock().enterCriticalSection(b) ;
        inTransaction.set(true) ;
    }

    @Override
    protected void _commit() {
        if ( readWrite.get() == ReadWrite.WRITE )
            sync() ;
        dsg.getLock().leaveCriticalSection() ;
        this.readWrite.set(null) ;
        inTransaction.set(false) ;
    }

    @Override
    protected void _abort() {
        // OK for read, not for write.
        if ( readWrite.get() == ReadWrite.WRITE ) {
            // Still clean up.
            _end() ;
            throw new JenaTransactionException("Can't abort a write lock-transaction") ;
        }
        _end() ;
    }

    @Override
    protected void _end() {
        if ( isInTransaction() )
            dsg.getLock().leaveCriticalSection() ;
        inTransaction.set(false) ;
    }

    @Override
    protected void _close() {
        if ( dsg != null )
            dsg.close() ;
    }

    @Override
    public Context getContext() {
        return get().getContext() ;
    }

    @Override
    public void sync() {
        SystemARQ.sync(dsg) ;
    }

    @Override
    public String toString() {
        try {
            return dsg.toString() ;
        } catch (Exception ex) {
            return Lib.className(this) ;
        }
    }
}
