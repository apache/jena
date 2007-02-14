/*
 * (c) Copyright 2007 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.query.engine.ref;

import java.util.Stack;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hp.hpl.jena.query.algebra.Evaluator;
import com.hp.hpl.jena.query.algebra.Table;
import com.hp.hpl.jena.query.algebra.op.*;

public class Eval
{
    private static Log log = LogFactory.getLog(Eval.class) ;
    
    public static Table eval(Evaluator evaluator, Op op)
    {
        EvaluatorDispatch ev = new EvaluatorDispatch(evaluator) ;
        op.visit(ev) ;
        Table table = ev.getResult() ;
        return table ;
    }
    
    /**  Class to provide type-safe eval() dispatch using the visitor support of Op */

    static class EvaluatorDispatch implements OpVisitor
    {

        private Stack stack = new Stack() ;
        private Evaluator evaluator ;
        
        EvaluatorDispatch(Evaluator evaluator)
        {
            this.evaluator = evaluator ;
        }

        private Table eval(Op op)
        {
            op.visit(this) ;
            return pop() ;
        }
        
        Table getResult()
        {
            if ( stack.size() != 1 )
                log.warn("Warning: getResult: stack size = "+stack.size()) ;
            
            Table table = pop() ;
            return table ;
        }
        
        public void visit(OpBGP opBGP)
        {
            Table table = evaluator.basicPattern(opBGP.getPattern()) ;
            push(table) ;
        }

        public void visit(OpQuadPattern quadPattern)
        {
            // TODO OpGraph
            push(quadPattern.eval(evaluator)) ;
            //throw new ARQNotImplemented("EvaDispatch/quadPattern") ;
        }

        public void visit(OpJoin opJoin)
        {
            Table left = eval(opJoin.getLeft()) ;
            Table right = eval(opJoin.getRight()) ;
            Table table = evaluator.join(left, right) ;
            push(table) ;
        }

        public void visit(OpLeftJoin opLeftJoin)
        {
            Table left = eval(opLeftJoin.getLeft()) ;
            Table right = eval(opLeftJoin.getRight()) ;
            Table table = evaluator.leftJoin(left, right, opLeftJoin.getExprs()) ;
            push(table) ;
        }

        public void visit(OpUnion opUnion)
        {
            Table left = eval(opUnion.getLeft()) ;
            Table right = eval(opUnion.getRight()) ;
            Table table = evaluator.union(left, right) ;
            push(table) ;
        }

        public void visit(OpFilter opFilter)
        {
            Table table = eval(opFilter.getSubOp()) ;
            table = evaluator.filter(opFilter.getExprs(), table) ;
            push(table) ;
        }

        public void visit(OpGraph opGraph)
        {
            //eval(opGraph.getSubOp()) ;
            // TODO OpGraph
            push(opGraph.eval(evaluator)) ;
            //throw new ARQNotImplemented("EvaDispatch/opGraph") ;
        }

        public void visit(OpDatasetNames dsNames)
        {
            // TODO OpDatasetNames
            push(dsNames.eval(evaluator)) ;
            //throw new ARQNotImplemented("EvaDispatch/opGraph") ;
        }

        public void visit(OpUnit opUnit)
        {
            push(TableFactory.createUnit()) ;
        }

        public void visit(OpExt opExt)
        {
            // TODO OpExt
            push(opExt.eval(evaluator)) ;
            //throw new ARQNotImplemented("EvaDispatch/OpExt") ;
        }

        public void visit(OpOrder opOrder)
        {
            Table table = eval(opOrder.getSubOp()) ;
            table = evaluator.order(table, opOrder.getConditions()) ;
            push(table) ;
        }

        public void visit(OpProject opProject)
        {
            Table table = eval(opProject.getSubOp()) ;
            table = evaluator.project(table, opProject.getVars()) ;
            push(table) ;
        }

        public void visit(OpDistinct opDistinct)
        {
            Table table = eval(opDistinct.getSubOp()) ;
            table = evaluator.distinct(table, opDistinct.getVars()) ;
            push(table) ;
        }

        public void visit(OpSlice opSlice)
        {
            Table table = eval(opSlice.getSubOp()) ;
            table = evaluator.slice(table, opSlice.getStart(), opSlice.getLength()) ;
            push(table) ;
        }

        private void push(Table table)  { stack.push(table) ; }
        private Table pop()
        { 
            if ( stack.size() == 0 )
                log.warn("Warning: pop: empty stack") ;
            return (Table)stack.pop() ;
        }
    }
}

/*
 * (c) Copyright 2007 Hewlett-Packard Development Company, LP
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