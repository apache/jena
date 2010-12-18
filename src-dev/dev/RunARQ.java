/*
 * (c) Copyright 2007, 2008, 2009 Hewlett-Packard Development  Company, LP
 * (c) Copyright 2010 Talis Systems Ltd
 * All rights reserved.
 * [See end of file]
 */

package dev;

import java.util.Iterator ;

import javax.xml.datatype.DatatypeFactory ;
import javax.xml.datatype.Duration ;

import junit.framework.TestSuite ;
import org.apache.xerces.impl.dv.xs.DurationDV ;
import org.openjena.atlas.io.IndentedWriter ;
import org.openjena.atlas.json.JSON ;
import org.openjena.atlas.json.JsonValue ;
import org.openjena.atlas.lib.StrUtils ;
import org.openjena.atlas.logging.Log ;
import org.openjena.riot.ErrorHandlerFactory ;
import org.openjena.riot.checker.CheckerIRI ;
import org.openjena.riot.pipeline.normalize.CanonicalizeLiteral ;

import com.hp.hpl.jena.datatypes.TypeMapper ;
import com.hp.hpl.jena.datatypes.xsd.XSDDatatype ;
import com.hp.hpl.jena.datatypes.xsd.XSDDuration ;
import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.iri.IRI ;
import com.hp.hpl.jena.iri.IRIFactory ;
import com.hp.hpl.jena.iri.Violation ;
import com.hp.hpl.jena.query.Query ;
import com.hp.hpl.jena.query.QueryExecution ;
import com.hp.hpl.jena.query.QueryExecutionFactory ;
import com.hp.hpl.jena.query.QueryFactory ;
import com.hp.hpl.jena.query.QuerySolutionMap ;
import com.hp.hpl.jena.query.ResultSetFormatter ;
import com.hp.hpl.jena.rdf.model.Model ;
import com.hp.hpl.jena.sparql.algebra.Algebra ;
import com.hp.hpl.jena.sparql.algebra.Op ;
import com.hp.hpl.jena.sparql.core.DatasetGraph ;
import com.hp.hpl.jena.sparql.core.DatasetGraphFactory ;
import com.hp.hpl.jena.sparql.expr.Expr ;
import com.hp.hpl.jena.sparql.expr.ExprEvalException ;
import com.hp.hpl.jena.sparql.expr.NodeValue ;
import com.hp.hpl.jena.sparql.function.FunctionEnvBase ;
import com.hp.hpl.jena.sparql.graph.NodeTransform ;
import com.hp.hpl.jena.sparql.junit.ScriptTestSuiteFactory ;
import com.hp.hpl.jena.sparql.junit.SimpleTestRunner ;
import com.hp.hpl.jena.sparql.sse.SSE ;
import com.hp.hpl.jena.sparql.util.ExprUtils ;
import com.hp.hpl.jena.sparql.util.QueryExecUtils ;
import com.hp.hpl.jena.sparql.util.Timer ;
import com.hp.hpl.jena.update.GraphStore ;
import com.hp.hpl.jena.update.GraphStoreFactory ;
import com.hp.hpl.jena.update.UpdateAction ;
import com.hp.hpl.jena.update.UpdateFactory ;
import com.hp.hpl.jena.update.UpdateRequest ;
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
    
    public static void testXSDDurationBug() {
        Node d1 = Node.createLiteral("PT110S", null, XSDDatatype.XSDduration);
        Node d2 = Node.createLiteral("PT1M50S", null, XSDDatatype.XSDduration);
        System.out.println(d1.getLiteral().isWellFormed());
        System.out.println(d2.getLiteral().isWellFormed());
        XSDDuration dur1 = (XSDDuration) d1.getLiteralValue();
        XSDDuration dur2 = (XSDDuration) d2.getLiteralValue();
        int cmp = dur1.compare(dur2);
        System.out.println("Compare = " + cmp);
    }
    
    public static void main(String[] argv) throws Exception
    {
        riotcmd.turtle.main("ttl-with-bom.ttl") ; System.exit(0) ;
        
        
        arq.qparse.main("--print=op", "--print=opt", "--query=Q.rq") ;
        System.exit(0) ;
        
        //testXSDDurationBug() ; System.exit(0) ;
        
        /*
"P1Y2M"^^xsd:yearMonthDuration
-> value space is xs:integer month values

"P1Y2M"^^xsd:dayTimeDuration
-> value space is fractional second values.
         */
        
        Duration javaDuration = DatatypeFactory.newInstance().newDuration("-PT5H") ;
        System.out.println(javaDuration) ;
        javaDuration = DatatypeFactory.newInstance().newDuration("PT100M") ;
        System.out.println(javaDuration) ;
        System.out.println(javaDuration.getHours()) ; 
        System.out.println(javaDuration.getMinutes()) ;
        
        DurationDV durationDV = null ; 

//        RDFDatatype rdfDT_dayTime = new BaseDatatype("http://www.w3.org/2001/XMLSchema#dayTimeDuration") ;
//        RDFDatatype rdfDT_yearMonth = new BaseDatatype("http://www.w3.org/2001/XMLSchema#yearMonthDuration") ;
        
        // Fake implementation of  dayTimeDuration, yearMonthDuration
        
        TypeMapper.getInstance().registerDatatype(DT_DayTimeDuration.get()) ;
        
        //Node lit = Node.createLiteral("-PT5H", null, XSDDatatype.XSDduration) ; 
        Node lit1 = Node.createLiteral("-PT60M", null, DT_DayTimeDuration.get()) ;
        Node lit2 = Node.createLiteral("-PT1H", null, DT_DayTimeDuration.get()) ;
        //Node lit2 = Node.createLiteral("P1Y3M", null, rdfDT_yearMonth) ;
        
        Node nv = lit1 ;
        
        if (  nv.getLiteralDatatype() != null && nv.getLiteralDatatype() == DT_DayTimeDuration.get() )
        {
            String lex = nv.getLiteralLexicalForm();
            Object value ;
            
            if ( nv.getLiteral().isWellFormed() )
                value = nv.getLiteral().getValue() ;
            else
                // Which will probably fail ...
                value = DT_DayTimeDuration.get().parse(lex) ;
            XSDDuration duration = (XSDDuration)value ;
            System.out.println(duration) ;
            //return new NodeValueDuration(duration, node) ;
        }
        
        System.out.println("DONE") ;
        System.exit(0) ;
        
        riotcmd.riot.main("D.nt") ; System.exit(0) ;
        
        arq.qparse.main("-query=Q.rq") ; System.exit(0) ;
        
        // arq.sparql.main("--data=D.ttl", "-query=Q.rq") ;
        // testXSDDurationBug() ; System.exit(0) ;
 
        String DIR = "/home/afs/W3C/SPARQL-docs/tests/data-sparql11/delete" ;
        TestSuite ts = ScriptTestSuiteFactory.make(DIR+"/manifest.ttl") ;
        SimpleTestRunner.runAndReport(ts) ;
        System.exit(0) ;
        {
            UpdateRequest request = UpdateFactory.read(DIR+"/delete-01.ru") ;
            divider() ;
            System.out.println(request) ;
            divider() ;
            
            Model m = FileManager.get().loadModel(DIR+"/delete-pre-01.ttl") ;
            m.write(System.out, "TTL") ;
            UpdateAction.execute(request, m) ;
            divider() ;
            System.out.println("# Result:") ;
            m.write(System.out, "TTL") ;
            divider() ;
            System.out.println("# Expected:") ;
            FileManager.get().loadModel(DIR+"/delete-post-01s.ttl").write(System.out, "TTL") ;
            
            System.exit(0) ;
        }
        
        if ( false )
        {
            NodeTransform ntLitCanon = CanonicalizeLiteral.get();
            // To do :
            //   double and floats.
            //   decimals and X.0
            String[] strings = { "123", "0123", "0123.00900" , "-0089", "-0089.0" , "1e5", "+001.5e6", "'fred'"} ;
            for ( String s : strings )
            {
                Node n = SSE.parseNode(s) ;
                Node n2 = ntLitCanon.convert(n) ;
                System.out.println(n+" => "+n2) ;
            }
            System.exit(0) ;
        }
    }

    public static void runUpdate()
    {
        UpdateRequest request = UpdateFactory.create("INSERT DATA { GRAPH <G> { <s> <p> <o> }}") ;
        DatasetGraph dsg = DatasetGraphFactory.createMem() ;
        GraphStore gs = GraphStoreFactory.create(dsg) ;
        UpdateAction.execute(request, gs) ;
        SSE.write(gs) ;
        System.exit(0) ;
    }
    
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

    private static void json()
    {
        // JSON
        // ** Double space for end of object, end of object. 
        JsonValue obj = JSON.readAny("D.json") ;
        IndentedWriter out = new IndentedWriter(System.out) ; 
        out.setFlatMode(true) ;
        //out.setEndOfLineMarker("$") ;
        JSON.write(out, obj) ;
        out.flush() ;
        System.exit(0) ;
        

    }
    
    private static void processIRI(String iriStr)
    {
        IRI iri = IRIFactory.iriImplementation().create(iriStr) ;
        System.out.println(iri) ;
        System.out.println("Relative: "+iri.isRelative()) ;

        Iterator<Violation> vIter = iri.violations(true) ;
        for ( ; vIter.hasNext() ; )
        {
            System.out.println(vIter.next()) ;
        }
        System.out.println(iriStr + " ==> "+iri) ;
        CheckerIRI.iriViolations(iri, ErrorHandlerFactory.errorHandlerWarn) ;
        System.exit(0) ;
    }
    
    public static void analyseQuery(String ...queryString)
    {
        String qs = StrUtils.strjoinNL(queryString) ;
        Query query = QueryFactory.create(qs) ;
        Op op = Algebra.compile(query) ;
        divider() ;
        System.out.println(op) ;
        Op op2 = Algebra.optimize(op) ;
        divider() ;
        System.out.println(op2) ;
        divider() ;
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
    
    private static void runQTest(String dir, String manifest)
    {
        if ( ! dir.endsWith("/") )
            dir = dir + "/" ;
        String []a1 = { "--strict", dir+manifest } ;
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
