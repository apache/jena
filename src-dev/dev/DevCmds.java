/*
 * (c) Copyright 2009 Talis Systems Ltd
 * All rights reserved.
 * [See end of file]
 */

package dev;

import java.util.Arrays ;
import java.util.List ;

import junit.framework.TestCase ;
import org.junit.runner.JUnitCore ;
import org.junit.runner.Result ;
import org.openjena.atlas.junit.TextListener2 ;

import com.hp.hpl.jena.query.* ;
import com.hp.hpl.jena.rdf.model.Model ;
import com.hp.hpl.jena.sparql.resultset.SPARQLResult ;
import com.hp.hpl.jena.tdb.TC_TDB ;
import com.hp.hpl.jena.tdb.junit.QueryTestTDB ;
import com.hp.hpl.jena.tdb.sys.TDBMaker ;

public class DevCmds
{
    static void query(String str, Dataset dataset)
    {
        query(str, dataset, null) ;
    }
    
    static void query(String str, Dataset dataset, QuerySolution qs)
    {
        System.out.println(str) ; 
        Query q = QueryFactory.create(str, Syntax.syntaxARQ) ;
        QueryExecution qexec = QueryExecutionFactory.create(q, dataset, qs) ;
        ResultSet rs = qexec.execSelect() ;
        ResultSetFormatter.out(rs) ;
        qexec.close() ;
    }
    
    static void query(String str, Model model)
    {
        Query q = QueryFactory.create(str, Syntax.syntaxARQ) ;
        QueryExecution qexec = QueryExecutionFactory.create(q, model) ;
        ResultSet rs = qexec.execSelect() ;
        ResultSetFormatter.out(rs) ;
        qexec.close() ;
    }
    
    static void test()
    {
        String testNum = "2" ;
        String dir = "testing/UnionGraph/" ;
        List<String> dftGraphs = Arrays.asList(dir+"data-dft.ttl") ;
        List<String> namedGraphs = Arrays.asList(dir+"data-1.ttl", dir+"data-2.ttl") ;
        String queryFile = dir+"merge-"+testNum+".rq" ;
        ResultSet rs = ResultSetFactory.load(dir+"merge-"+testNum+"-results.srx") ;
        
        TestCase t = new QueryTestTDB("Test", null, "uri", dftGraphs, namedGraphs, new SPARQLResult(rs), queryFile, TDBMaker.memFactory) ;
        JUnitCore runner = new org.junit.runner.JUnitCore() ;
        runner.addListener(new TextListener2(System.out)) ;
        
        TC_TDB.beforeClass() ;
        Result result = runner.run(t) ;
        TC_TDB.afterClass() ;
    }
    
    
    
    static void tdbquery(String... args)
    {
        tdb.tdbquery.main(args) ;
        System.exit(0) ;
    }
    
    static void tdbloader(String... args)
    {
        tdb.tdbloader.main(args) ; 
        System.exit(0) ;
    }
    
    static void tdbconfig(String... args) 
    {
        tdb.tdbconfig.main(args) ;
        System.exit(0) ;
    }

}

/*
 * (c) Copyright 2009 Talis Systems Ltd
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