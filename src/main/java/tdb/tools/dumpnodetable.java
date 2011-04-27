/*
 * (c) Copyright 2010 Talis Systems Ltd.
 * (c) Copyright 2011 Epimorphics Ltd.
 * All rights reserved.
 * [See end of file]
 */

package tdb.tools;

import java.io.OutputStream ;
import java.util.Arrays ;
import java.util.Iterator ;
import java.util.List ;

import org.openjena.atlas.io.IndentedWriter ;
import org.openjena.atlas.lib.Pair ;
import org.openjena.atlas.logging.Log ;
import tdb.cmdline.ModLocation ;
import arq.cmdline.CmdGeneral ;

import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.graph.Node_Literal ;
import com.hp.hpl.jena.sparql.util.FmtUtils ;
import com.hp.hpl.jena.sparql.util.Utils ;
import com.hp.hpl.jena.tdb.TDBFactory ;
import com.hp.hpl.jena.tdb.base.file.Location ;
import com.hp.hpl.jena.tdb.nodetable.NodeTable ;
import com.hp.hpl.jena.tdb.store.DatasetGraphTDB ;
import com.hp.hpl.jena.tdb.store.NodeId ;
import com.hp.hpl.jena.tdb.sys.Names ;
import com.hp.hpl.jena.tdb.sys.SetupTDB ;
import com.hp.hpl.jena.tdb.sys.SystemTDB ;

public class dumpnodetable extends CmdGeneral
{
    ModLocation modLocation = new ModLocation() ;
    
    static public void main(String... argv)
    { 
        Log.setLog4j() ;
        new dumpnodetable(argv).mainRun() ;
    }

    @Override
    protected void exec()
    {
        List<String> tripleIndexes = Arrays.asList(Names.tripleIndexes) ;
        List<String> quadIndexes = Arrays.asList(Names.quadIndexes) ;
        Location loc = modLocation.getLocation() ;
        
        DatasetGraphTDB dsg = TDBFactory.createDatasetGraph(loc) ;
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
        dump(w, location, Names.indexNode2Id, SystemTDB.Node2NodeIdCacheSize, Names.indexId2Node, SystemTDB.NodeId2NodeCacheSize) ;
    }

    public static void dumpPrefixes(OutputStream w, String location)
    {
        dump(w, location, Names.prefixNode2Id, 100, Names.prefixId2Node, 100) ;
    }

    
    public static void dump(OutputStream w, String location, 
                            String indexNode2Id, int node2NodeIdCacheSize, 
                            String indexId2Node, int nodeId2NodeCacheSize)
    {
        NodeTable nodeTable = SetupTDB.makeNodeTable(new Location(location), 
                                                     indexNode2Id, node2NodeIdCacheSize,
                                                     indexId2Node, nodeId2NodeCacheSize) ;
    }
    
    
    public static void dump(OutputStream w, NodeTable nodeTable)
    {
        // Better to hack the indexes?
        Iterator<Pair<NodeId, Node>> iter = nodeTable.all() ;
        long count = 0 ;
        IndentedWriter iw = new IndentedWriter(w) ;
        for ( ; iter.hasNext() ; )
        {
            Pair<NodeId, Node> pair = iter.next() ;
            iw.print(pair.car()) ;
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

/*
 * (c) Copyright 2010 Talis Systems Ltd.
 * (c) Copyright 2011 Epimorphics Ltd.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. The name of the author may not be used to endorse or promote products
 *    derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */