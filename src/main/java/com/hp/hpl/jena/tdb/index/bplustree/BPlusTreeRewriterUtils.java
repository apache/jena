/*
 * (c) Copyright 2010 Epimorphics Ltd.
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.tdb.index.bplustree;

import java.util.Iterator ;
import java.util.List ;

import org.openjena.atlas.iterator.Iter ;
import org.openjena.atlas.lib.Pair ;

import com.hp.hpl.jena.tdb.base.record.Record ;
import com.hp.hpl.jena.tdb.base.recordfile.RecordBufferPage ;
import com.hp.hpl.jena.tdb.base.recordfile.RecordBufferPageMgr ;

class BPlusTreeRewriterUtils
{

    static String divider = "----------------------------------------" ;
    static String nextDivider = null ;

    static Iterator<Pair<Integer, Record>> summarizeDataBlocks(Iterator<Pair<Integer, Record>> iter, RecordBufferPageMgr recordPageMgr)
    {
        divider() ;
        List<Pair<Integer, Record>> x = Iter.toList(iter) ;
        for (Pair<Integer, Record> pair : x )
        {
            RecordBufferPage rbp = recordPageMgr.get(pair.car()) ;
            System.out.printf("%s -- RecordBufferPage[id=%d,link=%d] (%d) -> [%s]\n", pair, rbp.getId(), rbp.getLink(), rbp.getCount(), rbp.getRecordBuffer().getHigh() ) ;
        }
        return x.iterator() ;
    }

    static Iterator<Pair<Integer, Record>> summarizeIndexBlocks(Iterator<Pair<Integer, Record>> iter2, BPTreeNodeMgr bptNodeMgr)
    {
        divider() ;
        List<Pair<Integer, Record>> x = Iter.toList(iter2) ;
        for (Pair<Integer, Record> pair : x )
        {
            BPTreeNode bpNode = bptNodeMgr.get(pair.car(), BPlusTreeParams.RootParent) ;
            
            String hr = "null" ;
            if ( ! bpNode.getRecordBuffer().isEmpty() ) 
                hr = bpNode.getRecordBuffer().getHigh().toString() ;
            
            System.out.printf("%s -- BPTreeNode: %d (%d) -> [%s]\n", pair, bpNode.getId(), bpNode.getCount(), hr) ;
        }
        return x.iterator() ;
    }

    private static Iterator<Pair<Integer, Record>> printDataBlocks(Iterator<Pair<Integer, Record>> iter, RecordBufferPageMgr recordPageMgr)
    {
        divider() ;
        List<Pair<Integer, Record>> x = Iter.toList(iter) ;
        System.out.printf(">>Packed data blocks\n") ;
        for (Pair<Integer, Record> pair : x )
        {
            System.out.printf("  %s\n",pair) ;
            RecordBufferPage rbp = recordPageMgr.get(pair.car()) ;
            //System.out.printf("RecordBufferPage[id=%d,link=%d] %d\n", rbp.getId(), rbp.getLink(), rbp.getCount() ) ;
            System.out.println(rbp) ;
        }
        System.out.printf("<<Packed data blocks\n") ;
        System.out.printf("Blocks: %d\n", x.size()) ;
        return x.iterator() ;
    }

    static Iterator<Pair<Integer, Record>> printIndexBlocks(Iterator<Pair<Integer, Record>> iter2, BPTreeNodeMgr bptNodeMgr)
    {
        divider() ;
        List<Pair<Integer, Record>> x = Iter.toList(iter2) ;
        System.out.printf(">>Packed index blocks\n") ;
        for (Pair<Integer, Record> pair : x )
        {
            System.out.printf("  %s\n",pair) ;
            BPTreeNode bpNode = bptNodeMgr.get(pair.car(), BPlusTreeParams.RootParent) ;
            bpNode.setIsLeaf(true) ;
            System.out.printf("BPTreeNode: %d\n", bpNode.getId()) ;
            System.out.println(bpNode) ;
        }
        System.out.printf("<<Packed index blocks\n") ;
        return x.iterator() ;
    }

    static void divider()
    {
        if ( nextDivider != null )
            System.out.println(nextDivider) ;
        nextDivider = divider ;
    }

}

/*
 * (c) Copyright 2010 Epimorphics Ltd.
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