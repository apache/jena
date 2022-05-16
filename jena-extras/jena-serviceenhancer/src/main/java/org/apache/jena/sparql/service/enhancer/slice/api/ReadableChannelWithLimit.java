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

import org.apache.jena.ext.com.google.common.primitives.Ints;

public class ReadableChannelWithLimit<A>
    implements ReadableChannel<A>
{
    protected ReadableChannel<A> delegate;
    protected long limit;
    protected long remaining;

    public ReadableChannelWithLimit(ReadableChannel<A> backend, long limit) {
        super();
        this.delegate = backend;
        this.limit = limit;
        this.remaining = limit;
    }

    public ReadableChannel<A> getDelegate() {
        return delegate;
    }

    @Override
    public ArrayOps<A> getArrayOps() {
        return getDelegate().getArrayOps();
    }

    @Override
    public void close() throws IOException {
        getDelegate().close();
    }

    @Override
    public boolean isOpen() {
        return getDelegate().isOpen();
    }

    @Override
    public int read(A array, int position, int length) throws IOException {
        int result;
        if (remaining <= 0) {
            result = -1;
        } else {
            int n = Math.min(Ints.saturatedCast(remaining), length);
            result = getDelegate().read(array, position, n);

            if (result > 0) {
                remaining -= result;
            }
        }

        return result;
    }
}
