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

/**
 * BufferLike is a common interface for Buffer and RangeBuffer.
 * Even though both specializations have most methods in common, the semantics differ in subtle ways:
 * A buffer must support reading any slice of data within its capacity.
 * A range buffer only allows for reading within valid ranges and raises an exception upon violation.
 */
public interface BufferLike<A>
    extends ArrayWritable<A>, ArrayReadable<A>
{
    /** Buffers with 'unlimited' capacity should return Long.MAX_VALUE */
    long getCapacity();

    // The original API also some additional operations; they may be needed for disk-based storage
    // BufferLike<A> slice(long offset, long length);
}
