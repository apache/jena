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

package org.apache.jena.sparql.algebra.optimize ;

import java.util.* ;

import org.apache.jena.atlas.lib.Lib ;
import org.apache.jena.graph.Node ;
import org.apache.jena.graph.Triple ;
import org.apache.jena.sparql.algebra.Op ;
import org.apache.jena.sparql.algebra.OpVars ;
import org.apache.jena.sparql.algebra.TransformCopy ;
import org.apache.jena.sparql.algebra.op.* ;
import org.apache.jena.sparql.core.BasicPattern ;
import org.apache.jena.sparql.core.Var ;
import org.apache.jena.sparql.expr.Expr ;
import org.apache.jena.sparql.expr.ExprLib ;
import org.apache.jena.sparql.expr.ExprList ;
import org.apache.jena.sparql.expr.ExprVars;
import org.apache.jena.sparql.pfunction.PropFuncArg ;
import org.apache.jena.sparql.util.VarUtils ;

/**
 * Rewrite an algebra expression to put filters as close to their bound
 * variables.
 * <p>Process BGP (whether triples or quads) is left as a separate step (but after this transform)
 * because it can desirable to reorder the BGP before placing filters,
 * or afterwards.
 */

public class TransformFilterPlacement extends TransformCopy {
    public static class Placement {
        final public Op op ;
        final public ExprList unplaced ; 
        public Placement(Op op, ExprList remaining) { 
            this.op = op ; 
            this.unplaced = remaining ; 
        }
        @Override
        public String toString() { return ""+op+" : "+unplaced ; }
        
        @Override
        public int hashCode() {
            return 31*Lib.hashCodeObject(op,1) + Lib.hashCodeObject(unplaced) ;
        }
        @Override
        public boolean equals(Object obj) {
            if ( this == obj ) return true ;
            if ( obj == null ) return false ;
            if ( getClass() != obj.getClass() ) return false ;
            Placement other = (Placement)obj ;
            return Objects.equals(op, other.op) && Objects.equals(unplaced, other.unplaced) ;
        }
    }
    
    // Empty, immutable ExprList
    static final ExprList emptyList = ExprList.emptyList  ;
    
    // No placement performed
    static final Placement noChangePlacement = null ; //new Placement(null, null) ;
    
    private static Placement result(Op op, ExprList remaining) { 
        if ( op == null )
            return noChangePlacement ;
        return new Placement(op, remaining) ; 
    }
    
    private Placement resultNoChange(Op original) { 
        return noChangePlacement ;
    }
    
    private boolean isNoChange(Placement placement) { 
        return ! isChange(placement) ;
    }

    private boolean isChange(Placement placement) { 
        return placement != noChangePlacement ;
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

    /** Operation exposes the filter placement mechanism 
     *  so that investigation of filter placement issues
     *  can be done from outside this class.
     *  <i>Do not use in application code : subject to removal or change at any time.</i>
     */
    public static Placement filterPlacement$(ExprList exprs, Op op) {
        TransformFilterPlacement t = new TransformFilterPlacement() ;
        return t.transform(exprs, op) ;
    }
    
    @Override
    public Op transform(OpFilter opFilter, Op x) {
        ExprList exprs = opFilter.getExprs() ;
        
        // Extract any expressions with "nasty" cases (RAND, UUID, STRUUID and BNODE)
        // which are not true functions (they return a different value every call so
        // number of calls matters.  NOW is safe (returns a fixed time point for the whole
        // query.
        
        // Phase one - check to see if work needed. 
        ExprList exprs2 = null ;
        for ( Expr expr : exprs ) {
            if ( ! ExprLib.isStable(expr) ) {
                if ( exprs2 == null )
                    exprs2 = new ExprList() ;
                exprs2.add(expr) ;
            }
        }
        
        // Phase 2 - if needed, split.
        if ( exprs2 != null ) {
            ExprList exprs1 = new ExprList() ;
            for ( Expr expr : exprs ) {
                // We are assuming fixup is rare. 
                if ( ExprLib.isStable(expr) )
                    exprs1.add(expr) ;
            }
            exprs = exprs1 ;
        }
        
        Placement placement = transform(exprs, x) ;
        if ( isNoChange(placement) )  
            // Didn't do anything.
            return super.transform(opFilter, x) ;
        Op op = buildFilter(placement) ;
        if ( exprs2 != null )
            // Add back the non-deterministic expressions
            op = OpFilter.filterBy(exprs2, op );
        return op ;
    }

    /** Transform and always at least wrap the op with the exprs */
    private Op transformOpAlways(ExprList exprs, Op x) {
        Placement placement = transform(exprs, x) ;
        if ( isNoChange(placement) )
            return buildFilter(exprs, x) ;
        return buildFilter(placement) ;
    }

    /** Transform and return a placement */ 
    private Placement transform(ExprList exprs, Op input) {
        // Dispatch by visitor??
        Placement placement = noChangePlacement ;

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
        else if ( input instanceof OpDisjunction )
            placement = placeDisjunction(exprs, (OpDisjunction)input) ;
        else if ( input instanceof OpLeftJoin )
            placement = placeLeftJoin(exprs, (OpLeftJoin)input) ;
        else if ( input instanceof OpFilter )
            placement = placeFilter(exprs, (OpFilter)input) ;
        else if ( input instanceof OpUnion )
            placement = placeUnion(exprs, (OpUnion)input) ;
        else if ( input instanceof OpPropFunc )
            placement = placePropertyFunction(exprs, (OpPropFunc)input) ;
        else if ( input instanceof OpProcedure )
            placement = placeProcedure(exprs, (OpProcedure)input) ;
        
        // These are operations where changing the order of operations
        // does not in itself make a difference but enables expressions
        // to be pushed down to where they might make a difference.
        // Otherwise these would be blockers.
        
        else if ( input instanceof OpExtend )
            placement = placeExtend(exprs, (OpExtend)input) ;
        else if ( input instanceof OpAssign )
            placement = placeAssign(exprs, (OpAssign)input) ;
        // Modifiers
//        else if ( input instanceof OpGroup ) {
//            placement = noChangePlacement ;
//        }
//        else if ( input instanceof OpSlice ) {
//            // Not sure what the best choice is here.
//            placement = noChangePlacement ;
//        }
//        else if ( input instanceof OpTopN ) {
//            // Not sure what the best choice is here.
//            placement = noChangePlacement ;
//        }

        else if ( input instanceof OpProject )
            placement = placeProject(exprs, (OpProject)input) ;
        else if ( input instanceof OpDistinctReduced ) 
            placement = placeDistinctReduced(exprs, (OpDistinctReduced)input) ;
        else if ( input instanceof OpTable )
            placement = placeTable(exprs, (OpTable)input) ;

        return placement ;
    }
    private Placement placeFilter(ExprList exprs, OpFilter input) {
        // If input.getSubOp is itself a filter, it has already been
        // processed because the Transform is applied bottom-up.

        // We must not let the filter's expressions go back as "unplaced"
        // as they are scoped to the input and if "unplaced" are available
        // out of that scope.

        Op op = input.getSubOp() ;
        ExprList exprsInner = input.getExprs() ;
        ExprList exprsOuter = exprs ;

        // Outer
        Placement p = transform(exprsOuter, input.getSubOp()) ;
        if ( isChange(p) ) {
            op = p.op ;
            exprsOuter = p.unplaced ;
        }
        // Put inner round the modified Op.
        // If op is also a filter, a single filter is created with
        // exprsInner now after placed filters.
        // ("after" means later in the exprList of the filter).
        Op f = OpFilter.filterBy(exprsInner, op) ;
        return new Placement(f, exprsOuter) ;
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
    // See also placeQuadPattern.
    // 
    // An improvement might be to put any filters that apply to exactly one triple
    // directly on the triple pattern.  At the moment, the filter is put over
    // the block leading up to the triple pattern.
    
    private static Placement placeBGP(ExprList exprsIn, BasicPattern pattern) {
        ExprList exprs = ExprList.copy(exprsIn) ;
        Set<Var> patternVarsScope = new HashSet<>() ;
        // Any filters that depend on no variables.
        Op op = insertAnyFilter$(exprs, patternVarsScope, null) ;

        for (Triple triple : pattern) {
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
            op = insertAnyFilter$(exprs, patternVarsScope, op) ;
        }
        
        return result(op, exprs) ;
    }

    /** Wrap the Basic Pattern with any applicable expressions from the ExprList
     * but do not break up the BasicPattern in any way.
     */
    private Placement wrapBGP(ExprList exprsIn, BasicPattern pattern) {
        Set<Var> vs = new HashSet<>();
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
        if (pushed.size() == 0) 
            return noChangePlacement ;
        // Safe to place some conditions around the BGP
        Op opx = OpFilter.filterBy(pushed, new OpBGP(pattern)) ;
        return result(opx, unpushed);

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
        return placeOrWrapQuadPattern(exprs, pattern.getGraphNode(), pattern.getBasicPattern()) ;
    }

    private Placement placeOrWrapQuadPattern(ExprList exprsIn, Node graphNode, BasicPattern pattern) {
        if ( includeBGPs )
            return placeQuadPattern(exprsIn, graphNode, pattern) ;
        else
            return wrapQuadPattern(exprsIn, graphNode, pattern) ;
    }
    
    private static Placement placeQuadPattern(ExprList exprsIn, Node graphNode, BasicPattern pattern) {
        ExprList exprs = ExprList.copy(exprsIn) ;
        Set<Var> patternVarsScope = new HashSet<>() ;
        // Any filters that depend on no variables.
        Op op = insertAnyFilter$(exprs, patternVarsScope, null) ;

        if ( Var.isVar(graphNode) ) {
            // Add in the graph node of the quad block.
            VarUtils.addVar(patternVarsScope, Var.alloc(graphNode)) ;
        }
        
        for (Triple triple : pattern) {
            OpQuadPattern opQuad = getQuads(op) ;
            if ( opQuad == null ) {
                opQuad = new OpQuadPattern(graphNode, new BasicPattern()) ;
                op = OpSequence.create(op, opQuad) ;
            }

            opQuad.getBasicPattern().add(triple) ;
            // Update variables in scope.
            VarUtils.addVarsFromTriple(patternVarsScope, triple) ;
            op = insertAnyFilter$(exprs, patternVarsScope, op) ;
        }
        return result(op, exprs) ;
    }
    
    /** Wrap the Graph node, Basic Pattern with any applicable expressions from the ExprList
     *  but do not break up the BasicPattern in any way.
     */
    private static Placement wrapQuadPattern(ExprList exprsIn, Node graphNode, BasicPattern pattern) {
        Set<Var> vs = new HashSet<>();
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
        return new Placement(OpFilter.filterBy(pushed, new OpQuadPattern(graphNode, pattern)), unpushed);
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

    private Placement placePropertyFunction(ExprList exprsIn, OpPropFunc input) {
        Set<Var> argVars = new HashSet<>() ;
        PropFuncArg.addVars(argVars, input.getSubjectArgs()) ;
        PropFuncArg.addVars(argVars, input.getObjectArgs()) ;
        return placePropertyFunctionProcedure(exprsIn, argVars, input) ;
    }

    private Placement placeProcedure(ExprList exprsIn, OpProcedure input) {
        Set<Var> argVars = new HashSet<>() ;
        ExprVars.varsMentioned(argVars, input.getArgs());
        return placePropertyFunctionProcedure(exprsIn, argVars, input) ;
    }
    
    private Placement placePropertyFunctionProcedure(ExprList exprsIn, Set<Var> varScope, Op1 op) {
        ExprList exprListPlaceable = new ExprList() ;
        ExprList exprListRetain = new ExprList() ;
        for ( Expr expr : exprsIn ) {
            Set<Var> mentioned = expr.getVarsMentioned() ;
            if ( Collections.disjoint(varScope, mentioned) )
                exprListPlaceable.add(expr);
            else
                exprListRetain.add(expr);
        }
        if ( ! exprListPlaceable.isEmpty() ) {
            Placement p = transform(exprListPlaceable, op.getSubOp()) ;
            if ( isNoChange(p) )
                return resultNoChange(op);
            Op newOp = op.copy(p.op) ;
            p.unplaced.addAll(exprListRetain);
            return result(newOp, p.unplaced) ;
        }
        return resultNoChange(op);
    }

    
    /*
     * A Sequence is a number of joins where scoping means the LHS can be
     * substituted into the right, i.e. there are no scoping issues. Assuming a
     * substitution join is going to be done, filtering once as soon as the
     * accumulated variables cover the filter is a good thing to do. It is
     * effectively pusing on the left side only - the right side, by
     * substitution, will never see the variables. The variable can not be
     * reintroduced (it will have been renamed away if it's the same name,
     * different scope, which is a different variable with the same name in the
     * orginal query).
     */
    private Placement placeSequence(ExprList exprsIn, OpSequence opSequence) {
        ExprList exprs = ExprList.copy(exprsIn) ;
        Set<Var> varScope = new HashSet<>() ;
        List<Op> ops = opSequence.getElements() ;

        Op op = null ;
        for ( Op op1 : ops )
        {
            op = insertAnyFilter$( exprs, varScope, op );
            Op seqElt = op1;
            Placement p = transform( exprs, seqElt );
            if ( isChange(p) ) {
                exprs = p.unplaced;
                seqElt = p.op;
            }
            varScope.addAll( fixedVars( seqElt ) );
            op = OpSequence.create( op, seqElt );
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

        // More sophisticated - consider optional variables as well.
        // This code check the two ways to get fixed vars yields the same
        // and it does for the test suite. 
//        //---
//        VarFinder vfLeft = VarFinder.process(left) ;
//        VarFinder vfRight = VarFinder.process(right) ;
//        if ( ! CollectionUtils.sameElts(leftVars, vfLeft.getFixed() ) )
//            System.err.println("Left: "+leftVars+" : "+vfLeft.getFixed() ) ;
//        if ( ! CollectionUtils.sameElts(rightVars, vfRight.getFixed() ) )
//            System.err.println("Right: "+rightVars+" : "+vfRight.getFixed() ) ;
//        //---
        
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
            opLeftNew = transformOpAlways(pushLeft, opLeftNew) ;

        Op opRightNew = right ;
        if ( !pushRight.isEmpty() )
            opRightNew = transformOpAlways(pushRight, opRightNew) ;

        Op op = OpJoin.create(opLeftNew, opRightNew) ;
        return result(op, unpushed) ;
    }

    /* A conditional is left join without scoping complications. */
    
    private Placement placeConditional(ExprList exprs, OpConditional opConditional) {
        Op left = opConditional.getLeft() ;
        Op right = opConditional.getRight() ;
        Placement nLeft = transform(exprs, left) ;
        if ( isNoChange(nLeft) )
            return result(opConditional, exprs) ;
        Op op = new OpConditional(nLeft.op, right) ;
        return result(op, nLeft.unplaced) ;
    }

    private Placement placeLeftJoin(ExprList exprs, OpLeftJoin opLeftJoin) {
        // Push LHS only.  RHS may result in no matches - is that safe to push into? 
        Op left = opLeftJoin.getLeft() ;
        Op right = opLeftJoin.getRight() ;
        Placement nLeft = transform(exprs, left) ;
        if ( isNoChange(nLeft) )
            return result(opLeftJoin, exprs) ;
        Op op = OpLeftJoin.create(nLeft.op, right, opLeftJoin.getExprs()) ;
        return result(op, nLeft.unplaced) ;
    }
    
    private Placement placeUnion(ExprList exprs, OpUnion input) {
        if ( false ) {
            // Push into both sides without thinking.
            // Left as a safety fallback.
            
            Op left = input.getLeft() ;
            Placement pLeft = transform(exprs, left) ;
            
            Op right = input.getRight() ;
            Placement pRight = transform(exprs, right) ;
            
            if ( pLeft != null && ! pLeft.unplaced.isEmpty() )
                return noChangePlacement ;
            if ( pRight != null && ! pRight.unplaced.isEmpty() )
                return noChangePlacement ;

            // Must be guarded by the above.
            left = transformOpAlways(exprs, left) ;
            right = transformOpAlways(exprs, right) ;
            
            Op op2 = OpUnion.create(left, right) ;
            return result(op2, emptyList) ;
        }
        
        Op left = input.getLeft() ;
        Placement pLeft = transform(exprs, left) ;
        
        Op right = input.getRight() ;
        Placement pRight = transform(exprs, right) ;
        
        // If it's placed in neither arm it should be passed back out for placement.
        //
        // If it's done in both arms, then expression can be left pushed in
        // and not passed back out for placement.
        
        // If it is done in one arm and not the other, then it can be left pushed
        // in but needs to be redone for the other arm as if it were no placed at all.

        // A filter applied twice is safe.
        
        ExprList exprs2 = null ;
        
        for ( Expr expr : exprs ) {
            boolean unplacedLeft =  ( isNoChange(pLeft)  || pLeft.unplaced.getList().contains(expr) ) ;
            boolean unplacedRight = ( isNoChange(pRight) || pRight.unplaced.getList().contains(expr) ) ;
            
//            if ( unplacedLeft && unplacedRight ) {
//                System.out.println("Unplaced:     "+expr) ;
//            } else if ( unplacedLeft ) {
//                System.out.println("Unplaced(L):  "+expr) ;
//            } else if ( unplacedRight ) {
//                System.out.println("Unplaced(R):  "+expr) ;
//            } else
//                System.out.println("Placed(L+R):  "+expr) ;
            
            boolean placed = !unplacedLeft && !unplacedRight ;
            if ( placed )
                // Went into both arms - expression has been handled completely.
                continue ;
            
            if ( exprs2 == null )
                exprs2 = new ExprList() ;
            exprs2.add(expr) ;
        }
        
        Op newLeft = (pLeft == null ) ? left : pLeft.op ;
        Op newRight = (pRight == null ) ? right : pRight.op ;
        if ( exprs2 == null )
            exprs2 = emptyList ;
        
        //Op op2 = OpUnion.create(newLeft, newRight) ;
        Op op2 = input.copy(newLeft, newRight) ;
        return result(op2, exprs2) ;
    }

    private Placement placeDisjunction(ExprList exprs, OpDisjunction input) {
        // Do on each arm.
        // better (neater) would be to pass out exprs not placed anywhere. 
        // Combine with union.
        
        if ( false ) {
            // Push everything, always
            // Left as a safty fall back.
            List<Op> x = new ArrayList<>() ;
            input.getElements().forEach(op->{
                Placement p = transform(exprs, op) ;
                if ( isNoChange(p) ) {
                    x.add(buildFilter(exprs, op)) ;
                } else {
                    Op op1 = buildFilter(p) ;
                    x.add(op1) ;
                }
            });
            return result(input.copy(x), emptyList) ; 
        }

        // Don't push any expressions that aren't used in any of the arms of the disjunction.
        // This is more about being tidy.
        List<Expr> unplaced = new ArrayList<>(exprs.getList()) ;
        //List<Placement> x = input.getElements().stream().map(op->transform(exprs, op)).collect(Collectors.toList()) ;
        List<Placement> placements = new ArrayList<>(exprs.size()) ;
        Boolean someChange = Boolean.FALSE ; 
        for ( Op op : input.getElements() ) {
            Placement p = transform(exprs, op) ;
            if ( isChange(p) ) {
                unplaced.retainAll(p.unplaced.getList()) ;
                someChange = Boolean.TRUE ; 
            } else
                p = result(op, exprs) ; 
            placements.add(p) ;
        };

        if ( ! someChange )
            return noChangePlacement ;

        List<Expr> retained = new ArrayList<>(exprs.getList()) ;
        retained.removeAll(unplaced) ;

        // Mutate placements to remove the expres going outside.
        List<Op> ops = new ArrayList<>(input.size()) ;
        for ( Placement p : placements ) {
            // No "noChange" at this point.
            p.unplaced.getListRaw().removeAll(unplaced) ;
            ops.add(buildFilter(p)) ;
        } ;
        return result(input.copy(ops), new ExprList(unplaced)) ; 
    }

    private Placement placeExtend(ExprList exprs, OpExtend input) {
        return processExtendAssign(exprs, input) ;
    }
    
    private Placement placeAssign(ExprList exprs, OpAssign input) {
        return processExtendAssign(exprs, input) ;
    }
    
    /** Try to optimize (filter (extend ...)) , (filter (let ...)) */
    private Placement processExtendAssign(ExprList exprs, OpExtendAssign input) {
        // We assume that each (extend) and (assign) is usually in simple form -
        // always one assignment. We cope with the general form (multiple
        // assignments) but do not attempt reordering of assignments.

        // There are three cases:
        // 1 - expressions that can be pushed into the subop.
        // 2 - expressions that are covered when the extend/assign has applied. [wrapping]
        // 3 - expressions that are not covered even at the outermost level. [unplaced]
        
        List<Var> vars1 = input.getVarExprList().getVars() ;
        Op subOp = input.getSubOp() ;
        
        // Case 1 : Do as much inner placement as possible.
        ExprList remaining = exprs ;
        Placement p = transform(exprs, input.getSubOp()) ;
        if ( isChange(p) ) {
            subOp = p.op ;
            remaining = p.unplaced ;
        }
        
        // Case 2 : wrapping
        // Case 3 : unplaced
        
        // Variables in subop and introduced by (extend)/(assign)
        Set<Var> subVars = OpVars.fixedVars(subOp) ;
        subVars.addAll(input.getVarExprList().getVars()) ;
        
        ExprList wrapping = new ExprList() ; 
        ExprList unplaced = new ExprList() ;
            
        for ( Expr expr : remaining ) {
            Set<Var> exprVars = expr.getVarsMentioned() ;
            if ( subVars.containsAll(exprVars) )
                wrapping.add(expr) ;
            else
                unplaced.add(expr) ;
        }
        
        Op result = input.copy(subOp) ;
        if ( ! wrapping.isEmpty() )
            result = OpFilter.filterBy(wrapping, result) ;
        return result(result, unplaced) ; 
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
        return processSubOp1(pushed, unpushed, input) ;
    }

    // For a modifier without expressions (distinct, reduced), we could
    // push that inside the modifier if that were all there was.  But the 
    // expressions may be processed elsewhere in the overall algebra.
    // Putting them inside the modifier would lock them here as they don't
    // get returned in the Placement as "unplaced."  
    
    // This is the cause of JENA-874.
   
    /* Complete processing for an Op1. 
     * Having split expressions into pushed and unpushed at this point,
     * try to push "pushed" down further into the subOp.
     */  
    private Placement processSubOp1(ExprList pushed, ExprList unpushed, Op1 input) {
        Op opSub = input.getSubOp() ;
        Placement subPlacement = transform(pushed, opSub) ;
        if ( isNoChange(subPlacement) ) {
            // (Same as if a placement of the exprlist and op passed in is given).
            // Didn't make any changes below, so add a filter for the 'pushed' and
            // return a placement for the unpushed. 
            Op op1 = input.getSubOp() ;
            if ( pushed != null &&! pushed.isEmpty() )
                op1 = OpFilter.filterBy(pushed, op1) ;
            Op op2 = input.copy(op1) ;
            return result(op2, unpushed) ;
        }
        // We did make changes below.  Add filter for these (which includes the 
        // "pushed" at this level, now in the p.op or left in p.unplaced.
        Op op_a = OpFilter.filterBy(subPlacement.unplaced, subPlacement.op) ;
        op_a =  input.copy(op_a) ;
        return result(op_a, unpushed) ;
    }

    private Placement placeDistinctReduced(ExprList exprs, OpDistinctReduced input) {
        Op subOp = input.getSubOp() ;
        Placement p = transform(exprs, subOp) ;

        if ( isNoChange(p) )
            // No effect - we do not manage to make a change.
            return resultNoChange(input) ;

        // Rebuild.
        // We managed to place at least some expressions.
        Op op = p.op ;
        // Put back distinct/reduced
        op = input.copy(op) ;
        // Return with unplaced filters. 
        return result(op, p.unplaced) ;
    }
    
    private Placement placeTable(ExprList exprs, OpTable input) {
        exprs = ExprList.copy(exprs) ;
        Op op = insertAnyFilter$(exprs, input.getTable().getVars(), input) ;
        return result(op, exprs) ;
    }

    private Set<Var> fixedVars(Op op) {
        return OpVars.fixedVars(op) ;
    }

    /** For any expression now in scope, wrap the op with a filter. */
    
    private static Op insertAnyFilter$(ExprList unplacedExprs, Collection<Var> patternVarsScope, Op op) {
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
        return Collections.disjoint(collection, possibleElts);
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

        for ( Expr expr : exprs ) {
            if ( op == null )
                op = OpTable.unit() ;
            op = OpFilter.filter(expr, op) ;
        }
        return op ;
    }
}
