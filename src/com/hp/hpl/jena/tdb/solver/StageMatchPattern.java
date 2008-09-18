/*
 * (c) Copyright 2008 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.tdb.solver;

import static com.hp.hpl.jena.tdb.solver.SolverLib.* ;
import java.util.Iterator;
import java.util.List;

import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.sparql.core.Substitute;
import com.hp.hpl.jena.sparql.core.BasicPattern;
import com.hp.hpl.jena.sparql.engine.ExecutionContext;
import com.hp.hpl.jena.sparql.engine.binding.Binding;
import com.hp.hpl.jena.tdb.pgraph.GraphTDB;
import com.hp.hpl.jena.tdb.solver.reorder.ReorderPattern;
import com.hp.hpl.jena.tdb.solver.reorder.ReorderProc;

import iterator.Iter;
import iterator.RepeatApplyIterator;
import iterator.SingletonIterator;

public class StageMatchPattern extends RepeatApplyIterator<Binding>
{
    private BasicPattern pattern ;
    private ReorderPattern reorderPattern ;
    private ReorderProc    reorderProc ;
    private GraphTDB graph ;
    private ExecutionContext execCxt ;
    
    protected StageMatchPattern(GraphTDB graph, Iterator<Binding> input, BasicPattern pattern, ReorderPattern reorder, ExecutionContext execCxt)
    {
        super(input) ;
        this.pattern = pattern ;
        this.reorderPattern = reorder ;
        this.reorderProc = null ;
        this.graph = graph ;
        this.execCxt = execCxt ;
    }

    @Override
    protected Iterator<Binding> makeNextStage(Binding b)
    {
        // ---- Reorder
        BasicPattern pattern2 = Substitute.substitute(pattern, b) ;

        if ( reorderPattern != null && pattern.size() > 1 )
        {
            if ( reorderProc == null )
                // Cache the reorder processor - ie. the first binding is used
                // as a template for later input bindings.   
                reorderProc = reorderPattern.reorderIndexes(pattern2) ;
            pattern2 = reorderProc.reorder(pattern2) ;
        }
        
        // ---- Execute
        @SuppressWarnings("unchecked")
        List<Triple> triples = (List<Triple>)pattern2.getList() ;
        
        Iterator<BindingNodeId> chain = 
            new SingletonIterator<BindingNodeId>(convFromBinding(graph).convert(b)) ;
        
        for ( Triple triple : triples )
            chain = solve(graph, chain, triple, execCxt) ;
        
        Iterator<Binding> iterBinding = Iter.map(chain, convToBinding(graph)) ;
        return iterBinding ;
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