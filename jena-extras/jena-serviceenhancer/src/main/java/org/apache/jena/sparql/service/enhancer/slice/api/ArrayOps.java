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

package org.apache.jena.sparql.service.enhancer.slice.api;

import java.lang.reflect.Array;
import java.util.function.IntFunction;

import org.apache.jena.sparql.service.enhancer.slice.impl.ArrayOpsObject;

/**
 * Abstraction for arrays of objects and primitive types (most prominently byte).
 */
public interface ArrayOps<A> {
    A create(int size);

    Object get(A array, int index);
    void set(A array, int index, Object value);

    int length(A array);

    void fill(A array, int offset, int length, Object value);
    void copy(A src, int srcPos, A dest, int destPos, int length);
    Object getDefaultValue();

    @SuppressWarnings("unchecked")
    default void fillRaw(Object array, int offset, int length, Object value) {
        fill((A)array, offset, length, value);
    }

    @SuppressWarnings("unchecked")
    default void copyRaw(Object src, int srcPos, Object dest, int destPos, int length) {
        copy((A)src, srcPos, (A)dest, destPos, length);
    }

    @SuppressWarnings("unchecked")
    default Object getRaw(Object array, int index) {
        return get((A)array, index);
    }

    @SuppressWarnings("unchecked")
    default void setRaw(Object array, int index, Object value) {
        set((A)array, index, value);
    }

    @SuppressWarnings("unchecked")
    default void lengthRaw(Object array) {
        length((A)array);
    }

    // TODO Cache with a ClassInstanceMap?
    @SuppressWarnings("unchecked")
    public static <T> ArrayOpsObject<T> createFor(Class<T> componentType) {
        return new ArrayOpsObject<>(size -> (T[])Array.newInstance(componentType, size));
    }

    public static <T> ArrayOpsObject<T> createFor(IntFunction<T[]> arrayConstructor) {
        return new ArrayOpsObject<>(arrayConstructor);
    }

    public static final ArrayOpsObject<Object> OBJECT = createFor(Object.class);
}
