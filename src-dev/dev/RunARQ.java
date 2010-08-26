/*
 * (c) Copyright 2007, 2008, 2009 Hewlett-Packard Development  Company, LP
 * (c) Copyright 2010 Talis Systems Ltd
 * All rights reserved.
 * [See end of file]
 */

package dev;

import static org.openjena.atlas.lib.StrUtils.strjoinNL ;

import java.io.Reader ;
import java.io.StringReader ;
import java.util.HashSet ;
import java.util.Iterator ;
import java.util.Set ;

import org.openjena.atlas.io.IndentedLineBuffer ;
import org.openjena.atlas.io.IndentedWriter ;
import org.openjena.atlas.lib.StrUtils ;
import org.openjena.atlas.logging.Log ;
import org.openjena.riot.ErrorHandlerLib ;
import org.openjena.riot.checker.CheckerIRI ;

import com.hp.hpl.jena.iri.IRI ;
import com.hp.hpl.jena.iri.IRIFactory ;
import com.hp.hpl.jena.iri.Violation ;
import com.hp.hpl.jena.query.ARQ ;
import com.hp.hpl.jena.query.Query ;
import com.hp.hpl.jena.query.QueryExecution ;
import com.hp.hpl.jena.query.QueryExecutionFactory ;
import com.hp.hpl.jena.query.QueryFactory ;
import com.hp.hpl.jena.query.QuerySolutionMap ;
import com.hp.hpl.jena.query.ResultSetFormatter ;
import com.hp.hpl.jena.rdf.model.Model ;
import com.hp.hpl.jena.sparql.algebra.Algebra ;
import com.hp.hpl.jena.sparql.algebra.Op ;
import com.hp.hpl.jena.sparql.algebra.Transform ;
import com.hp.hpl.jena.sparql.algebra.opt.TransformPropertyFunction ;
import com.hp.hpl.jena.sparql.core.Var ;
import com.hp.hpl.jena.sparql.engine.RenamerVars ;
import com.hp.hpl.jena.sparql.engine.main.VarRename ;
import com.hp.hpl.jena.sparql.expr.Expr ;
import com.hp.hpl.jena.sparql.expr.ExprEvalException ;
import com.hp.hpl.jena.sparql.expr.NodeValue ;
import com.hp.hpl.jena.sparql.function.FunctionEnvBase ;
import com.hp.hpl.jena.sparql.lang.ParserSPARQL11Update ;
import com.hp.hpl.jena.sparql.modify.request.UpdateRequest ;
import com.hp.hpl.jena.sparql.modify.request.UpdateWriter ;
import com.hp.hpl.jena.sparql.serializer.SerializationContext ;
import com.hp.hpl.jena.sparql.sse.SSE ;
import com.hp.hpl.jena.sparql.util.ExprUtils ;
import com.hp.hpl.jena.sparql.util.QueryExecUtils ;
import com.hp.hpl.jena.sparql.util.Timer ;
import com.hp.hpl.jena.util.FileManager ;

public class RunARQ
{
    static String divider = "----------------------------------------" ;
    static String nextDivider = null ;
    static void divider()
    {
        if ( nextDivider != null )
            System.out.println(nextDivider) ;
        nextDivider = divider ;
    }
    
    static { Log.setLog4j() ; }
    
    public static void runTest()
    {
        String dir = "/home/afs/W3C/SPARQL-docs/tests/data-sparql11/negation/" ;
        dir = "testing/ARQ/PropertyFunctions/" ;
        runTest(dir, "data-1.ttl", "list-8.rq") ;
    }

    public static void runTest(String dir, String dataFile, String queryFile)
    {
        if ( ! dir.endsWith("/") )
            dir = dir + "/" ;
        String queryArg = "--query="+dir+queryFile ;
        String dataArg = "--data="+dir+dataFile ;
        arq.sparql.main(/*"--engine=ref",*/ dataArg, queryArg) ;
    }
    
    // ----
    

    public static void main(String[] argv) throws Exception
    {
        //qparse("--query=Q.rq", "--print=query", "--print=op") ; System.exit(0) ;
        sparql11update() ; System.exit(0) ; 

        String DIR = "WorkSpace/PropertyPathTestCases" ;
        runTest(DIR, "data-path-1.ttl", "pp-all-03.rq") ; System.exit(0) ;

        if ( false )
        {
            Query q = QueryFactory.read("Q.arq") ;
            Op op = Algebra.compile(q) ;
            divider() ;
            System.out.println(op) ;

            Set<Var> fixed = new HashSet<Var>() ;
            fixed.add(Var.alloc("y")) ;
            RenamerVars vrn = new RenamerVars(fixed) ;
            op = VarRename.rename(op, vrn) ;
            divider() ;
            System.out.println(op) ;
            System.exit(0) ;
        }
        
        /*
         * urn:x-arq:DefaultGraphNode -- generated
         * urn:x-arq:DefaultGraph -- explicit
         * urn:x-arq:UnionGraph
         */
        Op op = SSE.parseOp(strjoinNL("(prefix ((: <http://example/>))",
                                      "(graph <g>",
                                      "  (graph <urn:x-arq:UnionGraph>",
                                      "    (graph <g1>",
                                      "     (bgp (?s ?p ?o))))",
                                      ")",
                                      ")"
        )) ;

        Op op2 = Algebra.unionDefaultGraph(op) ;
        divider() ;
        System.out.println(op) ;
        divider() ;
        System.out.println(op2) ;
        System.exit(0) ;
    }
    
    private static void processIRI(String iriStr)
    {
        IRI iri = IRIFactory.iriImplementation().create(iriStr) ;
        System.out.println(iri) ;
        System.out.println(iri.isRelative()) ;

        Iterator<Violation> vIter = iri.violations(true) ;
        for ( ; vIter.hasNext() ; )
        {
            System.out.println(vIter.next()) ;
        }
        System.out.println(iriStr + " ==> "+iri) ;
        CheckerIRI.iriViolations(iri, ErrorHandlerLib.errorHandlerWarn) ;
        System.exit(0) ;
    }
    
    private static void sparql11update()
    {
        sparql11update_1("LOAD  <foo>  INTO  GRAPH  <blah>") ;
        sparql11update_1("BASE <http://example/> PREFIX : <http://prefix/> LOAD  <foo>  INTO  GRAPH  :local") ;
        
        sparql11update_1("LOAD  <foo>") ;
        sparql11update_1("BASE <http://example/> LOAD  <foo> INTO GRAPH <local>") ;
        sparql11update_1("BASE <http://example/> CLEAR GRAPH <foo>") ;
        sparql11update_1("BASE <http://example/> DROP GRAPH <foo>") ;
//        sparql11update_1("DROP  ALL") ;
//        sparql11update_1("DROP  NAMED") ;
//        sparql11update_1("CLEAR  DEFAULT") ;
//        
//        sparql11update_1("DELETE WHERE { ?s ?p ?o }") ;
//        sparql11update_1("DELETE DATA { <?s> <p> <o> }") ;
//        
//        sparql11update_1("BASE <base:> ",
//                         "PREFIX : <http://example/>",
//                         "WITH :g",
//                         "DELETE { <s> ?p ?o }",
//                         "INSERT { ?s ?p <#o> }",
//                         "USING <g>",
//                         "USING NAMED :gn",
//                         "WHERE",
//                         "{ ?s ?p ?o }"
//                         ) ;
//        sparql11update_1("PREFIX : <http://example>",
//                         "WITH :g",
//                         "DELETE { ?s ?p ?o }",
//                         //"INSERT { ?s ?p ?o }",
//                         "USING <g>",
//                         "USING NAMED :gn",
//                         "WHERE",
//                         "{ ?s ?p ?o }"
//                         ) ;
//        sparql11update_1("PREFIX : <http://example>",
//                         //"WITH :g",
//                         //"DELETE { ?s ?p ?o }",
//                         "INSERT { ?s ?p ?o }",
//                         //"USING <g>",
//                         //"USING NAMED :gn",
//                         "WHERE",
//                         "{ ?s ?p ?o }"
//                         ) ;
//        sparql11update_1("PREFIX : <http://example>",
//                         //"WITH :g",
//                         //"DELETE { ?s ?p ?o }",
//                         "INSERT DATA { <s> <p> <o> } ;",
//                         "INSERT DATA { <s> <p> <o> GRAPH <g> { <s> <p> <o> }}"
//                         ) ;
       
        
        System.out.println("# DONE") ;
        
    }
    
    private static void sparql11update_1(String... str)
    {
        String str$ = StrUtils.strjoinNL(str) ; 
        divider() ;
        System.out.println("----Input:") ;
        System.out.println(str$);
        Reader r = new StringReader(str$) ;
        
        ParserSPARQL11Update p = new ParserSPARQL11Update() ;
        UpdateRequest update = new UpdateRequest() ;
        p.parse(update, str$) ;
        
        System.out.println("----Output:") ;
        SerializationContext sCxt = new SerializationContext(update) ;
        //SerializationContext sCxt = new SerializationContext() ;
        UpdateWriter.output(update, IndentedWriter.stdout, sCxt) ;
        IndentedWriter.stdout.flush();
        
        IndentedLineBuffer buff = new IndentedLineBuffer() ;
        UpdateWriter.output(update, buff, sCxt) ;
        

        { // reparse
        String str2 = buff.asString() ;
        ParserSPARQL11Update p2 = new ParserSPARQL11Update() ;
        UpdateRequest update2 = new UpdateRequest() ;
        p2.parse(update2, str2) ;
        }
    }
    
    private static void execTimed(Query query, Model model)
    {
//        System.out.println(ARQ.VERSION); 
//        System.out.println(Jena.VERSION); 

        Timer timer = new Timer() ;
        timer.startTimer() ;
        exec(query, model) ;
        long time = timer.endTimer() ;
        System.out.printf("Time = %.2fs\n", time/1000.0) ;
    }

    private static void exec(Query query, Model model)
    {
        QueryExecution qexec = QueryExecutionFactory.create(query, model) ;
        QueryExecUtils.executeQuery(query, qexec) ;
    }
    
    public static NodeValue eval(String string)
    {
        try {
            Expr expr = ExprUtils.parse(string) ;
            return expr.eval(null, new FunctionEnvBase()) ;
        } catch (ExprEvalException ex)
        {
            ex.printStackTrace(System.err) ;
            return null ;
        }
    }
    
    public static void evalPrint(String string)
    {
        System.out.print(string) ;
        System.out.print(" ==> ") ;
        try {
            Expr expr = ExprUtils.parse(string) ;
            NodeValue nv = expr.eval(null, new FunctionEnvBase()) ;
            System.out.println(nv) ;
        } catch (ExprEvalException ex)
        {
            System.out.println(" ** "+ex) ;
        }
    }

    private static void qparse(String  ... a)
    {
        arq.qparse.main(a) ;
        System.exit(0) ;
    }
    
    private static void parseUpdate()
    {
        String str = "INSERT DATA { GRAPH <G> { } }" ;
        ParserSPARQL11Update p = new ParserSPARQL11Update() ;
        UpdateRequest update = new UpdateRequest() ;
        p.parse(update, str) ;
        System.out.println("DONE") ;
        System.exit(0) ;
    } 
    
    private static void runUpdate()
    {
        String a[] = {"--desc=dataset.ttl", "--update=update.ru", "--dump"} ;
        arq.update.main(a) ;
        System.exit(0) ;
    }
    
    private static void runQTest()
    {
        String DIR = "testing/ARQ/DAWG-Final/" ;
        String []a1 = { "--strict", "--data="+DIR+"data.ttl",
            "--query="+DIR+"assign-01.arq",
            "--result="+DIR+"assign-01.srx"} ;

        arq.qtest.main(a1) ;
        System.exit(0 ) ; 
  
    }

    private static void execQueryCode(String datafile, String queryfile)
    {
        Model model = FileManager.get().loadModel(datafile) ;
        Query query = QueryFactory.read(queryfile) ;
        
        QuerySolutionMap initialBinding = new QuerySolutionMap();
        //initialBinding.add("s", model.createResource("http://example/x1")) ;
        initialBinding.add("o", model.createResource("http://example/z")) ;
        
        QueryExecution qExec = QueryExecutionFactory.create(query, model, initialBinding) ;
        ResultSetFormatter.out(qExec.execSelect()) ;
    }

    private static void execRemote()
    {
        System.setProperty("socksProxyHost", "socks-server") ;
    
        String a2[] = { "--service=http://dbpedia.org/sparql",
        "SELECT * WHERE {  <http://dbpedia.org/resource/Angela_Merkel> <http://dbpedia.org/property/reference> ?object.  FILTER  (!isLiteral(?object))}"} ;
        arq.remote.main(a2) ;
        System.exit(0) ;
    }
}

/*
 * (c) Copyright 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * (c) Copyright 2010 Talis Systems Ltd
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
