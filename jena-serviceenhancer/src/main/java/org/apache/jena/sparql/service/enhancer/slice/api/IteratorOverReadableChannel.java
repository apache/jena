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

import java.io.IOException;

import com.google.common.base.Preconditions;
import com.google.common.collect.AbstractIterator;

import org.apache.jena.atlas.iterator.IteratorCloseable;

public class IteratorOverReadableChannel<T>
    extends AbstractIterator<T>
    implements IteratorCloseable<T>
{
    protected ReadableChannel<T[]> dataStream;

    protected ArrayOps<T[]> arrayOps;

    // We need to use Object because assigning arrays of primitive typesto T[]
    // raises a class cast exception
    protected Object array;
    protected int arrayLength;

    protected int currentOffset;
    protected int currentDataLength;

    /**
     *
     * @param arrayOps
     * @param dataStream
     * @param internalBufferSize The number of items to read from the dataStream at once.
     */
    public IteratorOverReadableChannel(ArrayOps<T[]> arrayOps, ReadableChannel<T[]> dataStream, int internalBufferSize) {
        super();
        Preconditions.checkArgument(internalBufferSize >= 0, "Internal buffer size must be greater than 0");

        this.arrayOps = arrayOps;
        this.dataStream = dataStream;
        this.arrayLength = internalBufferSize;
        this.array = arrayOps.create(internalBufferSize);

        this.currentDataLength = 0;

        // Initialized at end of buffer in order to trigger immediate read on next computeNext() call.
        this.currentOffset = 0;
    }

    @Override
    protected T computeNext() {
        if (currentOffset >= currentDataLength) {
            try {
                currentDataLength = dataStream.readRaw(array, 0, arrayLength);
                currentOffset = 0;
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        Object tmp;
        if (currentDataLength == -1) {
            tmp = endOfData();
        } else {
            tmp = arrayOps.getRaw(array, currentOffset);
            if (tmp == null) {
                throw new NullPointerException("Unexpected null value");
            }
        }

        ++currentOffset;

        @SuppressWarnings("unchecked")
        T result = (T)tmp;
        return result;
    }

    @Override
    public void close() {
        try {
            dataStream.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
