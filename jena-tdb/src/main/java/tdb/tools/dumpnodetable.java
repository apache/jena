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

package tdb.tools;

import java.io.OutputStream ;
import java.util.Arrays ;
import java.util.Iterator ;
import java.util.List ;

import org.apache.jena.atlas.io.IndentedWriter ;
import org.apache.jena.atlas.lib.Pair ;
import org.apache.jena.atlas.logging.Log ;
import org.apache.jena.atlas.logging.LogCtl ;
import tdb.cmdline.ModLocation ;
import arq.cmdline.CmdGeneral ;

import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.graph.Node_Literal ;
import com.hp.hpl.jena.sparql.util.FmtUtils ;
import com.hp.hpl.jena.sparql.util.Utils ;
import com.hp.hpl.jena.tdb.StoreConnection ;
import com.hp.hpl.jena.tdb.base.file.Location ;
import com.hp.hpl.jena.tdb.setup.Build ;
import com.hp.hpl.jena.tdb.store.DatasetGraphTDB ;
import com.hp.hpl.jena.tdb.store.NodeId ;
import com.hp.hpl.jena.tdb.store.nodetable.NodeTable ;
import com.hp.hpl.jena.tdb.sys.Names ;
import com.hp.hpl.jena.tdb.sys.SystemTDB ;

public class dumpnodetable extends CmdGeneral
{
    ModLocation modLocation = new ModLocation() ;
    
    static public void main(String... argv)
    { 
        LogCtl.setLog4j() ;
        new dumpnodetable(argv).mainRun() ;
    }

    @Override
    protected void exec()
    {
        List<String> tripleIndexes = Arrays.asList(Names.tripleIndexes) ;
        List<String> quadIndexes = Arrays.asList(Names.quadIndexes) ;
        Location loc = modLocation.getLocation() ;
        
        StoreConnection sConn = StoreConnection.make(loc) ; 
        DatasetGraphTDB dsg = sConn.getBaseDataset() ;
        NodeTable nodeTable = dsg.getQuadTable().getNodeTupleTable().getNodeTable() ;
        dump(System.out, nodeTable) ;
    }
    
    protected dumpnodetable(String[] argv)
    {
        super(argv) ;
        super.addModule(modLocation) ;
    }

    public static void dumpNodes(OutputStream w, String location)
    {
        dump(w, location, Names.indexNode2Id, SystemTDB.Node2NodeIdCacheSize, Names.indexId2Node, SystemTDB.NodeId2NodeCacheSize, SystemTDB.NodeMissCacheSize) ;
    }

    public static void dumpPrefixes(OutputStream w, String location)
    {
        dump(w, location, Names.prefixNode2Id, 100, Names.prefixId2Node, 100, 10) ;
    }

    
    public static void dump(OutputStream w, String location, 
                            String indexNode2Id, int node2NodeIdCacheSize, 
                            String indexId2Node, int nodeId2NodeCacheSize, //

                            int sizeNodeMissCacheSize)
    {
        NodeTable nodeTable = Build.makeNodeTable(new Location(location), 
                                                    indexNode2Id, node2NodeIdCacheSize,
                                                    indexId2Node, nodeId2NodeCacheSize,
                                                    sizeNodeMissCacheSize) ;
    }
    
    
    public static void dump(OutputStream w, NodeTable nodeTable)
    {
        // Better to hack the indexes?
        Iterator<Pair<NodeId, Node>> iter = nodeTable.all() ;
        long count = 0 ;
        try(IndentedWriter iw = new IndentedWriter(w)) {
            for ( ; iter.hasNext() ; )
            {
                Pair<NodeId, Node> pair = iter.next() ;
                iw.print(pair.car().toString()) ;
                iw.print(" : ") ;
                //iw.print(pair.cdr()) ;
                Node n = pair.cdr() ;
                String $ = stringForNode(n) ;
                iw.print($) ;
                iw.println() ;
                count++ ;
            }
            iw.println() ;
            iw.printf("Total: "+count) ;
            iw.println() ;
            iw.flush() ;
        }
    }
    
    private static String stringForNode(Node n)
    {
        if ( n == null )
            return "<<null>>" ;
        
        if ( n.isBlank() )
            return "_:"+n.getBlankNodeLabel() ;
        
        if ( n.isLiteral() )
            return stringForLiteral((Node_Literal)n) ;

        if ( n.isURI() )
        {
            String uri = n.getURI() ;
            return stringForURI(uri) ;
        }
        
        if ( n.isVariable() )
            return "?"+n.getName() ;
        
        if ( n.equals(Node.ANY) )
            return "ANY" ;

        Log.warn(FmtUtils.class, "Failed to turn a node into a string: "+n) ;
        return n.toString() ;
    }
    
    public static String stringForURI(String uri)
    {
        return "<"+uri+">" ;
    }
    
    public static String stringForLiteral(Node_Literal literal)
    {
        String datatype = literal.getLiteralDatatypeURI() ;
        String lang = literal.getLiteralLanguage() ;
        String s = literal.getLiteralLexicalForm() ;
        
        StringBuilder sbuff = new StringBuilder() ;
        sbuff.append("\"") ;
        FmtUtils.stringEsc(sbuff, s, true) ;
        sbuff.append("\"") ;
        
        // Format the language tag 
        if ( lang != null && lang.length()>0)
        {
            sbuff.append("@") ;
            sbuff.append(lang) ;
        }

        if ( datatype != null )
        {
            sbuff.append("^^") ;
            sbuff.append(stringForURI(datatype)) ;
        }
        
        return sbuff.toString() ;
    }

    @Override
    protected void processModulesAndArgs()
    {
        if ( modVersion.getVersionFlag() )
            modVersion.printVersionAndExit() ;
        if ( modLocation.getLocation() == null )
            cmdError("Location required") ;
    }

    @Override
    protected String getSummary()
    {
        return getCommandName()+" --loc=DIR IndexName" ;
    }

    @Override
    protected String getCommandName()
    {
        return Utils.className(this) ;
    }
    
}
