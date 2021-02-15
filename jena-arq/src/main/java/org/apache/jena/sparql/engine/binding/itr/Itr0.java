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

package org.apache.jena.sparql.engine.binding.itr;

import java.util.Iterator;
import java.util.NoSuchElementException;

/** Iterator of 0 objects */
class Itr0<X> implements Iterator<X> {
    // Same as Iter.nullIterator but named for tracking and development usage.
    static Itr0<?> NULL = new Itr0<>();
    @SuppressWarnings("unchecked")
    public static <T> Iterator<T> itr0() {
        return (Itr0<T>)NULL;
    }

    Itr0() { }

    @Override
    public boolean hasNext() {
        return false;
    }

    @Override
    public X next() {
        throw new NoSuchElementException();
    }

    @Override
    public void remove() { throw new UnsupportedOperationException("Itr0.remove"); }
}
