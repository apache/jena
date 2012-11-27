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

package com.hp.hpl.jena.tdb.base.block;

import java.util.Iterator ;

import org.apache.jena.atlas.lib.FileOps ;
import org.slf4j.Logger ;

public abstract class BlockMgrBase implements BlockMgr
{
    protected final int blockSize ;
    private String label ;
    protected abstract Logger log() ;

    // Fixed size, fixed block type.
    protected BlockMgrBase(String label, int blockSize)
    {
        this.label = FileOps.basename(label) ;
        this.blockSize = blockSize ;
    }

    @Override
    public final Block allocate(int blkSize)
    {
        if ( blkSize > 0 && blkSize != this.blockSize )
            throw new BlockException("Fixed blocksize BlockMgr: request= "+blkSize+"  fixed size="+this.blockSize) ;
        return allocate() ;
    }
    
    protected abstract Block allocate() ;
    
    @Override final public String getLabel() { return label ; } 

    @Override public void beginIterator(Iterator<?> iter)   {}
    @Override public void endIterator(Iterator<?> iter)     {}
    @Override public void endUpdate()       {}
    @Override public void beginUpdate()     {}
    @Override public void beginRead()       {}
    @Override public void endRead()         {}
}
