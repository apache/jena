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

package com.hp.hpl.jena.sparql.algebra;

import java.util.ArrayList ;
import java.util.Iterator ;
import java.util.List ;
import java.util.Stack ;

import com.hp.hpl.jena.query.SortCondition ;
import com.hp.hpl.jena.sparql.algebra.OpWalker.WalkerVisitor ;
import com.hp.hpl.jena.sparql.algebra.op.* ;
import com.hp.hpl.jena.sparql.algebra.optimize.ExprTransformApplyTransform ;
import com.hp.hpl.jena.sparql.core.Var ;
import com.hp.hpl.jena.sparql.core.VarExprList ;
import com.hp.hpl.jena.sparql.expr.ExprAggregator ;
import com.hp.hpl.jena.sparql.expr.Expr ;
import com.hp.hpl.jena.sparql.expr.ExprList ;
import com.hp.hpl.jena.sparql.expr.ExprTransformer ;
import com.hp.hpl.jena.sparql.expr.aggregate.Aggregator ;
import org.openjena.atlas.logging.Log ;

/** A botton-top application of a transformation of SPARQl algebra */  
public class Transformer
{
    private static Transformer singleton = new Transformer();
    
    // TopQuadrant extend Transformer for use in their SPARQL debugger.
    /** Get the current transformer */
    public static Transformer get() { return singleton; }
    
    /** Set the current transformer - use with care */
    public static void set(Transformer value) { Transformer.singleton = value; }
    
    /** Transform an algebra expression */
    public static Op transform(Transform transform, Op op)
    { return get().transformation(transform, op, null, null) ; }
    
    public static Op transform(Transform transform, Op op, OpVisitor beforeVisitor, OpVisitor afterVisitor)
    {
        return get().transformation(transform, op, beforeVisitor, afterVisitor) ;
    }

    /** Transform an algebra expression except skip (leave alone) any OpService nodes */
    public static Op transformSkipService(Transform transform, Op op)
    {
        return transformSkipService(transform, op, null, null) ; 
    }

    
    /** Transform an algebra expression except skip (leave alone) any OpService nodes */
    public static Op transformSkipService(Transform transform, Op op, OpVisitor beforeVisitor, OpVisitor afterVisitor)
    {
        // Skip SERVICE
        if ( true )
        {
            // Simplest way but still walks the OpService subtree (and throws away the transformation).
            transform = new TransformSkipService(transform) ;
            return Transformer.transform(transform, op, beforeVisitor, afterVisitor) ;
        }
        else
        {
            // Don't transform OpService and don't walk the sub-op 
            ApplyTransformVisitorServiceAsLeaf v = new ApplyTransformVisitorServiceAsLeaf(transform) ;
            WalkerVisitorSkipService walker = new WalkerVisitorSkipService(v, beforeVisitor, afterVisitor) ;
            OpWalker.walk(walker, op, v) ;
            return v.result() ;
        }
    }
    
    // To allow subclassing this class, we use a singleton pattern 
    // and theses protected methods.
    protected Op transformation(Transform transform, Op op, OpVisitor beforeVisitor, OpVisitor afterVisitor)
    {
        ApplyTransformVisitor v = new ApplyTransformVisitor(transform) ;
        return transformation(v, op, beforeVisitor, afterVisitor) ;
    }
    
    protected Op transformation(ApplyTransformVisitor transformApply,
                                Op op, OpVisitor beforeVisitor, OpVisitor afterVisitor)
    {
        if ( op == null )
        {
            Log.warn(this, "Attempt to transform a null Op - ignored") ;
            return op ;
        }
        return applyTransformation(transformApply, op, beforeVisitor, afterVisitor) ;
    }

    /** The primitive operation to apply a transformation to an Op */
    protected Op applyTransformation(ApplyTransformVisitor transformApply,
                                     Op op, OpVisitor beforeVisitor, OpVisitor afterVisitor)
    {
        OpWalker.walk(op, transformApply, beforeVisitor, afterVisitor) ;
        Op r = transformApply.result() ;
        return r ;
    }

    
    protected Transformer() { }
    
    public static
    class ApplyTransformVisitor extends OpVisitorByType
    {
        protected final Transform transform ;
        private final ExprTransformApplyTransform exprTransform ;

        private final Stack<Op> stack = new Stack<Op>() ;
        protected final Op pop() { return stack.pop(); }
        
        protected final void push(Op op)
        { 
            // Including nulls
            stack.push(op) ;
        }
        
        public ApplyTransformVisitor(Transform transform)
        { 
            this.transform = transform ;
            this.exprTransform = new ExprTransformApplyTransform(transform) ;

        }
        
        final Op result()
        {
            if ( stack.size() != 1 )
                Log.warn(this, "Stack is not aligned") ;
            return pop() ; 
        }
    
        // ----
        // Algebra operations that involve an Expr, and so might include NOT EXISTS 
        
        @Override
        public void visit(OpFilter opFilter)
        {
            ExprList ex = new ExprList() ;
            boolean changed = false ;
            for ( Expr e : opFilter.getExprs() )
            {
                Expr e2 = ExprTransformer.transform(exprTransform, e) ;
                ex.add(e2) ;
                if ( e != e2 )
                    changed = true ;
            }
            OpFilter f = opFilter ;
            if ( changed )
                f = (OpFilter)OpFilter.filter(ex, opFilter.getSubOp()) ;
            visit1(f) ;
        }

        @Override
        public void visit(OpOrder opOrder)
        {
            List<SortCondition> conditions = opOrder.getConditions() ;
            List<SortCondition> conditions2 = new ArrayList<SortCondition>() ;
            boolean changed = false ;

            for ( SortCondition sc : conditions )
            {
                Expr e = sc.getExpression() ;
                Expr e2 = ExprTransformer.transform(exprTransform, e) ;
                conditions2.add(new SortCondition(e2, sc.getDirection())) ;
                if ( e != e2 )
                    changed = true ;
            }
            OpOrder x = opOrder ;
            if ( changed )
                x = new OpOrder(opOrder.getSubOp(), conditions2) ;
            visit1(x) ;
        }
        
        @Override
        public void visit(OpAssign opAssign)
        { 
            VarExprList varExpr = opAssign.getVarExprList() ;
            List<Var> vars = varExpr.getVars() ;
            VarExprList varExpr2 = process(varExpr) ;
            OpAssign opAssign2 = opAssign ;
            if ( varExpr != varExpr2 )
                opAssign2 = OpAssign.assignDirect(opAssign.getSubOp(), varExpr2) ;
            visit1(opAssign2) ;
        }
        
        private VarExprList process(VarExprList varExpr)
        {
            List<Var> vars = varExpr.getVars() ;
            VarExprList varExpr2 = new VarExprList() ;
            boolean changed = false ;
            for ( Var v : vars )
            {
                Expr e = varExpr.getExpr(v) ;
                Expr e2 =  e ;
                if ( e != null )
                    e2 = ExprTransformer.transform(exprTransform, e) ;
                if ( e2 == null )
                    varExpr2.add(v) ;
                else
                    varExpr2.add(v, e2) ; 
                if ( e != e2 )
                    changed = true ;
            }
            if ( ! changed ) return varExpr ;
            return varExpr2 ;
        }

        @Override
        public void visit(OpGroup opGroup)
        {
            boolean changed = false ;

            VarExprList varExpr = opGroup.getGroupVars() ;
            VarExprList varExpr2 = process(varExpr) ;
            if ( varExpr != varExpr2 )
                changed = true ;
            
            
            List<ExprAggregator> aggs = opGroup.getAggregators() ;
            List<ExprAggregator> aggs2 = aggs ;
            
            //And the aggregators...
            aggs2 = new ArrayList<ExprAggregator>() ;
            for ( ExprAggregator agg : aggs )
            {
                Aggregator aggregator = agg.getAggregator() ;
                Var v = agg.getVar() ;
                
                // Variable associated with the aggregate
                Expr eVar = agg.getAggVar() ;   // Not .getExprVar()
                Expr eVar2 = ExprTransformer.transform(exprTransform, eVar) ;
                if ( eVar != eVar2 )
                    changed = true ;

                // The Aggregator expression
                Expr e = aggregator.getExpr() ;
                Expr e2 = e ;
                if ( e != null )    // Null means "no relevant expression" e.g. COUNT(*)
                    ExprTransformer.transform(exprTransform, e) ;
                if ( e != e2 )
                    changed = true ;
                Aggregator a2 = aggregator.copy(e2) ;
                aggs2.add(new ExprAggregator(eVar2.asVar(), a2)) ;
            }

            OpGroup opGroup2 = opGroup ;
            if ( changed )
                opGroup2 = new OpGroup(opGroup.getSubOp(), varExpr2, aggs2) ;
            visit1(opGroup2) ;
        }
        
        // ----

        @Override
        protected void visit0(Op0 op)
        {
            push(op.apply(transform)) ;
        }
        
        @Override
        protected void visit1(Op1 op)
        {
            Op subOp = null ;
            if ( op.getSubOp() != null )
                subOp = pop() ;
            push(op.apply(transform, subOp)) ;
        }

        @Override
        protected void visit2(Op2 op)
        { 
            Op left = null ;
            Op right = null ;
    
            // Must do right-left because the pushes onto the stack were left-right. 
            if ( op.getRight() != null )
                right = pop() ;
            if ( op.getLeft() != null )
                left = pop() ;
            Op opX = op.apply(transform, left, right) ; 
            push(opX) ;
        }
        
        @Override
        protected void visitN(OpN op)
        {
            List<Op> x = new ArrayList<Op>(op.size()) ;
            
            for ( Iterator<Op> iter = op.iterator() ; iter.hasNext() ; )
            {
                Op sub = iter.next() ;
                Op r = pop() ;
                // Skip nulls.
                if ( r != null )
                    // Add in reverse.
                    x.add(0, r) ;
            }
            Op opX = op.apply(transform, x) ;  
            push(opX) ;
        }
        
        @Override
        protected void visitExt(OpExt op)
        {
            push(transform.transform(op)) ;
        }
    }
    
    // --------------------------------
    // Transformations that avoid touching SERVICE.
    // Modified classes to avoid transforming SERVICE/OpService.
    // Plan A: In the application of the transform, skip OpService. 
    
    /** Treat OpService as a leaf of the tree */
    static class ApplyTransformVisitorServiceAsLeaf extends ApplyTransformVisitor
    {
        public ApplyTransformVisitorServiceAsLeaf(Transform transform)
        {
            super(transform) ;
        }
        
        @Override
        public void visit(OpService op)
        {
            // Treat as a leaf that does not change.
            push(op) ;
        }
    }
    
    // Plan B: The walker skips walking into OpService nodes.
    
    /** Don't walk down an OpService sub-operation */
    static class WalkerVisitorSkipService extends WalkerVisitor
    {
        public WalkerVisitorSkipService(OpVisitor visitor, OpVisitor beforeVisitor, OpVisitor afterVisitor)
        {
            super(visitor, beforeVisitor, afterVisitor) ;
        }
        
        public WalkerVisitorSkipService(OpVisitor visitor)
        {
            super(visitor) ;
        }
        
        @Override
        public void visit(OpService op)
        { 
            before(op) ;
            // visit1 code from WalkerVisitor
//            if ( op.getSubOp() != null ) op.getSubOp().visit(this) ;
            
            // Just visit the OpService node itself.
            // The transformer needs to push the code as a result (see ApplyTransformVisitorSkipService)
            if ( visitor != null ) op.visit(visitor) ;
            
            after(op) ;
        }
        
    }
    
    // --------------------------------
    // Safe: ignore transformation of OpService and return the original.
    // Still walks the sub-op of OpService 
    static class TransformSkipService extends TransformWrapper
    {
        public TransformSkipService(Transform transform)
        {
            super(transform) ;
        }
        
        @Override
        public Op transform(OpService opService, Op subOp)
        { return opService ; } 
    }
}
