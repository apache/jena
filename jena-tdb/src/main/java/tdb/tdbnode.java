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

package tdb;

import java.util.Iterator ;

import org.apache.jena.atlas.lib.Bytes ;
import tdb.cmdline.CmdTDB ;

import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.tdb.lib.NodeLib ;
import com.hp.hpl.jena.tdb.store.DatasetGraphTDB ;
import com.hp.hpl.jena.tdb.store.Hash ;
import com.hp.hpl.jena.tdb.store.NodeId ;
import com.hp.hpl.jena.tdb.store.nodetable.NodeTable ;
import com.hp.hpl.jena.tdb.sys.SystemTDB ;

public class tdbnode extends CmdTDB
{
    // Debugging tool.
    static public void main(String... argv)
    { 
        CmdTDB.init() ;
        new tdbnode(argv).mainRun() ;
    }

    protected tdbnode(String[] argv)
    {
        super(argv) ;
    }

    @Override
    protected String getSummary()
    {
        return getCommandName()+" NodeId ..." ;
    }

    @Override
    protected void exec()
    {
        DatasetGraphTDB dsg = getDatasetGraphTDB() ;
        NodeTable nodeTable = dsg.getTripleTable().getNodeTupleTable().getNodeTable() ;
        Iterator<String> iter = super.getPositional().iterator() ;
        if ( ! iter.hasNext() )
        {
            System.err.println("No node ids") ;
            return ;
        }
        
        for ( ; iter.hasNext() ; )
        {
            String id = iter.next() ;
            try {
                long x = Long.parseLong(id) ;
                NodeId nodeId = new NodeId(x) ;
                Node n = nodeTable.getNodeForNodeId(nodeId) ;
                //System.out.printf("%s [%d] => %s\n", id, x, n) ;

                Hash h = new Hash(SystemTDB.LenNodeHash) ;
                NodeLib.setHash(h, n) ;
                String str = Bytes.asHex(h.getBytes()) ;
                System.out.printf("%s %08d 0x%s # %s\n", id, x, str, n) ;
            } catch (Exception ex)
            {
                System.out.println("Failed to decode: "+id) ;
            }
        }
    }
}
