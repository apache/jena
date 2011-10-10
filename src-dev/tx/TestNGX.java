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

import org.slf4j.Logger ;
import org.slf4j.LoggerFactory ;

import com.hp.hpl.jena.graph.Graph ;
import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.sparql.sse.SSE ;
import com.hp.hpl.jena.tdb.ConfigTest ;
import com.hp.hpl.jena.tdb.DatasetGraphTxn ;
import com.hp.hpl.jena.tdb.ReadWrite ;
import com.hp.hpl.jena.tdb.StoreConnection ;
import com.hp.hpl.jena.tdb.base.file.Location ;

public class TestNGX
{
    static { org.openjena.atlas.logging.Log.setLog4j() ; }
    private static Logger log = LoggerFactory.getLogger(TestTransSystemJena91.class) ;

    static boolean MEM = false ;
    
    static final Location LOC = MEM ? Location.mem() : new Location(ConfigTest.getTestingDirDB()) ;
    
    protected static synchronized StoreConnection getStoreConnection()
    {
        StoreConnection sConn = StoreConnection.make(LOC) ;
        //sConn.getTransMgr().recording(true) ;
        return sConn ;
    }
    
    public static void main(String ...args)
    {
        StoreConnection sConn = getStoreConnection() ;
        DatasetGraphTxn dsg = sConn.begin(ReadWrite.WRITE) ;
        Node gn = Node.createURI("http://example/") ;
        Graph g = dsg.getGraph(gn) ;
        g.add(SSE.parseTriple("(<s> <p> <o>)")) ;
        dsg.commit() ;
        
        DatasetGraphTxn dsg2 = sConn.begin(ReadWrite.READ) ;
        System.out.println(dsg2) ;
        dsg2.close() ;
    }
    
    
    
}
