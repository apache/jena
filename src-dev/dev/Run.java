/*
 * (c) Copyright 2007, 2008 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package dev;

import java.util.Iterator;


import arq.sparql;
import arq.sse_query;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;

import com.hp.hpl.jena.util.FileManager;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.shared.PrefixMapping;
import com.hp.hpl.jena.shared.impl.PrefixMappingImpl;

import com.hp.hpl.jena.sparql.algebra.Op;
import com.hp.hpl.jena.sparql.algebra.Transform;
import com.hp.hpl.jena.sparql.algebra.Transformer;
import com.hp.hpl.jena.sparql.algebra.op.*;
import com.hp.hpl.jena.sparql.algebra.opt.TransformFilterPlacement;
import com.hp.hpl.jena.sparql.core.PathBlock;
import com.hp.hpl.jena.sparql.core.Prologue;
import com.hp.hpl.jena.sparql.core.TriplePath;
import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.sparql.path.Path;
import com.hp.hpl.jena.sparql.path.PathCompiler;
import com.hp.hpl.jena.sparql.path.PathParser;
import com.hp.hpl.jena.sparql.serializer.PrologueSerializer;
import com.hp.hpl.jena.sparql.sse.SSE;
import com.hp.hpl.jena.sparql.util.IndentedLineBuffer;
import com.hp.hpl.jena.sparql.util.IndentedWriter;
import com.hp.hpl.jena.sparql.util.StringUtils;

import com.hp.hpl.jena.query.*;

import com.hp.hpl.jena.update.*;

public class Run
{
    public static void main(String[] argv) throws Exception
    {
        if ( false )
        {
            Model m = ModelFactory.createDefaultModel() ;
            m.setNsPrefix("", "http://example/") ;
            Query query = QueryFactory.make() ;
            query.setPrefixMapping(m) ;
            QueryFactory.parse(query, "SELECT * { :s :p :o }", null, Syntax.syntaxSPARQL) ;
            System.out.println(query) ;
            System.exit(0) ;
        }
        
        {
            Model m = ModelFactory.createDefaultModel() ;
            m.setNsPrefix("", "http://example/") ;
            Prologue prologue = new Prologue(m) ;
            IndentedLineBuffer b = new IndentedLineBuffer() ;
            PrologueSerializer.output(b.getIndentedWriter(), prologue) ;
            System.out.println(b.asString()) ;
            
//            query.setPrefixMapping(m) ;
//            QueryFactory.parse(query, "SELECT * { :s :p :o }", null, Syntax.syntaxSPARQL) ;
//            System.out.println(query) ;
            System.exit(0) ;
        }
        
        
        
        
        opt.RunT.main(argv) ; 
        //runQParse() ;
        execQuery("D.ttl", "Q.arq") ;
        
        path() ; System.exit(0) ;
        {
            String []a = { "--engine=ref", "--file=Q.rq", "--print=op" } ;
            arq.qparse.main(a) ;
            System.exit(0) ;
        }
        {
            String qs = StringUtils.join("\n",
                                         new String[]
                                                    {
                "PREFIX dc: <http://purl.org/dc/elements/1.1/>",
                "INSERT DATA INTO <http://example/bookStore>",
                                                    "{ <http://example/book3>  dc:title  'Fundamentals of Compiler Design'}"}
            ) ;
            UpdateRequest request2 = UpdateFactory.create(qs);
            request2.output(IndentedWriter.stdout);
            IndentedWriter.stdout.flush();
            System.exit(0) ;
        }
        
        
        UpdateRequest r = UpdateFactory.read("update.ru") ;
        GraphStore gs = GraphStoreFactory.create() ;
        System.out.println(r) ;
        
        UpdateAction.execute(r, gs) ;

        SSE.write(gs) ;
        System.exit(0) ;
        
        
        //code() ; System.exit(0) ;
        runUpdate() ; System.exit(0) ;
        
        //QueryEngineMain.register() ;
        String a[] = new String[]{
            //"-v",
            //"--engine=ref",
            "--data=D.ttl",
            "-query=Q.rq" ,
        } ;

        sparql.main(a) ;
        System.exit(0) ;
    }
    
    private static void path()
    {
//        String[] a = { "--print=op", "--file=Q.sse" } ;
//        arq.sse_query.main(a) ;
//        System.exit(0) ;
        
        Model model = FileManager.get().loadModel("D.ttl") ;

        PrefixMapping pmap = new PrefixMappingImpl() ;
        pmap.setNsPrefixes(PrefixMapping.Standard) ;
        pmap.setNsPrefix("", "http://example/") ;
        
        // | alt.
        
        if ( false )
        {
            path1(":p*/:q", pmap) ;
            path1("^:p", pmap) ;
            path1("^:p/:q", pmap) ;
            path1("^(:p/:q)", pmap) ;
            path1(":p*/:q", pmap) ;
            path1(":p^:q", pmap) ;
        }
        
        if ( false )
        {
            Path path = PathParser.parse("rdf:type/rdfs:subClassOf*", pmap) ;
            //path = new P_Link(RDF.type.asNode()) ;
            
            TriplePath triplePath = new TriplePath(Var.alloc("s"), path, Var.alloc("o")) ; 
            OpPath opPath = new OpPath(triplePath) ;
            System.out.println(opPath.toString(pmap)) ;
            String x = opPath.toString(pmap) ;
            Op op = SSE.parseOp(x, pmap) ;
            System.out.println(op.toString(pmap)) ;
        }
        
        System.exit(0) ;
    }

    private static void path1(String str, PrefixMapping pmap)
    {
        Path path = PathParser.parse(str, pmap) ;
        PathBlock pBlk = new PathBlock() ;
        Node s = Node.createURI("s") ;
        Node o = Node.createURI("o") ;
        TriplePath tp = new TriplePath(s, path, o) ;
        pBlk.add(tp) ;
        
        System.out.println("Path: "+str) ;
        PathBlock x = new PathCompiler().reduce(pBlk) ;
        for ( Iterator iter = x.iterator() ; iter.hasNext() ; )
            System.out.println("  "+iter.next()) ;
        System.out.println() ;
    }
    
    public static void code()
    {
        Transform t = new TransformFilterPlacement() ;
        
        Op op = SSE.readOp("Q.sse") ;
        System.out.println(op) ;
        op = Transformer.transform(new TransformFilterPlacement(), op) ;
        System.out.println(op) ;
        System.exit(0) ;
        
    }
    private static void runQParse()
    {
        String []a = { "--engine=quad", "--file=Q.arq", "--print=op" } ;
        arq.qparse.main(a) ;
        System.exit(0) ;
    }
    
    private static void runQParseARQ()
    {
        String []a = { "--file=Q.arq", "--out=arq", "--print=op", "--print=query" } ;
        arq.qparse.main(a) ;
        System.exit(0) ;
    }
    
    private static void runUpdate()
    {
        String a[] = {/*"--desc=etc/graphstore.ttl",*/ "--update=update.ru", "--dump"} ;
        arq.update.main(a) ;
        System.exit(0) ;
    }
    
    private static void runQTest()
    {
        String DIR = "/home/afs/W3C/DataAccess/tests/data-r2/expr-equals/" ;
        String []a1 = { "--strict", "--data="+DIR+"data-eq.ttl",
            "--query="+DIR+"query-eq2-2.rq",
            "--result="+DIR+"result-eq2-2.ttl"} ;

        String []a2 = { "--strict", "--data="+DIR+"data-eq.ttl",
            "--query="+DIR+"query-eq2-graph-1.rq",
            "--result="+DIR+"result-eq2-graph-1.ttl"} ;

        arq.qtest.main(a1) ;
        System.exit(0 ) ; 
  
    }

    private static void execQuery(String datafile, String queryfile)
    {
        //QueryEngineMain.register() ;
        String a[] = new String[]{
            //"-v",
            //"--engine=ref", 
            "--data="+datafile,
            "-query="+queryfile , 
        } ;
        
        sparql.main(a) ;
        System.exit(0) ;
    }

    private static void execQuery2(String datafile, String queryfile)
    {
        //QueryEngineMain.register() ;
        String a[] = new String[]{
            //"-v",
            "--data="+datafile,
            "-query="+queryfile , 
        } ;
        
        sparql.main(a) ;
        a = new String[]{
            //"-v",
            "--engine=ref", 
            "--data="+datafile,
            "-query="+queryfile , 
        } ;
        sparql.main(a) ;
        
        System.exit(0) ;
    }
    
    private static void execQuerySSE(String datafile, String queryfile)
    {
        //com.hp.hpl.jena.sparql.engine.ref.QueryEngineRef.register() ;
        String a[] = new String[]{
            //"-v",
            //"--engine=ref",
            "--data="+datafile,
            "-query="+queryfile , 
        } ;
        
        sse_query.main(a) ;
        System.exit(0) ;
        
    }
    
    private static void execQueryCode(String datafile, String queryfile)
    {
        Model model = FileManager.get().loadModel(datafile) ;
        Query query = QueryFactory.read(queryfile) ;
        
        QuerySolutionMap initialBinding = new QuerySolutionMap();
        initialBinding.add("s", model.createResource("http://example.org/x2")) ;
        initialBinding.add("o", model.createResource("http://example.org/z")) ;
        
        QueryExecution qExec = QueryExecutionFactory.create(query, model, initialBinding) ;
        ResultSetFormatter.out(qExec.execSelect()) ;
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