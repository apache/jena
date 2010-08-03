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

import org.openjena.atlas.logging.Log ;
import org.openjena.riot.ErrorHandlerLib ;
import org.openjena.riot.checker.CheckerIRI ;

import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.iri.IRI ;
import com.hp.hpl.jena.iri.IRIFactory ;
import com.hp.hpl.jena.iri.Violation ;
import com.hp.hpl.jena.query.* ;
import com.hp.hpl.jena.rdf.model.Model ;
import com.hp.hpl.jena.shared.JenaException ;
import com.hp.hpl.jena.sparql.algebra.Algebra ;
import com.hp.hpl.jena.sparql.algebra.Op ;
import com.hp.hpl.jena.sparql.algebra.Transform ;
import com.hp.hpl.jena.sparql.algebra.opt.TransformPropertyFunction ;
import com.hp.hpl.jena.sparql.expr.Expr ;
import com.hp.hpl.jena.sparql.expr.ExprEvalException ;
import com.hp.hpl.jena.sparql.expr.NodeValue ;
import com.hp.hpl.jena.sparql.function.FunctionEnvBase ;
import com.hp.hpl.jena.sparql.lang.ParserSPARQL11Update ;
import com.hp.hpl.jena.sparql.lang.sparql_11.SPARQLParser11 ;
import com.hp.hpl.jena.sparql.path.Path ;
import com.hp.hpl.jena.sparql.path.PathEval ;
import com.hp.hpl.jena.sparql.sse.SSE ;
import com.hp.hpl.jena.sparql.util.ExprUtils ;
import com.hp.hpl.jena.sparql.util.QueryExecUtils ;
import com.hp.hpl.jena.sparql.util.Timer ;
import com.hp.hpl.jena.update.UpdateRequest ;
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
    
    static { Log.setLog4j() ; }
    
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
        arq.query.main("--query=Q.arq", "--data=D.ttl") ; System.exit(0) ;
        
        
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
        
        //ReportSlowDatatype.main() ;
        //unionTransform() ;
        //streamInference() ;
        
        //new TransformFilterPlacement()
        arq.sparql.main("--data=D1.ttl", "--query=Q.arq", "--engine=ref") ;
        arq.sparql.main("--data=D1.ttl", "--query=Q.arq"/*, "--engine=ref"*/) ; System.exit(0) ;
        
        // DELETE
        
        Transform t = new TransformPropertyFunction(ARQ.getContext()) ;

        divider() ;

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
            Log.fatal(RunARQ.class, "Unexpected throwable: ",th) ;
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
