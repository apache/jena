/*
 * (c) Copyright 2007, 2008, 2009 Hewlett-Packard Development  Company, LP
 * (c) Copyright 2010 Talis Systems Ltd
 * All rights reserved.
 * [See end of file]
 */

package dev;

import java.io.Reader ;
import java.io.StringReader ;
import java.util.Iterator ;

import org.openjena.atlas.io.IndentedLineBuffer ;
import org.openjena.atlas.io.IndentedWriter ;
import org.openjena.atlas.lib.Sink ;
import org.openjena.atlas.lib.SinkWrapper ;
import org.openjena.atlas.lib.StrUtils ;
import org.openjena.riot.ErrorHandlerLib ;
import org.openjena.riot.RiotLib ;
import org.openjena.riot.checker.CheckerIRI ;
import reports.ReportSlowDatatype ;
import riot.inf.infer ;

import com.hp.hpl.jena.iri.IRI ;
import com.hp.hpl.jena.iri.IRIFactory ;
import com.hp.hpl.jena.iri.Violation ;
import com.hp.hpl.jena.query.* ;
import com.hp.hpl.jena.rdf.model.Model ;
import com.hp.hpl.jena.shared.JenaException ;
import com.hp.hpl.jena.sparql.algebra.Algebra ;
import com.hp.hpl.jena.sparql.algebra.ExtBuilder ;
import com.hp.hpl.jena.sparql.algebra.Op ;
import com.hp.hpl.jena.sparql.algebra.OpExtRegistry ;
import com.hp.hpl.jena.sparql.algebra.Transform ;
import com.hp.hpl.jena.sparql.algebra.TransformUnionQuery ;
import com.hp.hpl.jena.sparql.algebra.Transformer ;
import com.hp.hpl.jena.sparql.algebra.op.OpExt ;
import com.hp.hpl.jena.sparql.algebra.op.OpFetch ;
import com.hp.hpl.jena.sparql.algebra.op.OpFilter ;
import com.hp.hpl.jena.sparql.algebra.opt.Optimize ;
import com.hp.hpl.jena.sparql.algebra.opt.TransformPropertyFunction ;
import com.hp.hpl.jena.sparql.core.DatasetGraph ;
import com.hp.hpl.jena.sparql.core.Prologue ;
import com.hp.hpl.jena.sparql.core.QueryCheckException ;
import com.hp.hpl.jena.sparql.engine.ExecutionContext ;
import com.hp.hpl.jena.sparql.engine.QueryIterator ;
import com.hp.hpl.jena.sparql.engine.ref.QueryEngineRef ;
import com.hp.hpl.jena.sparql.expr.Expr ;
import com.hp.hpl.jena.sparql.expr.ExprEvalException ;
import com.hp.hpl.jena.sparql.expr.NodeValue ;
import com.hp.hpl.jena.sparql.function.FunctionEnvBase ;
import com.hp.hpl.jena.sparql.lang.sparql_11.SPARQLParser11 ;
import com.hp.hpl.jena.sparql.resultset.ResultsFormat ;
import com.hp.hpl.jena.sparql.serializer.SerializationContext ;
import com.hp.hpl.jena.sparql.sse.Item ;
import com.hp.hpl.jena.sparql.sse.ItemList ;
import com.hp.hpl.jena.sparql.sse.SSE ;
import com.hp.hpl.jena.sparql.sse.SSEParseException ;
import com.hp.hpl.jena.sparql.sse.WriterSSE ;
import com.hp.hpl.jena.sparql.sse.builders.BuildException ;
import com.hp.hpl.jena.sparql.sse.builders.BuilderGraph ;
import com.hp.hpl.jena.sparql.util.ALog ;
import com.hp.hpl.jena.sparql.util.ExprUtils ;
import com.hp.hpl.jena.sparql.util.NodeIsomorphismMap ;
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
//      String queryFile = dir+"graph-subquery-1.rq" ;
//      String dataFile = "--data="+dir+"data-sq.ttl" ;
      String queryFile = "--query="+dir+"temporalProximity01.rq" ;
      String dataFile = "--data="+dir+"temporalProximity01.ttl" ;
      
      arq.qtest.main(dir+"/manifest.ttl") ;System.exit(0) ;
      arq.rset.main(dir+"/temporalProximity01.srx") ;System.exit(0) ;
      
      arq.sparql.main(/*"--engine=ref",*/ "--syntax=sparql_11", dataFile, queryFile) ;
      System.exit(0) ; 
    }

    public static void main(String[] argv) throws Exception
    {
        
//        Expr expr = SSE.parseExpr("(+ ?x ?y)") ;
//        ExprTransform et = new ExprTransformCopy(true) ;
//        Expr expr2 = ExprTransformer.transform(et, expr) ;
//        System.out.println(expr2) ;
//        System.exit(0) ;

        if ( false )
        {
            // BUG
            Transform t = new TransformPropertyFunction(ARQ.getContext()) ;
            Op op = SSE.parseOp("(prefix ((list: <http://jena.hpl.hp.com/ARQ/list#>)) (filter (exists (bgp (?s list:member ?o))) (bgp (?s1 list:member ?o1))))") ;
            Op op2 = Transformer.transform(t, op) ;
            System.out.println(op2) ;
            System.exit(0) ;
        }

        ReportSlowDatatype.main() ;
        System.exit(0) ;
        
        System.out.println(RiotLib.parse("'hello'")) ;
        System.out.println(RiotLib.parse("123")) ;
        System.out.println(RiotLib.parse("'123'^^xsd:byte")) ;
        System.exit(0) ;
        
        runTest() ;
        
//        DatasetGraph dsg = DatasetLoader.load("D.trig") ;
//        Dump.write(System.out, dsg) ;
//        System.out.println("DONE") ;
//        System.exit(0) ;
        
        
//        final Binding b = new Binding0() ;
//        Renamer renamer = new Renamer() {
//            public Node rename(Node node)
//            {
//                if ( ! Var.isVar(node) ) return null ;
//                Var v = Var.alloc(node) ;
//                return b.get(v) ;
//            } } ;
        
        streamInference() ;
        
        //unionTransform() ;
        
    }
    
    public static void unionTransform()
    {
        //qparse("--print=op", "PREFIX : <http://example/> SELECT :function(?x +?y) ?z {}" ) ;
        
//        String dir = "testing/ARQ/Union" ;
//        ARQ.setStrictMode() ;
//        execQuery(dir+"/data-1.ttl", dir+"/union-5.rq") ;
        
            // Tests of SSE. 
            // Round triple items.
            
        String x = StrUtils.strjoinNL("(dataset",
                                      "  (graph (<s> <p> <o>) (<x> <p> <o>) (<x2> <p> <o>))",
                                      "  (graph <g1> (triple <s1> <p1> <o1>))",
                                      "  (graph <g2> (triple <s2> <p2> <o2>))",
                                      "  (graph <g3> (triple <s2> <p2> <o2>))", // Duplicate triple
                                      ")") ;
        Item item = SSE.parse(x) ;
        DatasetGraph dsg = BuilderGraph.buildDataset(item) ;

        Query query = QueryFactory.create("SELECT ?s ?p ?o { ?s ?p ?o }") ;
        query.setResultVars() ;
        Op op = Algebra.compile(query) ;
//        QueryExecUtils.executeAlgebra(op, dsg, ResultsFormat.FMT_TEXT) ;


//        divider() ;
//        System.out.println(op) ;

        Op op2 = TransformUnionQuery.transform(op) ;
        System.out.print(op2) ;
//        System.out.print(OpAsQuery.asQuery(op3)) ;

        QueryExecUtils.executeAlgebra(op2, dsg, ResultsFormat.FMT_TEXT) ;

        System.exit(0) ;
    }
    
    static class SinkInsertText<T> extends SinkWrapper<T>
    {
        String string ;
        public SinkInsertText(Sink<T> sink, String string)
        {
            super(sink) ;
            this.string = string ;
        }
        
        @Override
        public void send(T item)
        {
            super.send(item) ;
            System.out.print(string) ;
        }
    }
    
    public static void streamInference()
    {
        String dir = "../TDB/IBM-2010-07-14" ;
        String base = "https://shelsen.torolab.ibm.com:9443/jazz/storage/com.ibm.xtools.rmps.index/resource" ;
        
                        
        
        
        DataSource ds = DatasetFactory.create() ;
        
        // Q1 is right, Q2 is wrong - std engine
        String queryfile = dir+"/Q4.rq" ;
        
//        arq.sparql.main("--namedGraph="+dir+"/resource1.nt", "--namedGraph="+dir+"/resource2.nt", "--namedGraph="+dir+"/resource3.nt", "--namedGraph="+dir+"/resource4.nt", "--namedGraph="+dir+"/resource5.nt", 
//                        "--namedGraph="+dir+"/resource6.nt", "--namedGraph="+dir+"/resource7.nt", "--namedGraph="+dir+"/resource8.nt", "--namedGraph="+dir+"/resource9.nt" , 
//                        "--data="+dir+"/resource1.nt", "--data="+dir+"/resource2.nt", "--data="+dir+"/resource3.nt", "--data="+dir+"/resource4.nt", "--data="+dir+"/resource5.nt", 
//                        "--data="+dir+"/resource6.nt", "--data="+dir+"/resource7.nt", "--data="+dir+"/resource8.nt", "--data="+dir+"/resource9.nt",
//                        "--query="+queryfile) ;
//        System.exit(0) ;

        
        //arq.qparse.main("--print=opt", "--query="+queryfile) ;
        
        for ( int i = 1 ; i <= 9 ; i++ )
        {
            String fn = dir+"/resource"+i+".nt" ;
            
            Model model1 = FileManager.get().loadModel(fn) ;
            Model model2 = FileManager.get().loadModel(fn) ;
            ds.addNamedModel(base+i, model1) ;
            ds.getDefaultModel().add(model2) ;
        }

        if ( true )
        {
            System.out.println("----") ;
            Query query = QueryFactory.read(queryfile) ;
            Op op1 = Algebra.compile(query) ;
            System.out.println("op1 = \n"+op1) ;
            Op op2 = Algebra.optimize(op1) ;
            System.out.println("op2 = \n"+op2) ;
            System.out.println() ;
            
            Optimize.noOptimizer() ;
            
            System.out.println("Execute op1") ;
            QueryExecUtils.executeAlgebra(op1, ds.asDatasetGraph(), ResultsFormat.FMT_TEXT) ;

            System.out.println("Execute op2") ;
            QueryExecUtils.executeAlgebra(op2, ds.asDatasetGraph(), ResultsFormat.FMT_TEXT) ;

//            QueryEngineRef.register() ;
//
//            System.out.println("Execute op1 (ref)") ;
//            QueryExecUtils.executeAlgebra(op1, ds.asDatasetGraph(), ResultsFormat.FMT_TEXT) ;
//            
//
//            QueryEngineRef.unregister() ;
            System.exit(0) ;
            
        }
        
        {
            Query query = QueryFactory.read(queryfile) ;
            QueryExecution qExec = QueryExecutionFactory.create(query, ds) ;
            ResultSetFormatter.out(qExec.execSelect()) ;
        }
        QueryEngineRef.register() ;
        {
            Query query = QueryFactory.read(queryfile) ;
            QueryExecution qExec = QueryExecutionFactory.create(query, ds) ;
            ResultSetFormatter.out(qExec.execSelect()) ;
        }

        System.exit(0) ;
        
        
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
    
    public static void check(Query query, boolean optimizeAlgebra)
    {
        Op op = Algebra.compile(query) ;
        check( op, optimizeAlgebra, query) ;        
    }
    
    private static void check(Op op, boolean optimizeAlgebra, Prologue prologue)
    {
        IndentedLineBuffer buff = new IndentedLineBuffer() ;
        if ( optimizeAlgebra )
            op =  Algebra.optimize(op) ;
        WriterSSE.out(buff, op, prologue) ;
        String str = buff.toString() ;
        
        try {
            Op op2 = SSE.parseOp(str) ;
            if ( op.hashCode() != op2.hashCode() )
            {
                System.out.println(str) ;
                System.out.println(op) ;
                System.out.println(op2) ;
                
                throw new QueryCheckException("reparsed algebra expression hashCode does not equal algebra from query") ;
            }
            System.out.println(op) ;
            System.out.println(op2) ;
            
            // Expression in assignment for op 
            
            if ( ! op.equals(op2) )
            {
                Expr e1 = ((OpFilter)op).getExprs().get(0) ;
                Expr e2 = ((OpFilter)op2).getExprs().get(0) ;

                op = ((OpFilter)op).getSubOp() ;
                op2 = ((OpFilter)op2).getSubOp() ;

                if ( ! op.equals(op2) )
                    System.err.println("Sub patterns unequal") ;
                
                if ( ! e1.equals(e2) )
                {
                    System.err.println(e1) ;
                    System.err.println(e2) ;
                    System.err.println("Expressions unequal") ;
                }
                
                throw new QueryCheckException("reparsed algebra expression does not equal query algebra") ;
            }
        } catch (SSEParseException ex)
        { 
            System.err.println(str);
            throw ex ; 
        }      // Breakpoint
        catch (BuildException ex)
        {
            System.err.println(str);
            throw ex ; 
        }

    }

    public static void fetch()
    {
        OpFetch.enable() ;
        arq.sparql.main(new String[]{"--file=Q.arq"}) ;
        //System.out.println("----") ;
        System.exit(0) ; 
    }
    
    public static void opExtension()
    {
        OpExtRegistry.register(new ExtBuilder(){
            public OpExt make(ItemList argList)
            {
                System.out.println("Args: "+argList) ;
                return new OpExtTest(argList) ;
            }

            public String getTagName() { return "ABC" ; }
        }) ;
        
        Op op1 = SSE.parseOp("(ext ABC 123 667)") ;
        System.out.println(op1); 

        Op op2 = SSE.parseOp("(ABC 123 667)") ;
        System.out.println(op2); 

        System.out.println("----") ; System.exit(0) ; 
    }

    static class OpExtTest extends OpExt 
    {
        private ItemList argList ;
    
        public OpExtTest(ItemList argList)
        { super("TAG") ; this.argList = argList ; }
    
        @Override
        public Op effectiveOp()
        {
            return null ;
        }
    
        @Override
        public QueryIterator eval(QueryIterator input, ExecutionContext execCxt)
        {
            return null ;
        }
    
        @Override
        public boolean equalTo(Op other, NodeIsomorphismMap labelMap)
        {
            if ( ! ( other instanceof OpExtTest) ) return false ;
            return argList.equals(((OpExtTest)other).argList) ;
        }
    
        @Override
        public void outputArgs(IndentedWriter out, SerializationContext sCxt)
        {
            boolean first = true ;
            for ( Iterator<Item> iter = argList.iterator() ; iter.hasNext() ; )
            {
                Item item = iter.next();
                if ( first )
                    first = false ;
                else
                    out.print(" ") ;
                out.print(item) ;
            }
        }
        
        @Override
        public int hashCode()
        {
            return argList.hashCode() ;
        }
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
