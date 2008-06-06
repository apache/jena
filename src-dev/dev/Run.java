/*
 * ong time (c) Copyright 2007, 2008 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package dev;

import static lib.FileOps.clearDirectory;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import lib.FileOps;

import com.hp.hpl.jena.assembler.JA;
import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.ResultSetFormatter;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.shared.PrefixMapping;
import com.hp.hpl.jena.sparql.core.Prologue;
import com.hp.hpl.jena.sparql.core.assembler.AssemblerUtils;
import com.hp.hpl.jena.sparql.sse.SSE;
import com.hp.hpl.jena.sparql.sse.writers.WriterBasePrefix;
import com.hp.hpl.jena.sparql.util.FmtUtils;
import com.hp.hpl.jena.sparql.util.IndentedWriter;
import com.hp.hpl.jena.tdb.TDBFactory;
import com.hp.hpl.jena.tdb.base.record.RecordFactory;
import com.hp.hpl.jena.tdb.btree.BTreeParams;
import com.hp.hpl.jena.tdb.lib.StringAbbrev;
import com.hp.hpl.jena.tdb.pgraph.GraphBTree;
import com.hp.hpl.jena.tdb.pgraph.NodeId;
import com.hp.hpl.jena.tdb.pgraph.PGraphBase;
import com.hp.hpl.jena.tdb.solver.StageGeneratorPGraphBGP;
import com.sleepycat.je.*;

public class Run
{
    static String divider = "" ;
    static String nextDivider = null ;
    static void divider()
    {
        if ( nextDivider != null )
            System.out.println(nextDivider) ;
        nextDivider = "" ;
    }
    
    
    public static void main(String ... args)
    {

        tdbquery("dataset.ttl", "SELECT * { ?s ?p 1}") ;
        
        Node s = SSE.parseNode("<http://example/x>") ;
        Node p = SSE.parseNode("<http://example/p>") ;
        Node n = SSE.parseNode("'1'^^<http://www.w3.org/2001/XMLSchema#int>") ;
        
        FileOps.clearDirectory("tmp") ;
        Graph g = TDBFactory.createGraph("tmp") ;
        g.add(new Triple(s,p,n)) ;
        System.out.println(g) ;
        g.close();
        System.exit(0) ;
        
        
        NodeId nodeId = NodeId.inline(n) ;
        System.out.println(nodeId) ;
        System.exit(0) ;
        
        btreePacking(3, 32, 8*1024) ; System.exit(0) ;
        tdb.tdbconfig.main("stats", "--desc=dataset.ttl") ;
        System.exit(0) ;
                
//        typedNode("'2008-04-27T16:52:17+01:00'^^xsd:dateTime") ;
//        typedNode("'2008-04-27T16:52:17-05:00'^^xsd:dateTime") ;
//        typedNode("'2008-04-27T16:52:17Z'^^xsd:dateTime") ;
//        typedNode("'2008-04-27T16:52:17+00:00'^^xsd:dateTime") ;
        typedNode("'2008-04-27T16:52:17'^^xsd:dateTime") ;
        typedNode("'2008-04-27'^^xsd:date") ;
        System.exit(0) ;
        
        btreePacking(3, 64, 8*1024) ;
        btreePacking(4, 128, 8*1024) ;
        System.exit(0) ;
        
        if ( false )
        {
            List<Triple> triples = new ArrayList<Triple>() ;

            triples.add(SSE.parseTriple("(?x ?s ?p)")) ;
            triples.add(SSE.parseTriple("(?x <p> ?y)")) ;
            triples.add(SSE.parseTriple("(?x <p> 12)")) ;

            PGraphBase graph = GraphBTree.create() ;
            triples = StageGeneratorPGraphBGP.reorder(graph, triples) ;

            for ( Triple t : triples )
                System.out.println(t) ;
            System.exit(0) ;
        }
        
//        tdb.tdbloader.main("--desc", "dataset.ttl", "D.ttl") ;
        tdb.tdbquery.main(new String[]{"--desc=dataset.ttl", "--query=Q.rq"}) ;
        System.exit(0) ;
        
        
        prefixes() ; 
        System.exit(0) ;
        
        String[] a = { "--desc", "dataset.ttl", "--", "-"};
        tdb.tdbloader.main(a) ;
        System.exit(0) ;
        
        String dir = "tmp" ;
        clearDirectory(dir) ;
        
        
        
        System.exit(0) ;
    }
     
    public static void btreePacking(int slots, int slotSize, int blkSize)
    {
        divider() ;
        RecordFactory f  = new RecordFactory(slots*slotSize/8,0) ;
        System.out.printf("Input: %d slots, size %d bytes, %d blocksize\n", slots,slotSize/8, blkSize ) ;
        System.out.println("Btree: "+BTreeParams.calcOrder(blkSize, f.recordLength())) ;      
        System.out.println("Packed leaf : "+blkSize/f.recordLength()) ;
        BTreeParams p = new BTreeParams(BTreeParams.calcOrder(blkSize, f.recordLength()), f) ;
        System.out.println(p) ;
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
    
    private static void query(String str, Model model)
    {
        System.out.println(str) ; 
        Query q = QueryFactory.create(str) ;
        QueryExecution qexec = QueryExecutionFactory.create(q, model) ;
        ResultSet rs = qexec.execSelect() ;
        ResultSetFormatter.out(rs) ;
        qexec.close() ;
    }
    
    private static void tdbquery(String assembler, String query)
    {
        String[] a = { "--desc="+assembler, query } ;
        tdb.tdbquery.main(a) ;
        System.exit(0) ;
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