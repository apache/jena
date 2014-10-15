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

import com.hp.hpl.jena.tdb.base.block.BlockConverter ;
import com.hp.hpl.jena.tdb.base.block.BlockMgr ;
import com.hp.hpl.jena.tdb.base.page.PageBlockMgr ;

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
