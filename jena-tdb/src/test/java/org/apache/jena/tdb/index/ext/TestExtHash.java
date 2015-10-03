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

package org.apache.jena.tdb.index.ext;

//import static ext.ExtHashTestBase.* ; 
import org.apache.jena.tdb.base.block.BlockMgr ;
import org.apache.jena.tdb.base.block.BlockMgrFactory ;
import org.apache.jena.tdb.base.file.PlainFileMem ;
import org.apache.jena.tdb.base.record.RecordFactory ;
import org.apache.jena.tdb.index.AbstractTestIndex ;
import org.apache.jena.tdb.index.Index ;
import org.apache.jena.tdb.index.ext.ExtHash ;
import org.apache.jena.tdb.sys.SystemTDB ;
import org.junit.AfterClass;
import org.junit.BeforeClass;

public class TestExtHash extends AbstractTestIndex
{

    static boolean originalNullOut ; 
    static boolean b ; 

    @BeforeClass static public void setup()
    {
        originalNullOut = SystemTDB.NullOut ;
        SystemTDB.NullOut = true ;
        ExtHash.Checking = true ;
        ExtHash.Logging = false ;
        b = BlockMgrFactory.AddTracker ;
        BlockMgrFactory.AddTracker = false ;
    }
    
    @AfterClass static public void teardown()
    {
        BlockMgrFactory.AddTracker = b  ;
        SystemTDB.NullOut = originalNullOut ;
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
