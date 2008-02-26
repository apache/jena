/*
 * (c) Copyright 2007, 2008 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package dev;

import java.net.MalformedURLException;

import arq.sparql;
import arq.sse_query;

import com.hp.hpl.jena.iri.IRI;
import com.hp.hpl.jena.iri.IRIFactory;
import com.hp.hpl.jena.iri.IRIRelativize;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.sparql.algebra.Algebra;
import com.hp.hpl.jena.sparql.algebra.Op;
import com.hp.hpl.jena.sparql.algebra.OpVisitorBase;
import com.hp.hpl.jena.sparql.algebra.op.OpFilter;

public class Run
{
    public static void main(String[] argv) throws Exception
    {
        //rewrite() ;
        
        execQuery("D.ttl", "Q.rq") ;
        
        //QueryEngineMain.register() ;
        String a[] = new String[]{
            //"-v",
            //"--engine=ref",
            "-query=Q.rq" ,
            
        } ;
        
        sparql.main(a) ;
        System.exit(0) ;
    }

    static void rewrite()
    {
        Query query = QueryFactory.create("SELECT * { ?s ?p ?o FILTER(?o = <x>) }") ;
        Op op = Algebra.compile(query, true) ;
        System.out.println(op) ;
        op = Algebra.optimize(op) ;
        System.out.println(op) ;
        System.exit(0) ;
    }
    
    public static void code()
    {
        // see Rewrite.java
        Query q = QueryFactory.create("SELECT * { ?s ?p ?o FILTER(?o = 3) }") ;
        Op op = Algebra.compile(q, true) ;
        op = Algebra.optimize(op) ;
        System.out.println(op) ; 
        
    }

    static class F extends OpVisitorBase
    {
        public void visit(OpFilter opFilter)
        {
            //WriteropFilter.getExprs()
        }
    }
    
    
    private static void code1(String uri, String base)
    {
        int relFlags = IRIRelativize.SAMEDOCUMENT | IRIRelativize.CHILD ;
        IRI baseIRI = IRIFactory.jenaImplementation().construct(base) ;
        IRI rel = baseIRI.relativize(uri, relFlags) ;
        
        String s = null ; 
        try { s = rel.toASCIIString() ; }
        catch (MalformedURLException  ex) { s = rel.toString() ; }
            
        System.out.println("("+uri+" ["+base+"]) ==> <"+s+">") ;
        
        String s2 = baseIRI.create(s).toString() ;
        System.out.println("     "+s2) ;
        
    }

    private static void runQParse()
    {
        String []a = { "--file=Q.rq", "--print=op" } ;
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
        String a[] = {"--desc=etc/graphstore.ttl", "--update=update.ru", "--dump"} ;
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