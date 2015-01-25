package org.seaborne.transaction.txn ;

import java.nio.ByteBuffer ;



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

public class TransactionComponentWrapper implements TransactionalComponent {
    protected final TransactionalComponent other ;

    public TransactionComponentWrapper(TransactionalComponent other) {
        this.other = other ;
    }

    @Override
    public void startRecovery() {
        other.startRecovery() ;
    }

    @Override
    public void recover(ByteBuffer ref) {
        other.recover(ref) ;
    }

    @Override
    public void finishRecovery() {
        other.finishRecovery() ;
    }

    @Override
    public ComponentId getComponentId() {
        return other.getComponentId() ;
    }

    @Override
    public void begin(Transaction transaction) {
        other.begin(transaction) ;
    }

    @Override
    public ByteBuffer commitPrepare(Transaction transaction) {
        return other.commitPrepare(transaction) ;
    }

    @Override
    public void commit(Transaction transaction) {
        other.commit(transaction) ;
    }

    @Override
    public void commitEnd(Transaction transaction) {
        other.commitEnd(transaction) ;
    }

    @Override
    public void abort(Transaction transaction) {
        other.abort(transaction) ;
    }

    @Override
    public void complete(Transaction transaction) {
        other.complete(transaction) ;
    }

    @Override
    public void shutdown() {
        other.shutdown() ;
    }

    @Override 
    public String toString() { return "W:"+other.toString() ; }
}

