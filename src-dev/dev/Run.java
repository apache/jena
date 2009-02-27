/*
 * (c) Copyright 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package dev;

import static com.hp.hpl.jena.tdb.sys.Names.tripleIndexes;

import java.io.IOException;

import lib.FileOps;
import lib.cache.CacheNG;
import arq.cmd.CmdUtils;

import com.hp.hpl.jena.rdf.model.*;

import com.hp.hpl.jena.shared.PrefixMapping;
import com.hp.hpl.jena.shared.ReificationStyle;

import com.hp.hpl.jena.sparql.algebra.Algebra;
import com.hp.hpl.jena.sparql.algebra.Op;
import com.hp.hpl.jena.sparql.algebra.Transformer;

import com.hp.hpl.jena.query.*;

import com.hp.hpl.jena.tdb.TDBFactory;
import com.hp.hpl.jena.tdb.base.block.BlockMgrMem;
import com.hp.hpl.jena.tdb.base.file.FileSet;
import com.hp.hpl.jena.tdb.base.file.Location;
import com.hp.hpl.jena.tdb.index.IndexBuilder;
import com.hp.hpl.jena.tdb.nodetable.NodeTable;
import com.hp.hpl.jena.tdb.nodetable.NodeTableFactory;
import com.hp.hpl.jena.tdb.solver.reorder.ReorderLib;
import com.hp.hpl.jena.tdb.solver.reorder.ReorderTransformation;
import com.hp.hpl.jena.tdb.store.*;

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
 
    public static void t(String fn) 
    {
        System.out.println(fn+" => "+FileOps.split(fn)) ;
    }
    
    public static void main(String ... args) throws IOException
    {
        tdbquery("--tdb=tdb.ttl", "--file=Q.arq") ; 
        
//        Location loc = Location.dirname("DB/SPO") ;
//        //new File("DB/SPO").get
//        System.out.println("Loc = "+loc) ;
//        System.exit(0) ;
//        tdb.tdbdump.main("index","DB/SPO") ; System.exit(0) ;

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
        DatasetPrefixes prefixes = DatasetPrefixes.create(indexBuilder, location) ;
        GraphTDB g = new GraphTriplesTDB(table, prefixes, transform, location) ;
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