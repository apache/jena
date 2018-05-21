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

package org.apache.jena.dboe.transaction.txn ;

import java.nio.ByteBuffer ;

public class TransactionalComponentWrapper implements TransactionalComponent {
    protected final TransactionalComponent other ;

    public TransactionalComponentWrapper(TransactionalComponent other) {
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
    public void cleanStart() {
        other.cleanStart() ;
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
    public boolean promote(Transaction transaction) {
        return other.promote(transaction) ;
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
    public SysTransState detach() {
        return other.detach() ;
    }

    @Override
    public void attach(SysTransState systemState) {
        other.attach(systemState) ;
    }

    @Override
    public void shutdown() {
        other.shutdown() ;
    }

    @Override 
    public String toString() { return "W:"+other.toString() ; }
}

