/*
 * (c) Copyright 2010 Talis Systems Ltd.
 * All rights reserved.
 * [See end of file]
 */

package dump;

import java.io.OutputStream ;
import java.util.Iterator ;
import java.util.regex.Matcher ;
import java.util.regex.Pattern ;

import org.openjena.atlas.io.IndentedWriter ;
import org.openjena.atlas.lib.Pair ;

import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.sparql.util.Utils ;
import com.hp.hpl.jena.tdb.base.file.Location ;
import com.hp.hpl.jena.tdb.nodetable.NodeTable ;
import com.hp.hpl.jena.tdb.store.NodeId ;
import com.hp.hpl.jena.tdb.sys.Names ;
import com.hp.hpl.jena.tdb.sys.SetupTDB ;
import com.hp.hpl.jena.tdb.sys.SystemTDB ;

public class DumpNodeTable
{
    public static void main(String ... argv)
    {
        if (  argv.length == 0 || isHelp(argv) )
            usage() ;
        dumpNodes(System.out, argv[0]) ;
    }
    
    static Pattern pattern = Pattern.compile("/(-|--)(h|help)") ; 
    
    private static boolean isHelp(String[] argv)
    {
        if ( argv.length == 0 ) return false ;
        Matcher m = pattern.matcher(argv[0]) ;
        return m.matches() ;
    }

     
    
    private static void usage()
    {
        System.err.println("Usage: "+Utils.className(pattern)+"location") ;
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
        
        // Better to hack the indexes?
        Iterator<Pair<NodeId, Node>> iter = nodeTable.all() ;
        long count = 0 ;
        IndentedWriter iw = new IndentedWriter(w) ;
        for ( ; iter.hasNext() ; )
        {
            Pair<NodeId, Node> pair = iter.next() ;
            iw.print(pair.car()) ;
            iw.print(" : ") ;
            iw.print(pair.cdr()) ;
            iw.println() ;
            count++ ;
        }
        iw.println() ;
        iw.printf("Total: "+count) ;
        iw.flush() ;
    }
}

/*
 * (c) Copyright 2010 Talis Systems Ltd.
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