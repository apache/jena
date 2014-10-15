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

package com.hp.hpl.jena.tdb.transaction;

import com.hp.hpl.jena.sparql.core.DatasetGraphWrapper ;
import com.hp.hpl.jena.tdb.store.DatasetGraphTDB ;

/** A DatasetGraph that is a single transaction.
 * It does not support transactions, it is a transaction (single use).
 */
public class DatasetGraphTxn extends DatasetGraphWrapper
{
    private Transaction transaction ;

    public DatasetGraphTxn(DatasetGraphTDB dsg, Transaction txn)
    {
        super(dsg) ;
        this.transaction = txn ;
    }

    public Transaction getTransaction() { return transaction ; }
    
    /** Return the view (storage) for this transaction */ 
    public DatasetGraphTDB getView() { return (DatasetGraphTDB)getWrapped() ; }
    
    public void commit()
    {
        transaction.commit() ;
    }

    public void abort()
    {
        transaction.abort() ;
    }
    
    // Context copied in DatasetBuilderTxn.build.
//    @Override
//    public Context getContext() { ... }
    
    @Override
    public String toString()
    { return "Txn:"+super.toString() ; }
    
    public void end()
    {
        if ( transaction != null )
            transaction.close() ;
        transaction = null ;
    }

}
