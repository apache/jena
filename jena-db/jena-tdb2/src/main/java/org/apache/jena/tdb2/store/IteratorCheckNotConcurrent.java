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

package org.apache.jena.tdb2.store;

import static java.lang.String.format;

import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.jena.atlas.iterator.Iter;
import org.apache.jena.atlas.lib.Closeable;

/** Wrapper to check an iterator has not been used across some write operation. */
class IteratorCheckNotConcurrent<T> implements Iterator<T>, Closeable
{
    private Iterator<T> iter;
    private AtomicLong eCount;  // Needs fixing to be per thera in some way - either from the caller or here.
    private boolean finished = false;
    private long startEpoch;

    IteratorCheckNotConcurrent(Iterator<T> iter, AtomicLong eCount) {
        // Assumes correct locking to set up, i.e. eCount not changing
        // (writer on separate thread).
        this.iter = iter;
        this.eCount = eCount;
        this.startEpoch = eCount.get();
    }

    private void checkConcurrentModification() {
        if ( finished )
            return;

        long now = eCount.get();
        if ( now != startEpoch ) {
            policyError(format("Iterator: started at %d, now %d", startEpoch, now));
        }
    }

    private static void policyError(String message) {
        throw new ConcurrentModificationException(message);
    }

    @Override
    public boolean hasNext() {
        checkConcurrentModification();
        boolean b = iter.hasNext();
        if ( !b )
            close();
        return b;
    }

    @Override
    public T next() {
        checkConcurrentModification();
        try { return iter.next(); }
        catch (NoSuchElementException ex) {
            close();
            throw ex;
        }
    }

    @Override
    public void remove() {
        checkConcurrentModification();
        iter.remove();
        // Reset the epoch.
        startEpoch = eCount.get();
    }

    @Override
    public void close() {
        finished = true;
        Iter.close(iter);
    }
}