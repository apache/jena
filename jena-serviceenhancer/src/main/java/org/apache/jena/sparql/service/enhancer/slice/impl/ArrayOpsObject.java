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

package org.apache.jena.sparql.service.enhancer.slice.impl;

import java.util.Arrays;
import java.util.function.IntFunction;

import org.apache.jena.sparql.service.enhancer.slice.api.ArrayOps;

/**
 * Class for abstracting (bulk) operations on arrays.
 * This indirection allows for uniform handling of arrays of primitive and non-primitive types.
 */
public class ArrayOpsObject<T>
    implements ArrayOps<T[]>
{
    // When operations operate on that many items then use the system functions
    public static final int SYSTEM_THRESHOLD = 16;
    protected IntFunction<T[]> arrayConstructor;

    public ArrayOpsObject(IntFunction<T[]> arrayConstructor) {
        super();
        this.arrayConstructor = arrayConstructor;
    }

    @Override
    public T[] create(int size) {
        return arrayConstructor.apply(size);
    }

    @Override
    public Object getDefaultValue() {
        return null;
    }

    @Override
    public Object get(Object[] array, int index) {
        return array[index];
    }

    @Override
    public void set(Object[] array, int index, Object value) {
        array[index] = value;
    }

    @Override
    public void fill(Object[] array, int offset, int length, Object value) {
        if (length < SYSTEM_THRESHOLD) {
            for (int i = 0; i < length; ++i) {
                array[offset + i] = value;
            }
        } else {
            Arrays.fill(array, offset, length, value);
        }
    }

    @Override
    public void copy(Object[] src, int srcPos, Object[] dest, int destPos, int length) {
        if (length < SYSTEM_THRESHOLD) {
            for (int i = 0; i < length; ++i) {
                dest[destPos + i] = src[srcPos + i];
            }
        } else {
            System.arraycopy(src, srcPos, dest, destPos, length);
        }
    }

    @Override
    public int length(Object[] array) {
        return array.length;
    }
}
