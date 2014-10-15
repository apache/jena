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

package com.hp.hpl.jena.tdb.index.bplustree;

import org.junit.AfterClass ;
import org.junit.BeforeClass ;

import com.hp.hpl.jena.tdb.base.record.RecordLib ;
import com.hp.hpl.jena.tdb.index.RangeIndex ;
import com.hp.hpl.jena.tdb.index.AbstractTestRangeIndex ;
import com.hp.hpl.jena.tdb.sys.SystemTDB ;

public class TestBPlusTree extends AbstractTestRangeIndex
{
    static boolean originalNullOut ; 
    @BeforeClass static public void beforeClass()
    {
        BPlusTreeParams.CheckingNode = true ;
        //BPlusTreeParams.CheckingTree = true ;   // Breaks with block tracking.
        originalNullOut = SystemTDB.NullOut ;
        SystemTDB.NullOut = true ;    
    }
    
    @AfterClass static public void afterClass()
    {
        SystemTDB.NullOut = originalNullOut ;    
    }

    
    @Override
    protected RangeIndex makeRangeIndex(int order, int minRecords)
    {
        BPlusTree bpt = BPlusTree.makeMem(order, minRecords, RecordLib.TestRecordLength, 0) ;
        if ( false )
        {
            // Breaks with CheckingTree = true ; because they deep reads the tree.
            BPlusTreeParams.CheckingNode = true ;
            BPlusTreeParams.CheckingTree = false ;
            bpt = BPlusTree.addTracking(bpt) ;
        }
        return bpt ; 
    }
}
