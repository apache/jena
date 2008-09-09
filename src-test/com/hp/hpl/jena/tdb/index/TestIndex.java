/*
 * (c) Copyright 2007, 2008 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.tdb.index;

//import static com.hp.hpl.jena.tdb.base.record.RecordLib.r;
//import static com.hp.hpl.jena.tdb.base.record.RecordLib.toIntList;
//import static com.hp.hpl.jena.tdb.index.IndexTestLib.add;
//import static com.hp.hpl.jena.tdb.index.IndexTestLib.randTest;
//import static com.hp.hpl.jena.tdb.index.IndexTestLib.testInsert;
//import static com.hp.hpl.jena.tdb.index.IndexTestLib.testInsertDelete;

import static com.hp.hpl.jena.tdb.index.IndexTestLib.testInsert;

import org.junit.After;
import org.junit.Test;
import test.BaseTest;

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
    protected abstract Index makeIndex() ;
    
    // TODO
    @Test public void tree_ins_0()
    {
        // Empty tree
        int[] keys = {};
        test(keys) ;
    }
    
    @Test public void tree_ins_1()
    {
        int[] keys = {1};
        test(keys) ;
    }
    
    @Test public void tree_ins_2()
    {
        int[] keys = {1,2,3,4,5,6,7,8,9};
        test(keys) ;
    }
    
    private void test(int[] keys)
    {
        index = makeIndex() ;
        testInsert(index, keys) ;
        long x = index.size() ;
        if ( x >= 0 )
            assertEquals(keys.length, x) ;
    }
}

/*
 * (c) Copyright 2007, 2008 Hewlett-Packard Development Company, LP
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
