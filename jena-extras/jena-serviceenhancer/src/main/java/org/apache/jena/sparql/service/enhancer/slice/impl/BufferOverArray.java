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

import com.google.common.primitives.Ints;

import org.apache.jena.sparql.service.enhancer.slice.api.ArrayOps;

public class BufferOverArray<A>
    implements Buffer<A>
{
    protected ArrayOps<A> arrayOps;
    protected A array;

    public BufferOverArray(ArrayOps<A> arrayOps, int size) {
        this(arrayOps, arrayOps.create(size));
    }

    public BufferOverArray(ArrayOps<A> arrayOps, A array) {
        this.arrayOps = arrayOps;
        this.array = array;
    }

    public static <A> BufferOverArray<A> create(ArrayOps<A> arrayOps, int size) {
        return new BufferOverArray<>(arrayOps, size);
    }

    public static <A> BufferOverArray<A> create(ArrayOps<A> arrayOps, A array) {
        return new BufferOverArray<>(arrayOps, array);
    }

    @Override
    public void write(long offsetInBuffer, A arrayWithItemsOfTypeT, int arrOffset, int arrLength) {
        int offsetInBufferInt = Ints.checkedCast(offsetInBuffer);
        arrayOps.copy(arrayWithItemsOfTypeT, arrOffset, array, offsetInBufferInt, arrLength);
    }

    @Override
    public long getCapacity() {
        return arrayOps.length(array);
    }

    @Override
    public ArrayOps<A> getArrayOps() {
        return arrayOps;
    }

    @Override
    public int readInto(A tgt, int tgtOffset, long srcOffset, int length) {
        int capacityInt = arrayOps.length(array);
        int srcOffsetInt = Ints.checkedCast(srcOffset);
        int result = Math.max(Math.min(capacityInt - srcOffsetInt, length), 0);
        arrayOps.copy(array, srcOffsetInt, tgt, tgtOffset, result);

        if (result == 0 && length > 0) {
            result = -1;
        }

        return result;
    }

    @Override
    public void put(long offset, Object item) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Object get(long index) {
        throw new UnsupportedOperationException();
    }
}
