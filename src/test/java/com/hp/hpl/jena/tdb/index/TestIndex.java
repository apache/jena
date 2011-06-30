/*
 * (c) Copyright 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.tdb.index;

import static com.hp.hpl.jena.tdb.base.record.RecordLib.intToRecord;
import static com.hp.hpl.jena.tdb.index.IndexTestLib.*;

import com.hp.hpl.jena.tdb.base.record.Record;
import com.hp.hpl.jena.tdb.base.record.RecordLib;

import org.junit.After;
import org.junit.Test;
import org.openjena.atlas.junit.BaseTest ;

//import com.hp.hpl.jena.tdb.base.record.RecordLib;

public abstract class TestIndex extends BaseTest 
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

/*
 * (c) Copyright 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. The name of the author may not be used to endorse or promote products
 *    derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
