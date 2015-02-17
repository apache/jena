/**
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

package org.seaborne.dboe.trans.bplustree;

import org.seaborne.dboe.base.block.BlockMgrBuilder ;
import org.seaborne.dboe.base.file.FileSet ;
import org.seaborne.dboe.base.record.RecordFactory ;
import org.seaborne.dboe.index.Index ;
import org.seaborne.dboe.index.IndexBuilder ;
import org.seaborne.dboe.index.IndexParams ;
import org.seaborne.dboe.transaction.txn.ComponentId ;

/** IndexBuilder for BPlusTrees */ 
public class IndexBuilderBPTree implements IndexBuilder
{
    protected BlockMgrBuilder bMgrNodes ;
    protected BlockMgrBuilder bMgrRecords ;
    protected RangeIndexBuilderBPTree other ;

    public IndexBuilderBPTree(ComponentId base, BlockMgrBuilder bMgrNodes, BlockMgrBuilder bMgrRecords) {
        this.bMgrNodes = bMgrNodes ;
        this.bMgrRecords = bMgrRecords ;
        this.other = new RangeIndexBuilderBPTree(base, bMgrNodes, bMgrRecords) ;
    }

    @Override
    public Index buildIndex(FileSet fileSet, RecordFactory recordFactory, IndexParams indexParams) {
        return other.buildRangeIndex(fileSet, recordFactory, indexParams) ;
    }
}
