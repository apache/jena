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

package org.apache.jena.dboe.base.file;

import org.apache.jena.atlas.lib.Closeable ;
import org.apache.jena.atlas.lib.Sync ;
import org.apache.jena.dboe.base.block.Block;

/**
 * Interface to concrete storage - read and write Blocks, addressed by id. 
 * Suitable for memory mapped I/O (returns
 * internally allocated space for read, not provided from outside; write() can
 * insist the block written comes from allocate()).
 * This interface can also be backed by an in-memory implementation 
 * ({@link BlockAccessMem}, {@link BlockAccessByteArray}).
 * 
 * This is wrapped in a BlockMgr to provide a higher level abstraction.
 * 
 * @see BufferChannel
 */
public interface BlockAccess extends Sync, Closeable
{
    public Block allocate(int size) ;
    
    public Block read(long id) ;
    
    public void write(Block block) ;
    
    public void overwrite(Block block) ;
    
    public boolean isEmpty() ; 
    
    /* The limit of the current allocation space.
     * Allocated blocks have ids in [0, allocBoundary).
     * Allocation units need not be in +1 increments.
      */  
    public long allocBoundary() ;

    /** Reset the allocation space (i.e. truncate).
     * The boundary should be a number obtained from a previous
     * call of allocLimit. It can not be greater than the current
     * allocation boundary.
     */
    public void resetAllocBoundary(long boundary) ;
    
    public boolean valid(long id) ;

    public String getLabel() ;
}
