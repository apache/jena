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

package org.seaborne.dboe.transaction.txn ;

import java.nio.ByteBuffer ;
import java.util.ArrayList ;
import java.util.List ;

/** TransactionComponent of N other sub components. */ 
public class TransactionalComponentN implements TransactionalComponent {
    private final List<TransactionalComponent> other = new ArrayList<>() ;
    private final ComponentId cid ;

    public TransactionalComponentN(ComponentId cid, TransactionalComponent ... sub) {
        this.cid = cid ;
        for ( TransactionalComponent tc : sub ) {
            other.add(tc) ;
        }
    }

    // Some of these operations do not make sense.
    
    @Override
    public void startRecovery() {
        throw new UnsupportedOperationException() ;
        //other.forEach(x->x.startRecovery()) ;
    }

    @Override
    public void recover(ByteBuffer ref) {
        throw new UnsupportedOperationException() ;
        //other.forEach(x->x.recover(ref)) ;
    }

    @Override
    public void finishRecovery() {
        throw new UnsupportedOperationException() ;
        //other.forEach(x->x.finishRecovery()) ;
    }
    
    @Override
    public void cleanStart() {
        other.forEach(x->x.cleanStart()) ;
    }

    @Override
    public ComponentId getComponentId() {
        return cid ;
    }

    @Override
    public void begin(Transaction transaction) {
        other.forEach(x->x.begin(transaction)) ;
    }

    @Override
    public ByteBuffer commitPrepare(Transaction transaction) {
        return null ;
        //return other.forEach(x->x.commitPrepare(transaction) ;
    }

    @Override
    public void commit(Transaction transaction) {
        other.forEach(x->x.commit(transaction)) ;
    }

    @Override
    public void commitEnd(Transaction transaction) {
        other.forEach(x->x.commitEnd(transaction)) ;
    }

    @Override
    public void abort(Transaction transaction) {
        other.forEach(x->x.abort(transaction)) ;
    }

    @Override
    public void complete(Transaction transaction) {
        other.forEach(x->x.complete(transaction)) ;
    }

    @Override
    public SysTransState detach() {
        throw new UnsupportedOperationException() ;
        //other.forEach(x->x.detach()) ;
    }

    @Override
    public void attach(SysTransState systemState) {
        throw new UnsupportedOperationException() ;
        //other.forEach(x->x.attach(systemState)) ;
    }

    @Override
    public void shutdown() {
        other.forEach(x->x.shutdown()) ;
    }

    @Override 
    public String toString() { return "N:"+cid ; }
}

