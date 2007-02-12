/*
 * (c) Copyright 2006, 2007 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.query.algebra.op;

import java.util.Iterator;

import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.query.algebra.Evaluator;
import com.hp.hpl.jena.query.algebra.Table;
import com.hp.hpl.jena.query.core.ARQInternalErrorException;
import com.hp.hpl.jena.query.core.Var;
import com.hp.hpl.jena.query.engine.ExecutionContext;
import com.hp.hpl.jena.query.engine.ref.EvaluatorFactory;
import com.hp.hpl.jena.query.engine.ref.TableFactory;
import com.hp.hpl.jena.query.engine.ref.table.TableEmpty;

public class OpGraph extends Op1
    // Must override evaluation - need to flip the execution context on the way down
{
    Node node ;

    public OpGraph(Node node, Op pattern)
    { 
        super(pattern) ; 
        this.node = node ;
    }
    
    public Node getNode() { return node ; }
    
//    public Table eval_1(Table table, Evaluator evaluator)
//    {
//        return evaluator.graph(getNode(), table) ;
//    }

    public Table eval(Evaluator evaluator)
    {
        // TODO Move into evaluator
        // Complicated by the fact we can't eval the subnode then eval the op.
        // This would be true if we had a more quad-like view of execution.
        ExecutionContext execCxt = evaluator.getExecContext() ;
        
        if ( ! Var.isVar(node) )
        {
            Graph graph = execCxt.getDataset().getNamedGraph(node.getURI()) ;
            if ( graph == null )
                // No such name in the dataset
                return new TableEmpty() ;
            ExecutionContext execCxt2 = new ExecutionContext(execCxt, graph) ;
            Evaluator e2 = EvaluatorFactory.create(execCxt2) ;
            return getSubOp().eval(e2) ;
        }
        
        // Graph node is a variable.
        Var gVar = Var.alloc(node) ;
        Table current = null ;
        for ( Iterator iter = execCxt.getDataset().listNames() ; iter.hasNext() ; )
        {
            String uri = (String)iter.next();
            Graph graph = execCxt.getDataset().getNamedGraph(uri) ;
            ExecutionContext execCxt2 = new ExecutionContext(execCxt, graph) ;
            Evaluator e2 = EvaluatorFactory.create(execCxt2) ;
            
            Table tableVarURI = TableFactory.create(gVar, Node.createURI(uri)) ;
            // Evaluate the pattern, join with this graph node possibility.
            
            Table patternTable = getSubOp().eval(e2) ;
            Table stepResult = evaluator.join(patternTable, tableVarURI) ;
            
            if ( current == null )
                current = stepResult ;
            else
                current = evaluator.union(current, stepResult) ;
        }
        
        if ( current == null )
            // Nothing to loop over
            return new TableEmpty() ;
        return current ;
    }

    public String getName()                         { return "Graph" ; }

    public Op apply(Transform transform, Op op)     { return transform.transform(this, op) ; } 
    public void visit(OpVisitor opVisitor)          { opVisitor.visit(this) ; }
    public Op copy(Op newOp)                        { return new OpGraph(node, newOp) ; }

    public Table eval_1(Table table, Evaluator evaluator)
    {
        throw new ARQInternalErrorException("OpGraph.eval_1 called") ;
    }
}

/*
 * (c) Copyright 2006, 2007 Hewlett-Packard Development Company, LP
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