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

package org.apache.jena.sparql.service.enhancer.impl.util;

import java.io.Closeable;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * An abstract base class for iterating over containers of unknown size. This
 * works by prefetching junks of the container: Whenever the iterator reaches
 * the end of a chunk, the method "myPrefetch" is called.
 *
 *
 * Note that once the iterator is finished (myPrefetch returned null),
 * myPrefetch will never be called again. This means, that if myPrefetch is
 * called, the iterator hasn't reached its end yet.
 *
 */
public abstract class SinglePrefetchIterator<T>
    implements Iterator<T>, Closeable
{
    private T	    current		= null;
    private boolean finished	= false;

    private boolean advance     = true;

    private boolean wasNextCalled = false;

    protected abstract T prefetch()
        throws Exception;

    protected SinglePrefetchIterator()
    {
    }

    protected T finish()
    {
        this.finished = true;

        close();
        return null;
    }

    private void _prefetch()
    {
        try {
            current = prefetch();
        }
        catch(Exception e) {
            current = null;
            throw new RuntimeException("Prefetching data failed. Reason: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean hasNext()
    {
        wasNextCalled = false;
        if (advance) {
            _prefetch();
            advance = false;
        }

        return finished == false;
    }

    @Override
    public T next()
    {
        wasNextCalled = true;

        if (finished) {
            throw new NoSuchElementException();
        }

        if (advance) {
            _prefetch();
        }

        advance = true;
        return current;
    }

    public T current() {
        T result;
        if (hasNext()) {
            result = current;
        } else {
            throw new NoSuchElementException();
        }
        return result;
    }

    /** Whether the next call to next() or hasNext() will trigger loading the next element */
    protected boolean willAdvance() {
        return advance;
    }

    protected boolean wasNextCalled() {
        return wasNextCalled;
    }

    protected boolean wasHasNextCalled() {
        return !finished && !wasNextCalled && !advance;
    }

    /**
     * An iterator must always free all resources once done with iteration.
     * However, if iteration is aborted, this method should be called.
     */
    @Override
    public void close() {
        // Nothing to do
    }

    @Override
    public final void remove() {
        if (!wasNextCalled) {
            throw new RuntimeException("remove must not be called after .hasNext() - invoke .next() first");
        }

        doRemove(current);
    }

    /**
     * @param item The item being removed
     */
    protected void doRemove(T item) {
        throw new UnsupportedOperationException("Not supported.");
    }
}
