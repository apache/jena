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

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * Add an "onClose" action to an Iterator.
 */
public class IteratorOnClose<T> extends IteratorWrapper<T> implements IteratorCloseable<T> {
    private final Runnable closeHandler;
    private boolean hasClosed = false;

    public static <T> IteratorOnClose<T> atEnd(Iterator<T> iterator, Runnable closeHandler) {
        return new IteratorOnClose<>(iterator, closeHandler);
    }

    private IteratorOnClose(Iterator<T> iterator, Runnable closeHandler) {
        super(iterator);
        this.closeHandler = closeHandler;
    }

    @Override
    public boolean hasNext() {
        if( hasClosed )
            return false;
        boolean b = super.hasNext();
        if ( !b )
            close();
        return b;
    }

    @Override
    public T next() {
        try {
            return get().next();
        } catch (NoSuchElementException ex) {
            close();
            throw ex;
        }
    }

    @Override
    public void close() {
        if ( ! hasClosed ) {
            try {
                if ( closeHandler != null )
                    closeHandler.run();
            }
            finally { hasClosed = true; }
        }
        // Multiple calls possible.
        super.close();
    }
}
