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

package org.apache.jena.sparql.core;

import org.apache.jena.query.ReadWrite ;
import org.apache.jena.shared.Lock ;

/** Transactional by mutual exclusion. */
public class TransactionalMutex implements Transactional
{
    private final Lock lock;
    private ThreadLocal<Boolean> isInTransaction = ThreadLocal.withInitial(()->false) ;
    
    public TransactionalMutex(Lock lock) {
        this.lock = lock ;
    }
    
    @Override
    public void begin(ReadWrite readWrite) {
        lock.enterCriticalSection(false);       // Always take a write lock - i.e. exclusive.
        isInTransaction.set(true); 
    }

    @Override
    public void commit() {
        end() ;
    }

    @Override
    public void abort() {
        end() ;
    }

    @Override
    public boolean isInTransaction() { 
        return isInTransaction == null ? false : isInTransaction.get() ;
    }
    
    @Override
    public void end() {
        if ( isInTransaction() ) {
            isInTransaction.set(false);
            lock.leaveCriticalSection();
        }
        isInTransaction.remove();
    }
}
