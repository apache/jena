/*
 * Licensed to the Apache Software Foundation (ASF) under one or more contributor license agreements. See the NOTICE
 * file distributed with this work for additional information regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the
 * License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package org.apache.jena.atlas.lib.tuple;

import java.util.function.Function;

/**
 * Represents a function that accepts three arguments and produces a result. This is a three-arity specialization of
 * {@link Function}.
 * <p>
 * This is a functional interface whose functional method is {@link #apply}.
 *
 * @param <X> the type of the first argument to the function
 * @param <Y> the type of the second argument to the function
 * @param <Z> the type of the second argument to the function
 * @param <W> the type of the result of the function
 * @see Function
 */
@FunctionalInterface
public interface TriFunction<X, Y, Z, W> {
    W apply(final X x, final Y y, final Z z);

    /**
     * A specialization of {@link TriFunction} in which all arguments are of the same type.
     *
     * @param <X> the type of all arguments
     * @param <Z> the type of the result of the operation
     */
    @FunctionalInterface
    static interface TriOperator<X, Z> extends TriFunction<X, X, X, Z> {}

}