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

package org.apache.jena.dboe.trans.bplustree.rewriter ;

import java.util.Iterator ;
import java.util.List ;

import org.apache.jena.atlas.iterator.Iter ;
import org.apache.jena.atlas.lib.Pair ;
import org.apache.jena.dboe.base.record.Record;
import org.apache.jena.dboe.base.recordbuffer.RecordBufferPage;
import org.apache.jena.dboe.base.recordbuffer.RecordBufferPageMgr;
import org.apache.jena.dboe.trans.bplustree.BPTreeNode;
import org.apache.jena.dboe.trans.bplustree.BPTreeNodeMgr;
import org.apache.jena.dboe.trans.bplustree.BPlusTreeParams;

class BPlusTreeRewriterUtils {
    static String divider     = "----------------------------------------" ;
    static String nextDivider = null ;

    static Iterator<Pair<Integer, Record>> summarizeDataBlocks(Iterator<Pair<Integer, Record>> iter, RecordBufferPageMgr recordPageMgr) {
        divider() ;
        List<Pair<Integer, Record>> pairs = Iter.toList(iter) ;
        System.out.println("summarizeDataBlocks: " + pairs) ;
        for ( Pair<Integer, Record> pair : pairs ) {
            RecordBufferPage rbp = recordPageMgr.getRead(pair.car()) ;
            System.out.printf("%s -- RecordBufferPage[id=%d,link=%d] (%d) -> [%s]\n", pair, rbp.getId(), rbp.getLink(), rbp.getCount(),
                              rbp.getRecordBuffer().getHigh()) ;
            recordPageMgr.release(rbp) ;
        }
        return pairs.iterator() ;
    }

    static Iterator<Pair<Integer, Record>> summarizeIndexBlocks(Iterator<Pair<Integer, Record>> iter2, BPTreeNodeMgr bptNodeMgr) {
        divider() ;
        List<Pair<Integer, Record>> pairs = Iter.toList(iter2) ;
        for ( Pair<Integer, Record> pair : pairs ) {
            BPTreeNode bpNode = bptNodeMgr.getRead(pair.car(), BPlusTreeParams.RootParent) ;

            String hr = "null" ;
            if ( !bpNode.getRecordBuffer().isEmpty() )
                hr = bpNode.getRecordBuffer().getHigh().toString() ;

            System.out.printf("%s -- BPTreeNode: %d (%d) -> [%s]\n", pair, bpNode.getId(), bpNode.getCount(), hr) ;
            bptNodeMgr.release(bpNode) ;
        }
        return pairs.iterator() ;
    }

    private static Iterator<Pair<Integer, Record>> printDataBlocks(Iterator<Pair<Integer, Record>> iter, RecordBufferPageMgr recordPageMgr) {
        divider() ;
        List<Pair<Integer, Record>> pairs = Iter.toList(iter) ;
        System.out.printf(">>Packed data blocks\n") ;
        for ( Pair<Integer, Record> pair : pairs ) {
            System.out.printf("  %s\n", pair) ;
            RecordBufferPage rbp = recordPageMgr.getRead(pair.car()) ;
            // System.out.printf("RecordBufferPage[id=%d,link=%d] %d\n",
            // rbp.getId(), rbp.getLink(), rbp.getCount() ) ;
            System.out.println(rbp) ;
            recordPageMgr.release(rbp) ;
        }
        System.out.printf("<<Packed data blocks\n") ;
        System.out.printf("Blocks: %d\n", pairs.size()) ;
        return pairs.iterator() ;
    }

    static Iterator<Pair<Integer, Record>> printIndexBlocks(Iterator<Pair<Integer, Record>> iter2, BPTreeNodeMgr bptNodeMgr) {
        divider() ;
        List<Pair<Integer, Record>> pairs = Iter.toList(iter2) ;
        System.out.printf(">>Packed index blocks\n") ;
        for ( Pair<Integer, Record> pair : pairs ) {
            System.out.printf("  %s\n", pair) ;
            BPTreeNode bpNode = bptNodeMgr.getRead(pair.car(), BPlusTreeParams.RootParent) ;
            bpNode.setIsLeaf(true) ;
            System.out.printf("BPTreeNode: %d\n", bpNode.getId()) ;
            System.out.println(bpNode) ;
            bptNodeMgr.release(bpNode) ;
        }
        System.out.printf("<<Packed index blocks\n") ;
        return pairs.iterator() ;
    }

    static void divider() {
        if ( nextDivider != null )
            System.out.println(nextDivider) ;
        nextDivider = divider ;
    }

}
