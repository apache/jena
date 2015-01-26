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

package org.seaborne.jena.tdb.index.bplustree;

import org.seaborne.jena.tdb.base.block.BlockConverter ;
import org.seaborne.jena.tdb.base.block.BlockMgr ;
import org.seaborne.jena.tdb.base.page.PageBlockMgr ;

abstract class BPTreePageMgr<T extends BPTreePage> extends PageBlockMgr<T>
{
    // BPTreeRecordMgr works on a RecordBufferPageMgr
    // BPTreeNodeMgr works on a BPTreeNodeMgr 
    // so they share very little.
    
    protected final BPlusTree bpTree ;
    
    BPTreePageMgr(BPlusTree bpTree, BlockConverter<T> pageFactory, BlockMgr blockMgr)
    {
        super(pageFactory, blockMgr) ;
        this.bpTree = bpTree ;
    }
}
