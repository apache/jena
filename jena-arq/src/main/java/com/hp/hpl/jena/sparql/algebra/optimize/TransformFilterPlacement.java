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

package com.hp.hpl.jena.sparql.algebra.optimize ;

import java.util.Collection ;
import java.util.Iterator ;
import java.util.List ;
import java.util.Set ;

import org.apache.jena.atlas.lib.CollectionUtils ;
import org.apache.jena.atlas.lib.DS ;

import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.graph.Triple ;
import com.hp.hpl.jena.sparql.algebra.Op ;
import com.hp.hpl.jena.sparql.algebra.OpVars ;
import com.hp.hpl.jena.sparql.algebra.TransformCopy ;
import com.hp.hpl.jena.sparql.algebra.op.* ;
import com.hp.hpl.jena.sparql.core.BasicPattern ;
import com.hp.hpl.jena.sparql.core.Var ;
import com.hp.hpl.jena.sparql.expr.Expr ;
import com.hp.hpl.jena.sparql.expr.ExprList ;
import com.hp.hpl.jena.sparql.util.VarUtils ;

/**
 * Rewrite an algebra expression to put filters as close to their bound
 * variables.
 * <p>Process BGP (whether triples or quads) is left as a separate step (but after this transform)
 * because it can desirable to reorder the BGP before placing filters,
 * or afterwards.   
 */

public class TransformFilterPlacement extends TransformCopy {
    
    static class Placement {
        final Op op ;
        final ExprList unplaced ; 
        Placement(Op op, ExprList remaining) { this.op = op ; this.unplaced = remaining ; }
    }
    
    static final ExprList emptyList = new ExprList() ;
    static final Placement noChangePlacement = null ; //new Placement(null, null) ;
    
    private static Placement result(Op op, ExprList remaining) { 
        if ( op == null )
            return null ;
        return new Placement(op, remaining) ; 
    }
    
    private Placement resultNoChange(Op original) { 
        return noChangePlacement ;
    }
    
    private boolean isNoChange(Placement placement) { 
        return placement == noChangePlacement ;
    }

    /** Apply filter placement to a BGP */
    public static Op transform(ExprList exprs, BasicPattern bgp) {
        Placement placement = placeBGP(exprs, bgp) ;
        Op op = ( placement == null ) ? new OpBGP(bgp) : placement.op ;
        if ( placement != null )
            op = buildFilter(placement.unplaced, op) ;
        return op ;
    }

    /** Apply filter placement to a named graph BGP */
    public static Op transform(ExprList exprs, Node graphNode, BasicPattern bgp) {
        Placement placement = placeQuadPattern(exprs, graphNode, bgp) ;
        Op op = ( placement == null ) ? new OpQuadPattern(graphNode, bgp) : placement.op ;
        if ( placement != null )
            op = buildFilter(placement.unplaced, op) ;
        return op ;
    }

    private final boolean includeBGPs ;

    public TransformFilterPlacement() { this(true) ; }
    
    public TransformFilterPlacement(boolean includeBGPs)
    { this.includeBGPs = includeBGPs ; }

    @Override
    public Op transform(OpFilter opFilter, Op x) {
        ExprList exprs = opFilter.getExprs() ;
        Placement placement = transform(exprs, x) ;
        if ( placement == null )  
            // Didn't do anything.
            return super.transform(opFilter, x) ;
        Op op = buildFilter(placement) ;
        return op ;
    }

    private Op transformOp(ExprList exprs, Op x) {
        Placement placement = transform(exprs, x) ;
        if ( placement == null )
            return buildFilter(exprs, x) ;
        return buildFilter(placement) ;
    }

    private Placement transform(ExprList exprs, Op input) {
        // Dispatch by visitor??
        Placement placement = null ;

        if ( input instanceof OpBGP )
            placement = placeOrWrapBGP(exprs, (OpBGP)input) ;
        else if ( input instanceof OpQuadPattern )
            placement = placeOrWrapQuadPattern(exprs, (OpQuadPattern)input) ;   
        else if ( input instanceof OpSequence )
            placement = placeSequence(exprs, (OpSequence)input) ;
        else if ( input instanceof OpJoin )
            placement = placeJoin(exprs, (OpJoin)input) ;
        else if ( input instanceof OpConditional )
            placement = placeConditional(exprs, (OpConditional)input) ;
        else if ( input instanceof OpLeftJoin )
            placement = placeLeftJoin(exprs, (OpLeftJoin)input) ;
        else if ( input instanceof OpFilter )
            placement = placeFilter(exprs, (OpFilter)input) ;
        else if ( input instanceof OpUnion )
            placement = placeUnion(exprs, (OpUnion)input) ;
        
        // These are operations where changing the order of operations
        // does not in itself make a difference but enables expressions
        // to be pushed down to where they might make a difference.
        // Otherwise these would be blockers.
        
        else if ( input instanceof OpExtend )
            placement = placeExtend(exprs, (OpExtend)input) ;
        else if ( input instanceof OpAssign )
            placement = placeAssign(exprs, (OpAssign)input) ;
        else if ( input instanceof OpProject )
            placement = placeProject(exprs, (OpProject)input) ;

        return placement ;
    }
    
    private Placement placeFilter(ExprList exprs, OpFilter input) {
        // Thrown the filter expressions into the 
        if ( exprs.size() != 0 ) {
            exprs = ExprList.copy(exprs) ;
            exprs.addAll(input.getExprs());
        } else
            exprs = input.getExprs() ;
        
        Placement p = transform(exprs, input.getSubOp()) ;
        return p ;
    }

    private Placement placeOrWrapBGP(ExprList exprs, OpBGP x) {
        return placeOrWrapBGP(exprs, x.getPattern()) ;
    }

    /** Either just wrap the BGP with possible expressions or also consider breaking up the BGP */
    private Placement placeOrWrapBGP(ExprList exprsIn, BasicPattern pattern) {
        if ( includeBGPs )
            return placeBGP(exprsIn, pattern) ;
        else
            return wrapBGP(exprsIn, pattern) ;
    }
    
    private static Placement placeBGP(ExprList exprsIn, BasicPattern pattern) {
        ExprList exprs = ExprList.copy(exprsIn) ;
        Set<Var> patternVarsScope = DS.set() ;
        // Any filters that depend on no variables.
        Op op = null ;

        for (Triple triple : pattern) {
            // Place any filters that are now covered.
            op = insertAnyFilter(exprs, patternVarsScope, op) ;
            // Consider this triple.
            // Get BGP that is accumulating triples.
            OpBGP opBGP = getBGP(op) ;
            if ( opBGP == null ) {
                // Last thing was not a BGP (so it likely to be a filter)
                // Need to pass the results from that into the next triple.
                opBGP = new OpBGP() ;
                op = OpSequence.create(op, opBGP) ;
            }

            opBGP.getPattern().add(triple) ;
            // Update variables in scope.
            VarUtils.addVarsFromTriple(patternVarsScope, triple) ;
        }
        
        // Place any filters this whole BGP covers. 
        op = insertAnyFilter(exprs, patternVarsScope, op) ;
        return result(op, exprs) ;
    }

    /** Wrap the Basic Pattern with any applicable expressions from the ExprList
     * but do not break up the BasicPattern in any way.
     */
    private Placement wrapBGP(ExprList exprsIn, BasicPattern pattern) {
        Set<Var> vs = DS.set();
        VarUtils.addVars(vs, pattern);
        ExprList pushed = new ExprList();
        ExprList unpushed = new ExprList();
        for (Expr e : exprsIn) {
            Set<Var> eVars = e.getVarsMentioned();
            if (vs.containsAll(eVars))
                pushed.add(e);
            else
                unpushed.add(e);
        }
        
        // Can't push anything into a filter around this BGP
        if (pushed.size() == 0) return null;
        
        // Safe to place some conditions around the BGP
        return new Placement(OpFilter.filter(pushed, new OpBGP(pattern)), unpushed);

    }

    /** Find the current OpBGP, or return null. */
    private static OpBGP getBGP(Op op) {
        if ( op instanceof OpBGP )
            return (OpBGP)op ;

        if ( op instanceof OpSequence ) {
            // Is last in OpSequence an BGP?
            OpSequence opSeq = (OpSequence)op ;
            List<Op> x = opSeq.getElements() ;
            if ( x.size() > 0 ) {
                Op opTop = x.get(x.size() - 1) ;
                if ( opTop instanceof OpBGP )
                    return (OpBGP)opTop ;
                // Drop through
            }
        }
        // Can't find.
        return null ;
    }

    private Placement placeOrWrapQuadPattern(ExprList exprs, OpQuadPattern pattern) {
        return placeQuadPattern(exprs, pattern.getGraphNode(), pattern.getBasicPattern()) ;
    }

    private Placement placeOrWrapQuadPattern(ExprList exprsIn, Node graphNode, BasicPattern pattern) {
        if ( includeBGPs )
            return placeQuadPattern(exprsIn, graphNode, pattern) ;
        else
            return wrapQuadPattern(exprsIn, graphNode, pattern) ;
    }
    
    private static Placement placeQuadPattern(ExprList exprsIn, Node graphNode, BasicPattern pattern) {
        ExprList exprs = ExprList.copy(exprsIn) ;
        Set<Var> patternVarsScope = DS.set() ;
        // Any filters that depend on no variables.

        if ( Var.isVar(graphNode) ) {
            // Add in the graph node of the quad block.
            VarUtils.addVar(patternVarsScope, Var.alloc(graphNode)) ;
        }
        Op op = null ;
        
        for (Triple triple : pattern) {
            op = insertAnyFilter(exprs, patternVarsScope, op) ;
            OpQuadPattern opQuad = getQuads(op) ;
            if ( opQuad == null ) {
                opQuad = new OpQuadPattern(graphNode, new BasicPattern()) ;
                op = OpSequence.create(op, opQuad) ;
            }

            opQuad.getBasicPattern().add(triple) ;
            // Update variables in scope.
            VarUtils.addVarsFromTriple(patternVarsScope, triple) ;
        }
        // Place any filters this whole quad block covers. 
        op = insertAnyFilter(exprs, patternVarsScope, op) ;
        return result(op, exprs) ;
    }

    
    /** Wrap the Graph node, Basic Pattern with any applicable expressions from the ExprList
     *  but do not break up the BasicPattern in any way.
     */
    private static Placement wrapQuadPattern(ExprList exprsIn, Node graphNode, BasicPattern pattern) {
        Set<Var> vs = DS.set();
        VarUtils.addVars(vs, pattern);
        if (Var.isVar(graphNode)) 
            vs.add(Var.alloc(graphNode));
        ExprList pushed = new ExprList();
        ExprList unpushed = new ExprList();
        for (Expr e : exprsIn) {
            Set<Var> eVars = e.getVarsMentioned();
            if (vs.containsAll(eVars)) {
                pushed.add(e);
            } else {
                unpushed.add(e);
            }
        }

        // Can't push anything into a filter around this quadpattern
        if (pushed.size() == 0) return null;

        // Safe to place some conditions around the quadpattern
        return new Placement(OpFilter.filter(pushed, new OpQuadPattern(graphNode, pattern)), unpushed);
    }

    /** Find the current OpQuadPattern, or return null. */
    private static OpQuadPattern getQuads(Op op) {
        if ( op instanceof OpQuadPattern )
            return (OpQuadPattern)op ;

        if ( op instanceof OpSequence ) {
            // Is last in OpSequence an BGP?
            OpSequence opSeq = (OpSequence)op ;
            List<Op> x = opSeq.getElements() ;
            if ( x.size() > 0 ) {
                Op opTop = x.get(x.size() - 1) ;
                if ( opTop instanceof OpQuadPattern )
                    return (OpQuadPattern)opTop ;
                // Drop through
            }
        }
        return null ;
    }

    /*
     * A Sequence is a number of joins where scoping means the LHS can be
     * substituted into the right, i.e. there are no scoping issues. Assuming a
     * substitution join is going to be done, filtering once as soon as the
     * accumulated variables cover the filter is a good thing to do. It is
     * effectively pusing on teh left side only - the right side, by
     * substitution, will never see the variables. The variable can not be
     * reintroduced (it will have been renamed away if it's the same name,
     * different scope, which is a different variable with the same name in the
     * orginal query.
     */

    private Placement placeSequence(ExprList exprsIn, OpSequence opSequence) {
        ExprList exprs = ExprList.copy(exprsIn) ;
        Set<Var> varScope = DS.set() ;
        List<Op> ops = opSequence.getElements() ;

        Op op = null ;
        // No point placing on the last element as that is the same as filtering the entire expression.
        for (int i = 0 ; i < ops.size() ; i++ ) {
            op = insertAnyFilter(exprs, varScope, op) ;
            Op seqElt = ops.get(i) ;
            if ( i != ops.size()-1 ) {
                Placement p = transform(exprs, seqElt) ;
                if ( p != null ) {
                    exprs = p.unplaced ;
                    seqElt = p.op ;
                }
                varScope.addAll(fixedVars(seqElt)) ;
            }
            op = OpSequence.create(op, seqElt) ;
        }
        return result(op, exprs) ;
    }
    
    // Whether to push a covered filter into the RHS even if pushed into the LHS.
    // If this is run after join->sequence, then this is good to do.
    static boolean pushRightAsWellAsLeft = true ; 
    
    private Placement placeJoin(ExprList exprs, OpJoin opJoin) {
        Op left = opJoin.getLeft() ;
        Op right = opJoin.getRight() ;
        Collection<Var> leftVars = fixedVars(left) ;
        Collection<Var> rightVars = fixedVars(right) ;
        ExprList unpushed = new ExprList() ;
        ExprList pushLeft = new ExprList() ;
        ExprList pushRight = new ExprList() ;

        for (Expr expr : exprs) {
            Set<Var> vars = expr.getVarsMentioned() ;
            boolean pushed = false ;

            if ( leftVars.containsAll(vars) ) {
                pushLeft.add(expr) ;
                pushed = true ;
            }
            
            if ( pushed && ! pushRightAsWellAsLeft )
                continue ;
            // If left only, make this "else if" of left test, remove "continue" 
            if ( rightVars.containsAll(vars) ) {
                // Push right
                pushRight.add(expr) ;
                pushed = true ;
            }

            if ( !pushed )
                unpushed.add(expr) ;
        }

        if ( pushLeft.isEmpty() && pushRight.isEmpty() )
            return null ;

        Op opLeftNew = left ;
        if ( !pushLeft.isEmpty() )
            opLeftNew = transformOp(pushLeft, opLeftNew) ;

        Op opRightNew = right ;
        if ( !pushRight.isEmpty() )
            opRightNew = transformOp(pushRight, opRightNew) ;

        Op op = OpJoin.create(opLeftNew, opRightNew) ;
        return result(op, unpushed) ;
    }

    /* A conditional is left join without scoping complications. */
    
    private Placement placeConditional(ExprList exprs, OpConditional opConditional) {
        Op left = opConditional.getLeft() ;
        Op right = opConditional.getRight() ;
        Placement nLeft = transform(exprs, left) ;
        if ( nLeft == null )
            return result(opConditional, exprs) ;
        Op op = new OpConditional(nLeft.op, right) ;
        return result(op, nLeft.unplaced) ;
    }

    private Placement placeLeftJoin(ExprList exprs, OpLeftJoin opLeftJoin) {
        // Push LHS only.  RHS may result in no matches - is that safe to push into? 
        Op left = opLeftJoin.getLeft() ;
        Op right = opLeftJoin.getRight() ;
        Placement nLeft = transform(exprs, left) ;
        if ( nLeft == null )
            return result(opLeftJoin, exprs) ;
        Op op = OpLeftJoin.create(nLeft.op, right, opLeftJoin.getExprs()) ;
        return result(op, nLeft.unplaced) ;
    }
    
    private Placement placeUnion(ExprList exprs, OpUnion input) {
        // Push into both sides.
        Op left = input.getLeft() ;
        Placement pLeft = transform(exprs, left) ;
        
        Op right = input.getRight() ;
        Placement pRight = transform(exprs, right) ;
        
        // JENA-652 Temporary fix
        if ( pLeft != null && ! pLeft.unplaced.isEmpty() )
            return null ;
        if ( pRight != null && ! pRight.unplaced.isEmpty() )
            return null ;

        // Old code.
        left = transformOp(exprs, left) ;
        right = transformOp(exprs, right) ;
        
        Op op2 = OpUnion.create(left, right) ;
        return result(op2, emptyList) ;
    }

    /** Try to optimize (filter (extend ...)) */
    private Placement placeExtend(ExprList exprs, OpExtend input) {
        return processExtendAssign(exprs, input) ;
    }
    
    private Placement placeAssign(ExprList exprs, OpAssign input) {
        return processExtendAssign(exprs, input) ;
        
    }

    private Placement processExtendAssign(ExprList exprs, OpExtendAssign input) {
        // Could break up the VarExprList
        Collection<Var> vars1 = input.getVarExprList().getVars() ;
        ExprList pushed = new ExprList() ;
        ExprList unpushed = new ExprList() ;
        
        for ( Expr expr : exprs ) {
            Set<Var> exprVars = expr.getVarsMentioned() ;
            if ( disjoint(vars1, exprVars) )
                pushed.add(expr);
            else
                unpushed.add(expr) ;
        }
                
        if ( pushed.isEmpty() ) 
            return resultNoChange(input) ;
        
        // (filter ... (extend ... ))
        //   ===>
        // (extend ... (filter ... ))
        Op opSub = input.getSubOp() ;
        
        // And try down the expressions
        Placement p = transform(pushed, opSub) ;

        if ( p == null ) {
            // Couldn't place an filter expressions.  Do nothing.
            return null ;
        }
        
        if ( ! p.unplaced.isEmpty() )
            // Some placed, not all.
            // Pass back out all untouched expressions.
            unpushed.addAll(p.unplaced) ; 
        Op op1 = input.copy(p.op) ;
        
        return result(op1, unpushed) ;
    }

    private Placement placeProject(ExprList exprs, OpProject input) {
        Collection<Var> varsProject = input.getVars() ;
        ExprList pushed = new ExprList() ;
        ExprList unpushed = new ExprList() ;
        
        for ( Expr expr : exprs ) {
            Set<Var> exprVars = expr.getVarsMentioned() ;
            if ( varsProject.containsAll(exprVars) )
                pushed.add(expr);
            else
                unpushed.add(expr) ;
        }
        if ( pushed.isEmpty() ) 
            return resultNoChange(input) ;
        // (filter (project ...)) ===> (project (filter ...)) 
        Op opSub = input.getSubOp() ;
        Placement p = transform(pushed, opSub) ;
        if ( p == null ) {
            Op op1 = OpFilter.filter(pushed, opSub) ;
            Op op2 = input.copy(op1) ;
            return result(op2, unpushed) ;
        }
        Op op1 = OpFilter.filter(p.unplaced, p.op) ;
        Op op2 = input.copy(op1) ;
        return result(op2, unpushed) ;
    }
    
    private Set<Var> fixedVars(Op op) {
        return OpVars.fixedVars(op) ;
    }

    /** For any expression now in scope, wrap the op with a filter.
     * Caution - the ExprList is an in-out argument which is modified.
     * This function modifies ExprList passed in to remove any filter
     * that is placed. 
     */
    
    private static Op insertAnyFilter(ExprList unplacedExprs, Set<Var> patternVarsScope, Op op) {
        for (Iterator<Expr> iter = unplacedExprs.iterator(); iter.hasNext();) {
            Expr expr = iter.next() ;
            // Cache
            Set<Var> exprVars = expr.getVarsMentioned() ;
            if ( patternVarsScope.containsAll(exprVars) ) {
                if ( op == null )
                    op = OpTable.unit() ;
                op = OpFilter.filter(expr, op) ;
                iter.remove() ;
            }
        }
        return op ;
    }

    private static <T> boolean disjoint(Collection<T> collection, Collection<T> possibleElts) {
        return CollectionUtils.disjoint(collection, possibleElts) ;
    }

    /** Place expressions around an Op */
    private static Op buildFilter(Placement placement) {
        if ( placement == null )
            return null ;
        if ( placement.unplaced.isEmpty() )
            return placement.op ;
        return buildFilter(placement.unplaced, placement.op) ;
    }
    
    private static Op buildFilter(ExprList exprs, Op op) {
        if ( exprs == null || exprs.isEmpty() )
            return op ;

        for (Iterator<Expr> iter = exprs.iterator(); iter.hasNext();) {
            Expr expr = iter.next() ;
            if ( op == null )
                op = OpTable.unit() ;
            op = OpFilter.filter(expr, op) ;
        }
        return op ;
    }
}
