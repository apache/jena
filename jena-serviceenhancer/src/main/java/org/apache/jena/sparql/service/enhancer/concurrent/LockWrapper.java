/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 *   SPDX-License-Identifier: Apache-2.0
 */

package org.apache.jena.sparql.service.enhancer.concurrent;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;

public abstract class LockWrapper
    implements Lock
{
    protected abstract Lock getDelegate();

    @Override
    public void lock() {
        getDelegate().lock();
    }

    @Override
    public void lockInterruptibly() throws InterruptedException {
        getDelegate().lockInterruptibly();
    }

    @Override
    public boolean tryLock() {
        return getDelegate().tryLock();
    }

    @Override
    public boolean tryLock(long time, TimeUnit unit) throws InterruptedException {
        return getDelegate().tryLock();
    }

    @Override
    public void unlock() {
        getDelegate().unlock();
    }

    @Override
    public Condition newCondition() {
        return getDelegate().newCondition();
    }
}
