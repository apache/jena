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
import java.util.Iterator ;

import org.openjena.riot.ErrorHandlerLib ;
import org.openjena.riot.checker.CheckerIRI ;
import riot.inf.infer ;

import com.hp.hpl.jena.iri.IRI ;
import com.hp.hpl.jena.iri.IRIFactory ;
import com.hp.hpl.jena.iri.Violation ;
import com.hp.hpl.jena.query.* ;
import com.hp.hpl.jena.rdf.model.Model ;
import com.hp.hpl.jena.shared.JenaException ;
import com.hp.hpl.jena.sparql.algebra.Op ;
import com.hp.hpl.jena.sparql.algebra.Transform ;
import com.hp.hpl.jena.sparql.algebra.Transformer ;
import com.hp.hpl.jena.sparql.algebra.opt.TransformPropertyFunction ;
import com.hp.hpl.jena.sparql.expr.Expr ;
import com.hp.hpl.jena.sparql.expr.ExprEvalException ;
import com.hp.hpl.jena.sparql.expr.NodeValue ;
import com.hp.hpl.jena.sparql.function.FunctionEnvBase ;
import com.hp.hpl.jena.sparql.lang.sparql_11.SPARQLParser11 ;
import com.hp.hpl.jena.sparql.sse.SSE ;
import com.hp.hpl.jena.sparql.util.ALog ;
import com.hp.hpl.jena.sparql.util.ExprUtils ;
import com.hp.hpl.jena.sparql.util.QueryExecUtils ;
import com.hp.hpl.jena.sparql.util.Timer ;
import com.hp.hpl.jena.util.FileManager ;

public class RunARQ
{
    static String divider = "----------" ;
    static String nextDivider = null ;
    static void divider()
    {
        if ( nextDivider != null )
            System.out.println(nextDivider) ;
        nextDivider = divider ;
    }
    
    static { ALog.setLog4j() ; }
    
    public static void runTest()
    {
        String dir = "/home/afs/W3C/SPARQL-docs/tests/data-sparql11/negation/" ;
        dir = "testing/ARQ/PropertyFunctions/" ;
        String queryFile = "--query="+dir+"list-8.rq" ;
        String dataFile = "--data="+dir+"data-1.ttl" ;
        arq.sparql.main(/*"--engine=ref",*/ "--syntax=sparql_11", dataFile, queryFile) ;
        System.exit(0) ; 
    }

    // ----
    
    public static void main(String[] argv) throws Exception
    {
        //ReportSlowDatatype.main() ;
        //unionTransform() ;
        //streamInference() ;
        
        //new TransformFilterPlacement()
        arq.sparql.main("--data=D1.ttl", "--query=Q.arq", "--engine=ref") ;
        arq.sparql.main("--data=D1.ttl", "--query=Q.arq"/*, "--engine=ref"*/) ; System.exit(0) ;
        
        // DELETE
        
        Transform t = new TransformPropertyFunction(ARQ.getContext()) ;

        divider() ;
//        Query query = QueryFactory.create(strjoinNL("PREFIX list: <http://jena.hpl.hp.com/ARQ/list#>",
//                                                    "PREFIX  apf:     <http://jena.hpl.hp.com/ARQ/property#>",
//                                                    "PREFIX  rdf:    <http://www.w3.org/1999/02/22-rdf-syntax-ns#>", 
//                                                    "SELECT *",
//                                                    "{",
//                                                    "   ?s1 list:member ?o1 .",
//                                                    "   FILTER NOT EXISTS { ?s apf:splitIRI (?ns ?ln) }",
//                                                    "}"), Syntax.syntaxSPARQL_11) ;
//
//        Query query = QueryFactory.create(strjoinNL("PREFIX list: <http://jena.hpl.hp.com/ARQ/list#>",
//                                                    "PREFIX  apf:     <http://jena.hpl.hp.com/ARQ/property#>",
//                                                    "PREFIX  rdf:    <http://www.w3.org/1999/02/22-rdf-syntax-ns#>", 
//                                                    "SELECT *",
//                                                    "{",
//                                                    "   ?s ?p ?o",
//                                                    "   LET(?x := NOT EXISTS { ?list list:member ?x })" ,
//                                                    "}"), Syntax.syntaxARQ) ;
//
//        Query query = QueryFactory.create(strjoinNL("PREFIX list: <http://jena.hpl.hp.com/ARQ/list#>",
//                                                    "PREFIX  apf:     <http://jena.hpl.hp.com/ARQ/property#>",
//                                                    "PREFIX  rdf:    <http://www.w3.org/1999/02/22-rdf-syntax-ns#>", 
//                                                    "SELECT *",
//                                                    "{",
//                                                    "   ?s ?p ?o",
//                                                    "} GROUP BY NOT EXISTS { ?s list:member ?x }"), Syntax.syntaxARQ) ;
//
//        Query query = QueryFactory.create(strjoinNL("PREFIX list: <http://jena.hpl.hp.com/ARQ/list#>",
//                                                    "PREFIX  apf:     <http://jena.hpl.hp.com/ARQ/property#>",
//                                                    "PREFIX  rdf:    <http://www.w3.org/1999/02/22-rdf-syntax-ns#>", 
//                                                    "SELECT *",
//                                                    "{",
//                                                    "   ?s ?p ?o",
//                                                    "} ORDER BY NOT EXISTS { ?s list:member ?x }"), Syntax.syntaxARQ) ;
//
//        Op op = Algebra.compile(query) ;

        Op op = SSE.parseOp(strjoinNL("(prefix ((list: <http://jena.hpl.hp.com/ARQ/list#>))",
                                      "  (join",
                                      "     (bgp (?x ?y ?z))",
                                      "     (filter (&& (notexists (bgp (?s list:member ?o)))",
                                      "                 (< 1 2))",
                                      "        (bgp (?s1 list:member ?o1)))",
        "   ))")) ;
        //System.out.println(op) ;

        // Move this to Optimize.apply.
        //Transform t2 = new TransformApplyInsideExprFunctionOp(t) ;
        Transform t2 = t ;
        // Better is to apply it all in one go.

        if ( t2 != t )
        {
            divider() ;
            Op op2 = Transformer.transform(t, op) ;
            System.out.println(op2) ;
        }
        divider() ;
        Op op3 = Transformer.transform(t2, op) ;
        System.out.println(op3) ;
        //            Query q2 = OpAsQuery.asQuery(op3) ;
        //            q2.getPrefixMapping().setNsPrefixes(query.getPrefixMapping()) ;
        //            System.out.println(q2) ;
        System.exit(0) ;
    }
    
    public static void streamInference()
    {
        Model m = FileManager.get().loadModel("V.ttl") ;

        infer.main("--rdfs=V.ttl") ;
        
//        SinkCounting<Triple> outputSink = new SinkCounting<Triple>(new SinkPrint<Triple>()) ;
//        
//        SinkCounting<Triple> inputSink1 = new SinkCounting<Triple>(new InferenceExpanderRDFS(outputSink, m)) ;
//        // Add gaps between parser triples. 
//        Sink<Triple> inputSink2 = new SinkInsertText<Triple>(inputSink1, "--\n") ;
//        
//        Sink<Triple> inputSink = inputSink2 ;
//        
//        InputStream input = IO.openFile("D.ttl") ;
//        
//        LangRIOT parser = RiotReader.createParserTurtle(input, "http://base/", inputSink) ;
//        parser.parse() ;
//        inputSink.flush() ;
//
//        System.out.println() ;
//        System.out.printf("Input  =  %d\n", inputSink1.getCount()) ;
//        System.out.printf("Total  =  %d\n", outputSink.getCount()) ;
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
        sparql11update_1("LOAD <foo> INTO <blah>") ;
        System.out.println("DONE") ;
        
    }
    
    private static void sparql11update_1(String str)
    {
        System.out.println(str);
        Reader r = new StringReader(str) ;
        SPARQLParser11 parser = null ;
        try {
            parser = new SPARQLParser11(r) ;
            parser.setUpdateRequest(null) ;
            parser.UpdateUnit() ;
            System.out.println();
            //validateParsedUpdate(update) ;
        }
        catch (com.hp.hpl.jena.sparql.lang.sparql_11.ParseException ex)
        { 
            throw new QueryParseException(ex.getMessage(),
                                          ex.currentToken.beginLine,
                                          ex.currentToken.beginColumn
            ) ; }
        catch (com.hp.hpl.jena.sparql.lang.sparql_11.TokenMgrError tErr)
        {
            // Last valid token : not the same as token error message - but this should not happen
            int col = parser.token.endColumn ;
            int line = parser.token.endLine ;
            throw new QueryParseException(tErr.getMessage(), line, col) ; }

        catch (QueryException ex) { throw ex ; }
        catch (JenaException ex)  { throw new QueryException(ex.getMessage(), ex) ; }
        catch (Error err)
        {
            // The token stream can throw errors.
            throw new QueryParseException(err.getMessage(), err, -1, -1) ;
        }
        catch (Throwable th)
        {
            ALog.fatal(RunARQ.class, "Unexpected throwable: ",th) ;
            throw new QueryException(th.getMessage(), th) ;
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

    private static void execQuery(String datafile, String queryfile)
    {
        //ARQ.getContext().set(ARQ.enableExecutionTimeLogging, true) ; 

        //QueryEngineMain.register() ;
        String a[] = new String[]{
            //"-v",
            //"--engine=ref", 
            "--data="+datafile,
            "-query="+queryfile , 
        } ;
        
        arq.sparql.main(a) ;
        //System.exit(0) ;
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
