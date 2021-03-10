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

import java.util.Arrays ;
import java.util.Objects;
import java.util.function.Function;

/** A Tuple of N items */
public class TupleN<X> extends TupleBase<X> {
    private final X[] tuple ;

    /** Create a TupleN - safely copy the input */
    @SafeVarargs
    public static <X> TupleN<X> create(X... xs) {
        X[] xs2 = Arrays.copyOf(xs, xs.length) ;
        return new TupleN<>(xs2) ;
    }

    // When the array will not be modified.
    /*package*/ static <X> TupleN<X> wrap(X[] xs) {
        return new TupleN<>(xs) ;
    }

    /** Put a TupleN wrapper around a X[].
     *  The array must not be subsequently modified.
     *  The statics {@link #create} and {@link wrap} determine whether to copy or not.
     */
    protected TupleN(X[] xs) {
        tuple = xs ;
    }

    @Override
    public final X get(int i) {
        return tuple[i] ;
    }

    @Override
    public int len() {
        return tuple.length;
    }

    @Override
    public <Y> Tuple<Y> map(Function<X, Y> function) {
        int N = tuple.length;
        @SuppressWarnings("unchecked")
        Y[] tuple2 = (Y[])new Object[N];
        for ( int i = 0 ; i < N ; i++ ) {
            tuple2[i] = function.apply(tuple[i]);
        }
        return wrap(tuple2);
    }


    @Override
    public boolean contains(X item) {
        int N = tuple.length;
        for ( int i = 0 ; i < N ; i++ ) {
            if ( Objects.equals(tuple[i], item) )
                return true;
        }
        return false;
    }

}
