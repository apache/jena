/*
 * (c) Copyright 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package dev;

import static com.hp.hpl.jena.tdb.sys.Names.tripleIndexes;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import junit.framework.TestCase;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import tdb.tdbclean;
import arq.cmd.CmdUtils;
import atlas.junit.TextListener2;

import com.hp.hpl.jena.query.*;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.sparql.algebra.Algebra;
import com.hp.hpl.jena.sparql.algebra.Op;
import com.hp.hpl.jena.sparql.algebra.Transformer;
import com.hp.hpl.jena.tdb.InstallationTest;
import com.hp.hpl.jena.tdb.TDB;
import com.hp.hpl.jena.tdb.TDBFactory;
import com.hp.hpl.jena.tdb.base.block.BlockMgrMem;
import com.hp.hpl.jena.tdb.base.file.FileSet;
import com.hp.hpl.jena.tdb.base.file.Location;
import com.hp.hpl.jena.tdb.base.objectfile.ObjectFileDiskDirect;
import com.hp.hpl.jena.tdb.base.record.Record;
import com.hp.hpl.jena.tdb.index.Index;
import com.hp.hpl.jena.tdb.index.IndexBuilder;
import com.hp.hpl.jena.tdb.junit.QueryTestTDB;
import com.hp.hpl.jena.tdb.nodetable.NodeTable;
import com.hp.hpl.jena.tdb.nodetable.NodeTableFactory;
import com.hp.hpl.jena.tdb.solver.reorder.ReorderLib;
import com.hp.hpl.jena.tdb.solver.reorder.ReorderTransformation;
import com.hp.hpl.jena.tdb.store.DatasetPrefixes;
import com.hp.hpl.jena.tdb.store.FactoryGraphTDB;
import com.hp.hpl.jena.tdb.store.GraphTDB;
import com.hp.hpl.jena.tdb.store.GraphTriplesTDB;
import com.hp.hpl.jena.tdb.store.TripleTable;
import com.hp.hpl.jena.util.FileManager;

import dump.DumpIndex;
import dump.DumpNodes;

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

        Model m1 = TDBFactory.assembleModel("tdb2.ttl") ;
        m1.write(System.out, "TTL") ;
        System.exit(0) ;
        
        // Problem 1: When symUnionDefaultGraph -> quads but not a DatasetGraphTDB but a DataSourceGraphImpl
        // See OpExecutor.execute(OpQuadPattern) line 111
        // [DONE]
        
        // Problem 2: BGP on named model is not quads.
        // See OpExecutor.execute(OpBGP) line ~335
        // Line 224 is the entry into the chained OpExecutorTDB.
        
        TDB.getContext().set(TDB.symUnionDefaultGraph, true);
        //TDB.setExecutionLogging(true) ; 
        Dataset dataset = TDBFactory.createDataset( "DB" );
        
        // "SELECT * { GRAPH <urn:x-arq:UnionGraph> { ?s ?p ?o}}"
        String queryString =  "SELECT * {?s ?p ?o}" ;
        
        query(queryString, dataset) ;
        System.exit(0) ;
        
        
        System.out.println("Default model") ;
        query(queryString, dataset.getDefaultModel()) ;
        // Separate dataset.getMergedNamedModels();

        System.out.println("graph1") ;
        query(queryString, dataset.getNamedModel("http://example/graph1")) ;
        
        System.out.println("union") ;
        query(queryString, dataset.getNamedModel("urn:x-arq:UnionGraph")) ;
        
        System.out.println("named default") ;
        query(queryString, dataset.getNamedModel("urn:x-arq:DefaultGraph")) ;
        
        System.out.println("generated default") ;
        query(queryString, dataset.getNamedModel("urn:x-arq:DefaultGraphNode")) ;

        System.exit(0) ;

        
        
        if ( true )
        {
            TDB.init();
            FileManager.get().loadModel("D.ttl").write(System.out, "TTL") ;
            System.out.println("====") ;
            System.exit(0) ;
        }
        
        
        Location location = new Location("DB") ;
        
        if ( false )
        {
            ObjectFileDiskDirect f = new ObjectFileDiskDirect(location.getPath("nodes.dat")) ;
            DumpNodes.dump(System.out, f) ;
            System.out.flush();
            System.exit(0) ;
        }
        
        if ( true )
        {
//            FileSet fs = IndexBuilder.filesetForIndex(new Location("DB"), "SPO") ;
//            Index index = IndexBuilder.createIndex(fs, FactoryGraphTDB.indexRecordTripleFactory) ;
            
            // Better index factory operations.
            // Metafiles remove the need for record facories other than first use.Node  
            
            //Index creations?
          FileSet fs = IndexBuilder.filesetForIndex(new Location("DB"), "node2id") ;
          Index index = IndexBuilder.createIndex(fs, FactoryGraphTDB.nodeRecordFactory) ;
          
          boolean b = index.isEmpty() ;
          
            ByteArrayOutputStream out = new ByteArrayOutputStream() ;
            DumpIndex.dump(out, index) ;
            String x = new String(out.toByteArray()) ;
            System.out.println(x) ;
            
            ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray()) ;
            Index index2 = IndexBuilder.createIndex(FileSet.mem(), FactoryGraphTDB.nodeRecordFactory) ;
            DumpIndex.reload(in, index2) ;
            // Compare indexes.
            for ( Iterator<Record> iter2 = index2.iterator() ; iter2.hasNext() ; )
            {
                Record r2 = iter2.next() ;
                System.out.println(r2) ;
            }
            System.out.println() ;
            DumpIndex.dump(out, index2) ;
            
            System.out.flush();
            System.exit(0) ;
        }
        FileSet fs = new FileSet(".", ".") ;
        System.exit(0) ;
        
        if ( true )
        {
            tdbclean.main("DB") ;
            //tdb.tdbloader.main("--tdb=tdb.ttl", "D.ttl") ;
            Model m = TDBFactory.createModel("DB") ;
            FileManager.get().readModel(m, "D.ttl") ;
            m.write(System.out, "TTL") ;
            m.close();
            m.close();
        }
        
        if ( false )
        {
            System.out.println("----") ;
            Model m = TDBFactory.createModel("DB") ;
            m.write(System.out, "TTL") ;
        }
        System.exit(0) ;
        
        // Directory metadata files.

        FileSet fileSet = new FileSet(new Location("DB"), "SPO") ;
        System.out.println("Exists meta? "+fileSet.existsMetaData()) ;
        System.out.println("Exists? "+fileSet.exists("idn")) ;
        fileSet.setProperty("item1", "snork") ;
        fileSet.flush() ;
        System.out.println("Exists meta? "+fileSet.existsMetaData()) ;
        System.out.println("----") ;
        System.exit(0) ;

        
        fileSet.setProperty("item1", "snork") ;
        fileSet.flush() ;
        
        fileSet = new FileSet(".", "DATA") ;
        System.out.println(fileSet.getProperty("item1")) ;
        System.out.println("----") ;
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
        DatasetPrefixes prefixes = DatasetPrefixes.create(indexBuilder, location) ;
        GraphTDB g = new GraphTriplesTDB(null, table, prefixes, transform, location) ;
        return g ;
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
    
    private static void test()
    {
        String testNum = "2" ;
        String dir = "testing/UnionGraph/" ;
        List<String> dftGraphs = Arrays.asList(dir+"data-dft.ttl") ;
        List<String> namedGraphs = Arrays.asList(dir+"data-1.ttl", dir+"data-2.ttl") ;
        String queryFile = dir+"merge-"+testNum+".rq" ;
        ResultSet rs = ResultSetFactory.load(dir+"merge-"+testNum+"-results.srx") ;
        
        TestCase t = new QueryTestTDB("Test", null, "uri", dftGraphs, namedGraphs, rs, queryFile, TDBFactory.memFactory) ;
        JUnitCore runner = new org.junit.runner.JUnitCore() ;
        runner.addListener(new TextListener2(System.out)) ;
        
        InstallationTest.beforeClass() ;
        Result result = runner.run(t) ;
        InstallationTest.afterClass() ;
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
    
    private static void tdbtest(String...args)
    {
        tdb.tdbtest.main(args) ;
        System.exit(0) ;
    }
    
    private static void tdbconfig(String... args) 
    {
        tdb.tdbconfig.main(args) ;
        System.exit(0) ;
    }
}

/*
 * (c) Copyright 2007, 2008, 2009 Hewlett-Packard Development Company, LP
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