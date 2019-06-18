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

package org.apache.jena.dboe.transaction;

import org.apache.jena.query.ReadWrite;
import org.apache.jena.query.TxnType;

/** Wrapper for {@link Transactional} */
public class TransactionalWrapper implements Transactional {

    private Transactional other;
    protected Transactional get() { return other; } 
    
    public TransactionalWrapper(Transactional other) {
        this.other = other;
    }
    
    @Override
    public void begin(TxnType type) {
        get().begin(type);
    }

    @Override
    public void begin(ReadWrite readWrite) {
        get().begin(readWrite);
    }

    @Override
    public boolean promote(Promote mode) {
        return get().promote(mode);
    }

    @Override
    public void commit() {
        get().commit();
    }

    @Override
    public void abort() {
        get().abort();
    }

    @Override
    public void end() {
        get().end();
    }

    @Override
    public ReadWrite transactionMode() {
        return get().transactionMode();
    }

    @Override
    public TxnType transactionType() {
        return get().transactionType();
    }

    @Override
    public boolean isInTransaction() {
        return get().isInTransaction();
    }
}
