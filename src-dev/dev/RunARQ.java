/*
 * (c) Copyright 2007, 2008, ;
 * 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package dev;

import java.util.Iterator ;

import org.junit.Assert ;

import arq.qexpr ;
import arq.sparql ;
import arq.sse_query ;

import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.graph.test.GraphTestBase ;
import com.hp.hpl.jena.query.* ;
import com.hp.hpl.jena.rdf.model.Model ;
import com.hp.hpl.jena.rdf.model.ModelFactory ;
import com.hp.hpl.jena.rdf.model.Resource ;
import com.hp.hpl.jena.rdf.model.ResourceFactory ;
import com.hp.hpl.jena.rdf.model.impl.Util ;
import com.hp.hpl.jena.sparql.algebra.Algebra ;
import com.hp.hpl.jena.sparql.algebra.ExtBuilder ;
import com.hp.hpl.jena.sparql.algebra.Op ;
import com.hp.hpl.jena.sparql.algebra.OpAsQuery ;
import com.hp.hpl.jena.sparql.algebra.OpExtRegistry ;
import com.hp.hpl.jena.sparql.algebra.Transformer ;
import com.hp.hpl.jena.sparql.algebra.op.OpExt ;
import com.hp.hpl.jena.sparql.algebra.op.OpFetch ;
import com.hp.hpl.jena.sparql.algebra.op.OpFilter ;
import com.hp.hpl.jena.sparql.algebra.op.OpJoin ;
import com.hp.hpl.jena.sparql.algebra.op.OpLeftJoin ;
import com.hp.hpl.jena.sparql.algebra.opt.Optimize ;
import com.hp.hpl.jena.sparql.core.DatasetGraph ;
import com.hp.hpl.jena.sparql.core.Prologue ;
import com.hp.hpl.jena.sparql.core.QueryCheckException ;
import com.hp.hpl.jena.sparql.engine.ExecutionContext ;
import com.hp.hpl.jena.sparql.engine.QueryIterator ;
import com.hp.hpl.jena.sparql.engine.main.JoinClassifier ;
import com.hp.hpl.jena.sparql.engine.main.LeftJoinClassifier ;
import com.hp.hpl.jena.sparql.expr.Expr ;
import com.hp.hpl.jena.sparql.expr.NodeValue ;
import com.hp.hpl.jena.sparql.expr.nodevalue.XSDFuncOp ;
import com.hp.hpl.jena.sparql.resultset.ResultsFormat ;
import com.hp.hpl.jena.sparql.serializer.SerializationContext ;
import com.hp.hpl.jena.sparql.sse.Item ;
import com.hp.hpl.jena.sparql.sse.ItemList ;
import com.hp.hpl.jena.sparql.sse.SSE ;
import com.hp.hpl.jena.sparql.sse.SSEParseException ;
import com.hp.hpl.jena.sparql.sse.WriterSSE ;
import com.hp.hpl.jena.sparql.sse.builders.BuildException ;
import com.hp.hpl.jena.sparql.sse.builders.BuilderExec ;
import com.hp.hpl.jena.sparql.sse.writers.WriterOp ;
import com.hp.hpl.jena.sparql.util.* ;
import com.hp.hpl.jena.update.GraphStore ;
import com.hp.hpl.jena.update.GraphStoreFactory ;
import com.hp.hpl.jena.update.UpdateAction ;
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
    
//    public static void x(String str) 
//    {
//        System.out.println(str) ; 
//        Item item = SSE.parse(str) ;
//        ExprList exprList = BuilderExpr.buildExprOrExprList(item) ;
//        System.out.println(exprList) ; 
//        
//    }
    
    public static void main(String[] argv) throws Exception
    {
        NodeValue four = NodeValue.makeInteger(4) ;
        NodeValue two = NodeValue.makeInteger(2) ;
        NodeValue result = XSDFuncOp.sqrt( four ) ;
        Assert.assertTrue( NodeValue.sameAs( two, result)) ;
        Assert.assertTrue( two.asNode().sameValueAs(result.asNode()) ) ;
        
        System.exit(0) ;
        //assertEquals( two, XSDFuncOp.sqrt( result ).asNode() );

        
        
        String str = "http://host/foo%2Fbar" ;
        System.out.println("<"+str+">") ;
        
        int x = Util.splitNamespace(str) ;
        String ns = str.substring(0,x) ;
        String ln = str.substring(x) ;
        System.out.println("-->"+ns+":"+ln) ;
        
        Resource r = ResourceFactory.createResource(str) ;
        System.out.println("ns="+r.getNameSpace()) ;
        System.out.println("ln="+r.getLocalName()) ;
        
        System.exit(0) ;
        
        GraphStore gs = GraphStoreFactory.create() ;
        UpdateAction.readExecute("update.ru", gs) ;
        //UpdateAction.readExecute("update.ru", gs) ;
        
        Model m = ModelFactory.createModelForGraph(gs.getGraph(Node.createURI("http://foo/model04"))) ;
        m.write(System.out, "RDF/XML-ABBREV") ;
        System.exit(0) ;
        
        
        
        
        //runUpdate() ;
        
//        x("123") ;
//        x("(= 123 456)") ;
//        x("(exprlist (= 123 456) (= 123 456))") ;
//        x("((= 123 456) (= 123 456))") ;
//        
//        System.exit(0) ;
        
        {
            String queryString = StrUtils.strjoinNL(
                                                    "PREFIX : <http://example/>", 
                                                    "SELECT *",
                                                    "{",
                                                    "    GRAPH ?g {",
                                                    "    ?s ?p ?o",
                                                    "    }",
                                                    "    FILTER(?o = :x2 )",
                                                    "    FILTER( ?g = :x1 || ?g != :x4 || ?g = :x3 )",
                                                    "}") ;
            // More cautious.  Only on || equalities
            Query q = QueryFactory.create(queryString, Syntax.syntaxARQ) ;
            divider() ;
            System.out.println(q) ;
            Op op = Algebra.compile(q) ;
            System.out.println("Compile::") ;
            System.out.println(op) ;
            
            op = Algebra.optimize(op) ;
            divider() ;
            System.out.println("Optimize::") ;
            System.out.println(op) ;
            //Not yet active.
            op = Transformer.transform(new TransformFilterDisjunction(), op) ;
            
            divider() ;
            System.out.println("Transform disjunctions::") ;
            System.out.println(op) ;

            DatasetGraph dsg = DatasetUtils.createDataset("D.ttl", null).asDatasetGraph() ;
            QueryExecUtils.executeAlgebra(op, dsg, ResultsFormat.FMT_TEXT) ;
            System.exit(0) ;
        }
        
        qexpr.main("fn:round(1)") ; System.exit(0) ; 
        
        report() ; System.exit(0) ;
        
        execQuery("D.ttl", "Q.arq") ;
        
        
        arq.qexpr.main("coalesce(123)") ; System.exit(0) ;
        String queryString = StrUtils.strjoinNL("PREFIX : <http://example/>",
                                                "SELECT *",
                                                "{",
                                                "   ?s ?p1 ?x OPTIONAL { ?s ?p2 ?x OPTIONAL { ?s ?p3 ?x } }" ,
                                                //"   ?s ?p ?x OPTIONAL { ?s ?p ?o FILTER(?x) }" ,
                                                "}") ;
        Query query = QueryFactory.create(queryString, Syntax.syntaxARQ) ;
        Op op = Algebra.compile(query) ;
        
        boolean b = LeftJoinClassifier.isLinear((OpLeftJoin)op) ;
        System.out.println(op) ;
        System.out.println(b) ;
        
        Op op2 = Optimize.optimize(op, ARQ.getContext()) ;
        System.out.println(op2) ;
        System.exit(0) ;
        
        //WriterOp.output(IndentedWriter.stdout, op) ;
        //IndentedWriter.stdout.flush();
        
        IndentedLineBuffer buff = new IndentedLineBuffer() ;
        IndentedWriter iWriter = buff.getIndentedWriter() ;
        iWriter.setFlatMode(true) ;
        WriterOp.output(iWriter, op) ;
        iWriter.flush() ;
        String s = buff.asString() ;
        System.out.println(s) ;
        
        
        System.exit(0) ;
        
        execQuery("D.ttl", "Q.arq") ; System.exit(0) ;
        divider() ;
    }

    
    private static void classify()
    {
        String queryString = StrUtils.strjoinNL("PREFIX : <http://example/>",
                                                "SELECT *",
                                                "{",
                                                "   {:x :p ?x} { :y :q ?w OPTIONAL { ?w :r ?x2 }}" ,
                                                "}") ;
        Query query = QueryFactory.create(queryString) ;
        Op op = Algebra.compile(query) ;

        boolean b = JoinClassifier.isLinear((OpJoin)op) ;

        System.out.println(op) ;
        System.out.println(b) ;
        System.exit(0) ;
    }
    

    
    private static void testOpToSyntax(String opStr, String queryString)
    {
        Op op = SSE.parseOp(opStr) ;
        Query queryConverted = OpAsQuery.asQuery(op) ;
        queryConverted.setResultVars() ;
        
        Query queryExpected = QueryFactory.create(queryString) ;
        System.out.println(queryConverted) ;
        System.out.println(queryExpected) ;
        queryExpected.setResultVars() ;
        
        System.out.println( queryExpected.getQueryPattern().equals(queryConverted.getQueryPattern()))  ;
        System.out.println( queryExpected.equals(queryConverted))  ;
    }

    public static void report()
    {
        String sparqlQuery = StrUtils.strjoinNL(
                  "PREFIX  rdf:    <http://www.w3.org/1999/02/22-rdf-syntax-ns#>",
                  "PREFIX  rdfs:   <http://www.w3.org/2000/01/rdf-schema#>",
                  "PREFIX  pre:   <http://example/>",        
                  "SELECT ?x WHERE { ?x rdf:type ?class . ?class rdfs:subClassOf pre:myClass . }"); 
        Model model = ModelFactory.createDefaultModel() ;

        while (true) {
            Query query = QueryFactory.create(sparqlQuery, Syntax.syntaxSPARQL);
            QueryExecution exec = QueryExecutionFactory.create(QueryFactory.create(query), model);
            ResultSet result = exec.execSelect();

                
            while (result.hasNext()) {
                // do something
            }
            result = null;
            
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) { }
        }
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
        WriterSSE.out(buff.getIndentedWriter(), op, prologue) ;
        String str = buff.getBuffer().toString() ;
        
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

    static void queryEquality()
    {
        String[] x1 = { "PREFIX  :     <http://example/>", 
            "PREFIX  foaf: <http://xmlns.com/foaf/0.1/>",
            "",
            "SELECT  ?x ?n",
            "WHERE", 
            "  { ?x foaf:name ?n" ,
            "    { SELECT  ?x",
            "      WHERE",
            "        { ?x foaf:knows ?z}",
            "      GROUP BY ?x",
            "      HAVING ( count(*) >= 10 )",
            "    }",
        "  }" } ;
        
        String[] x2 = { "PREFIX  :     <http://example/>", 
            "PREFIX  foaf: <http://xmlns.com/foaf/0.1/>",
            "SELECT  *",
            "{ }",
            "GROUP BY ?x",
            "HAVING ( count(*) )"
        } ;
        
        String[] x3 = { "(filter (>= ?.0 10)",
            "   (group (?x) ((?.0 (count)))" ,
            "   (table unit)",
            "))" } ;
        
//        Op op = SSE.parseOp(StringUtils.join("\n", x3)) ;
//        checkOp(op, false, null) ;
//        System.out.println();
        Query query = QueryFactory.create(StringUtils.join("\n", x2), Syntax.syntaxARQ) ;
        check(query, false) ;
        System.exit(0) ;

    }
    
    private static void compare(String string, Op op1, Op op2)
    {
        divider() ;
        System.out.println("Compare: "+string) ;
        
        if ( op1.hashCode() != op2.hashCode() )
        {
//            System.out.println(str) ;
//            System.out.println(op) ;
//            System.out.println(op2) ;
//            
            throw new QueryCheckException("reparsed algebra expression hashCode does not equal algebra from query") ;
        }
//        System.out.println(op) ;
//        System.out.println(op2) ;
        
        // Expression in assignment for op 
        
        if ( ! op1.equals(op2) )
            throw new QueryCheckException("reparsed algebra expression does not equal query algebra") ;
        
    }
    
    public static void fetch()
    {
        OpFetch.enable() ;
        sparql.main(new String[]{"--file=Q.arq"}) ;
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
        //initialBinding.add("s", model.createResource("http://example/x1")) ;
        initialBinding.add("o", model.createResource("http://example/z")) ;
        
        QueryExecution qExec = QueryExecutionFactory.create(query, model, initialBinding) ;
        ResultSetFormatter.out(qExec.execSelect()) ;
        System.exit(0) ;
    }

    private static void execRemote()
    {
        System.setProperty("socksProxyHost", "socks-server") ;
    
        String a2[] = { "--service=http://dbpedia.org/sparql",
        "SELECT * WHERE {  <http://dbpedia.org/resource/Angela_Merkel> <http://dbpedia.org/property/reference> ?object.  FILTER  (!isLiteral(?object))}"} ;
        arq.remote.main(a2) ;
        System.exit(0) ;
    }
    
    private static void runExecuteSSE(String[] argv)
    {
        
        String[] a = { "--file=SSE/all.sse" } ;
        BuilderExec.main(a) ;
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
