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

package com.hp.hpl.jena.tdb.base.file;

import java.nio.ByteBuffer ;

import org.apache.jena.atlas.lib.Closeable ;

/**
 * An allocator for retrieving ByteBuffers of a given size.
 */
public interface BufferAllocator extends Closeable
{
    /**
     * Allocate and return a ByteBuffer of the given size
     * @param capacity the desired size of the ByteBuffer
     * @return a ByteBuffer with the capacity set to the desired size
     */
    public ByteBuffer allocate(int capacity);
    
    /**
     * Call this method when you are finished with all of the ByteBuffers
     * retrieved from allocate.  The BufferAllocator is then free to reuse
     * memory that was previously handed out.
     */
    public void clear();
}
