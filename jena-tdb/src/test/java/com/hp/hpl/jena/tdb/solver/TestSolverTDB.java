/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.hp.hpl.jena.tdb.solver;


import java.util.ArrayList ;
import java.util.Iterator ;
import java.util.List ;

import org.apache.jena.atlas.iterator.Iter ;
import org.apache.jena.atlas.junit.BaseTest ;
import org.apache.jena.atlas.lib.StrUtils ;
import org.junit.BeforeClass ;
import org.junit.Test ;

import com.hp.hpl.jena.graph.Graph ;
import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.graph.Triple ;
import com.hp.hpl.jena.query.ResultSet ;
import com.hp.hpl.jena.query.ResultSetFactory ;
import com.hp.hpl.jena.query.ResultSetFormatter ;
import com.hp.hpl.jena.query.ResultSetRewindable ;
import com.hp.hpl.jena.rdf.model.Model ;
import com.hp.hpl.jena.rdf.model.ModelFactory ;
import com.hp.hpl.jena.shared.PrefixMapping ;
import com.hp.hpl.jena.shared.impl.PrefixMappingImpl ;
import com.hp.hpl.jena.sparql.algebra.Algebra ;
import com.hp.hpl.jena.sparql.algebra.Op ;
import com.hp.hpl.jena.sparql.algebra.OpVars ;
import com.hp.hpl.jena.sparql.core.Var ;
import com.hp.hpl.jena.sparql.engine.QueryIterator ;
import com.hp.hpl.jena.sparql.engine.binding.Binding ;
import com.hp.hpl.jena.sparql.resultset.ResultSetCompare ;
import com.hp.hpl.jena.sparql.sse.SSE ;
import com.hp.hpl.jena.tdb.ConfigTest ;
import com.hp.hpl.jena.tdb.TDBFactory ;
import com.hp.hpl.jena.util.FileManager ;

public class TestSolverTDB extends BaseTest
{
    static String graphData = null ;
    static Graph graph = null ;
    static PrefixMapping pmap = null ;

    @BeforeClass static public void beforeClass()
    { 
        graphData = ConfigTest.getTestingDataRoot()+"/Data/solver-data.ttl" ;
        graph = TDBFactory.createDatasetGraph().getDefaultGraph() ;
        Model m = ModelFactory.createModelForGraph(graph) ;
        FileManager.get().readModel(m, graphData) ;

        pmap = new PrefixMappingImpl() ;
        pmap.setNsPrefix("", "http://example/") ;
        
    }
            
    static private void addAll(Graph srcGraph, Graph dstGraph)
    {
        Iterator<Triple> triples = srcGraph.find(Node.ANY, Node.ANY, Node.ANY) ;
        for ( Triple t : Iter.iter(triples) )
            dstGraph.add(t) ;
    }


    @Test public void solve_01()
    {
        ResultSet rs1 = exec("(bgp (:s :p :o))", graph) ;
        ResultSet rs2 = results("unit") ;
        equals(rs1, rs2) ;
    }
    
    @Test public void solve_02()
    {
        ResultSet rs1 = exec("(bgp (:s :p :o2))", graph) ;
        ResultSet rs2 = results("empty") ;
        equals(rs1, rs2) ;
    }
    
    @Test public void solve_03()
    {
        // Above everything.
        ResultSet rs1 = exec("(bgp (:zzzz :p 999999))", graph) ;
        ResultSet rs2 = results("empty") ;
        equals(rs1, rs2) ;
    }
    
    @Test public void solve_04()
    {
        // Below everything.
        ResultSet rs1 = exec("(bgp (:a :p :a))", graph) ;
        ResultSet rs2 = results("empty") ;
        equals(rs1, rs2) ;
    }

    @Test public void solve_05()
    {
        ResultSet rs1 = exec("(project (?s ?y) (bgp (?s :p ?z) (?z :q ?y)))", graph) ;
        ResultSet rs2 = results("(row (?s :s) (?y :y))") ;
        equals(rs1, rs2) ;
    }
    
    @Test public void solve_06()
    {
        ResultSet rs1 = exec("(bgp (:s ?p ?o))", graph) ;
        ResultSet rs2 = results("(row (?p :p) (?o :o))",
                                "(row (?p :p) (?o 10))",
                                "(row (?p :p) (?o :x))"
                                ) ;
        equals(rs1, rs2) ;
    }

    // ------
    
    private static void equals(ResultSet rs1, ResultSet rs2)
    { same(rs1, rs2, true) ; }
    
    private static void same(ResultSet rs1, ResultSet rs2, boolean result)
    {
        ResultSetRewindable rsw1 = ResultSetFactory.makeRewindable(rs1) ;
        ResultSetRewindable rsw2 = ResultSetFactory.makeRewindable(rs2) ;
        boolean b = ResultSetCompare.equalsByValue(rsw1, rsw2) ;
        if ( b != result)
        {
            System.out.println("Different: ") ;
            rsw1.reset() ;
            rsw2.reset() ;
            ResultSetFormatter.out(rsw1) ;
            ResultSetFormatter.out(rsw2) ;
            System.out.println() ;
        }
        
        assertTrue(b == result) ;
    }
    
    private static ResultSet results(String... rows)
    {
        String str = "(table "+StrUtils.strjoin("", rows)+")" ;
        return SSE.parseTable(str).toResultSet() ; 
    }
    
    
    private static ResultSet exec(String pattern, Graph graph)
    {
        Op op = SSE.parseOp(pattern, pmap) ;
        List<Var> vars =  new ArrayList<>() ;
        vars.addAll(OpVars.visibleVars(op)) ;
        QueryIterator qIter = Algebra.exec(op, graph) ;
        return ResultSetFactory.create(qIter, Var.varNames(vars)) ;
    }
    
    private static List<Binding> toList(QueryIterator qIter)
    {
        List<Binding> x = new ArrayList<>() ;
        for ( ; qIter.hasNext() ; )
            x.add(qIter.nextBinding()) ;
        return x ;
    }
}
