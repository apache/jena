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

import java.util.NoSuchElementException;

/**
 * Iterator base class that adds "closeable" to an iterator.
 * close() is always called, whether explicitly or by end-of-iterator.
 */
public abstract class IteratorBase<X> implements IteratorCloseable<X> {

    private boolean hasClosed = false;

    protected IteratorBase() {}

    protected abstract boolean hasNextElt();
    protected abstract X nextElt();
    protected abstract void onFinish();

    @Override
    final
    public boolean hasNext() {
        boolean b = hasNextElt();
        if ( ! b )
            close();
        return b;
    }

    @Override
    final
    public X next() {
        try {
            return nextElt();
        } catch(NoSuchElementException ex) {
            close();
            throw ex;
        }
    }

    @Override
    public void close() {
        if ( ! hasClosed )
            onFinish();
        hasClosed = true;
    }
}
