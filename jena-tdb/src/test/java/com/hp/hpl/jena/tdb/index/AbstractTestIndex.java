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

package com.hp.hpl.jena.tdb.index;

import static com.hp.hpl.jena.tdb.base.record.RecordLib.intToRecord;
import static com.hp.hpl.jena.tdb.index.IndexTestLib.*;

import com.hp.hpl.jena.tdb.base.record.Record;
import com.hp.hpl.jena.tdb.base.record.RecordLib;

import org.apache.jena.atlas.junit.BaseTest ;
import org.junit.After;
import org.junit.Test;

//import com.hp.hpl.jena.tdb.base.record.RecordLib;

public abstract class AbstractTestIndex extends BaseTest 
{
    Index index = null ;
    
    @After public void afterTest()
    { 
        if ( index != null )
            index.close();
        index = null ;
    }
    
    // ---- Overridable maker
    protected abstract Index makeIndex(int kLen, int vLen) ;
    
    @Test public void index_ins_0()
    {
        // Empty tree
        int[] keys = {};
        test(keys) ;
    }
    
    @Test public void index_ins_1()
    {
        int[] keys = {1};
        test(keys) ;
    }
    
    @Test public void index_ins_2()
    {
        int[] keys = new int[20] ; 
        for ( int i = 0 ; i < keys.length ; i++ )
            keys[i] = i ;
        test(keys) ;
    }
    
    @Test public void index_ins_3()
    {
        int[] keys = new int[20] ; 
        for ( int i = keys.length-1 ; i >= 0 ; i-- )
            keys[i] = i ;
        test(keys) ;
    }

    @Test public void index_ins_4()
    {
        int[] keys = new int[10] ; 
        for ( int i = 0 ; i < keys.length ; i++ )
            keys[i] = 1<<i ;
        test(keys) ;
    }
    
    @Test public void index_ins_5()
    {
        int[] keys = new int[10] ; 
        for ( int i = keys.length-1 ; i >= 0 ; i-- )
            keys[i] = 1<<i ;
        test(keys) ;
    }
    
    @Test public void index_find_1()
    {
        int[] keys = {1};
        Index index = test(keys) ;
        Record r = intToRecord(1, RecordLib.TestRecordLength) ;
        r = index.find(r) ;
        assertNotNull(r) ;
    }
    
    @Test public void index_find_2()
    {
        int[] keys = {1,2,3,4,5,6,7,8,9};
        Index index = test(keys) ;
        Record r = intToRecord(20, RecordLib.TestRecordLength) ;
        r = index.find(r) ;
        assertNull(r) ;
    }
    
    @Test public void index_del_1()
    {
        int[] keys1 = {0, 1, 2};
        int[] keys2 = {0, 1, 2};
        int[] keys3 = {} ;
        test(keys1, keys2, keys3) ;
    }

    @Test public void index_del_2()
    {
        int[] keys1 = {0, 1, 2};
        int[] keys2 = {0, 1};
        int[] keys3 = {2} ;
        test(keys1, keys2, keys3) ;
    }

    @Test public void index_del_3()
    {
        int[] keys1 = {0, 1, 2};
        int[] keys2 = {0, 99};
        int[] keys3 = {2, 1} ;
        test(keys1, keys2, keys3) ;
    }

    private Index test(int[] insKeys, int[] delKeys, int[] expected)
    {
        index = makeIndex(4,0) ;
        testInsert(index, insKeys) ;
        long x = index.size() ;
        if ( x >= 0 )
            assertEquals(insKeys.length, x) ;
        
        if ( delKeys != null )
        {
            testDelete(index, delKeys) ;
            
        }
        
        if ( expected != null ) 
            testIndexContents(index, expected) ;
        return index ;
    }

    private Index test(int[] keys)
    {
        return test(keys, null, keys) ;
    }
}
