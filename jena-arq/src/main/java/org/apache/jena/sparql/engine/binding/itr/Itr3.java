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
import java.util.Objects;

/** Iterator of 3 objects */
class Itr3<X> implements Iterator<X> {
    private int idx;
    private final X elt1;
    private final X elt2;
    private final X elt3;

    Itr3(X x1, X x2, X x3) {
        idx = 0;
        elt1 = Objects.requireNonNull(x1);
        elt2 = Objects.requireNonNull(x2);
        elt3 = Objects.requireNonNull(x3);
    }

    @Override
    public boolean hasNext() {
        return idx < 3;
    }

    @Override
    public X next() {
        idx++;
        if ( idx == 1 ) return elt1;
        if ( idx == 2 ) return elt2;
        if ( idx == 3 ) return elt3;
        throw new NoSuchElementException();
    }
}
