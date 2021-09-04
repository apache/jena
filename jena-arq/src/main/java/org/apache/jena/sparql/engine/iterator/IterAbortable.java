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

package org.apache.jena.sparql.engine.iterator;

import java.util.Iterator;

import org.apache.jena.atlas.iterator.IteratorCloseable;
import org.apache.jena.atlas.iterator.IteratorWrapper;
import org.apache.jena.query.QueryCancelledException;

/**
 * Plain iterator version to add "abort" functionality.
 * Iterator that adds an abort operation which can be called at any time,
 * including from another thread, and causes the iterator to throw an exception
 * when next touched (hasNext, next).
 */
public class IterAbortable<T> extends IteratorWrapper<T> implements Abortable, IteratorCloseable<T> {
    private volatile boolean abortFlag = false;
    private boolean haveAborted = false;

    public IterAbortable(Iterator<T> iterator) {
        super(iterator);
    }

    /** Can call asynchronously at any time */
    @Override
    public void abort() {
        abortFlag = true;
    }

    private void execAbort() {
        if ( ! haveAborted )
            close();
        haveAborted = true;
    }

    @Override
    public boolean hasNext() {
        if ( abortFlag ) {
            execAbort();
            throw new QueryCancelledException();
        }
        return iterator.hasNext();
    }

    @Override
    public T next() {
        if ( abortFlag ) {
            execAbort();
            throw new QueryCancelledException();
        }
        return iterator.next();
    }

    @Override
    public void remove() {
        if ( abortFlag ) {
            execAbort();
            throw new QueryCancelledException();
        }
        iterator.remove();
    }
}
