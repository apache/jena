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
import java.nio.channels.Channel;

/**
 * A data stream allows for repeated retrieval of arrays of consecutive items.
 * Data streams can be seen as a low level generalizaton / unification of Iterators and InputStreams.
 *
 * Akin to an InputStream, the {@link ReadableChannel} interface does not provide a seek() method.
 * Usually there should be another factory that creates data streams
 * for given offsets. The reason is, that a sequential reader is typically backed by a stream of items
 * (such as a http response, or a sql/sparql result set) and that stream needs to be re-created when
 * jumping to arbitrary offsets.
 *
 * @param <A> The array type for transferring data in blocks
 */
public interface ReadableChannel<A>
    extends HasArrayOps<A>, Channel
{
    /**
     * Read method following the usual InputStream protocol.
     *
     * @param array The array into which to put the read data
     * @param position Offset into array where to start writing
     * @param length Maximum number of items to read.
     * @return The number of items read. Return -1 if end of data was reached, and 0 iff length was 0.
     *
     * @throws IOException
     */
    int read(A array, int position, int length) throws IOException;

    @SuppressWarnings("unchecked")
    default int readRaw(Object array, int position, int length) throws IOException {
        return read((A)array, position, length);
    }
}
