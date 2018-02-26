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

package org.apache.jena.sparql.core ;

import org.apache.jena.atlas.lib.Lib ;
import org.apache.jena.atlas.lib.Sync ;
import org.apache.jena.query.ReadWrite ;
import org.apache.jena.query.TxnType;
import org.apache.jena.sparql.JenaTransactionException ;
import org.apache.jena.sparql.SystemARQ ;
import org.apache.jena.sparql.util.Context ;

/**
 * A DatasetGraph that uses the dataset lock to give weak transactional
 * behaviour, that is the application see transaction but they are not durable.
 * Only provides multiple-reader OR single-writer, and no write-transaction
 * abort.
 * @deprecated Will be removed.
 */
// NOT USED
@Deprecated
public class DatasetGraphWithLock extends DatasetGraphTrackActive implements Sync {
    private final ThreadLocal<Boolean> writeTxn = ThreadLocal.withInitial(()->false) ;
    private final DatasetGraph dsg ;
    private final TransactionalLock transactional ;  
    // Associated DatasetChanges (if any, may be null)
    private final DatasetChanges dsChanges ;
    private final boolean abortSupported ;

    public DatasetGraphWithLock(DatasetGraph dsg) {
        this(dsg, false) ;
    }
    
    public DatasetGraphWithLock(DatasetGraph dsg, boolean abortSupported) {
        this.dsg = dsg ;
        this.dsChanges = findDatasetChanges(dsg) ;
        this.transactional = TransactionalLock.create(dsg.getLock()) ;
        this.abortSupported = abortSupported ;
    }
    
    /** Find a DatasetChanges handler.
     *  Unwrap layers of DatasetGraphWrapper to
     *  look for a DatasetGraphMonitor.
     */
    private static DatasetChanges findDatasetChanges(DatasetGraph dataset) {
        for(;;) {
            // DatasetGraphMonitor extends DatasetGraphWrapper
            if ( dataset instanceof DatasetGraphMonitor )
                return ((DatasetGraphMonitor)dataset).getMonitor() ;
            if ( ! ( dataset instanceof DatasetGraphWrapper ) )
                return null ;
            dataset = ((DatasetGraphWrapper)dataset).getWrapped() ;
        }
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
        return transactional.isInTransaction() ;
    }

    protected boolean isTransactionMode(ReadWrite readWriteMode) {
        return transactional.isTransactionMode(readWriteMode) ;
    }
    
    /** @deprecated Use {@link #isTransactionMode} */
    @Deprecated
    protected boolean isTransactionType(ReadWrite readWriteMode) {
        return transactional.isTransactionMode(readWriteMode) ;
    }

    @Override
    protected void _begin(TxnType txnType) {
        ReadWrite readWrite = TxnType.convert(txnType);
        transactional.begin(txnType);
        writeTxn.set(readWrite.equals(ReadWrite.WRITE));
        if ( dsChanges != null )
            // Replace by transactional state.
            dsChanges.start() ;
    }

    @Override
    protected boolean _promote(Promote promoteMode) {
        throw new JenaTransactionException("promote not supported");
    }

    @Override
    protected void _commit() {
        if ( writeTxn.get() ) {
            sync() ;
        }
        transactional.commit();
        _end() ;
    }

    @Override
    protected void _abort() {
        if ( writeTxn.get() && ! supportsTransactionAbort() ) {
            // Still clean up.
            _end() ; // This clears the transaction type.  
            throw new JenaTransactionException("Can't abort a write lock-transaction") ;
        }
        transactional.abort(); 
        _end() ;
    }

    /** Return whether abort is provided.
     *  Just by locking, a transaction can not write-abort (the changes have been made and not recorded).
     *  Subclasses may do better and still rely on this locking class.  
     */
    
    @Override
    public boolean supportsTransactions() {
        return true;
    }

    @Override
    public boolean supportsTransactionAbort() {
        return abortSupported;
    }
    
    @Override
    protected void _end() {
        if ( dsChanges != null )
            dsChanges.finish();
        transactional.end(); 
        writeTxn.remove();
    }

    @Override
    protected void _close() {
        if ( get() != null )
            get().close() ;
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
