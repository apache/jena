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

package org.seaborne.dboe.index.ext;

//import static ext.ExtHashTestBase.* ; 
import org.junit.AfterClass ;
import org.junit.BeforeClass ;
import org.seaborne.dboe.base.block.BlockMgr ;
import org.seaborne.dboe.base.block.BlockMgrFactory ;
import org.seaborne.dboe.base.file.PlainFileMem ;
import org.seaborne.dboe.base.record.RecordFactory ;
import org.seaborne.dboe.index.Index ;
import org.seaborne.dboe.index.ext.ExtHash ;
import org.seaborne.dboe.index.test.AbstractTestIndex ;
import org.seaborne.dboe.sys.SystemIndex ;

public class TestExtHash extends AbstractTestIndex
{

    static boolean originalNullOut ; 
    static boolean b ; 
    
    @BeforeClass static public void setup()
    {
        originalNullOut = SystemIndex.getNullOut() ;
        SystemIndex.setNullOut(true) ;    
        ExtHash.Checking = true ;
        ExtHash.Logging = false ;
        b = BlockMgrFactory.AddTracker ;
        BlockMgrFactory.AddTracker = false ;
    }
    
    @AfterClass static public void teardown()
    {
        BlockMgrFactory.AddTracker = b  ;
        SystemIndex.setNullOut(originalNullOut) ;   
    }

    @Override
    protected Index makeIndex(int kLen, int vLen)
    {
        RecordFactory factory = new RecordFactory(kLen, vLen) ;
        BlockMgr mgr = BlockMgrFactory.createMem("EXT", 32) ;
        ExtHash eHash = new ExtHash(new PlainFileMem(), factory, mgr) ;
        return eHash ;
    }
}
