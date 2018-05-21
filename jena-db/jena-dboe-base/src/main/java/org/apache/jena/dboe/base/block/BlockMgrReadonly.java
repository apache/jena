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

package org.apache.jena.dboe.base.block;

import org.apache.jena.dboe.DBOpEnvException;

public class BlockMgrReadonly extends BlockMgrWrapper
{
    public BlockMgrReadonly(BlockMgr blockMgr) {
        super(blockMgr) ;
    }

    @Override public void beginUpdate()                 { throw new DBOpEnvException("Read-only block manager") ; }
    @Override public void endUpdate()                   { throw new DBOpEnvException("Read-only block manager") ; }
    @Override public Block allocate(int blockSize)      { throw new DBOpEnvException("Read-only block manager") ; }
    @Override public Block getWrite(long id)            { throw new DBOpEnvException("Read-only block manager") ; }
    @Override public Block promote(Block block)         { throw new DBOpEnvException("Read-only block manager") ; }
    @Override public void write(Block block)            { throw new DBOpEnvException("Read-only block manager") ; }
    @Override public void overwrite(Block block)        { throw new DBOpEnvException("Read-only block manager") ; }
    @Override public void free(Block block)             { throw new DBOpEnvException("Read-only block manager") ; }
    
//    @Override 
//    public void sync()
//    {
//        blockMgr.sync() ;
//    }

    @Override public String toString()          { return "RO:"+super.toString() ; } 
}
