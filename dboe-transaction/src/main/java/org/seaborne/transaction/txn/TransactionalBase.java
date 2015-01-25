/**
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *  See the NOTICE file distributed with this work for additional
 *  information regarding copyright ownership.
 */

package org.seaborne.transaction.txn;

import java.util.Objects ;

import org.seaborne.transaction.Transactional ;

import com.hp.hpl.jena.query.ReadWrite ;

/**
 * Framework for implementing a Transactional.
 */

public class TransactionalBase implements Transactional {
    private final String label ; 
    protected boolean isShutdown = false ; 
    protected final TransactionCoordinator txnMgr ;
    
    // Per thread transaction.
    private final ThreadLocal<Transaction> theTxn = new ThreadLocal<Transaction>() ;
    
    public TransactionalBase(String label, TransactionCoordinator txnMgr) {
        this.label = label ;
        this.txnMgr = txnMgr ;
    }
    
    public TransactionalBase(TransactionCoordinator txnMgr) {
        this(null, txnMgr) ;
    }

    @Override
    public final void begin(ReadWrite readWrite) {
        Objects.nonNull(readWrite) ;
        checkNotActive() ;
        Transaction transaction = txnMgr.begin(readWrite) ;
        theTxn.set(transaction) ;
    }

    @Override
    public final void commit() {
        // Thse steps are per-thread so no synchronization needed.
        checkActive() ;
        Transaction txn = theTxn.get() ;
        txn.commit() ;
        _end() ;
    }

    @Override
    public final void abort() {
        checkActive() ;
        Transaction txn = theTxn.get() ;
        try {
            txn.abort() ;
        }
        finally {
            _end() ;
        }
    }

    @Override
    public final void end() {
        // Don't check if active or if any thread locals exist
        // because this may have already been called.
        // txn.get() ; -- may be null -- test repeat calls.
        _end() ;
        theTxn.remove() ; 
    }

    final 
    public boolean isInTransaction() {
        return theTxn.get() != null ;
    }

    /**
     * Shutdown component, aborting any in-progress transactions. This operation
     * is not guaranteed to be called.
     */
    public void shutdown() {
        isShutdown = true ;
    }

    protected String label(String msg) {
        if ( label == null )
            return msg ;
        return label+": "+msg ;
    }
    
    final
    protected void checkActive()
    {
        checkNotShutdown() ;
        if ( ! isInTransaction() )
            throw new TransactionException(label("Not in a transaction")) ;
    }

    final
    protected void checkNotActive() {
        checkNotShutdown() ;
        if ( isInTransaction() )
            throw new TransactionException(label("Currently in a transaction")) ;
    }

    final
    protected void checkNotShutdown() {
        if ( isShutdown )
            throw new TransactionException(label("Already shutdown")) ;
    }

    private final void _end() {
        checkNotShutdown() ;
        Transaction txn = theTxn.get() ;
        if ( txn != null ) {
            txn.end() ;
            theTxn.set(null) ;
        }
    }
}

