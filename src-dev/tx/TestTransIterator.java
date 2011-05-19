/*
 * (c) Copyright 2011 Epimorphics Ltd.
 * All rights reserved.
 * [See end of file]
 */

package tx;

import java.util.Iterator ;

import org.junit.Test ;
import org.openjena.atlas.iterator.Iter ;
import org.openjena.atlas.junit.BaseTest ;

import com.hp.hpl.jena.tdb.base.record.Record ;
import com.hp.hpl.jena.tdb.base.record.RecordLib ;
import com.hp.hpl.jena.tdb.index.IndexTestLib ;
import com.hp.hpl.jena.tdb.index.RangeIndex ;
import com.hp.hpl.jena.tdb.index.bplustree.BPlusTree ;

public class TestTransIterator extends BaseTest
{
    static BPlusTree build(int order, int[] values)
    {
        // See TestBPlusTree
        BPlusTree bpt = BPlusTree.makeMem(order, order, RecordLib.TestRecordLength, 0) ;
        bpt = BPlusTree.addTracking(bpt) ;
        IndexTestLib.add(bpt, values) ;
        return bpt ;
    }
 
    @Test public void transIter_01()
    {
        int vals[] = { } ;
        RangeIndex rIndex = build(2, vals) ;
        Iterator<Record> iter1 = rIndex.iterator() ;
        Iterator<Record> iter2 = rIndex.iterator() ;
        count(iter1, vals.length) ;
        count(iter2, vals.length) ;
    }

    @Test public void transIter_02()
    {
        int vals[] = { 1, 2, 3, 4, 5, 6, 7 } ;
        RangeIndex rIndex = build(2, vals) ;
        Iterator<Record> iter1 = rIndex.iterator() ;
        Iterator<Record> iter2 = rIndex.iterator() ;
        count(iter1, vals.length) ;
        System.out.println("Iter2") ;
        count(iter2, vals.length) ;
    }
    
    @Test public void transIter_03()
    {
        // Interleaved.
        int vals[] = { 1, 2, 3, 4, 5, 6, 7 } ;
        RangeIndex rIndex = build(2, vals) ;
        Iterator<Record> iter1 = rIndex.iterator() ;
        Iterator<Record> iter2 = rIndex.iterator() ;
        
        System.out.println("03") ;
        for ( ; iter1.hasNext() ; )
        {
            Record r1 = iter1.next();
            System.out.println("r1 = "+r1) ;
            Record r2 = iter2.next();
            System.out.println("r2 = "+r2) ;
        }
        assertFalse(iter2.hasNext()) ;
    }
    
    private static void count(Iterator<Record> iter, long expected)
    {
        long x = Iter.count(iter) ; 
        assertEquals(expected, x) ;
    }
    
}

/*
 * (c) Copyright 2011 Epimorphics Ltd.
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