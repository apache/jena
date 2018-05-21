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

import org.apache.jena.atlas.lib.FileOps ;
import org.apache.jena.dboe.ConfigTestDBOE;
import org.apache.jena.dboe.base.block.BlockMgr;
import org.apache.jena.dboe.base.block.BlockMgrFactory;
import org.apache.jena.dboe.base.block.BlockMgrFileAccess;
import org.apache.jena.dboe.base.file.BlockAccess;
import org.apache.jena.dboe.base.file.BlockAccessDirect;
import org.junit.AfterClass;
import org.junit.BeforeClass;

public class TestBlockMgrDirect extends AbstractTestBlockMgr
{
    static final String filename = ConfigTestDBOE.getTestingDir()+"/block-mgr" ;
    
    @BeforeClass static public void remove1() { FileOps.delete(filename) ; } 
    @AfterClass  static public void remove2() { FileOps.delete(filename) ; }
    
    @Override
    protected BlockMgr make()
    { 
        // Make directly - no wrapper, no cache, no free block mgt.
        FileOps.delete(filename) ;
        BlockAccess file = new BlockAccessDirect(filename, BlkSize) ;
        BlockMgr mgr = new BlockMgrFileAccess(file, BlkSize) ;
        if ( BlockMgrFactory.AddTracker )
            mgr = BlockMgrFactory.tracker(mgr) ;
        return mgr ;
    }
}
