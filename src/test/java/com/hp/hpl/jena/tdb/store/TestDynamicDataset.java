/*
 * (c) Copyright 2010 Talis Systems Ltd.
 * (c) Copyright 2011 Epimorphics Ltd.
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.tdb.store;

import java.util.Iterator ;
import java.util.Set ;

import org.apache.log4j.Level ;
import org.apache.log4j.Logger ;
import org.junit.Test ;
import org.openjena.atlas.junit.BaseTest ;
import org.openjena.atlas.lib.StrUtils ;

import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.graph.Triple ;
import com.hp.hpl.jena.query.Dataset ;
import com.hp.hpl.jena.query.DatasetFactory ;
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
import com.hp.hpl.jena.sparql.core.DatasetGraph ;
import com.hp.hpl.jena.sparql.core.Quad ;
import com.hp.hpl.jena.sparql.engine.optimizer.reorder.ReorderLib ;
import com.hp.hpl.jena.sparql.sse.Item ;
import com.hp.hpl.jena.sparql.sse.SSE ;
import com.hp.hpl.jena.sparql.sse.builders.BuilderGraph ;
import com.hp.hpl.jena.tdb.TDB ;
import com.hp.hpl.jena.tdb.TDBFactory ;
import com.hp.hpl.jena.tdb.migrate.DynamicDatasets ;
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
    
    @Test public void dynamic01()    { testCount("SELECT * {?s ?p ?o}", 3, dataset) ; }
    
    @Test public void dynamic02()    { testCount("SELECT ?g { GRAPH ?g {} }", 5, dataset) ; }
    
    @Test public void dynamic03()    { testCount("SELECT * FROM <graph:1> {?s <uri:p> ?o}", 1, dataset) ; }

    @Test public void dynamic04()    { testCount("SELECT * FROM <graph:1> { GRAPH ?g { ?s ?p ?o} }", 0, dataset) ; }
    
    @Test public void dynamic05()    { testCount("SELECT * FROM <graph:1> FROM <graph:2> {?s <uri:p> ?o}", 2, dataset) ; }

    // Duplicate surpression
    @Test public void dynamic06()    { testCount("SELECT ?s FROM <graph:1> FROM <graph:2> {?s <uri:q> ?o}", 1, dataset) ; }
    
    @Test public void dynamic07()    { testCount("SELECT ?s FROM NAMED <graph:1> {?s <uri:q> ?o}", 0, dataset) ; }
    
    @Test public void dynamic08()    { testCount("SELECT ?s FROM <graph:2> FROM NAMED <graph:1> {?s <uri:q> ?o}", 1, dataset) ; }

    @Test public void dynamic09()    { testCount("SELECT * "+
                                                "FROM <graph:1> FROM <graph:2> "+
                                                "FROM NAMED <graph:3> FROM NAMED <graph:4> "+
                                                "{ GRAPH ?g { ?s <uri:q> ?o }}",
                                                2, dataset) ; 
                                    }
    
    @Test public void dynamic10()    { testCount("SELECT * "+
                                                "FROM <graph:1> FROM <graph:2>"+
                                                "FROM NAMED <graph:3> FROM NAMED <graph:4> "+
                                                "{ GRAPH ?g { ?s <uri:q> ?o }}",
                                                2, dataset) ; 
                                    }

    @Test public void dynamic11()    { testCount("SELECT * "+
                                                "FROM <x:unknown>"+
                                                "{ GRAPH ?g { ?s <uri:q> ?o }}",
                                                0, dataset) ; 
                                    }

    @Test public void dynamic12()    { testCount("SELECT * "+
                                                 "FROM  <graph:1>"+
                                                 "{ GRAPH ?g { }}",
                                                 0, dataset) ; 
                                     }

    @Test public void dynamic13()    { testCount("SELECT * "+
                                                 "FROM NAMED <graph:1>"+
                                                 "{ GRAPH ?g { }}",
                                                 1, dataset) ; 
                                     }

    @Test public void dynamic14()    { testCount("SELECT * "+
                                                 "FROM NAMED <graph:1> FROM NAMED <graph:2>"+
                                                 "FROM <graph:3> "+
                                                 "{ GRAPH ?g { }}",
                                                 2, dataset) ; 
                                     }
    

    // If  context.isTrue(TDB.symUnionDefaultGraph)
    
    @Test public void dynamicAndUnion1() {
        try { TDB.getContext().setTrue(TDB.symUnionDefaultGraph) ;
        testCount("SELECT * "+
                  "FROM NAMED <graph:3> FROM NAMED <graph:4> "+
                  "{ GRAPH ?g { ?s <uri:q> ?o }}",
                  2, dataset) ; 
        TDB.getContext().unset(TDB.symUnionDefaultGraph) ;
        } finally { TDB.getContext().unset(TDB.symUnionDefaultGraph) ; }
    }    

    @Test public void dynamicAndUnion2() {
        try { TDB.getContext().setTrue(TDB.symUnionDefaultGraph) ;
        testCount("SELECT * "+
                  "FROM NAMED <graph:3> FROM NAMED <graph:4> "+
                  "{ ?s <uri:q> ?o }",    // Same in each graph
                  1, dataset) ; 
        } finally { TDB.getContext().unset(TDB.symUnionDefaultGraph) ; } 
    }    

    @Test public void dynamicAndUnion3() {
        try { TDB.getContext().setTrue(TDB.symUnionDefaultGraph) ;
        testCount("SELECT * "+
                  "FROM NAMED <graph:3> FROM NAMED <graph:4> "+
                  "{ ?s <uri:p> ?o }",    // Different in each graph
                  2, dataset) ; 
        } finally { TDB.getContext().unset(TDB.symUnionDefaultGraph) ; } 
    }    

    @Test public void dynamicAndUnion4() {
        try { TDB.getContext().setTrue(TDB.symUnionDefaultGraph) ;
        testCount("SELECT * "+
                  "FROM <graph:1> FROM <graph:2>"+
                  "FROM NAMED <graph:3> FROM NAMED <graph:4> "+
                  "{ ?s <uri:p> ?o }",    // Different in each graph
                  4, dataset) ; 
        } finally { TDB.getContext().unset(TDB.symUnionDefaultGraph) ; } 
    }  

    @Test public void dynamicAndUnion5() {
        testCount("SELECT * "+
                  "FROM <graph:1>"+
                  "FROM NAMED <graph:3> FROM NAMED <graph:4> "+
                  "{ GRAPH <urn:x-arq:DefaultGraph> { ?s <uri:p> ?o } }",    // Different in each graph
                  1, dataset) ;
    }  
    
    @Test public void dynamicAndUnion6() {
        try {
            TDB.getContext().setTrue(TDB.symUnionDefaultGraph) ;
            testCount("SELECT * "+
                      "FROM <graph:1>"+
                      "FROM NAMED <graph:3> FROM NAMED <graph:4> "+
                      "{ GRAPH <urn:x-arq:DefaultGraph> { ?s <uri:p> ?o } }",
                      1, dataset) ;
        } finally { TDB.getContext().unset(TDB.symUnionDefaultGraph) ; } 
    }  
    
    @Test public void dynamicAndUnion7() {
        testCount("SELECT * "+
                  "FROM <graph:1>"+
                  "FROM NAMED <graph:3> FROM NAMED <graph:4> "+
                  "{ GRAPH <urn:x-arq:UnionGraph> { ?s <uri:p> ?o } }",
                  2, dataset) ;
    }  
    
    @Test public void dynamicAndUnion8() {
        try {
            TDB.getContext().setTrue(TDB.symUnionDefaultGraph) ;
            testCount("SELECT * "+
                      "FROM <graph:1>"+
                      "FROM NAMED <graph:3> FROM NAMED <graph:4> "+
                      "{ GRAPH <urn:x-arq:UnionGraph> { ?s <uri:p> ?o } }",
                      2, dataset) ;
        } finally { TDB.getContext().unset(TDB.symUnionDefaultGraph) ; } 
    }  

    @Test public void dynamic99() {
        // Check we did not mess with the global context in getting previous tests to pass.
        testCount("SELECT * FROM NAMED <graph:3> { ?s ?p ?o }", 0, dataset) ;
    }
    
    // Tests of patterns and paths across graphs.
    
    private static String dataStr = StrUtils.strjoinNL(
       "(dataset" ,
       "  (graph" ,
       "   (triple <http://example/s> <http://example/p> 'dft')" ,
       "   (triple <http://example/s> <http://example/p> <http://example/x>)" ,
       "   (triple <http://example/x> <http://example/p> <http://example/o>)" ,
       " )" ,
       " (graph <http://example/g1>",
       "   (triple <http://example/s> <http://example/p> 'g1')",
       "   (triple <http://example/s> <http://example/p1> <http://example/x>)",
       "   (triple <http://example/x> <http://example/p2> <http://example/o>)",
       " )",
       " (graph <http://example/g2>", 
       "   (triple <http://example/s> <http://example/p> 'g2')",
       "   (triple <http://example/x> <http://example/p1> <http://example/z>)",
       "   (triple <http://example/x> <http://example/p2> <http://example/o>)",
       "   (triple <http://example/x> <http://example/p2> <http://example/o2>)",
       " )",
       " (graph <http://example/g3>",
       "   (triple <http://example/s> <http://example/p> 'g3')",
       "   (triple <http://example/s> <http://example/p1> <http://example/y>)",
       " ))") ;
    private static Dataset dataset2 = TDBFactory.createDataset() ; 
    static {
        Item item = SSE.parse(dataStr) ;
        DatasetGraph dsg = BuilderGraph.buildDataset(item) ;
        
        Iterator<Quad> iter = dsg.find() ;
        for ( ; iter.hasNext(); )
            dataset2.asDatasetGraph().add(iter.next()) ;    
    }
    private static Node gn1 = SSE.parseNode("<http://example/g1>") ;
    private static Node gn2 = SSE.parseNode("<http://example/g2>") ;
    private static Node gn3 = SSE.parseNode("<http://example/g3>") ;
    private static Node gn9 = SSE.parseNode("<http://example/g9>") ;

    private static final String prefix = "PREFIX : <http://example/> " ; 
    
    // g1+g2 { ?s :p1 ?x . ?x :p2 ?o } ==> 1
    // g1+g2 { ?s :p1* ?o } ==> 1
    
    @Test public void pattern_01()
    {
        testCount(prefix + "SELECT * FROM :g1 FROM :g2 { ?s :p1 ?x . ?x :p2 ?o }", 2, dataset2) ; 
    }
    
    @Test public void pattern_02()
    {
        String qs = prefix + "SELECT * FROM :g1 FROM :g2 { ?s :p1+ ?x }" ;
//        Query query = QueryFactory.create(qs) ;
//        Dataset ds = DatasetFactory.create(DynamicDatasets.dynamicDataset(query, dataset2.asDatasetGraph())) ;
//        QueryExecution qExec = QueryExecutionFactory.create(query, ds) ;
//        ResultSetFormatter.out(qExec.execSelect()) ;
        testCount(qs, 3, dataset2) ; 
    }
    
    @Test public void pattern_03()
    {
        // Do it externally to the TDB query engine.
        String qs = prefix + "SELECT * FROM :g1 FROM :g2 { ?s :p1+ ?x }" ;
        Query query = QueryFactory.create(qs) ;
        Dataset ds = DatasetFactory.create(DynamicDatasets.dynamicDataset(query, dataset2.asDatasetGraph())) ;
        testCount(qs, 3, ds) ; 
    }

    
    private static void testCount(String queryString, int expected, Dataset ds)
    {
        Query query = QueryFactory.create(queryString) ;
        if ( false ) trace(query) ;
        QueryExecution qExec = QueryExecutionFactory.create(query, ds) ;
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
 * (c) Copyright 2011 Epimorphics Ltd.
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