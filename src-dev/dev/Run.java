/*
 * (c) Copyright 2007, 2008 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package dev;

import static com.hp.hpl.jena.tdb.sys.Names.tripleIndexes;

import java.io.IOException;
import java.util.Map;

import lib.FileOps;
import lib.cache.CacheNG;
import arq.cmd.CmdUtils;

import com.hp.hpl.jena.rdf.model.*;

import com.hp.hpl.jena.util.FileManager;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.shared.PrefixMapping;
import com.hp.hpl.jena.shared.ReificationStyle;

import com.hp.hpl.jena.sparql.algebra.Algebra;
import com.hp.hpl.jena.sparql.algebra.Op;
import com.hp.hpl.jena.sparql.algebra.Transformer;
import com.hp.hpl.jena.sparql.core.Quad;
import com.hp.hpl.jena.sparql.sse.SSE;

import com.hp.hpl.jena.query.*;

import com.hp.hpl.jena.tdb.TDBFactory;
import com.hp.hpl.jena.tdb.base.block.BlockMgrMem;
import com.hp.hpl.jena.tdb.base.file.Location;
import com.hp.hpl.jena.tdb.index.IndexBuilder;
import com.hp.hpl.jena.tdb.nodetable.NodeTable;
import com.hp.hpl.jena.tdb.nodetable.NodeTableFactory;
import com.hp.hpl.jena.tdb.solver.reorder.ReorderLib;
import com.hp.hpl.jena.tdb.solver.reorder.ReorderTransformation;
import com.hp.hpl.jena.tdb.store.*;

import dev.opt.TransformIndexJoin;

public class Run
{
    static String divider = "----------" ;
    static String nextDivider = null ;
    static void divider()
    {
        if ( nextDivider != null )
            System.out.println(nextDivider) ;
        nextDivider = divider ;
    }

    static { CmdUtils.setLog4j() ; }
 
    public static void main(String ... args) throws IOException
    {
        //prefixes() ; System.exit(0) ;
        tdb.tdbquery.main(new String[]{"--tdb=tdb.ttl", "--set=tdb:unionDefaultGraph=true", "--query=Q.arq"}) ; System.exit(0) ;
        //tdb.tdbdump.main("--tdb=tdb.ttl") ; System.exit(0) ;
        
        namedGraphs() ;        
        
        reification() ;
        
        
        tdb.perf.tdbperf.main("parse", "/home/afs/Datasets/MusicBrainz/tracks-10k.nt") ; System.exit(0) ;
        
        tdbquery("--tdb=tdb.ttl", "SELECT count(*) { ?s ?p ?o }") ;
        
        
        tdbquery("--set=tdb:logExec=true", "--file=Q.rq") ;
        System.exit(0) ;
        rewrite() ; System.exit(0) ;
    }
    
    private static void prefixes()
    {
        prefixes1() ;
        prefixes2() ;
        System.exit(0) ;
        
    }
    
    private static void prefixes1()
    {
        
        Location location = new Location("DB2") ;
        FileOps.clearDirectory("DB2") ;
        String graphName = "http://graph/" ;
        Dataset ds = TDBFactory.createDataset(location) ;
        DatasetGraphTDB dsg = (DatasetGraphTDB)ds.asDatasetGraph() ;
        PrefixMapping pmap = dsg.getPrefixes().getPrefixMapping(graphName) ;
        
//        DatasetPrefixes dsp = new DatasetPrefixes(location) ;
//        PrefixMapping pmap = dsp.getPrefixMapping(graphName) ;
        
        pmap.setNsPrefix("x", "http://example/") ;
        
        String x = pmap.expandPrefix("x:foo") ;
        System.out.println("x:foo ==> "+x) ;
        pmap.setNsPrefix("x", "http://example/ns#") ;
        pmap.setNsPrefix("y", "http://example/y#") ;
        String x2 = pmap.expandPrefix("x:foo") ;
        System.out.println("x:foo ==> "+x2) ;
        System.out.println("** >>") ;
        printPrefixMapping(pmap) ;
        System.out.println("<<End>>") ;
//        dsp.close() ;
    }

    private static void prefixes2()
    {
        Location location = new Location("DB2") ;

        DatasetPrefixes dsp = new DatasetPrefixes(location) ;
        String graphName = "http://graph/" ;
        PrefixMapping pmap1 = dsp.getPrefixMapping(graphName) ;
        PrefixMapping pmap2 = dsp.getPrefixMapping() ;
        
        System.out.println("1: "+pmap1.expandPrefix("x:foo")) ;
        System.out.println("2: "+pmap2.expandPrefix("x:foo")) ;
        
        String x2 = pmap1.expandPrefix("x:foo") ;
        
        pmap2.setNsPrefix("ns", "http://ns/#") ;
        System.out.println("1: ns:bar ==> "+pmap1.expandPrefix("ns:bar")) ;
        System.out.println("2: ns:bar ==> "+pmap2.expandPrefix("ns:bar")) ;
        
        System.out.println("1 >>") ;
        printPrefixMapping(pmap1) ;

        System.out.println("2 >>") ;
        printPrefixMapping(pmap2) ;
        
        System.out.println("<<End>>") ;
        dsp.close() ;
    }
    
    private static void printPrefixMapping(PrefixMapping pmap)
    {
        @SuppressWarnings("unchecked")
        Map<String, String> x = (Map<String, String>)pmap.getNsPrefixMap() ;
        for ( String k : x.keySet() )
            System.out.println(k+" : "+x.get(k)) ;
    }
    
    private static void reification()
    {
        FileOps.clearDirectory("DB") ;
        divider() ;
        Model m = ModelFactory.createDefaultModel(ReificationStyle.Standard) ;
        
        m = TDBFactory.createModel("DB") ;
        
        m.setNsPrefixes(PrefixMapping.Standard) ;
        
        Resource r1 = m.createResource("http://example/r1") ;
        Resource r2 = m.createResource("http://example/r2") ;
        Property p1 = m.createProperty("http://example/p1") ;
        Property p2 = m.createProperty("http://example/p2") ;
        Literal lit1 = m.createLiteral("ABC") ;
        Literal lit2 = m.createLiteral("XYZ") ;
        
        Statement stmt1 = m.createStatement(r1, p1, lit1) ;
        Statement stmt2 = m.createStatement(r1, p2, lit2) ;
        ReifiedStatement rs1 = m.createReifiedStatement(stmt1) ;
        ReifiedStatement rs2 = m.createReifiedStatement(stmt2) ;
        
        Resource r = m.getAnyReifiedStatement(stmt2) ;
        System.out.println("r = "+r) ;
        
        RSIterator rsIter = m.listReifiedStatements() ;
        while(rsIter.hasNext())
        {
            ReifiedStatement rs = rsIter.nextRS() ;
            System.out.println(rs) ;
        }
        
        divider() ;
        m.write(System.out, "TTL") ;
        m.close();
        divider() ;
        m = TDBFactory.createModel("DB") ;
        m.setNsPrefixes(PrefixMapping.Standard) ;
        m.write(System.out, "TTL") ;
        divider() ;
        
        System.exit(0) ;
    }

    private static GraphTDB setup()
    {
        // Setup a graph - for experimental alternatives.
        BlockMgrMem.SafeMode = false ;
        IndexBuilder indexBuilder = IndexBuilder.mem() ;
        Location location = null ;
        
        NodeTable nodeTable = NodeTableFactory.create(indexBuilder, location) ;
        
        TripleTable table = FactoryGraphTDB.createTripleTable(indexBuilder, nodeTable, location, tripleIndexes) ; 
        ReorderTransformation transform = ReorderLib.identity() ;
        DatasetPrefixes prefixes = new DatasetPrefixes(indexBuilder, location) ;
        GraphTDB g = new GraphTriplesTDB(table, prefixes, transform, location) ;
        return g ;
    }

    private static void namedGraphs()
    {
        FileOps.clearDirectory("DS") ;
        Dataset ds = TDBFactory.assembleDataset("tdb-ds.ttl") ;
        //Dataset ds = TDBFactory.createDataset(new Location("DS")) ;
        
        //SSE.write(ds) ;
        
        if ( true )
        {
            Model mNamed1 = ds.getNamedModel("http://example/d1/") ;
            Model mNamed2 = ds.getNamedModel("http://example/d2/") ;
            Model dftModel = ds.getDefaultModel() ;

            FileManager.get().readModel(dftModel, "D.ttl") ;
            FileManager.get().readModel(mNamed1, "D1.ttl") ;
            FileManager.get().readModel(mNamed2, "D2.ttl") ;
        }
        Node n[] =     { null, Node.createURI("http://example/d2/") , Quad.defaultGraphIRI, Quad.defaultGraphNode, Quad.unionGraph } ;
        String str[] = { "?g", "d2", "defaultGraphIRI", "defaultGraphNode", "unionGraph" } ;
        
//        Node n[] = { Quad.unionGraph } ;
//        String str[] = { "unionGraph" } ;
        
//        System.out.println("**** default graph") ;
//        query("SELECT * { ?s ?p ?o }", ds) ;
//        
//        for ( int i = 0 ; i < n.length ; i++ )
//        {
//            Node g = n[i] ;
//            String label = str[i] ;
//            System.out.println("**** "+label) ;
//            QuerySolutionMap qs = new QuerySolutionMap() ;
//            if ( g != null )
//                qs.add("g", ResourceFactory.createResource(n[i].getURI())) ;
//            query("SELECT * { GRAPH ?g { ?s <http://example/d1/lang> ?o } }", ds, qs) ;
//        }
//        
//        SSE.write(ds) ;
//
//        System.out.println("**** Named graph") ;
//        Model modelNamed = ds.getNamedModel("http://example/d1/") ;
//        query("SELECT * { ?s <http://example/d1/lang> ?o }", modelNamed) ;
        
        query("SELECT * { ?s ?p ?o GRAPH ?g { ?s1 ?p1 ?o} }", ds) ;
        
        System.exit(0) ;
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
    private static void query(String str, Dataset dataset)
    {
        query(str, dataset, null) ;
    }
    
    private static void query(String str, Dataset dataset, QuerySolution qs)
    {
        System.out.println(str) ; 
        Query q = QueryFactory.create(str, Syntax.syntaxARQ) ;
        QueryExecution qexec = QueryExecutionFactory.create(q, dataset, qs) ;
        ResultSet rs = qexec.execSelect() ;
        ResultSetFormatter.out(rs) ;
        qexec.close() ;
    }
    
    private static void query(String str, Model model)
    {
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