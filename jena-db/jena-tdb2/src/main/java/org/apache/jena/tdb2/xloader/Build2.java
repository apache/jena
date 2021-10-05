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

package org.apache.jena.tdb2.xloader;

import org.apache.jena.atlas.lib.tuple.TupleMap;
import org.apache.jena.dboe.base.file.FileSet;
import org.apache.jena.dboe.base.file.Location;
import org.apache.jena.dboe.base.record.RecordFactory;
import org.apache.jena.dboe.index.RangeIndex;
import org.apache.jena.dboe.trans.bplustree.BPlusTree;
import org.apache.jena.dboe.trans.bplustree.BPlusTreeFactory;
import org.apache.jena.dboe.transaction.txn.ComponentId;
import org.apache.jena.tdb2.store.tupletable.TupleIndex;
import org.apache.jena.tdb2.store.tupletable.TupleIndexRecord;
import org.apache.jena.tdb2.sys.SystemTDB;

public class Build2 {

        // TDB2StorageBuiklder.makeTupleIndex
        public static TupleIndex openTupleIndex(Location location, String indexName, String primary, String indexOrder, int keyLength, int valueLength) {
            TupleMap cmap = TupleMap.create(primary, indexOrder);
            RecordFactory rf = new RecordFactory(SystemTDB.SizeOfNodeId * cmap.length(), 0);
            RangeIndex rIdx = makeRangeIndex(location, rf, indexName);
            TupleIndex tIdx = new TupleIndexRecord(primary.length(), cmap, indexName, rf, rIdx);
            return tIdx;
        }

        public static RangeIndex makeRangeIndex(Location location, RecordFactory recordFactory, String name) {
//        ComponentId cid = componentIdMgr.getComponentId(name);
//        FileSet fs = new FileSet(location, name);
//        BPlusTree bpt = BPlusTreeFactory.createBPTree(cid, fs, recordFactory);
//        components.add(bpt);
//        return bpt;
            ComponentId cid = null;
            FileSet fs = new FileSet(location, name);
            BPlusTree bpt = BPlusTreeFactory.createBPTree(cid, fs, recordFactory);
            return bpt;
        }

    }
