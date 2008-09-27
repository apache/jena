/*
 * (c) Copyright 2007, 2008 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package dev;

import java.io.IOException;

import lib.FileOps;
import lib.Pair;
import lib.cache.Cache2;
import arq.cmd.CmdUtils;

import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.ResultSetFormatter;
import com.hp.hpl.jena.query.Syntax;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.sparql.algebra.Algebra;
import com.hp.hpl.jena.sparql.algebra.Op;
import com.hp.hpl.jena.sparql.algebra.Transformer;
import com.hp.hpl.jena.sparql.engine.main.OpCompiler;
import com.hp.hpl.jena.sparql.sse.SSE;
import com.hp.hpl.jena.sparql.util.QueryExecUtils;
import com.hp.hpl.jena.tdb.TDB;
import com.hp.hpl.jena.tdb.TDBFactory;
import com.hp.hpl.jena.tdb.base.block.BlockMgr;
import com.hp.hpl.jena.tdb.base.block.BlockMgrFactory;
import com.hp.hpl.jena.tdb.base.buffer.RecordBuffer;
import com.hp.hpl.jena.tdb.base.file.Location;
import com.hp.hpl.jena.tdb.base.record.RecordFactory;
import com.hp.hpl.jena.tdb.base.recordfile.RecordBufferPage;
import com.hp.hpl.jena.tdb.base.recordfile.RecordBufferPageMgr;
import com.hp.hpl.jena.tdb.pgraph.GraphTDB;
import com.hp.hpl.jena.tdb.solver.reorder.ReorderFixed;
import com.hp.hpl.jena.tdb.solver.reorder.ReorderTransformation;
import com.hp.hpl.jena.tdb.solver.reorder.ReorderWeighted;
import com.hp.hpl.jena.tdb.solver.stats.StatsMatcher;
import com.hp.hpl.jena.tdb.sys.SystemTDB;
import com.hp.hpl.jena.util.FileManager;

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
        //smallGraph() ;
        //tdbloader("--desc=tdb.ttl", "--mem", "/home/afs/Datasets/MusicBrainz/tracks.nt") ;
 
        //indexification() ; System.exit(0) ;
        
        altCompile() ; System.exit(0) ;
        rewrite() ; System.exit(0) ;
        
        String[] a = { "--desc=tdb.ttl", 
                       "--set=tdb:logBGP=true",
                       "--file=Q.rq" } ;
        tdb.tdbquery.main(a) ;
        System.exit(0) ;
        
        //cache2() ;
        
        processBPTree() ;
        
        //tdbquery("dataset.ttl", "SELECT * { ?s ?p ?o }") ;

//      Model model = TDBFactory.createModel("tmp") ;
//      query("SELECT * { ?s ?p ?o}", model) ;
//      System.exit(0) ;

        
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

    public static void altCompile()
    {
        // Rewire.
        TDB.init();
        OpCompiler.factory = OpCompilerTDB.altFactory ;
        Query query = QueryFactory.read("Q.rq") ;

        Op op = Algebra.compile(query) ;
        System.out.println(op) ;
        Model model = TDBFactory.createModel("DB") ;
        QueryExecution qexec = QueryExecutionFactory.create(query, model) ;
        QueryExecUtils.executeQuery(query, qexec) ;
        System.out.println("----") ;
        System.exit(0) ;
        
    }
    
    public static void rewrite()
    {
        ReorderTransformation reorder = null ;
        if ( false )
            reorder = new ReorderFixed() ;
        else
        {
            StatsMatcher matcher = new StatsMatcher("stats.sse") ;
            //StatsMatcher matcher = new StatsMatcher("stats-var-count.opt") ;
            reorder = new ReorderWeighted(matcher) ;
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
        Cache2<Integer, String> pool = new Cache2<Integer, String>(2) ;
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

    private static void processBPTree()
    {
        divider() ;
        
        
        String root1 = "DB/OSP" ;
        String root2 = "DB/OSP-2" ;
        FileOps.delete(root2+".dat") ;

        Pair<Long, Long> results = BPlusTreeRewriter.rewrite(root1, root2) ;
        long total = results.car() ;
        long blocks = results.cdr() ;
        System.out.printf("Count = %d in %d blocks\n", total, blocks) ;
        System.out.println() ;
        scan(root1+".dat") ;
        scan(root2+".dat") ;
        
        // Now scan for split points
        BPlusTreeRewriter.phase2(root2+".dat") ; 
        
        System.exit(0) ;
    }

    // Checker.
    // Assumes the file is not open anywhere else.
    static void scan(String filename)
    {
        // Rely on OS read ahead.
        BlockMgr blkMgr = BlockMgrFactory.createStdFileNoCache(filename, SystemTDB.BlockSize) ;
        RecordFactory f = GraphTDB.indexRecordFactory ; 
        RecordBufferPageMgr recordPageMgr = new RecordBufferPageMgr(f, blkMgr) ;
        int idx = 0 ;
        int n = 0 ;
        int total = 0 ;
        
        while ( idx >= 0 )
        {
            RecordBufferPage page = recordPageMgr.get(idx) ;
            //System.out.printf("%04d :: %04d -> %04d [%d, %d]\n", n, page.getId(), page.getLink(), page.getCount(), page.getMaxSize()) ;
            RecordBuffer rb = page.getRecordBuffer() ;
            //System.out.printf("     :: %d %d\n", rb.getSize(), rb.maxSize() ) ;
            total += rb.size();
            idx = page.getLink() ;
            n++ ;
        }
        blkMgr.close() ;
        System.out.printf("%-15s [Scan] Count = %d in %d blocks\n", filename, total, n) ;
    }
    
    
    
    static public void smallGraph() 
    {
        // Do NOW!
        // TDB.getContext().set(TDB.symFileMode, "mapped") ;
        TDB.getContext().set(TDB.symIndexType, "bplustree") ;
        
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
    
    private static void tdbquery(String query)
    {
        String[] a = { query } ;
        tdb.tdbquery.main(a) ;
        System.exit(0) ;
    }
    
    private static void tdbquery(String assembler, String query)
    {
        String[] a = { "--set", "tdb:logBGP=true", "--desc="+assembler, query } ;
        tdb.tdbquery.main(a) ;
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
    
    private static void tdbconfig(String assembler, String file)
    {
        tdb.tdbconfig.main("stats", "--desc="+assembler) ;
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