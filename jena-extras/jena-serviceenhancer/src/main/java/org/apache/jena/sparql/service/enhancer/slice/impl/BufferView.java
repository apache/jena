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

import java.util.concurrent.locks.ReadWriteLock;

public interface BufferView<A> {
    RangeBuffer<A> getRangeBuffer();
    ReadWriteLock getReadWriteLock();

    /** A property to allow for quick checking of dirty buffers. Mainly intended for sync-to-disk.
     * Every write operation that introduces a change must increment the generation.
     * Write operations may check whether the written data matches exactly the prior one.
     * If there is no change then the generation may remain unchanged. */
    long getGeneration();
}
