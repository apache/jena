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

package org.apache.jena.sparql.service.enhancer.impl.util.iterator;

import java.util.NoSuchElementException;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.jena.atlas.lib.Lib;

public abstract class AbstractAbortableIterator<T>
    extends AbortableIteratorBase<T>
{
    private boolean slotIsSet = false;
    private boolean hasMore = true;
    private T slot = null;

    public AbstractAbortableIterator() {
        super();
    }

    public AbstractAbortableIterator(AtomicBoolean cancelSignal) {
        super(cancelSignal);
    }

    @Override
    protected final boolean hasNextBinding() {
        if ( slotIsSet )
            return true;

        if (!hasMore) {
            return false;
        }
    //    boolean r = hasMore();
    //    if ( !r ) {
    //        close();
    //        return false;
    //    }

        slot = moveToNext();
        // if ( slot == null ) {
        if (!hasMore) {
            close();
            return false;
        }

        slotIsSet = true;
        return true;
    }

    protected final T endOfData() {
        hasMore = false;
        return null;
    }

    @Override
    public final T moveToNextBinding() {
        if ( !hasNext() )
            throw new NoSuchElementException(Lib.className(this));

        T obj = slot;
        slot = null;
        slotIsSet = false;
        return obj;
    }

    @Override
    protected final void closeIterator() {
        // Called by QueryIterBase.close()
        slotIsSet = false;
        slot = null;

        closeIteratorActual();
    }

    protected abstract void closeIteratorActual();

//    @Override
//    protected void requestCancel() {
//    }

    /**
     * Method that must return the next non-null element.
     * A return value of null indicates that the iterator's end has been reached.
     */
    protected abstract T moveToNext();
}
