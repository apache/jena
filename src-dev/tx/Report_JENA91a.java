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

import java.util.Iterator ;

import org.openjena.atlas.lib.FileOps ;
import org.openjena.atlas.lib.Pair ;
import org.openjena.atlas.logging.Log ;
import org.slf4j.Logger ;
import org.slf4j.LoggerFactory ;

import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.tdb.DatasetGraphTxn ;
import com.hp.hpl.jena.tdb.ReadWrite ;
import com.hp.hpl.jena.tdb.StoreConnection ;
import com.hp.hpl.jena.tdb.base.file.Location ;
import com.hp.hpl.jena.tdb.nodetable.NodeTable ;
import com.hp.hpl.jena.tdb.store.NodeId ;

public class Report_JENA91a {
    
    static { Log.setLog4j() ; }
    static Logger log = LoggerFactory.getLogger("JENA-91") ;  
    
    static Location LOC = Location.mem() ;
    //static Location LOC = new Location("tmp/J91") ;
    
    static {
        if ( ! LOC.isMem() )
            FileOps.clearDirectory(LOC.getDirectoryPath()) ;
    }
    
    // Note to AFS:
    // **** Make sure you have "false" in FileFactory.createObjectFileMem
    // **** Later: go back and flip to get old mem specific failure.
    
    // ?? Offset in W3 should be offset of W2 + offset of W1 (=base)
    // ?? Maybe it's offset of base only. 
    
    private static StoreConnection sConn = StoreConnection.make(LOC) ;
    private static Node g = Node.createURI("g") ;
    private static Node s = Node.createURI("s") ;
    private static Node p = Node.createURI("p") ;
    private static Node o0 = Node.createLiteral("o") ;
    private static Node o1 = Node.createLiteral("o1") ;
    private static Node o2 = Node.createLiteral("o2") ;
    private static Node o3 = Node.createLiteral("o3") ;

    public static void main(String[] args) {
        
//        DatasetGraphTxn dsgW0 = sConn.begin(ReadWrite.WRITE) ;
//        dsgW0.add(g, s, p, o0) ;
//        dsgW0.commit() ;
//        dsgW0.close() ;
        
        
        log.info("Begin: R1") ;
        DatasetGraphTxn dsgR1 = sConn.begin(ReadWrite.READ) ;
        dumpNodeTable("R1", dsgR1) ;
        
        log.info("Begin: W1") ;
        DatasetGraphTxn dsgW1 = sConn.begin(ReadWrite.WRITE) ;
        dsgW1.add(g, s, p, o1) ;
        dumpNodeTable("R1-0", dsgR1) ;
        dumpNodeTable("W1", dsgW1) ;
        log.info("Commit: W1") ;
        dsgW1.commit() ;
        dsgW1.close() ;

        dumpNodeTable("R1-1", dsgR1) ;
        
        //ObjectFileStorage.logging = true ;
        log.info("Begin: W2") ;
        DatasetGraphTxn dsgW2 = sConn.begin(ReadWrite.WRITE) ;
        dsgW2.add(g, s, p, o2) ;

        dumpNodeTable("R1-2", dsgR1) ;
        dumpNodeTable("W2", dsgW2) ;
        log.info("Commit: W2") ;
        dsgW2.commit() ;
        dsgW2.close() ;
        dumpNodeTable("R1-3", dsgR1) ;
        
        log.info("Begin: W3") ;
        DatasetGraphTxn dsgW3 = sConn.begin(ReadWrite.WRITE) ;
        dsgW3.add(g, s, p, o3) ;
        dumpNodeTable("R1-4", dsgR1) ;
        dumpNodeTable("W3", dsgW3) ;
        log.info("Commit: W3") ;
        dsgW3.commit() ;
        dumpNodeTable("R1-5", dsgR1) ;
        dsgR1.close() ;
        
        System.out.println("DONE") ;
    }
    
    private static void dumpNodeTable (String label, DatasetGraphTxn dsg) {
        NodeTable nodeTable = dsg.getTripleTable().getNodeTupleTable().getNodeTable() ;
        Iterator<Pair<NodeId, Node>> iter = nodeTable.all() ;
        System.out.println("---------------[ " + label + " ]---------------") ;
        while ( iter.hasNext() ) { System.out.println(iter.next()) ; }
        System.out.println("------------------------------------") ;
        System.out.flush() ;
    }
}

