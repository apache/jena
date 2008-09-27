/*
 * (c) C;opyright 2008 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.tdb.solver;

import com.hp.hpl.jena.graph.Graph;

import com.hp.hpl.jena.sparql.core.BasicPattern;
import com.hp.hpl.jena.sparql.engine.ExecutionContext;
import com.hp.hpl.jena.sparql.engine.QueryIterator;
import com.hp.hpl.jena.sparql.engine.binding.Binding;
import com.hp.hpl.jena.sparql.engine.main.StageGenerator;

import com.hp.hpl.jena.tdb.pgraph.GraphTDB;
import com.hp.hpl.jena.tdb.solver.reorder.StageReorder;
import com.hp.hpl.jena.tdb.solver.stage.Stage;
import com.hp.hpl.jena.tdb.solver.stage.StageGenerator1;

// TODO Break up into reorder and execute.
public class StageGeneratorTDB implements StageGenerator
{
    StageGenerator above = null ;
    
    public StageGeneratorTDB(StageGenerator original)
    {
        above = original ;
    }
    
    @Override
    public QueryIterator execute(final BasicPattern pattern, QueryIterator input, ExecutionContext execCxt)
    {
        // --- In case this isn't for TDB
        Graph g = execCxt.getActiveGraph() ;
        
        if ( ! ( g instanceof GraphTDB ) )
            // Not us - bounce up the StageGenerator chain
            return above.execute(pattern, input, execCxt) ;
        final GraphTDB graph = (GraphTDB)g ;
        
        // The TDB raw execution
        Stage stage1 = new Stage(){
            @Override
            public QueryIterator execute(BasicPattern pattern, Binding binding, ExecutionContext execCxt)
            { return SolverLib.execute(graph, pattern, binding, execCxt) ; }
            } ;
        
        // Wrap with the BGP optimizer.
        final Stage stage2 = new StageReorder(graph.getReorderTransform(), stage1) ;
        
        // And wrap in the unbundling of the input iterator 
        return new StageGenerator1(stage2).execute(pattern, input, execCxt)  ;
        
        // == StageGenerator1
//        return new QueryIterRepeatApply(input, execCxt) 
//        {
//            @Override
//            protected QueryIterator nextStage(Binding binding)
//            {
//                return stage2.execute(pattern, binding, getExecContext()) ;
//            }
//        } ;
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