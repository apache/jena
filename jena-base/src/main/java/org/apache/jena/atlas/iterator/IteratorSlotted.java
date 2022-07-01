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

package org.apache.jena.atlas.iterator;

import java.util.NoSuchElementException ;

import org.apache.jena.atlas.lib.Lib ;

/** An Iterator with a one slot lookahead. */
public abstract class IteratorSlotted<T> implements IteratorCloseable<T> {
    private boolean finished = false;
    private boolean slotIsSet = false;
    private T slot = null;

    protected IteratorSlotted() {}

    // -------- The contract with the subclasses

    /** Next element of null for "none" */
    protected abstract T moveToNext();

    /** Can return true here then null from moveToNext() to indicate end. */
    protected abstract boolean hasMore();

    /** Close the iterator. */
    protected void closeIterator() {}

    // -------- The contract with the subclasses

    protected boolean isFinished() {
        return finished;
    }

    @Override final
    public boolean hasNext() {
        if ( finished )
            return false;
        if ( slotIsSet )
            return true;

        boolean r = hasMore();
        if ( !r ) {
            close();
            return false;
        }

        slot = moveToNext();
        if ( slot == null ) {
            close();
            return false;
        }

        slotIsSet = true;
        return true;
    }

    @Override final
    public T next() {
        if ( !hasNext() )
            throw new NoSuchElementException(Lib.className(this));

        T obj = slot;
        slot = null;
        slotIsSet = false;
        return obj;
    }

    /** Look at the next element - returns null when there is no element */
    public final T peek() {
        return peek(null);
    }

    /** Look at the next element - returns dft when there is no element */
    public final T peek(T dft) {
        hasNext();
        if ( !slotIsSet )
            return dft;
        return slot;
    }

    @Override
    public final void close() {
        if ( finished )
            return;
        closeIterator();
        slotIsSet = false;
        slot = null;
        finished = true;
    }
}
