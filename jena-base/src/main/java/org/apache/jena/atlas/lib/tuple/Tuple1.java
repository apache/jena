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
 * A tuple of 1 item.
 */
public class Tuple1<X> extends TupleBase<X> {
    protected final X x1 ;

    protected Tuple1(X x1) {
        this.x1 = x1 ;
    }

    @Override
    public final X get(int i) {
        if ( i == 0 )
            return x1 ;
        throw new IndexOutOfBoundsException() ;
    }

    @Override
    public final int len() {
        return 1 ;
    }

    @Override
    public <Y> Tuple<Y> map(Function<X,Y> function) { return new Tuple1<>(function.apply(x1)) ;}

    @Override
    public boolean contains(X item) {
        if ( Objects.equals(x1, item) ) return true;
        return false;
    }
}
