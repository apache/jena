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

import java.util.Iterator;
import java.util.Objects;

import org.apache.jena.ext.com.google.common.collect.AbstractIterator;
import org.apache.jena.ext.com.google.common.collect.PeekingIterator;

/** The atlas version does active read ahead; this one only fetches data when needed */
public class PeekIteratorLazy<T>
    extends AbstractIterator<T> // AbstractIterator already has a public peek method
    implements PeekingIterator<T>
{
    protected Iterator<T> delegate;

    public PeekIteratorLazy(Iterator<T> delegate) {
        super();
        this.delegate = Objects.requireNonNull(delegate);
    }

    public static <T> PeekIteratorLazy<T> create(Iterator<T> it) {
        PeekIteratorLazy<T> result = it instanceof PeekIteratorLazy
                ? (PeekIteratorLazy<T>)it
                : new PeekIteratorLazy<>(it);
        return result;
    }

    @Override
    protected T computeNext() {
        T result = delegate.hasNext()
                ? delegate.next()
                : endOfData();
        return result;
    }
}
