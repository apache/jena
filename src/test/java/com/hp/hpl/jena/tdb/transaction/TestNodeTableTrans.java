/**
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

package com.hp.hpl.jena.tdb.transaction;

import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.tdb.base.file.FileFactory ;
import com.hp.hpl.jena.tdb.base.objectfile.ObjectFile ;
import com.hp.hpl.jena.tdb.base.objectfile.ObjectFileMem ;
import com.hp.hpl.jena.tdb.base.record.RecordFactory ;
import com.hp.hpl.jena.tdb.index.Index ;
import com.hp.hpl.jena.tdb.index.IndexMap ;
import com.hp.hpl.jena.tdb.nodetable.NodeTable ;
import com.hp.hpl.jena.tdb.nodetable.NodeTableNative ;
import com.hp.hpl.jena.tdb.sys.SystemTDB ;
import com.hp.hpl.jena.tdb.transaction.NodeTableTrans ;

public class TestNodeTableTrans extends AbstractTestNodeTableTrans
{

    @Override
    protected NodeTableTrans create(Node...nodes)
    {
        RecordFactory recordFactory = new RecordFactory(SystemTDB.LenNodeHash, SystemTDB.SizeOfNodeId) ;
        
        NodeTable base = new NodeTableNative(new IndexMap(recordFactory), new ObjectFileMem()) ;
        for ( Node n : nodes )
            base.getAllocateNodeId(n) ;

        // Set up the transaction table.
        Index idx = new IndexMap(recordFactory) ;
        ObjectFile objectFile = FileFactory.createObjectFileMem() ;
        NodeTableTrans ntt = new NodeTableTrans(base, idx, objectFile) ;
        return ntt ;
    }
}

