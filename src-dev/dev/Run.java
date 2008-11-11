/*
 * (c) Copyright 2007, 2008 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package dev;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import lib.Bytes;
import lib.FileOps;
import lib.Tuple;
import lib.cache.CacheNG;
import arq.cmd.CmdUtils;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.RSIterator;

import com.hp.hpl.jena.util.FileManager;

import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;

import com.hp.hpl.jena.sparql.algebra.Algebra;
import com.hp.hpl.jena.sparql.algebra.Op;
import com.hp.hpl.jena.sparql.algebra.Transformer;
import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.sparql.engine.main.StageGenerator;
import com.hp.hpl.jena.sparql.engine.optimizer.StageGenOptimizedBasicPattern;
import com.hp.hpl.jena.sparql.sse.SSE;

import com.hp.hpl.jena.query.*;

import com.hp.hpl.jena.tdb.TDB;
import com.hp.hpl.jena.tdb.TDBFactory;
import com.hp.hpl.jena.tdb.base.file.Location;
import com.hp.hpl.jena.tdb.base.record.Record;
import com.hp.hpl.jena.tdb.base.record.RecordFactory;
import com.hp.hpl.jena.tdb.index.Index;
import com.hp.hpl.jena.tdb.index.IndexFactoryExtHash;
import com.hp.hpl.jena.tdb.pgraph.GraphTDB;
import com.hp.hpl.jena.tdb.solver.StageGeneratorGeneric;
import com.hp.hpl.jena.tdb.solver.reorder.ReorderLib;
import com.hp.hpl.jena.tdb.solver.reorder.ReorderTransformation;
import com.hp.hpl.jena.tdb.sys.SystemTDB;

import dev.idx2.ColumnMap;
import dev.idx2.TmpFactory;
import dev.opt.Reorganise;
import dev.opt.Scope;
import dev.opt.TransformIndexJoin;

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

    static { CmdUtils.setLog4j() ; }
 
    public static void main(String ... args) throws IOException
    {
        Graph g = TmpFactory.createGraphMem() ;
        Model m = ModelFactory.createModelForGraph(g) ;
        FileManager.get().readModel(m, "D.ttl") ;
        
        query("SELECT * { ?s ?p ?o}", m) ;
        System.exit(0) ;
        
        
        ColumnMap  d = new ColumnMap("SPO", "POS") ;
        System.out.println(d.toString()) ;
        Tuple<String> t = new Tuple<String>("S", "P", "O") ;
        System.out.println(d.fetchSlot(0, t)) ;
        System.out.println(d.fetchSlot(1, t)) ;
        System.out.println(d.fetchSlot(2, t)) ;
        
        System.out.println(d.map(t)) ;
        
        System.exit(0) ;
        
        
        
        
        
        
        //System.setProperty("tdb:settings", "tdb.properties") ;
        FileOps.clearDirectory("DB") ;
        tdbloader("--stats", "D.ttl") ;
        System.exit(0) ;
        //extHash() ; 
        
        tdbquery("--set=tdb:logExec=true", "--file=Q.rq") ;
        System.exit(0) ;
        
        memOpt() ; 
        
//        TDBFactory.assembleGraph( "Store/gbt.ttl") ;
//        System.out.println("Assembled") ;
//        System.exit(0) ;
        
        Op op = SSE.parseOp("(join (bgp (?w :q 123)) (filter (!= ?x :X) (bgp (?x :p ?v) (?x :q ?w))))") ;
        
        Map <Op, Set<Var>> x = Scope.scopeMap(op) ;
        if ( false )
        {
            for ( Op k : x.keySet() )
            {
                Set<Var> vars = x.get(k) ;
                String s = k.toString();
                s = s.replaceAll("\n", " ") ;
                s = s.replaceAll("\r", " ") ;
                s = s.replaceAll("  +", " ") ;
                System.out.println(vars+" <==> "+s) ;
            }
        }
        
        System.out.println(op) ;
        op = Reorganise.reorganise(op, x) ;
        System.out.println(op) ;
        System.out.println("----") ;
        
        System.exit(0) ;
        //smallGraph() ;
        //tdbloader("--desc=tdb.ttl", "--mem", "/home/afs/Datasets/MusicBrainz/tracks.nt") ;
 
        //indexification() ; System.exit(0) ;
        reification() ; System.exit(0) ;
        rewrite() ; System.exit(0) ;
        
        //cache2() ;
        //tdbquery("dataset.ttl", "SELECT * { ?s ?p ?o }") ;

//      Model model = TDBFactory.createModel("tmp") ;
//      query("SELECT * { ?s ?p ?o}", model) ;
//      System.exit(0) ;
    }
    
 
    private static void extHash()
    {
        IndexFactoryExtHash f = new IndexFactoryExtHash(8*1024) ;
        Location loc = new Location("tmp") ;
        RecordFactory rf = new RecordFactory(4,8) ;
        Index idx = f.createIndex(loc, "hash", rf) ;
        byte[] k = Bytes.packInt(12) ;
        byte[] v = Bytes.packLong(0x1234567812345678L) ;
        Record r = rf.create(k, v) ;
        idx.add(r) ;
        idx.close();
        // ---
        idx = f.createIndex(loc, "hash", rf) ;
        
        System.out.println("----") ;
        for ( Iterator<Record> iter = idx.iterator() ; iter.hasNext() ; )
        {
            Record r2 = iter.next() ;
            System.out.println(r2) ;
        }
        System.out.println("----") ;
        System.exit(0) ;
    }


    private static void memOpt()
    {
        if ( true )
        {
            StageGenOptimizedBasicPattern x = null ;
            StageGenerator stageGenerator = new StageGeneratorGeneric() ;
            ARQ.getContext().set(ARQ.stageGenerator, stageGenerator) ;
        }
        Model model = FileManager.get().loadModel("D.ttl") ;
        Query q = QueryFactory.read("Q.rq") ;
        QueryExecution qexec = QueryExecutionFactory.create(q, model) ;
        ResultSet rs = qexec.execSelect() ;
        ResultSetFormatter.out(rs) ;
        qexec.close() ;
    }
    
    private static void reification()
    {
        FileOps.clearDirectory("DB2") ;
        //Graph graph = TDBFactory.createGraph("DB2") ;
        Model model = TDBFactory.createModel("DB2") ;
//        Model model = new ModelCom( Factory.createGraphMem( ReificationStyle.Standard ) );
//        System.out.println(model.getReificationStyle()) ;
        
        FileManager.get().readModel(model, "D.ttl") ;
        
//        Resource x = model.createResource("http://example/x") ;
//        Property p = model.createProperty("http://example/p") ;
//        Literal z = model.createLiteral("z") ;
//
//        Statement s = model.createStatement(x, p, z) ;
//        model.add(s) ;
//        model.write(System.out, "TTL") ;
//        System.out.println("--------") ;
//        ReifiedStatement rs = model.createReifiedStatement(s) ;
        
        RSIterator rsIter = model.listReifiedStatements() ;
        while(rsIter.hasNext())
            System.out.println("**"+rsIter.next()) ;
        
        
        model.write(System.out, "TTL") ;
        System.out.println("--------") ;
        model.close();
        
        
        
        
        //rs = model.getAnyReifiedStatement(s) ;
        
        //((GraphTDB)model.getGraph()).sync(true) ;
        
        //Without closing the model.
        model = TDBFactory.createModel("DB2") ;
        model.write(System.out, "TTL") ;
        
    }
    
    private static void indexification()
    {
        Op op = SSE.readOp("Q.sse") ;
        System.out.println(op) ;
        System.out.println("----") ;
        op = Transformer.transform(new TransformIndexJoin(), op) ;
        System.out.println(op) ;
        System.out.println("----") ;
    }

    public static void rewrite()
    {
        ReorderTransformation reorder = null ;
        if ( false )
            reorder = ReorderLib.fixed() ;
        else
        {
            reorder = ReorderLib.weighted("stats.sse") ;
        }
        Query query = QueryFactory.read("Q.rq") ;
        Op op = Algebra.compile(query) ;
        System.out.println(op) ;
        
        op = Transformer.transform(new TransformReorderBGP(reorder), op) ;
        System.out.println(op) ;
        System.exit(0) ;
    }
    
    private static void cache2()
    {
        CacheNG<Integer, String> pool = new CacheNG<Integer, String>(2) ;
        pool.putObject(1, "X1") ;
        pool.putObject(2, "X2") ;
        pool.putObject(3, "X3") ;
        
        System.out.println(pool.contains(1)) ;
        System.out.println(pool.contains(2)) ;
        
        System.out.println(pool.getObject(3)) ;
        System.out.println(pool.getObject(3)) ;
        System.out.println(pool.getObject(3, true)) ;
        
        
        System.exit(0) ;
    }

 
    
    static public void smallGraph() 
    {
        // Do NOW!
        // TDB.getContext().set(TDB.symFileMode, "mapped") ;
        TDB.getContext().set(SystemTDB.symIndexType, "bplustree") ;
        
        Location loc = new Location("tmp") ;
        //FileOps.clearDirectory(loc.getDirectoryPath()) ;
        
        Graph graph = TDBFactory.createGraph(loc) ;
        
        if ( true )
        {
            
            Node s1 = SSE.parseNode("<s1>") ;
            Node p1 = SSE.parseNode("<p1>") ;
            Node o1 = SSE.parseNode("<o1>") ;
            Node s2 = SSE.parseNode("<s2>") ;
            Node p2 = SSE.parseNode("<p2>") ;
            Node o2 = SSE.parseNode("<o2>") ;
            Triple t1 = new Triple(s1,p1,o1) ; 
            Triple t2 = new Triple(s2,p2,o2) ;
            
            graph.add(t1) ;
            graph.add(t2) ;
            graph.delete(t1) ;
            
            graph.contains(s2, p2, SSE.parseNode("5")) ;
        }
        
        Model model = ModelFactory.createModelForGraph(graph) ;
        
        //query("SELECT * { <no> <no> <no>}", model) ;
        //System.out.flush() ;
        
        if ( model.isEmpty() )
        {  
            System.out.println("**** Load data") ;
            FileManager.get().readModel(model, "D.ttl") ;
            ((GraphTDB)graph).sync(true) ;
            System.out.println("Size = "+model.size()) ;
        }
        query("SELECT * { ?s ?p ?o }", model) ;
        
        
        model.close() ;
        
        System.exit(0) ;
        
    }
    
    private static void query(String str, Model model)
    {
        System.out.println(str) ; 
        Query q = QueryFactory.create(str, Syntax.syntaxARQ) ;
        QueryExecution qexec = QueryExecutionFactory.create(q, model) ;
        ResultSet rs = qexec.execSelect() ;
        ResultSetFormatter.out(rs) ;
        qexec.close() ;
    }
    
    private static void tdbquery(String... args)
    {
        tdb.tdbquery.main(args) ;
        System.exit(0) ;
    }
    
    private static void tdbloader(String... args)
    {
        tdb.tdbloader.main(args) ; 
        System.exit(0) ;
    }
    
    private static void tdbdump(String... args)
    {
        tdb.tdbdump.main(args) ; 
        System.exit(0) ;
    }
    
    private static void tdbconfig(String... args) 
    {
        tdb.tdbconfig.main(args) ;
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