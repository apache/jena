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

import java.util.* ;

import org.apache.jena.atlas.logging.Log ;

import com.hp.hpl.jena.query.SortCondition ;
import com.hp.hpl.jena.sparql.algebra.OpWalker.WalkerVisitor ;
import com.hp.hpl.jena.sparql.algebra.op.* ;
import com.hp.hpl.jena.sparql.algebra.optimize.ExprTransformApplyTransform ;
import com.hp.hpl.jena.sparql.core.Var ;
import com.hp.hpl.jena.sparql.core.VarExprList ;
import com.hp.hpl.jena.sparql.expr.* ;
import com.hp.hpl.jena.sparql.expr.aggregate.Aggregator ;

/** A bottom-top application of a transformation of SPARQL algebra */  
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
    
    /** Transform an algebra expression and the expressions */
    public static Op transform(Transform transform, ExprTransform exprTransform, Op op)
    { return get().transformation(transform, exprTransform, op, null, null) ; }

    /** Transformation with specific Transform and default ExprTransform (apply transform inside pattern expressions like NOT EXISTS) */ 
    public static Op transform(Transform transform, Op op, OpVisitor beforeVisitor, OpVisitor afterVisitor)
    {
        return get().transformation(transform, op, beforeVisitor, afterVisitor) ;
    }
    
    /** Transformation with specific Transform and ExprTransform applied */
    public static Op transform(Transform transform, ExprTransform exprTransform, Op op, OpVisitor beforeVisitor, OpVisitor afterVisitor)
    {
        return get().transformation(transform, exprTransform, op, beforeVisitor, afterVisitor) ;
    }

    /** Transform an algebra expression except skip (leave alone) any OpService nodes */
    public static Op transformSkipService(Transform transform, Op op)
    {
        return transformSkipService(transform, op, null, null) ; 
    }

    /** Transform an algebra expression except skip (leave alone) any OpService nodes */
    public static Op transformSkipService(Transform transform, ExprTransform exprTransform, Op op)
    {
        return transformSkipService(transform, exprTransform, op, null, null) ; 
    }


    /** Transform an algebra expression except skip (leave alone) any OpService nodes */
    public static Op transformSkipService(Transform transform, Op op, OpVisitor beforeVisitor, OpVisitor afterVisitor)
    {
        // Skip SERVICE
        if ( true )
        {
            // Simplest way but still walks the OpService subtree (and throws away the transformation).
            Transform walker = new TransformSkipService(transform) ;
            return Transformer.transform(walker, op, beforeVisitor, afterVisitor) ;
        }
        else
        {
            // Don't transform OpService and don't walk the sub-op 
            ExprTransform exprTransform = new ExprTransformApplyTransform(transform, beforeVisitor, afterVisitor) ;
            ApplyTransformVisitorServiceAsLeaf v = new ApplyTransformVisitorServiceAsLeaf(transform, exprTransform) ;
            WalkerVisitorSkipService walker = new WalkerVisitorSkipService(v, beforeVisitor, afterVisitor) ;
            OpWalker.walk(walker, op) ;
            return v.result() ;
        }
    }

    /** Transform an algebra expression except skip (leave alone) any OpService nodes */
    public static Op transformSkipService(Transform transform, ExprTransform exprTransform, Op op, OpVisitor beforeVisitor, OpVisitor afterVisitor)
    {
        // Skip SERVICE
        if ( true )
        {
            // Simplest way but still walks the OpService subtree (and throws away the transformation).
            Transform walker = new TransformSkipService(transform) ;
            return Transformer.transform(walker, exprTransform, op, beforeVisitor, afterVisitor) ;
        }
        else
        {
            ApplyTransformVisitorServiceAsLeaf v = new ApplyTransformVisitorServiceAsLeaf(transform, exprTransform) ;
            WalkerVisitorSkipService walker = new WalkerVisitorSkipService(v, beforeVisitor, afterVisitor) ;
            OpWalker.walk(walker, op) ;
            return v.result() ;
        }
    }

    // To allow subclassing this class, we use a singleton pattern 
    // and theses protected methods.
    protected Op transformation(Transform transform, Op op, OpVisitor beforeVisitor, OpVisitor afterVisitor)
    {
        ExprTransform exprTransform = new ExprTransformApplyTransform(transform, beforeVisitor, afterVisitor) ;
        return transformation(transform, exprTransform, op, beforeVisitor, afterVisitor) ;
    }
        
    protected Op transformation(Transform transform, ExprTransform exprTransform, Op op, OpVisitor beforeVisitor, OpVisitor afterVisitor)
    {
        ApplyTransformVisitor v = new ApplyTransformVisitor(transform, exprTransform) ;
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
        private final ExprTransform exprTransform ;

        private final Deque<Op> stack = new ArrayDeque<>() ;
        protected final Op pop() 
        { return stack.pop(); }
        
        protected final void push(Op op)
        { 
            // Including nulls
            stack.push(op) ;
        }
        
        public ApplyTransformVisitor(Transform transform, ExprTransform exprTransform)
        { 
            this.transform = transform ;
            this.exprTransform = exprTransform ;
        }
        
        final Op result()
        {
            if ( stack.size() != 1 )
                Log.warn(this, "Stack is not aligned") ;
            return pop() ; 
        }

        private static ExprList transform(ExprList exprList, ExprTransform exprTransform)
        {
            if ( exprList == null || exprTransform == null )
                return exprList ;
            return ExprTransformer.transform(exprTransform, exprList) ;
        }

        private static Expr transform(Expr expr, ExprTransform exprTransform)
        {
            if ( expr == null || exprTransform == null )
                return expr ;
            return ExprTransformer.transform(exprTransform, expr) ;
        }
        
        // ----
        // Algebra operations that involve an Expr, and so might include NOT EXISTS 

        @Override
        public void visit(OpOrder opOrder)
        {
            List<SortCondition> conditions = opOrder.getConditions() ;
            List<SortCondition> conditions2 = new ArrayList<>() ;
            boolean changed = false ;

            for ( SortCondition sc : conditions )
            {
                Expr e = sc.getExpression() ;
                Expr e2 = transform(e, exprTransform) ;
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
            VarExprList varExpr2 = process(varExpr, exprTransform) ;
            OpAssign opAssign2 = opAssign ;
            if ( varExpr != varExpr2 )
                opAssign2 = OpAssign.create(opAssign.getSubOp(), varExpr2) ;
            visit1(opAssign2) ;
        }
        
        @Override
        public void visit(OpExtend opExtend)
        { 
            VarExprList varExpr = opExtend.getVarExprList() ;
            VarExprList varExpr2 = process(varExpr, exprTransform) ;
            OpExtend opExtend2 = opExtend ;
            if ( varExpr != varExpr2 )
                opExtend2 = OpExtend.create(opExtend.getSubOp(), varExpr2) ;
            visit1(opExtend2) ;
        }
        
        private static VarExprList process(VarExprList varExpr, ExprTransform exprTransform)
        {
            List<Var> vars = varExpr.getVars() ;
            VarExprList varExpr2 = new VarExprList() ;
            boolean changed = false ;
            for ( Var v : vars )
            {
                Expr e = varExpr.getExpr(v) ;
                Expr e2 =  e ;
                if ( e != null )
                    e2 = transform(e, exprTransform) ;
                if ( e2 == null )
                    varExpr2.add(v) ;
                else
                    varExpr2.add(v, e2) ; 
                if ( e != e2 )
                    changed = true ;
            }
            if ( ! changed ) 
                return varExpr ;
            return varExpr2 ;
        }

        private static ExprList process(ExprList exprList, ExprTransform exprTransform)
        {
            if ( exprList == null )
                return null ;
            ExprList exprList2 = new ExprList() ;
            boolean changed = false ;
            for ( Expr e : exprList )
            {
                Expr e2 = process(e, exprTransform) ;
                exprList2.add(e2) ; 
                if ( e != e2 )
                    changed = true ;
            }
            if ( ! changed ) 
                return exprList ;
            return exprList2 ;
        }
        
        private static Expr process(Expr expr, ExprTransform exprTransform)
        {
            Expr e = expr ;
            Expr e2 =  e ;
            if ( e != null )
                e2 = transform(e, exprTransform) ;
            if ( e == e2 ) 
                return expr ;
            return e2 ;
        }

        @Override
        public void visit(OpGroup opGroup)
        {
            boolean changed = false ;

            VarExprList varExpr = opGroup.getGroupVars() ;
            VarExprList varExpr2 = process(varExpr, exprTransform) ;
            if ( varExpr != varExpr2 )
                changed = true ;
            
            
            List<ExprAggregator> aggs = opGroup.getAggregators() ;
            List<ExprAggregator> aggs2 = aggs ;
            
            //And the aggregators...
            aggs2 = new ArrayList<>() ;
            for ( ExprAggregator agg : aggs )
            {
                Aggregator aggregator = agg.getAggregator() ;
                Var v = agg.getVar() ;
                
                // Variable associated with the aggregate
                Expr eVar = agg.getAggVar() ;   // Not .getExprVar()
                Expr eVar2 = transform(eVar, exprTransform) ;
                if ( eVar != eVar2 )
                    changed = true ;

                // The Aggregator expression
                Expr e = aggregator.getExpr() ;
                Expr e2 = e ;
                if ( e != null )    // Null means "no relevant expression" e.g. COUNT(*)
                    e2 = transform(e, exprTransform) ;
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
            List<Op> x = new ArrayList<>(op.size()) ;
            
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
        protected void visitFilter(OpFilter opFilter)
        {
            Op subOp = null ;
            if ( opFilter.getSubOp() != null )
                subOp = pop() ;
            boolean changed = ( opFilter.getSubOp() != subOp ) ;

            ExprList ex = opFilter.getExprs() ;
            ExprList ex2 = process(ex, exprTransform) ;
            OpFilter f = opFilter ;
            if ( ex != ex2 )
                f = (OpFilter)OpFilter.filter(ex2, subOp) ;
            push(f.apply(transform, subOp)) ;
        }
        
        @Override
        protected void visitLeftJoin(OpLeftJoin op) {
            Op left = null ;
            Op right = null ;
        
            // Must do right-left because the pushes onto the stack were left-right. 
            if ( op.getRight() != null )
                right = pop() ;
            if ( op.getLeft() != null )
                left = pop() ;
            
            ExprList exprs = op.getExprs() ;
            ExprList exprs2 = process(exprs, exprTransform) ;
            OpLeftJoin x = op ;
            if ( exprs != exprs2 )
                x = OpLeftJoin.createLeftJoin(left, right, exprs2) ;
            Op opX = x.apply(transform, left, right) ; 
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
        public ApplyTransformVisitorServiceAsLeaf(Transform transform, ExprTransform exprTransform)
        {
            super(transform, exprTransform) ;
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
