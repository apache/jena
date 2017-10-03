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

package org.apache.jena.dboe.transaction.txn;

import java.nio.ByteBuffer ;

/** Single component aspect of a transaction */  
final 
class SysTrans {
    private final TransactionalComponent elt ;
    private final Transaction transaction ;
    private final TxnId txnId ;

    public SysTrans(TransactionalComponent elt, Transaction transaction, TxnId txnId) { 
        this.elt = elt ;
        this.transaction = transaction ;
        this.txnId = txnId ;
    }

    public void begin()                 { }
    public boolean promote()            { return elt.promote(transaction) ; }

    public ByteBuffer commitPrepare()   { return elt.commitPrepare(transaction) ; }

    public void commit()                { elt.commit(transaction); }

    public void commitEnd()             { elt.commitEnd(transaction); }

    public void abort()                 { elt.abort(transaction); }

    public void complete()              { elt.complete(transaction); }
    
    public Transaction getTransaction()             { return transaction ; } 
    public TxnId getTxnId()                         { return txnId ; } 
    public TransactionalComponent getComponent()    { return elt ; }
    public ComponentId getComponentId()             { return elt.getComponentId() ; }
}
