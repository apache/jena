/*
 * (c) Copyright 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package dev;

import static atlas.test.Gen.strings;
import static com.hp.hpl.jena.tdb.base.record.RecordLib.intToRecord;
import org.apache.log4j.Level;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.sparql.sse.SSE;
import com.hp.hpl.jena.sparql.util.FmtUtils;

import com.hp.hpl.jena.tdb.base.block.BlockMgrCache;
import com.hp.hpl.jena.tdb.base.record.Record;
import com.hp.hpl.jena.tdb.base.record.RecordLib;
import com.hp.hpl.jena.tdb.index.Index;
import com.hp.hpl.jena.tdb.index.IndexTestLib;
import com.hp.hpl.jena.tdb.index.RangeIndex;
import com.hp.hpl.jena.tdb.index.bplustree.BPlusTree;
import com.hp.hpl.jena.tdb.index.bplustree.BPlusTreeParams;
import com.hp.hpl.jena.tdb.lib.StringAbbrev;
import com.hp.hpl.jena.tdb.store.NodeId;
import com.hp.hpl.jena.tdb.sys.SystemTDB;

public class Snippets
{

    static void typedNode()
        {
    //      typedNode("'2008-04-27T16:52:17+01:00'^^xsd:dateTime") ;
    //      typedNode("'2008-04-27T16:52:17-05:00'^^xsd:dateTime") ;
    //      typedNode("'2008-04-27T16:52:17Z'^^xsd:dateTime") ;
    //      typedNode("'2008-04-27T16:52:17+00:00'^^xsd:dateTime") ;
          typedNodeOne("'2008-04-27T16:52:17'^^xsd:dateTime") ;
          typedNodeOne("'2008-04-27'^^xsd:date") ;
          System.exit(0) ;
        }

    static void typedNodeOne(String x)
    {
        System.out.println("Input = "+x) ;
        Node n = SSE.parseNode(x) ;
        NodeId nodeId = NodeId.inline(n) ;
        if ( nodeId == null )
        {
            System.out.println("null nodeid") ;
            return ;
        }
        
        System.out.printf("NodeId : %s\n", nodeId) ;
        Node n2 = NodeId.extract(nodeId) ;
        if ( n2 == null )
        {
            System.out.println("null node") ;
            return ;
        }
        String y = FmtUtils.stringForNode(n2) ;
        System.out.println("Output = "+y) ;
        if ( ! n.equals(n2) )
        {
            System.out.println("Different:") ;
            System.out.println("  "+n) ;
            System.out.println("  "+n2) ;
        }
    }

    static void abbrev()
    {
        StringAbbrev abbrev = new StringAbbrev() ;
        abbrev.add("z", "http://") ;
        abbrevOne(abbrev, "http://example") ;
        abbrevOne(abbrev, "foo") ;
        abbrevOne(abbrev, ":foo") ;
        abbrevOne(abbrev, "::foo") ;
        abbrevOne(abbrev, ":::foo") ;
    }

    static void abbrevOne(StringAbbrev abbrev, String string)
    {
        String a = abbrev.abbreviate(string) ;
        String a2 = abbrev.expand(a) ;
        System.out.println(string) ;
        System.out.println(a) ;
        System.out.println(a2) ;
        System.out.println() ;
    }

    private static void runIndexTest() 
         {
             SystemTDB.NullOut = true ;
             
             Index index = BPlusTree.makeMem("",2, 2, RecordLib.TestRecordLength, 0) ;
             BPlusTree bpt = (BPlusTree)index ; 
             int[] keys1 = {681, 309, 141, 325, 588, 147, 616, 460, 21, 26, 339, 160, 278, 183, 887, 388, 250, 761, 139, 894} ;
             int[] keys2 = {183, 278, 894, 160, 250, 588, 325, 887, 139, 681, 26, 147, 388, 616, 21, 141, 460, 339, 309, 761}; 
             BPlusTreeParams.checkAll() ;
             
             try {
                 IndexTestLib.testInsert(index, keys1);
                 if ( true )
                 {
                     // Checking tests.
                     IndexTestLib.testIndexContents(index, keys2);
                     // Test iteration - quite expensive.
                     if ( index instanceof RangeIndex )
                         IndexTestLib.testIteration((RangeIndex)index, keys1, 10) ;
                 }
                 
    //             BPlusTreeParams.checkAll() ;
    //             BPlusTreeParams.infoAll() ;
                 org.apache.log4j.LogManager.getLogger("com.hp.hpl.jena.tdb.index").setLevel(Level.ALL) ;
                 //org.apache.log4j.LogManager.getLogger("com.hp.hpl.jena.tdb.base.block").setLevel(Level.ALL) ;
                 if ( false )
                     IndexTestLib.testDelete(index, keys2) ;
                 else
                 {
                     //List<Record> x =  intToRecord(keys2) ;
                     int count = 0 ;
                     int debug = -1 ; // 0x00FA ;
                     
                     for ( int v : keys2 )
                     {
                         if ( v == debug )
                            System.out.println() ;
                             
                         Record r = intToRecord(v) ;
                         System.out.printf("==== Delete: %d (0x%04X)\n",v,v) ;
                         
                         if ( v == debug )
                         {
                             //bpt.dump(); 
                             BPlusTreeParams.infoAll() ;
                             BlockMgrCache.globalLogging = true ;
                         }
                         
                         boolean b = index.delete(r) ;
                         if ( b )
                             count ++ ;
                         if ( v == debug ) bpt.dump(); 
                     }
                 }
                 index.close() ;
             } catch (RuntimeException ex)
             {
                 System.err.printf("Index : %s\n", index.getClass().getName()) ;
                 System.err.printf("int[] keys1 = {%s} ;\n", strings(keys1)) ;
                 System.err.printf("int[] keys2 = {%s}; \n", strings(keys2)) ;
                 throw ex ;
             }
             System.out.println("Success") ;
             System.exit(0) ;
         }
}

/*
 * (c) Copyright 2008, 2009 Hewlett-Packard Development Company, LP
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