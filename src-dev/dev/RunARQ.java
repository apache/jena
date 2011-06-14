/*
 * (c) Copyright 2007, 2008, 2009 Hewlett-Packard Development  Company, LP
 * (c) Copyright 2010 Talis Systems Ltd
 * (c) Copyright 2010 Epimorphics Ltd
 * All rights reserved.
 * [See end of file]
 */

package dev;

import java.util.Iterator ;
import java.util.NoSuchElementException ;
import java.util.concurrent.ArrayBlockingQueue ;
import java.util.concurrent.BlockingQueue ;
import java.util.concurrent.ExecutorService ;
import java.util.concurrent.Executors ;

import org.openjena.atlas.io.IndentedWriter ;
import org.openjena.atlas.json.JSON ;
import org.openjena.atlas.json.JsonValue ;
import org.openjena.atlas.lib.Lib ;
import org.openjena.atlas.lib.Sink ;
import org.openjena.atlas.lib.StrUtils ;
import org.openjena.atlas.logging.Log ;
import org.openjena.riot.ErrorHandlerFactory ;
import org.openjena.riot.RiotReader ;
import org.openjena.riot.checker.CheckerIRI ;
import org.openjena.riot.out.NodeFmtLib ;
import org.openjena.riot.pipeline.normalize.CanonicalizeLiteral ;
import org.openjena.riot.tokens.Token ;
import org.openjena.riot.tokens.Tokenizer ;
import org.openjena.riot.tokens.TokenizerFactory ;

import com.hp.hpl.jena.datatypes.xsd.XSDDatatype ;
import com.hp.hpl.jena.datatypes.xsd.XSDDuration ;
import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.graph.Node_Blank ;
import com.hp.hpl.jena.graph.Triple ;
import com.hp.hpl.jena.iri.IRI ;
import com.hp.hpl.jena.iri.IRIFactory ;
import com.hp.hpl.jena.iri.Violation ;
import com.hp.hpl.jena.query.ARQ ;
import com.hp.hpl.jena.query.Query ;
import com.hp.hpl.jena.query.QueryCancelledException ;
import com.hp.hpl.jena.query.QueryExecution ;
import com.hp.hpl.jena.query.QueryExecutionFactory ;
import com.hp.hpl.jena.query.QueryFactory ;
import com.hp.hpl.jena.query.QuerySolutionMap ;
import com.hp.hpl.jena.query.ResultSet ;
import com.hp.hpl.jena.query.ResultSetFormatter ;
import com.hp.hpl.jena.rdf.model.Model ;
import com.hp.hpl.jena.shared.PrefixMapping ;
import com.hp.hpl.jena.sparql.ARQConstants ;
import com.hp.hpl.jena.sparql.ARQException ;
import com.hp.hpl.jena.sparql.algebra.Algebra ;
import com.hp.hpl.jena.sparql.algebra.Op ;
import com.hp.hpl.jena.sparql.algebra.Transformer ;
import com.hp.hpl.jena.sparql.algebra.op.OpJoin ;
import com.hp.hpl.jena.sparql.algebra.optimize.TransformJoinStrategy ;
import com.hp.hpl.jena.sparql.core.DatasetGraph ;
import com.hp.hpl.jena.sparql.core.DatasetGraphFactory ;
import com.hp.hpl.jena.sparql.engine.ExecutionContext ;
import com.hp.hpl.jena.sparql.engine.QueryExecutionBase ;
import com.hp.hpl.jena.sparql.engine.main.JoinClassifier ;
import com.hp.hpl.jena.sparql.expr.Expr ;
import com.hp.hpl.jena.sparql.expr.ExprEvalException ;
import com.hp.hpl.jena.sparql.expr.NodeValue ;
import com.hp.hpl.jena.sparql.function.FunctionBase1 ;
import com.hp.hpl.jena.sparql.function.FunctionEnv ;
import com.hp.hpl.jena.sparql.function.FunctionEnvBase ;
import com.hp.hpl.jena.sparql.function.FunctionRegistry ;
import com.hp.hpl.jena.sparql.graph.NodeTransform ;
import com.hp.hpl.jena.sparql.sse.SSE ;
import com.hp.hpl.jena.sparql.util.ExprUtils ;
import com.hp.hpl.jena.sparql.util.FmtUtils ;
import com.hp.hpl.jena.sparql.util.NodeFactory ;
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

    public static void exit(int code)
    {System.out.flush() ;
        System.out.println("DONE") ;
        System.exit(code) ;
    }
    

    // count(filter)
    
    public static void main(String[] argv) throws Exception
    {
        Node node1 = Node_Blank.createAnon();
        String str = NodeFmtLib.serialize(node1);
        Tokenizer tokenizer = TokenizerFactory.makeTokenizerString(str);
        Token token = tokenizer.next();
        Node node2 = token.asNode();
        System.out.println(node1) ;
        System.out.println(node2) ;
        exit(0) ;
        
        arq.qparse.main("--file=Q.arq") ; exit(0) ;
        
        String x = StrUtils.strjoinNL("(join",
                                      //"  (conditional",
                                      "  (leftjoin",
                                      "    (bgp (triple ?s ?p1 ?o1))" ,
                                      "    (bgp (triple ?s <foaf:knows> ?o2)))" ,
                                      "  (table",
                                      "    (row [?o2 :b])",
                                      "  ))") ;
        
        Op op = SSE.parseOp(x) ;
        System.out.print(op) ;
        Op left = ((OpJoin)op).getLeft() ;
        Op right = ((OpJoin)op).getRight() ;
        
        if ( false )
        {
            JoinClassifier.print = true ;
            System.out.flush() ;
            boolean b1 = JoinClassifier.isLinear(left, right) ;
            System.out.println("Left/right: "+b1) ;
           
            
            
            System.out.println() ;
            System.out.flush() ;
            boolean b2 = JoinClassifier.isLinear(right, left) ;
            System.out.println("Right/left: "+b2) ;
            exit(0) ;
            System.out.println() ;
            System.out.flush() ;
        }
        Op op2 = Transformer.transform(new TransformJoinStrategy(ARQ.getContext()), op) ;
        System.out.println(op2) ;
        System.out.flush() ;
        
        exit(0) ;
        
        // -----
        Query query = QueryFactory.read("Q.rq") ;
        Model m = FileManager.get().loadModel("D.ttl") ;
        //ARQ.setExecutionLogging(InfoLevel.ALL) ;
        QueryExecution qExec = QueryExecutionFactory.create(query, m) ;
        ResultSetFormatter.out(qExec.execSelect()) ;

        exit(0) ;
    }

    public static void canoncialNodes()
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
        exit(0) ;
    }
    
    
    static public class wait extends FunctionBase1 {

        @Override
        public NodeValue exec(NodeValue nv)
        {
            if ( ! nv.isInteger() )
                throw new ExprEvalException("Not an integer") ;
            int x = nv.getInteger().intValue() ;
            Lib.sleep(x) ;
            return nv ;
        }
    }
    
    public static void queryExecTimeout()
    {
        FunctionRegistry.get().put("http://example/f#wait", wait.class) ;
        
        Model model = FileManager.get().loadModel("D.nt") ;
        Query query = QueryFactory.create("PREFIX f: <http://example/f#> SELECT *{?s ?p ?o FILTER (f:wait(1)) } ") ;
        
        QueryExecution qExec = QueryExecutionFactory.create(query, model) ;
        
        ((QueryExecutionBase)qExec).setTimeout(100) ;
        System.out.println("0");        // Not started yet.
        Lib.sleep(500) ;
        
        System.out.println("1");
        ResultSet rs = qExec.execSelect() ;
        System.out.println("2");
        rs.hasNext() ;
        System.out.println("3");
        rs.next() ;
        System.out.println("4");
        Lib.sleep(1000) ;
        System.out.println("5");
        exit(0) ;
        
        //ResultSetFormatter.out(rs) ;
        //System.out.println(rs.next()) ;
        //qExec.cancel() ;
        try { rs.hasNext() ; }  catch (QueryCancelledException ex) { System.out.println("CANCEL 1") ; } 
        try { rs.hasNext() ; }  catch (QueryCancelledException ex) { System.out.println("CANCEL 2") ; }
        try { rs.next() ; }
        catch (QueryCancelledException ex) { System.out.println("CANCEL 3") ; }
        catch (NoSuchElementException  ex) { System.out.println("No Elt 3") ; }

        System.out.println(rs.next()) ;
        qExec.abort() ;
        try { rs.hasNext() ; }  catch (QueryCancelledException ex) { System.out.println("CANCEL 4") ; } 
        try { rs.hasNext() ; }  catch (QueryCancelledException ex) { System.out.println("CANCEL 5") ; }
        try { rs.next() ; }
        catch (QueryCancelledException ex) { System.out.println("CANCEL 6") ; }
        catch (NoSuchElementException  ex) { System.out.println("No Elt 6") ; }
    }
    
    public static void parallelParser() throws Exception
    {
        
        final Triple marker = new Triple(Node.NULL, Node.NULL, Node.NULL) ; 
        final String filename = "/home/afs/Datasets/MusicBrainz/tracks-1k.nt" ;
        final BlockingQueue<Triple> queue = new ArrayBlockingQueue<Triple>(10) ;
        
        final Sink<Triple> sink = new Sink<Triple>() {
    
            public void close()
            {
                System.out.println("Close sink") ;
                queue.add(marker) ;
            }
    
            public void send(Triple item)
            {
                try
                {
                    queue.put(item) ;
                } catch (InterruptedException e)
                {
                    e.printStackTrace();
                } 
            }
    
            public void flush()
            {}
        } ;
        
        Runnable r = new Runnable() {
            public void run()
            {
                RiotReader.parseTriples(filename, sink) ;
                sink.close() ;
                System.out.println("Thread end") ;
            }
        } ;
    
        ExecutorService executor = Executors.newFixedThreadPool(1);
        executor.execute(r) ;
        
        for ( ;; )
        {
            Triple triple = queue.take() ;
            if ( triple == marker )
                break ;
            System.out.println(triple);
        }
        System.out.println("Wait for thread") ;
        
        executor.shutdown() ;
            
            
        exit(0) ;
        
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
            Violation v = vIter.next() ;
            System.out.println(v.getShortMessage()) ;
            //System.out.println(v.getSpecificationURL()) ;
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
    
    public static void qexpr(String exprStr)
    {
        try {
            PrefixMapping pmap = PrefixMapping.Factory.create()  ;
            pmap.setNsPrefixes(ARQConstants.getGlobalPrefixMap()) ;
            pmap.setNsPrefix("", "http://example/") ;
            pmap.setNsPrefix("ex", "http://example/ns#") ;

            Expr expr = ExprUtils.parse(exprStr, pmap) ;
            // Default action
            ARQ.getContext().set(ARQConstants.sysCurrentTime, NodeFactory.nowAsDateTime()) ;
            FunctionEnv env = new ExecutionContext(ARQ.getContext(), null, null, null) ; 
            NodeValue r = expr.eval(null, env) ;
            //System.out.println(r.asQuotedString()) ;
            Node n = r.asNode() ;
            String s = FmtUtils.stringForNode(n) ;
            System.out.println(s) ;
        } catch (ARQException ex)
        {
            System.out.println(" ** "+ex) ;
        }
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
 * (c) Copyright 2010 Epimorphics Ltd
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
