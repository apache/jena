/*
 * (c) Copyright 2007, 2008 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package dev;

import arq.sparql;
import arq.sse_query;

import com.hp.hpl.jena.rdf.model.Model;

import com.hp.hpl.jena.util.FileManager;
import com.hp.hpl.jena.util.FileUtils;

import com.hp.hpl.jena.sparql.algebra.AlgebraQuad;
import com.hp.hpl.jena.sparql.algebra.Op;
import com.hp.hpl.jena.sparql.algebra.op.OpExt;
import com.hp.hpl.jena.sparql.sse.ItemList;
import com.hp.hpl.jena.sparql.sse.SSE;
import com.hp.hpl.jena.sparql.util.IndentedWriter;
import com.hp.hpl.jena.sparql.util.StringUtils;

import com.hp.hpl.jena.query.*;

import com.hp.hpl.jena.update.*;

import dev.OpExtFactory.ExtBuilder;

public class Run
{
    public static void main(String[] argv) throws Exception
    {
        OpExtFactory.register("ABC", new ExtBuilder(){

            public OpExt make(ItemList argList)
            {
                return null ;
            }}) ;
        
        SSE.parseOp("(ext ABC 123)") ;
        System.out.println("----") ; System.exit(0) ; 
        
        //execQuery("D.ttl", "Q.rq") ;
        //arq.query.main(new String[]{"--desc=umbel-db.ttl", "SELECT * {}"}) ;
        code() ; System.exit(0) ; 
        
        if ( false )
        {
//            Query query = QueryFactory.read("Q.arq", Syntax.syntaxARQ) ;
//            System.out.println(query) ;
//            System.exit(0) ;
            
            String x = FileUtils.readWholeFileAsUTF8("Q.arq") ;
            System.out.println(x) ;
            
            Query query1 = QueryFactory.read("Q.arq", Syntax.syntaxARQ) ;
            Query query2 = QueryFactory.read("Q.arq", Syntax.syntaxARQ) ;
            
            System.out.println(query1.hashCode()) ;
            System.out.println(query2.hashCode()) ;
            
            System.out.println(query1) ;
            
            query2 = QueryFactory.create(""+query1, Syntax.syntaxARQ) ;
            System.out.println(query2.hashCode()) ;
            
            System.exit(0) ;
        }
        //runQTest() ;
        runQParseARQ() ;
        // Compressed syntax
        // match(Subject, Path, Object, PrefixMapping)
        
        
        
        runQParse() ;
        execQuery("D.ttl", "Q.arq") ;
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
    
    public static void code()
    {

        Op op = SSE.readOp("Q.sse") ;
        //op = Algebra.optimize(op) ;
        //Op op3 = Algebra.compileQuad(query) ;

        System.out.println("---- Original") ;
        System.out.println(op) ;
        Op op2 = AlgebraQuad.quadize(op) ;

        System.out.println("---- Quadization") ;
        System.out.println(op2) ;
        System.exit(0) ;


    }

    private static void runQParse()
    {
        // "--engine=quad" "--opt"
        String []a = { "--file=Q.rq", "--print=op" , "--opt" } ; //, "--print=plan"} ;
        arq.qparse.main(a) ;
        System.exit(0) ;
    }
    
    private static void runQParseARQ()
    {
        String []a = { "--file=Q.arq", "--print=op" } ; //, "--opt", "--print=plan"} ;
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
        String DIR = "testing/ARQ/Assign/" ;
        String []a1 = { "--strict", "--data="+DIR+"data.ttl",
            "--query="+DIR+"assign-01.arq",
            "--result="+DIR+"assign-01.srx"} ;

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