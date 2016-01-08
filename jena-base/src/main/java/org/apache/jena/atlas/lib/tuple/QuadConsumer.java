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

import java.util.function.Consumer;

/**
 * Represents an operation that accepts four input arguments and returns no result. This is a four-arity specialization
 * of {@link Consumer}. Unlike most other functional interfaces, {@code QuadConsumer} is expected to operate via
 * side-effects.
 * <p>
 * This is a functional interface whose functional method is {@link #accept}.
 *
 * @param <W> the type of the first argument to the operation
 * @param <X> the type of the second argument to the operation
 * @param <Y> the type of the third argument to the operation
 * @param <Z> the type of the fourth argument to the operation
 * @see Consumer
 */
@FunctionalInterface
public interface QuadConsumer<W, X, Y, Z> {

    void accept(final W w, final X x, final Y y, final Z z);

    /**
     * A specialization of {@link QuadConsumer} in which all arguments are of the same type.
     *
     * @param <X> the type of all arguments
     */
    @FunctionalInterface
    static interface Consumer4<X> extends QuadConsumer<X, X, X, X> {}
}
