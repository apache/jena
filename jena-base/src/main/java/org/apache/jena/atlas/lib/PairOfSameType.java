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

package org.apache.jena.atlas.lib;

import java.util.function.*;

public class PairOfSameType<T> extends Pair<T, T> {

    public PairOfSameType(T a, T b) {
        super(a, b);
    }

    public void forEach(Consumer<T> op) {
        op.accept(a);
        op.accept(b);
    }

    public boolean both(Function<T, Boolean> op) {
        return apply(Boolean::logicalAnd, op);
    }

    public boolean either(Function<T, Boolean> op) {
        return apply(Boolean::logicalOr, op);
    }

    public <S, X> S apply(BiFunction<X, X, S> f, Function<T, X> op) {
        return f.apply(op.apply(a), op.apply(b));
    }
}
