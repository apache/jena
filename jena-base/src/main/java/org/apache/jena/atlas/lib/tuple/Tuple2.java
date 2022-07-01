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

package org.apache.jena.atlas.lib.tuple;

import java.util.Objects;
import java.util.function.Function;

/**
 * A tuple of 2 items.
 */
public class Tuple2<X> extends TupleBase<X> {
    protected final X x1 ;
    protected final X x2 ;

    protected Tuple2(X x1, X x2) {
        this.x1 = x1 ;
        this.x2 = x2 ;
    }

    @Override
    public final X get(int i) {
        switch (i) {
            case 0: return x1 ;
            case 1: return x2 ;
        }
        throw new IndexOutOfBoundsException() ;
    }

    @Override
    public final int len() {
        return 2 ;
    }

    @Override
    public <Y> Tuple<Y> map(Function<X, Y> function) {
        return new Tuple2<>(
                function.apply(x1),
                function.apply(x2)
                );
    }

    @Override
    public boolean contains(X item) {
        if ( Objects.equals(x1, item) ) return true;
        if ( Objects.equals(x2, item) ) return true;
        return false;
    }
}
