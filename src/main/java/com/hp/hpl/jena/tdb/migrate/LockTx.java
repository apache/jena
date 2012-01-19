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

package com.hp.hpl.jena.tdb.migrate;

import com.hp.hpl.jena.query.ReadWrite ;
import com.hp.hpl.jena.shared.Lock ;
import com.hp.hpl.jena.tdb.transaction.DatasetGraphTransaction ;
import com.hp.hpl.jena.tdb.transaction.TDBTransactionException ;

import static com.hp.hpl.jena.tdb.migrate.LockTx.LockTxState.* ;

/** Lock that provides transactions
 *  Not reentrant.
 */

public class LockTx implements Lock
{
    private DatasetGraphTransaction dsg ;
    
    public LockTx(DatasetGraphTransaction dsg) { this.dsg = dsg ; }
    
    static enum LockTxState { TxNONE, TxREAD, TxWRITE }
    
    private LockTxState state = TxNONE ;
    
    @Override
    public void enterCriticalSection(boolean readLockRequested)
    {
        if ( state != TxNONE )
            throw new TDBTransactionException("Illegal state: "+state) ;
        
        if ( readLockRequested )
        {
            state = TxREAD ;
            dsg.begin(ReadWrite.READ) ;
        }
        else
        {
            state = TxWRITE ;
            dsg.begin(ReadWrite.WRITE) ;
        }
    }

    @Override
    public void leaveCriticalSection()
    {
        switch (state)
        {
            case TxNONE :   throw new TDBTransactionException("Illegal state: "+state) ;
            case TxREAD :   dsg.close() ;  break ;
            case TxWRITE :  dsg.commit() ; break ;
        }
        state = TxNONE ;
    }

}
