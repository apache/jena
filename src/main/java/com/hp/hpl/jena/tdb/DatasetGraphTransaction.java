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

package com.hp.hpl.jena.tdb;

import com.hp.hpl.jena.query.ReadWrite ;
import com.hp.hpl.jena.sparql.JenaTransactionException ;
import com.hp.hpl.jena.sparql.core.DatasetGraph ;
import com.hp.hpl.jena.tdb.base.file.Location ;
import com.hp.hpl.jena.tdb.migrate.DatasetGraphTrackActive ;
import com.hp.hpl.jena.tdb.store.DatasetGraphTDB ;
import com.hp.hpl.jena.tdb.transaction.TDBTransactionException ;

/** Transactional DatasetGraph that allows one active transaction.
 * For multiple read transactions, create multiple DatasetGraphTX objects.
 * This is analogous to a "connection" in JDBC.
 */

public class DatasetGraphTransaction extends DatasetGraphTrackActive
{
    /* Initially, the app can use this DatasetGraph non-transactionally.
     * But as soon as it starts a transaction, the dataset can only be used
     * inside transactions. 
     */

    private DatasetGraphTxn dsgTxn = null ;
    private boolean haveUsedInTransaction = false ;
    private final StoreConnection sConn ;

    public DatasetGraphTransaction(Location location)
    {
        sConn = StoreConnection.make(location) ;
    }

    public DatasetGraphTransaction(DatasetGraphTDB dsg)
    {
        sConn = StoreConnection.make(dsg) ;
    }

    public Location getLocation()       { return sConn.getLocation() ; }
    
    @Override
    protected DatasetGraph get()
    {
        if ( isInTransaction() )
        {
            if ( dsgTxn == null )
                throw new TDBTransactionException("In a transaction but no translational DatasetGraph") ;
            return dsgTxn ;
        }
        
        if ( haveUsedInTransaction )
            throw new TDBTransactionException("Not in a transaction") ;

        // Never used in a transaction - return underlying database for old style (non-transactional) usage.  
        return sConn.getBaseDataset() ;
    }

    @Override
    protected void checkActive()
    {
        if ( haveUsedInTransaction && ! isInTransaction() )
            throw new JenaTransactionException("Not in a transaction ("+getLocation()+")") ;
    }

    @Override
    protected void checkNotActive()
    {
        if ( haveUsedInTransaction && isInTransaction() )
            throw new JenaTransactionException("Currently in a transaction ("+getLocation()+")") ;
    }

    @Override
    protected void _begin(ReadWrite readWrite)
    {
        dsgTxn = sConn.begin(readWrite) ;
    }

    @Override
    protected void _commit()
    {
        dsgTxn.commit() ;
    }

    @Override
    protected void _abort()
    {
        dsgTxn.abort() ;
    }

    @Override
    protected void _end()
    {
        dsgTxn.end() ;
    }
    
    @Override
    protected void _close()
    {
        // Don't close the base dataset.
//        if (get() != null)
//            get().close() ;
    }
}
