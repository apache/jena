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

package tx;

import java.util.Iterator;

import org.openjena.atlas.lib.FileOps ;
import org.openjena.atlas.lib.Pair;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.tdb.DatasetGraphTxn;
import com.hp.hpl.jena.tdb.ReadWrite;
import com.hp.hpl.jena.tdb.StoreConnection;
import com.hp.hpl.jena.tdb.base.file.Location;
import com.hp.hpl.jena.tdb.nodetable.NodeTable;
import com.hp.hpl.jena.tdb.store.NodeId;

public class Report_JENA91 {
    
    static Location LOC = Location.mem() ; // new Location("tmp/J91") ;
    
    static {
        if ( ! LOC.isMem() )
            FileOps.clearDirectory(LOC.getDirectoryPath()) ;
    }
    
    // Make sure you have "false" in FileFactory.createObjectFileMem
    // Later: go back and flip to get old mem specific failure.
    
    private static StoreConnection sConn = StoreConnection.make(LOC) ;
    private static Node g = Node.createURI("g") ;
    private static Node s = Node.createURI("s") ;
    private static Node p = Node.createURI("p") ;
    private static Node o1 = Node.createLiteral("o1") ;
    private static Node o2 = Node.createLiteral("o2") ;
    private static Node o3 = Node.createLiteral("o3") ;

    public static void main(String[] args) {
        DatasetGraphTxn dsgR1 = sConn.begin(ReadWrite.READ) ;
        // dumpNodeTable("R1", dsgR1) ;
        
        DatasetGraphTxn dsgW1 = sConn.begin(ReadWrite.WRITE) ;
        dsgW1.add(g, s, p, o1) ;
        dsgW1.commit() ;
        dumpNodeTable("W1", dsgW1) ;
        dumpNodeTable("R1", dsgR1) ;

        DatasetGraphTxn dsgW2 = sConn.begin(ReadWrite.WRITE) ;
        dsgW2.add(g, s, p, o2) ;
        dsgW2.commit() ;
        dumpNodeTable("W2", dsgW2) ;
        dumpNodeTable("R1", dsgR1) ;

        DatasetGraphTxn dsgW3 = sConn.begin(ReadWrite.WRITE) ;
        dsgW3.add(g, s, p, o3) ;
        dumpNodeTable("W3", dsgW3) ;
        dumpNodeTable("R1", dsgR1) ;
        dsgW3.commit() ;

        dsgR1.close() ;
        System.out.println("DONE") ;
    }
    
    private static void dumpNodeTable (String label, DatasetGraphTxn dsg) {
        NodeTable nodeTable = dsg.getTripleTable().getNodeTupleTable().getNodeTable() ;
        Iterator<Pair<NodeId, Node>> iter = nodeTable.all() ;
        System.out.println("---------------[ " + label + " ]---------------") ;
        while ( iter.hasNext() ) { System.out.println(iter.next()) ; }
        System.out.println("------------------------------------\n") ;
        System.out.flush() ;
    }
}

