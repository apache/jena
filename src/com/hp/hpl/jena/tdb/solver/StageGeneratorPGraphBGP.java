/*
 * (c) C;opyright 2008 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.tdb.solver;

import static com.hp.hpl.jena.tdb.lib.Lib.printAbbrev;
import iterator.Iter;

import java.util.Iterator;
import java.util.List;

import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.sparql.core.BasicPattern;
import com.hp.hpl.jena.sparql.engine.ExecutionContext;
import com.hp.hpl.jena.sparql.engine.QueryIterator;
import com.hp.hpl.jena.sparql.engine.binding.Binding;
import com.hp.hpl.jena.sparql.engine.main.StageGenerator;
import com.hp.hpl.jena.sparql.util.ALog;
import com.hp.hpl.jena.tdb.TDB;
import com.hp.hpl.jena.tdb.pgraph.GraphTDB;

// OLD - does not reorder BGP - no optimizer.
public class StageGeneratorPGraphBGP implements StageGenerator
{
    StageGenerator above = null ;
    
    public StageGeneratorPGraphBGP(StageGenerator original)
    {
        above = original ;
    }
    
    @Override
    public QueryIterator execute(BasicPattern pattern, QueryIterator input, ExecutionContext execCxt)
    {
        // --- In case this isn't for TDB
        Graph g = execCxt.getActiveGraph() ;
        if ( ! ( g instanceof GraphTDB ) )
            return above.execute(pattern, input, execCxt) ;
        GraphTDB graph = (GraphTDB)g ;
        
        if ( execCxt.getContext().isTrue(TDB.logBGP) )
            ALog.info(this, ">> BGP: \n  "+printAbbrev(pattern));
        
        @SuppressWarnings("unchecked")
        List<Triple> triples = (List<Triple>)pattern.getList() ;

        if ( execCxt.getContext().isTrue(TDB.logBGP) )
            ALog.info(this, "<< BGP: \n  "+SolverLib.strPattern(pattern));
        
        @SuppressWarnings("unchecked")
        Iterator<Binding> iter = (Iterator<Binding>)input ;
        // Binding ==> BindingNodeId.
        Iterator<BindingNodeId> chain = Iter.map(iter, SolverLib.convFromBinding(graph)) ;
        
        for ( Triple triple : triples )
        {
            chain = solve(graph, chain, triple, execCxt) ;
            //chain = Iter.debug(chain) ;
        }
        
        Iterator<Binding> iterBinding = Iter.map(chain, SolverLib.convToBinding(graph)) ;
        // Input passed in to ensure it gets closed when QueryIterTDB gets closed
        return new QueryIterTDB(iterBinding, input) ;
    }
    
    static Iterator<BindingNodeId> solve(GraphTDB graph,
                                         Iterator<BindingNodeId> chain,
                                         Triple triple, 
                                         ExecutionContext execCxt)
    {
        return new StageMatchTriple(graph, chain, triple, execCxt) ;
    }
}

/*
 * (c) Copyright 2008 Hewlett-Packard Development Company, LP
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