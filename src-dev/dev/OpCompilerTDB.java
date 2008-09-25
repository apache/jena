/*
 * (c) Copyright 2008 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package dev;

import com.hp.hpl.jena.sparql.algebra.Op;
import com.hp.hpl.jena.sparql.algebra.Transform;
import com.hp.hpl.jena.sparql.algebra.op.OpBGP;
import com.hp.hpl.jena.sparql.algebra.op.OpFilter;
import com.hp.hpl.jena.sparql.algebra.opt.TransformFilterPlacement;
import com.hp.hpl.jena.sparql.core.BasicPattern;
import com.hp.hpl.jena.sparql.engine.ExecutionContext;
import com.hp.hpl.jena.sparql.engine.QueryIterator;
import com.hp.hpl.jena.sparql.engine.main.OpCompiler;
import com.hp.hpl.jena.sparql.engine.main.QC;
import com.hp.hpl.jena.sparql.expr.ExprList;
import com.hp.hpl.jena.tdb.pgraph.GraphTDB;
import com.hp.hpl.jena.tdb.solver.reorder.ReorderTransformation;

public class OpCompilerTDB extends OpCompiler
{
    public static Factory altFactory = new Factory() {

        @Override
        public OpCompiler create(ExecutionContext execCxt)
        {
            return new OpCompilerTDB(execCxt) ;
        }} ;
    
    private boolean isForTDB ;
    private Transform opTransform = new TransformFilterPlacement() ;
    
    // A new compile object is created for each op compilation.
    // So the execCxt is changing as we go through the query-compile-excute process  
    public OpCompilerTDB(ExecutionContext execCxt)
    {
        super(execCxt) ;
        isForTDB = (execCxt.getActiveGraph() instanceof GraphTDB) ;
    }

//    // For reference, this is the standard BGP step. 
//    @Override
//    public QueryIterator compile(OpBGP opBGP, QueryIterator input)
//    {
//        BasicPattern pattern = opBGP.getPattern() ;
//        return StageBuilder.compile(pattern, input, execCxt) ;
//    }
    
    @Override
    public QueryIterator compile(OpBGP opBGP, QueryIterator input)
    {
        if ( ! isForTDB )
            return super.compile(opBGP, input) ;
        GraphTDB graph = (GraphTDB)execCxt.getActiveGraph() ;
        return execute(opBGP.getPattern(), null, input, graph) ;
        
        //return SolverLib.execute(graph, opBGP.getPattern(), input, execCxt) ;
    }
    
//    @Override
//    public QueryIterator compile(OpLabel opLabel, QueryIterator input)
//    {
//        return super.compile(opLabel, input) ;
//    }
    
    @Override
    public QueryIterator compile(OpFilter opFilter, QueryIterator input)
    {
        if ( ! isForTDB )
            return super.compile(opFilter, input) ;
        
        if ( ! OpBGP.isBGP(opFilter.getSubOp()) )
            return super.compile(opFilter, input) ;

        // Experimental.
        // Currently, optimize without considering the input stream.
        // Correct for top level patterns. 
        
        OpBGP opBGP = (OpBGP)opFilter.getSubOp() ;
        GraphTDB graph = (GraphTDB)execCxt.getActiveGraph() ;
        
        return execute(opBGP.getPattern(), opFilter.getExprs(), input, graph) ;
        //return super.compile(opFilter, input) ;
    }

    // Will migrate to SolverLib.
    static Transform placement = new TransformFilterPlacement() ;
    public QueryIterator execute(BasicPattern pattern, ExprList exprs, QueryIterator input, GraphTDB graph)
    {
        ReorderTransformation transform = graph.getReorderTransform() ;
        pattern = transform.reorder(pattern) ;
        Op op = new OpBGP(pattern) ;
        
        if ( exprs != null )
            op = TransformFilterPlacement.transform(exprs, pattern) ;
        
        System.out.println("Execute::") ;
        System.out.println(op) ;
        
        // HACK reset to avoid infinite loop.
        OpCompiler.factory = OpCompiler.stdFactory ;
        
        // Solve without messing around - because we changed the OpCompielr factory!  
        return QC.compile(op, input, execCxt) ;
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