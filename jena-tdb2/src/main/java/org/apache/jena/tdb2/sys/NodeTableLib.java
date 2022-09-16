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

package org.apache.jena.tdb2.sys;

import org.apache.jena.graph.Node;
import org.apache.jena.riot.out.NodeFmtLib;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.tdb2.store.DatasetGraphTDB;
import org.apache.jena.tdb2.store.NodeId;
import org.apache.jena.tdb2.store.nodetable.NodeTable;

public class NodeTableLib {
    /** Print the main node table - development helper */
    public static void printNodeTable(DatasetGraph dsg, long limit) {
        dsg.executeRead(()->{
            DatasetGraphTDB dsgtdb = TDBInternal.getDatasetGraphTDB(dsg);
            NodeTable nodeTable = dsgtdb.getTripleTable().getNodeTupleTable().getNodeTable();
            int x = 0;
            for ( var iter = nodeTable.all() ; iter.hasNext() ; ) {
                var pair = iter.next();
                x++;
                if ( x > limit )
                    return ;
                NodeId nid = pair.getLeft();
                Node n = pair.getRight();
                System.out.printf("%s %s\n", nid, NodeFmtLib.strNT(n));
            }
//            long x = Iter.count(nodeTable.all());
            System.out.println("Node table length: "+x);
        });
    }
}
