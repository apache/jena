/*
 * (c) Copyright 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * (c) Copyright 2010 Talis Information Ltd
 * All rights reserved.
 * [See end of file]
 */

package dev;

import java.io.Reader ;
import java.io.StringReader ;
import java.util.Iterator ;

import org.junit.Assert ;
import org.junit.Test ;

import arq.sparql ;

import com.hp.hpl.jena.Jena ;
import com.hp.hpl.jena.datatypes.xsd.XSDDatatype ;
import com.hp.hpl.jena.datatypes.xsd.XSDDateTime ;
import com.hp.hpl.jena.query.* ;
import com.hp.hpl.jena.rdf.model.Model ;
import com.hp.hpl.jena.rdf.model.ModelFactory ;
import com.hp.hpl.jena.shared.JenaException ;
import com.hp.hpl.jena.sparql.algebra.Algebra ;
import com.hp.hpl.jena.sparql.algebra.ExtBuilder ;
import com.hp.hpl.jena.sparql.algebra.Op ;
import com.hp.hpl.jena.sparql.algebra.OpExtRegistry ;
import com.hp.hpl.jena.sparql.algebra.op.OpExt ;
import com.hp.hpl.jena.sparql.algebra.op.OpFetch ;
import com.hp.hpl.jena.sparql.algebra.op.OpFilter ;
import com.hp.hpl.jena.sparql.core.Prologue ;
import com.hp.hpl.jena.sparql.core.QueryCheckException ;
import com.hp.hpl.jena.sparql.engine.ExecutionContext ;
import com.hp.hpl.jena.sparql.engine.QueryIterator ;
import com.hp.hpl.jena.sparql.expr.Expr ;
import com.hp.hpl.jena.sparql.expr.ExprEvalException ;
import com.hp.hpl.jena.sparql.expr.ExprEvalTypeException ;
import com.hp.hpl.jena.sparql.expr.NodeValue ;
import com.hp.hpl.jena.sparql.expr.nodevalue.XSDFuncOp ;
import com.hp.hpl.jena.sparql.function.FunctionEnvBase ;
import com.hp.hpl.jena.sparql.lang.sparql_11.SPARQLParser11 ;
import com.hp.hpl.jena.sparql.serializer.SerializationContext ;
import com.hp.hpl.jena.sparql.sse.Item ;
import com.hp.hpl.jena.sparql.sse.ItemList ;
import com.hp.hpl.jena.sparql.sse.SSE ;
import com.hp.hpl.jena.sparql.sse.SSEParseException ;
import com.hp.hpl.jena.sparql.sse.WriterSSE ;
import com.hp.hpl.jena.sparql.sse.builders.BuildException ;
import com.hp.hpl.jena.sparql.util.ALog ;
import com.hp.hpl.jena.sparql.util.ExprUtils ;
import com.hp.hpl.jena.sparql.util.IndentedLineBuffer ;
import com.hp.hpl.jena.sparql.util.IndentedWriter ;
import com.hp.hpl.jena.sparql.util.NodeIsomorphismMap ;
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

    public static void main(String[] argv) throws Exception
    {
        // Timezone stuff on G*
        arq.qexpr.main("1+2") ;
        {
            NodeValue nv1 = NodeValue.makeNode("1970-02-01T01:02:03", XSDDatatype.XSDdateTime) ;
            
            NodeValue nv2 = NodeValue.makeNode("2010-03-22", XSDDatatype.XSDdate) ;
            
            XSDDateTime dt1 = 
                (XSDDateTime)nv1.asNode().getLiteralValue() ;
            XSDDateTime dt2 = 
                (XSDDateTime)nv2.asNode().getLiteralValue() ;

            System.out.println(dt1) ;
            System.out.println(dt2) ;
            
            NodeValue nv3 = XSDFuncOp.dateTimeCast(nv1,  XSDDatatype.XSDtime) ;
            
            System.out.println("@--> "+nv3) ;
            System.out.println(nv3.asNode().getLiteral().isWellFormed()) ;
//            int cmp = dt1.compare(dt2) ;
//            System.out.println("Compare = "+cmp) ;
//
//            System.out.println("sameValue     = "+NodeValue.sameAs(nv1, nv2)) ;
//            System.out.println("notSameValue  = "+NodeValue.notSameAs(nv1, nv2)) ;
//            //System.out.println("compare       = "+NodeValue.compare(nv1, nv2)) ;
//            System.out.println("compare2      = "+NodeValue.compareAlways(nv1, nv2)) ;
            
            System.exit(0) ;
        }
        
        Query query = QueryFactory.create("SELECT * { FILTER ($a = 1 || $a = 2) }");
        Model model = ModelFactory.createDefaultModel();
        QuerySolutionMap map = new QuerySolutionMap();
        map.add("a", model.createLiteral("qwe"));
        QueryExecution queryExecution = QueryExecutionFactory.create(query, model, map);
        ResultSet resultSet = queryExecution.execSelect();
        ResultSetFormatter.out(resultSet) ;
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
    
    private static void execTimed()
    {
        System.out.println(ARQ.VERSION); 
        System.out.println(Jena.VERSION); 

        Query query = QueryFactory.read("Q.rq") ;

//        Op op = Algebra.compile(query.getQueryPattern()) ;
//        Transform t = new TransformJoinStrategy(null) ;
//        op = Transformer.transform(t, op) ;
//        System.out.println(op) ; 
//        System.exit(0) ;
        
        Model model = FileManager.get().loadModel("D.nt") ;
        //Model model = null;
        Timer timer = new Timer() ;
        timer.startTimer() ;
        exec(query, model) ;
        long time = timer.endTimer() ;
        System.out.printf("Time = %.2fs\n", time/1000.0) ;
        System.exit(0) ;
    }

    private static void exec(Query query, Model model)
    {
        QueryExecution qexec = QueryExecutionFactory.create(query, model) ;
        ResultSet rs = qexec.execSelect() ;
        ResultSetFormatter.out(rs) ;
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
}

/*
 * (c) Copyright 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * (c) Copyright 2010 Talis Information Ltd
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
