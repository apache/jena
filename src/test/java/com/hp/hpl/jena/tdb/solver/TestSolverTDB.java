/*
 * (c) Copyright 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.tdb.solver;


import java.util.ArrayList ;
import java.util.Iterator ;
import java.util.List ;

import org.junit.BeforeClass ;
import org.junit.Test ;
import org.openjena.atlas.iterator.Iter ;
import org.openjena.atlas.junit.BaseTest ;
import org.openjena.atlas.lib.StrUtils ;

import com.hp.hpl.jena.graph.Graph ;
import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.graph.Triple ;
import com.hp.hpl.jena.query.ResultSet ;
import com.hp.hpl.jena.query.ResultSetFactory ;
import com.hp.hpl.jena.query.ResultSetFormatter ;
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
import com.hp.hpl.jena.sparql.resultset.ResultSetRewindable ;
import com.hp.hpl.jena.sparql.sse.SSE ;
import com.hp.hpl.jena.tdb.TDBFactory ;
import com.hp.hpl.jena.util.FileManager ;

public class TestSolverTDB extends BaseTest
{
    static final String graphData = "testing/data.ttl" ;
    static Graph graph = null ;
    static PrefixMapping pmap = null ;

    @BeforeClass static public void beforeClass()
    { 
        graph = TDBFactory.createGraph() ;
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
        List<Var> vars =  new ArrayList<Var>() ;
        vars.addAll(OpVars.allVars(op)) ;
        QueryIterator qIter = Algebra.exec(op, graph) ;
        return ResultSetFactory.create(qIter, Var.varNames(vars)) ;
    }
    
    private static List<Binding> toList(QueryIterator qIter)
    {
        List<Binding> x = new ArrayList<Binding>() ;
        for ( ; qIter.hasNext() ; )
            x.add(qIter.nextBinding()) ;
        return x ;
    }
}

/*
 * (c) Copyright 2008, 2009 Hewlett-Packard Development Company, LP
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