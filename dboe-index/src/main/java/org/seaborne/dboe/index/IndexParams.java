/*
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *  See the NOTICE file distributed with this work for additional
 *  information regarding copyright ownership.
 */

package org.seaborne.dboe.index;

import org.seaborne.dboe.base.block.BlockParams ;
import org.seaborne.dboe.base.block.FileMode ;

public interface IndexParams extends BlockParams {
    /** File Mode */
    @Override public FileMode getFileMode() ;

    /** Block size - this is only configurable when the on-disk are created.
     * After that, the same value as at creation must be used each time.
     */
    @Override public Integer getBlockSize() ;
    
    /** Block read cache size (mmap'ed files do not have a block cache)*/
    @Override public Integer getBlockReadCacheSize() ;
    
    /** Block write cache size (mmap'ed files do not have a block cache)*/
    @Override public Integer getBlockWriteCacheSize() ;
}
