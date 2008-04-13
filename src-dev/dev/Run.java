/*
 * ong time (c) Copyright 2007, 2008 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package dev;

import static lib.FileOps.clearDirectory;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.util.Iterator;

import lib.StrUtils;
import lib.Tuple;

import com.sleepycat.je.*;

import com.hp.hpl.jena.assembler.JA;
import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.impl.RDFReaderFImpl;
import com.hp.hpl.jena.shared.PrefixMapping;
import com.hp.hpl.jena.tdb.base.block.BlockMgrFactory;
import com.hp.hpl.jena.tdb.base.record.RecordFactory;
import com.hp.hpl.jena.tdb.btree.BTree;
import com.hp.hpl.jena.tdb.btree.BTreeParams;
import com.hp.hpl.jena.tdb.graph.StatsGraph;
import com.hp.hpl.jena.tdb.index.TripleIndex;
import com.hp.hpl.jena.tdb.lib.StringAbbrev;
import com.hp.hpl.jena.tdb.pgraph.GraphBTree;
import com.hp.hpl.jena.tdb.pgraph.NodeId;
import com.hp.hpl.jena.tdb.pgraph.PGraphBase;
import com.hp.hpl.jena.util.FileManager;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;

import com.hp.hpl.jena.sparql.core.Prologue;
import com.hp.hpl.jena.sparql.core.assembler.AssemblerUtils;
import com.hp.hpl.jena.sparql.sse.SSE;
import com.hp.hpl.jena.sparql.sse.writers.WriterBasePrefix;
import com.hp.hpl.jena.sparql.util.FmtUtils;
import com.hp.hpl.jena.sparql.util.IndentedWriter;

import com.hp.hpl.jena.query.*;


public class Run
{
    public static void main(String ... args)
    {
        typedNode("1") ;
        typedNode("'1'^^xsd:int") ;
        typedNode("'1'") ;
        System.exit(0) ;
        
        
        
        
        z() ; System.exit(0) ;
        
        
        prefixes() ; 
        System.exit(0) ;
        
        String[] a = { "--desc", "dataset.ttl", "--", "-"};
        tdb.tdbloader.main(a) ;
        System.exit(0) ;
        
        String dir = "tmp" ;
        clearDirectory(dir) ;
        
        
        
        System.exit(0) ;
    }
    
    private static void typedNode(String x)
    {
        System.out.println("Input = "+x) ;
        Node n = SSE.parseNode(x) ;
        NodeId nodeId = NodeId.inline(n) ;
        if ( nodeId == null )
        {
            System.out.println("null nodeid") ;
            return ;
        }
        
        System.out.printf("NodeId : 0x%08X\n", nodeId.getId()) ;
        Node n2 = NodeId.extract(nodeId) ;
        if ( n2 == null )
        {
            System.out.println("null node") ;
            return ;
        }
        String y = FmtUtils.stringForNode(n2) ;
        System.out.println("Output = "+y) ;
        if ( ! n.equals(n2) )
            System.out.println("Different: "+n+" : "+n2) ;
    }
    
    private static void z()
    {
        StringAbbrev abbrev = new StringAbbrev() ;
        abbrev.add("z", "http://") ;
        zOne(abbrev, "http://example") ;
        zOne(abbrev, "foo") ;
        zOne(abbrev, ":foo") ;
        zOne(abbrev, "::foo") ;
        zOne(abbrev, ":::foo") ;
    }


    private static void zOne(StringAbbrev abbrev, String string)
    {
        String a = abbrev.abbreviate(string) ;
        String a2 = abbrev.expand(a) ;
        System.out.println(string) ;
        System.out.println(a) ;
        System.out.println(a2) ;
        System.out.println() ;
    }
    
    private static void prefixes()
    {
        //PrefixMapping pm = AssemblerUtils.readPrefixMapping("prefixes.ttl") ;
        PrefixMapping pm = (PrefixMapping)AssemblerUtils.build("prefixes.ttl", JA.PrefixMapping) ;
        Prologue prologue = new Prologue(pm) ;
        WriterBasePrefix.output(IndentedWriter.stdout, null, prologue) ;
        System.out.println(pm) ; 
    }

    public static void index(String ... args)
    {
        int IndexRecordLength = 3 * NodeId.SIZE ;
        RecordFactory indexRecordFactory = new RecordFactory(IndexRecordLength, 0) ; 
        int order = 3 ;
        BTreeParams params = new BTreeParams(order, indexRecordFactory) ;
        int blkSize = params.getBlockSize() ;

        BTree bTree1 = new BTree(params, BlockMgrFactory.createMem(blkSize)) ;

        TripleIndex index1 = new TripleIndex("OSP", bTree1) ;
        
        NodeId n1 = NodeId.create(1) ;
        NodeId n2 = NodeId.create(2) ;
        NodeId n3 = NodeId.create(3) ;

        NodeId n4 = NodeId.create(4) ;
        NodeId n5 = NodeId.create(5) ;
        NodeId n6 = NodeId.create(6) ;

        index1.add(n1, n2, n3) ;
        index1.add(n4, n2, n3) ;
        index1.add(n4, n5, n6) ;
        
        Iterator<Tuple<NodeId>> iter = index1.all() ;
        // SPO order.
        System.out.println("Index "+index1.getName()+" in SPO order") ;
        print(iter) ;

        // Directly get the contents of the index and print in index-order.
        System.out.println("Index "+index1.getName()+" in native order") ;
        printNative(index1) ;
        
        BTree bTree2 = new BTree(params, BlockMgrFactory.createMem(blkSize)) ;
        TripleIndex index2 = new TripleIndex("POS", bTree2) ;

        // Copy one to another.  Reordering taken careof
        index1.copyInto(index2) ;
        
        System.out.println("Index "+index2.getName()+" in native order") ;
        printNative(index2) ;

        System.out.println("Index "+index2.getName()+" in SPO order") ;
        print(index2.all()) ;

        BTree bTree3 = new BTree(params, BlockMgrFactory.createMem(blkSize)) ;
        TripleIndex index3 = new TripleIndex("SPO", bTree3) ;
        index1.copyInto(index3) ;
        
        BTree bTree4 = new BTree(params, BlockMgrFactory.createMem(blkSize)) ;
        TripleIndex index4 = new TripleIndex("SPO", bTree3) ;
        index2.copyInto(index4) ;
        
        System.out.println("sameAs: "+index3.sameAs(index4)) ;
        
        System.out.println("find") ;
        // OSP
        Iterator<Tuple<NodeId>> iterF = index1.find(null, n2, n3) ;
        print(iterF) ;
        
        
        System.exit(0) ;
        
 
    }

    private static void print(Iterator<Tuple<NodeId>> iter)
    {
        for ( int i = 0 ; iter.hasNext() ; i++ )
        {
            Tuple<NodeId> tuple = iter.next();
            System.out.printf("%2d: %s\n", i, tuple) ;
        } 
    }
    
    private static void printNative(TripleIndex index)
    {
        print(index.tuplesNativeOrder()) ;
    }
    
    private static void stats()
    {
        String bulkLoaderClass = "com.hp.hpl.jena.tdb.base.loader.BulkReader" ;
        RDFReaderFImpl.setBaseReaderClassName("N-TRIPLES", bulkLoaderClass) ;
        RDFReaderFImpl.setBaseReaderClassName("N-TRIPLE", bulkLoaderClass) ;
        StatsGraph g = new StatsGraph() ;
        Model m = ModelFactory.createModelForGraph(g) ;
        FileManager.get().readModel(m, "Workspace/dbp-infoboxes.nt") ;
        g.printStats() ;
        System.exit(0) ;
    }
    
    private static void BCD()
    {
        System.out.printf("0x%X\n", BCD.asBCD(15)) ;
        System.out.printf("%d\n", BCD.toInt(BCD.asBCD(15))) ;
        

        System.out.println("Strings") ;
        System.out.println(BCD.nibbleStrZeros(0x987654321L)) ;
        System.out.println(BCD.nibbleStr(0x987654321L)) ;
        System.out.println(BCD.nibbleStrLow(0x987654321L, 2)) ;
        System.out.println(BCD.nibbleStrHigh(0x987654321L, 10)) ;
        System.out.println(BCD.nibbleStr(0x987654321L, 2,4)) ;
        
        System.out.println(BCD.nibbleStrZeros(0x0L)) ;
        System.out.println(BCD.nibbleStr(0x0L)) ;
        
        System.out.println("Finished") ;
        
        System.exit(0) ;
    }
    
    private static void updateMusicBrainz()
    {
        String[] a = {"--desc=dataset-bdb.ttl", "LOAD <D.ttl>" } ;
        arq.update.main(a) ;
        queryMusicBrainz() ;
        System.exit(0) ;
    }

    
    private static void queryMusicBrainz()
    {
        String x = "<http://musicbrainz.org/mm-2.1/artist/456f9e8e-f3d8-464d-9e54-1c475016719d>" ;
        String qs = StrUtils.strjoinNL(
                                       "PREFIX  dc:     <http://purl.org/dc/elements/1.1/>",
                                       "SELECT * { "+x+" dc:title ?title }"
                                       );
//                                       "                dc:creator ?c . }" );
////                                       "           ?c ?p ?v }") ;
//
//        Model model = TDBFactory.createModel("tmp") ;
//        
//        Query query = QueryFactory.create(qs) ;
//        QueryExecution qExec = QueryExecutionFactory.create(query, model) ;
//        
//        
//        QueryExecUtils.executeQuery(query, qExec) ;
        // Causes TDB initialization.
        String[] a = {"--desc=dataset.ttl", qs } ;
        arq.sparql.main(a) ;
        System.exit(0) ;
    }


    public static void BDB()
    {
        Environment myDbEnvironment = null;
        Database myDatabase = null ;

        try {
            try {
                EnvironmentConfig envConfig = new EnvironmentConfig();
                envConfig.setAllowCreate(true);
                //envConfig.setTransactional(true) ;
                myDbEnvironment = new Environment(new File("tmp/dbEnv"), envConfig);

                // Open the database. Create it if it does not already exist.
                DatabaseConfig dbConfig = new DatabaseConfig();
                //dbConfig.setTransactional(true) ;

                dbConfig.setAllowCreate(true);
                myDatabase = myDbEnvironment.openDatabase(null, 
                                                          "GRAPH", 
                                                          dbConfig); 
                String aKey = "key" ;
                DatabaseEntry theKey = new DatabaseEntry(aKey.getBytes("UTF-8"));
                DatabaseEntry theData = new DatabaseEntry("DATA".getBytes("UTF-8"));

//                if ( myDatabase.put(null, theKey, theData) !=
//                    OperationStatus.SUCCESS )
//                {
//                    System.out.println("Bad put") ;
//                    return ;
//                }

                // Perform the get.
                if (myDatabase.get(null, theKey, theData, LockMode.DEFAULT) ==
                    OperationStatus.SUCCESS) {
                    System.out.println(theData) ;
                    String s = new String(theData.getData(), "UTF-8") ;
                    System.out.println("Get: "+s) ;
                }


            } catch (DatabaseException dbe) {
                dbe.printStackTrace(); 
                // Exception handling goes here
            } catch (UnsupportedEncodingException ex)
            {
                ex.printStackTrace();
            } 
            try {
                if (myDatabase != null) {
                    myDatabase.close();
                }
                if (myDbEnvironment != null) {
                    myDbEnvironment.cleanLog();
                    myDbEnvironment.close();
                } 
            } catch (DatabaseException dbe) {
                dbe.printStackTrace();
            }
        } finally { 
            System.out.println("Finished/BDB") ;
            System.exit(0) ;
        }
    }
    
    public static void graph()
    {
        Node x1 = Node.createURI("x1") ;
        Node y1 = Node.createURI("y1") ;
        Node z1 = Node.createURI("z1") ;

        Node x2 = Node.createURI("x2") ;
        Node y2 = Node.createURI("y2") ;
        Node z2 = Node.createURI("z2") ;

        //Graph g = new GraphBDB("tmp/GraphBDB") ;
        //Graph g = new GraphBTree(new Location("tmp")) ;
        Graph g = new GraphBTree() ;
        System.out.println("Add "+new Triple(x1, y1, z1)) ;
        g.add(new Triple(x1, y1, z1)) ;

        System.out.println("Add "+new Triple(x1, y2, z2)) ;
        g.add(new Triple(x1, y2, z2)) ;
        
//        System.out.println("Add (again) "+new Triple(x1, y1, z1)) ;
//        g.add(new Triple(x1, y1, z1)) ;

        
        {
            ExtendedIterator iter = null ;
            
            ((PGraphBase)g).dumpIndexes() ;
            
//            System.out.println("find(x1, y1, z1)") ;
//            iter = g.find(x1, y1, z1) ;
//            System.out.println(iter.hasNext()) ;

            System.out.println("find(ANY, y1, z1)") ;
            iter = g.find(Node.ANY, y1, z1) ;
            System.out.println(iter.hasNext()) ;
            
            System.out.println("contains(ANY, y1, z2)") ;
            System.out.println(g.contains(Node.ANY, y1, z2)) ;

            System.out.println("contains(ANY, y2, z1)") ;
            System.out.println(g.contains(Node.ANY, y2, z1));
            
            System.exit(0) ;
        }
        
        
        
        System.out.println("find(Node.ANY, null, null)") ;
        ExtendedIterator iter = g.find(Node.ANY, null, null) ;
        while ( iter.hasNext() )
        {
            Triple t = (Triple)iter.next() ;
            System.out.println(t) ;
        }
        iter.close();
      
        System.out.println("find(x1, null, null)") ;
        ExtendedIterator iter2 = g.find(x1, null, null) ;
        while ( iter2.hasNext() )
        {
            Triple t = (Triple)iter2.next() ;
            System.out.println(t) ;
        }
        iter2.close();
        
        System.out.println("find(x1, null, z1)") ;
        ExtendedIterator iter3 = g.find(x1, null, z1) ;
        while ( iter3.hasNext() )
        {
            Triple t = (Triple)iter3.next() ;
            System.out.println(t) ;
        }
        iter3.close();

        System.out.println("find(null, null, z1)") ;
        ExtendedIterator iter4 = g.find(null, null, z1) ;
        while ( iter4.hasNext() )
        {
            Triple t = (Triple)iter4.next() ;
            System.out.println(t) ;
        }
        iter4.close();

        System.out.println("g.size() = "+g.size()) ;

        System.out.println("contains(Node.ANY, y1, z1)") ;
        //System.out.println(g.contains(x1, y1, z1)) ;
        //g.contains(Node.ANY, y1, z1) ;
        System.out.println(g.contains(Node.ANY, y1, z1)) ;
        
        g.close() ;
        System.out.println("GraphBDB - end") ;
        System.exit(0) ;
    }

    private static void query(String str, Model model)
    {
        System.out.println(str) ; 
        Query q = QueryFactory.create(str) ;
        QueryExecution qexec = QueryExecutionFactory.create(q, model) ;
        ResultSet rs = qexec.execSelect() ;
        ResultSetFormatter.out(rs) ;
        qexec.close() ;
    }
}

/*
 * (c) Copyright 2007, 2008 Hewlett-Packard Development Company, LP
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