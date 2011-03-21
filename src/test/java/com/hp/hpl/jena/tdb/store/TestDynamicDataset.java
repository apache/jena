/*
 * (c) Copyright 2010 Talis Systems Ltd.
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.tdb.store;

import java.util.Set ;

import org.apache.log4j.Level ;
import org.apache.log4j.Logger ;
import org.junit.Test ;
import org.openjena.atlas.junit.BaseTest ;

import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.graph.Triple ;
import com.hp.hpl.jena.query.Dataset ;
import com.hp.hpl.jena.query.Query ;
import com.hp.hpl.jena.query.QueryExecution ;
import com.hp.hpl.jena.query.QueryExecutionFactory ;
import com.hp.hpl.jena.query.QueryFactory ;
import com.hp.hpl.jena.query.ResultSet ;
import com.hp.hpl.jena.query.ResultSetFormatter ;
import com.hp.hpl.jena.rdf.model.Model ;
import com.hp.hpl.jena.sparql.algebra.Algebra ;
import com.hp.hpl.jena.sparql.algebra.Op ;
import com.hp.hpl.jena.sparql.algebra.Transformer ;
import com.hp.hpl.jena.sparql.engine.optimizer.reorder.ReorderLib ;
import com.hp.hpl.jena.sparql.sse.SSE ;
import com.hp.hpl.jena.tdb.TDB ;
import com.hp.hpl.jena.tdb.TDBFactory ;
import com.hp.hpl.jena.tdb.migrate.NodeUtils2 ;
import com.hp.hpl.jena.tdb.migrate.TransformDynamicDataset ;
import com.hp.hpl.jena.tdb.sys.SystemTDB ;

public class TestDynamicDataset extends BaseTest
{
    static {
        Logger.getLogger("com.hp.hpl.jena.tdb.info").setLevel(Level.WARN) ;
        SystemTDB.defaultOptimizer = ReorderLib.identity() ;
    }
    
    static Dataset dataset = TDBFactory.createDataset() ; 
    static { init() ; }
    private static void init()
    {
        // Named graphs
        for ( int i = 0 ; i < 5 ; i++ )
            addGraph(dataset, i) ;
        // Default model.
        Model m = dataset.getDefaultModel() ;
        Triple t1 = SSE.parseTriple("(<uri:x> <uri:p> 0)") ;
        Triple t2 = SSE.parseTriple("(<uri:y> <uri:q> 'ABC')") ; 
        Triple t3 = SSE.parseTriple("(<uri:z> <uri:property> 'DEF')") ; 
        m.getGraph().add(t1) ;
        m.getGraph().add(t2) ;
        m.getGraph().add(t3) ;
    }
    
    private static void addGraph(Dataset dataset, int i)
    {
        // Not a very interesting model
        Model m = dataset.getNamedModel("graph:"+i) ;
        Triple t1 = SSE.parseTriple("(<uri:x> <uri:p> "+i+")") ;
        Triple t2 = SSE.parseTriple("(<uri:y> <uri:q> 'ABC')") ; 
        m.getGraph().add(t1) ;
        m.getGraph().add(t2) ;
    }
    
    
    @Test public void dynamic01()    { testCount("SELECT * {?s ?p ?o}", 3) ; }
    
    @Test public void dynamic02()    { testCount("SELECT ?g { GRAPH ?g {} }", 5) ; }
    
    @Test public void dynamic03()    { testCount("SELECT * FROM <graph:1> {?s <uri:p> ?o}", 1) ; }

    @Test public void dynamic04()    { testCount("SELECT * FROM <graph:1> { GRAPH ?g { ?s ?p ?o} }", 0) ; }
    
    @Test public void dynamic05()    { testCount("SELECT * FROM <graph:1> FROM <graph:2> {?s <uri:p> ?o}", 2) ; }

    // Duplicate surpression
    @Test public void dynamic06()    { testCount("SELECT ?s FROM <graph:1> FROM <graph:2> {?s <uri:q> ?o}", 1) ; }
    
    @Test public void dynamic07()    { testCount("SELECT ?s FROM NAMED <graph:1> {?s <uri:q> ?o}", 0) ; }
    
    @Test public void dynamic08()    { testCount("SELECT ?s FROM <graph:2> FROM NAMED <graph:1> {?s <uri:q> ?o}", 1) ; }

    @Test public void dynamic09()    { testCount("SELECT * "+
                                                "FROM <graph:1> FROM <graph:2> "+
                                                "FROM NAMED <graph:3> FROM NAMED <graph:4> "+
                                                "{ GRAPH ?g { ?s <uri:q> ?o }}",
                                                2) ; 
                                    }
    
    @Test public void dynamic10()    { testCount("SELECT * "+
                                                "FROM <graph:1> FROM <graph:2>"+
                                                "FROM NAMED <graph:3> FROM NAMED <graph:4> "+
                                                "{ GRAPH ?g { ?s <uri:q> ?o }}",
                                                2) ; 
                                    }

    @Test public void dynamic11()    { testCount("SELECT * "+
                                                "FROM <x:unknown>"+
                                                "{ GRAPH ?g { ?s <uri:q> ?o }}",
                                                0) ; 
                                    }

    @Test public void dynamic12()    { testCount("SELECT * "+
                                                 "FROM  <graph:1>"+
                                                 "{ GRAPH ?g { }}",
                                                 0) ; 
                                     }

    @Test public void dynamic13()    { testCount("SELECT * "+
                                                 "FROM NAMED <graph:1>"+
                                                 "{ GRAPH ?g { }}",
                                                 1) ; 
                                     }

    @Test public void dynamic14()    { testCount("SELECT * "+
                                                 "FROM NAMED <graph:1> FROM NAMED <graph:2>"+
                                                 "FROM <graph:3> "+
                                                 "{ GRAPH ?g { }}",
                                                 2) ; 
                                     }
    

    // If  context.isTrue(TDB.symUnionDefaultGraph)
    
    @Test public void dynamicAndUnion1() {
        try { TDB.getContext().setTrue(TDB.symUnionDefaultGraph) ;
        testCount("SELECT * "+
                  "FROM NAMED <graph:3> FROM NAMED <graph:4> "+
                  "{ GRAPH ?g { ?s <uri:q> ?o }}",
                  2) ; 
        TDB.getContext().unset(TDB.symUnionDefaultGraph) ;
        } finally { TDB.getContext().unset(TDB.symUnionDefaultGraph) ; }
    }    

    @Test public void dynamicAndUnion2() {
        try { TDB.getContext().setTrue(TDB.symUnionDefaultGraph) ;
        testCount("SELECT * "+
                  "FROM NAMED <graph:3> FROM NAMED <graph:4> "+
                  "{ ?s <uri:q> ?o }",    // Same in each graph
                  1) ; 
        } finally { TDB.getContext().unset(TDB.symUnionDefaultGraph) ; } 
    }    

    @Test public void dynamicAndUnion3() {
        try { TDB.getContext().setTrue(TDB.symUnionDefaultGraph) ;
        testCount("SELECT * "+
                  "FROM NAMED <graph:3> FROM NAMED <graph:4> "+
                  "{ ?s <uri:p> ?o }",    // Different in each graph
                  2) ; 
        } finally { TDB.getContext().unset(TDB.symUnionDefaultGraph) ; } 
    }    

    @Test public void dynamicAndUnion4() {
        try { TDB.getContext().setTrue(TDB.symUnionDefaultGraph) ;
        testCount("SELECT * "+
                  "FROM <graph:1> FROM <graph:2>"+
                  "FROM NAMED <graph:3> FROM NAMED <graph:4> "+
                  "{ ?s <uri:p> ?o }",    // Different in each graph
                  4) ; 
        } finally { TDB.getContext().unset(TDB.symUnionDefaultGraph) ; } 
    }  

    @Test public void dynamicAndUnion5() {
        testCount("SELECT * "+
                  "FROM <graph:1>"+
                  "FROM NAMED <graph:3> FROM NAMED <graph:4> "+
                  "{ GRAPH <urn:x-arq:DefaultGraph> { ?s <uri:p> ?o } }",    // Different in each graph
                  1) ;
    }  
    
    @Test public void dynamicAndUnion6() {
        try {
            TDB.getContext().setTrue(TDB.symUnionDefaultGraph) ;
            testCount("SELECT * "+
                      "FROM <graph:1>"+
                      "FROM NAMED <graph:3> FROM NAMED <graph:4> "+
                      "{ GRAPH <urn:x-arq:DefaultGraph> { ?s <uri:p> ?o } }",
                      1) ;
        } finally { TDB.getContext().unset(TDB.symUnionDefaultGraph) ; } 
    }  
    
    @Test public void dynamicAndUnion7() {
        testCount("SELECT * "+
                  "FROM <graph:1>"+
                  "FROM NAMED <graph:3> FROM NAMED <graph:4> "+
                  "{ GRAPH <urn:x-arq:UnionGraph> { ?s <uri:p> ?o } }",
                  2) ;
    }  
    
    @Test public void dynamicAndUnion8() {
        try {
            TDB.getContext().setTrue(TDB.symUnionDefaultGraph) ;
            testCount("SELECT * "+
                      "FROM <graph:1>"+
                      "FROM NAMED <graph:3> FROM NAMED <graph:4> "+
                      "{ GRAPH <urn:x-arq:UnionGraph> { ?s <uri:p> ?o } }",
                      2) ;
        } finally { TDB.getContext().unset(TDB.symUnionDefaultGraph) ; } 
    }  

    @Test public void dynamic99() {
        // Check we did not mess with the global context in getting previous tests to pass.
        testCount("SELECT * FROM NAMED <graph:3> { ?s ?p ?o }", 0) ;
    }  

    private static void testCount(String queryString, int expected)
    {
        Query query = QueryFactory.create(queryString) ;
        
        if ( false ) trace(query) ;
        
        QueryExecution qExec = QueryExecutionFactory.create(query, dataset) ;
        ResultSet rs = qExec.execSelect() ;
        int n = ResultSetFormatter.consume(rs) ;
        assertEquals(expected, n) ;
        //rs.
    }
    
    private static void trace(Query query)
    {
        Op op = Algebra.compile(query) ;
        op = Algebra.toQuadForm(op) ;
        Set<Node> defaultGraphs = NodeUtils2.convertToNodes(query.getGraphURIs()) ; 
        Set<Node> namedGraphs = NodeUtils2.convertToNodes(query.getNamedGraphURIs()) ;
        Op op2 = Transformer.transform(new TransformDynamicDataset(defaultGraphs, namedGraphs, false), op) ;
        System.out.println(op2) ;
    }
}

/*
 * (c) Copyright 2010 Talis Systems Ltd.
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