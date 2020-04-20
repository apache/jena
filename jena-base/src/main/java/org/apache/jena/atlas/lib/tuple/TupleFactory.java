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

import java.util.List ;

/** Tuple creation */
public class TupleFactory {

    private TupleFactory() {}

    /** Create a Tuple */
    @SafeVarargs
    public static <X> Tuple<X> tuple(X...xs) {
        switch(xs.length) {
            case 0 : return create0() ;
            case 1 : return create1(xs[0]) ;
            case 2 : return create2(xs[0], xs[1]) ;
            case 3 : return create3(xs[0], xs[1], xs[2]) ;
            case 4 : return create4(xs[0], xs[1], xs[2], xs[3]) ;
            case 5 : return create5(xs[0], xs[1], xs[2], xs[3], xs[4]) ;
            case 6 : return create6(xs[0], xs[1], xs[2], xs[3], xs[4], xs[5]) ;
            case 7 : return create7(xs[0], xs[1], xs[2], xs[3], xs[4], xs[5], xs[6]) ;
            case 8 : return create8(xs[0], xs[1], xs[2], xs[3], xs[4], xs[5], xs[6], xs[7]) ;
            default:
                // No need to copy.
                return asTuple(xs) ;
        }
    }

    /** Create a Tuple from an array */
    public static <X> Tuple<X> create(X[] xs) {
        switch(xs.length) {
            case 0 : return create0() ;
            case 1 : return create1(xs[0]) ;
            case 2 : return create2(xs[0], xs[1]) ;
            case 3 : return create3(xs[0], xs[1], xs[2]) ;
            case 4 : return create4(xs[0], xs[1], xs[2], xs[3]) ;
            case 5 : return create5(xs[0], xs[1], xs[2], xs[3], xs[4]) ;
            case 6 : return create6(xs[0], xs[1], xs[2], xs[3], xs[4], xs[5]) ;
            case 7 : return create7(xs[0], xs[1], xs[2], xs[3], xs[4], xs[5], xs[6]) ;
            case 8 : return create8(xs[0], xs[1], xs[2], xs[3], xs[4], xs[5], xs[6], xs[7]) ;
            default:
                return TupleN.create(xs) ;
        }
    }

    /** Treat an array as a Tuple.  The array must not be mutated */
    public static <X> Tuple<X> asTuple(X[] xs) {
        return TupleN.wrap(xs) ;
    }

    /** Create a Tuple from a list */
    public static <X> Tuple<X> create(List<X> xs) {
        @SuppressWarnings("unchecked")
        X[] xa = (X[])(new Object[xs.size()]) ;
        return TupleFactory.tuple(xs.toArray(xa)) ;
    }

    /** Create a Tuple of length 0 */
    public static <X> Tuple0<X> create0() {
        return new Tuple0<>() ;
    }

    /** Create a Tuple of length 1 */
    public static <X> Tuple1<X> create1(X x1) {
        return new Tuple1<>(x1) ;
    }

    /** Create a Tuple of length 2 */
    public static <X> Tuple2<X> create2(X x1, X x2) {
        return new Tuple2<>(x1, x2) ;
    }

    /** Create a Tuple of length 3 */
    public static <X> Tuple3<X> create3(X x1, X x2, X x3) {
        return new Tuple3<>(x1, x2, x3) ;
    }

    /** Create a Tuple of length 4 */
    public static <X> Tuple4<X> create4(X x1, X x2, X x3, X x4) {
        return new Tuple4<>(x1, x2, x3, x4) ;
    }

    /** Create a Tuple of length 5 */
    public static <X> Tuple5<X> create5(X x1, X x2, X x3, X x4, X x5) {
        return new Tuple5<>(x1, x2, x3, x4, x5) ;
    }

    /** Create a Tuple of length 6 */
    public static <X> Tuple6<X> create6(X x1, X x2, X x3, X x4, X x5, X x6) {
        return new Tuple6<>(x1, x2, x3, x4, x5, x6) ;
    }

    /** Create a Tuple of length 7 */
    public static <X> Tuple7<X> create7(X x1, X x2, X x3, X x4, X x5, X x6, X x7) {
        return new Tuple7<>(x1, x2, x3, x4, x5, x6, x7) ;
    }

    /** Create a Tuple of length 8 */
    public static <X> Tuple8<X> create8(X x1, X x2, X x3, X x4, X x5, X x6, X x7, X x8) {
        return new Tuple8<>(x1, x2, x3, x4, x5, x6, x7, x8) ;
    }
}
