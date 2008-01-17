/*
 * (c) Copyright 2006, 2007, 2008 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sdb.compiler;

import static com.hp.hpl.jena.sdb.iterator.SetUtils.intersection;
import static com.hp.hpl.jena.sdb.iterator.Stream.filter;
import static com.hp.hpl.jena.sdb.iterator.Stream.map;
import static com.hp.hpl.jena.sdb.iterator.Stream.toSet;

import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hp.hpl.jena.sdb.core.AliasesSql;
import com.hp.hpl.jena.sdb.core.SDBRequest;
import com.hp.hpl.jena.sdb.core.ScopeEntry;
import com.hp.hpl.jena.sdb.core.sqlnode.SqlNode;
import com.hp.hpl.jena.sdb.core.sqlnode.SqlSelectBlock;
import com.hp.hpl.jena.sdb.iterator.Iter;
import com.hp.hpl.jena.sdb.layout2.expr.RegexCompiler;
import com.hp.hpl.jena.sdb.shared.SDBInternalError;
import com.hp.hpl.jena.sparql.algebra.Op;
import com.hp.hpl.jena.sparql.algebra.TransformCopy;
import com.hp.hpl.jena.sparql.algebra.op.*;
import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.sparql.expr.Expr;
import com.hp.hpl.jena.sparql.expr.ExprList;

public class TransformSDB extends TransformCopy
{
    private static Log log = LogFactory.getLog(TransformSDB.class) ;
    private SDBRequest request ;
    private QuadBlockCompiler quadBlockCompiler ;
    //private boolean doLeftJoin = true ;
    
    public TransformSDB(SDBRequest request, QuadBlockCompiler quadBlockCompiler) 
    {
        this.request = request ;
        this.quadBlockCompiler = quadBlockCompiler ;
        //this.genTableAlias = request.generator(AliasesSql.SelectBlock) ;
    }
    
    @Override
    public Op transform(OpBGP opBGP)
    { throw new SDBInternalError("OpBGP should not appear") ; }

    @Override
    public Op transform(OpQuadPattern quadPattern)
    {
        QuadBlock qBlk = new QuadBlock(quadPattern) ;
        SqlNode node = quadBlockCompiler.compile(qBlk) ;
        return new OpSQL(node, quadPattern, request) ; 
    }

    @Override
    public Op transform(OpJoin opJoin, Op left, Op right)
    {
        if ( ! QC.isOpSQL(left) || ! QC.isOpSQL(right) )
            return super.transform(opJoin, left, right) ;
        
        SqlNode sqlLeft = ((OpSQL)left).getSqlNode() ;
        SqlNode sqlRight = ((OpSQL)right).getSqlNode() ;
        return new OpSQL(SqlBuilder.innerJoin(request, sqlLeft, sqlRight), opJoin, request) ;
    }

    @Override
    public Op transform(OpLeftJoin opJoin, Op left, Op right)
    {
        if ( ! request.LeftJoinTranslation )
            return super.transform(opJoin, left, right) ;
        
        if ( ! QC.isOpSQL(left) || ! QC.isOpSQL(right) )
            return super.transform(opJoin, left, right) ;

        // Condition(s) in the left join.  Punt for now. 
        if ( opJoin.getExprs() != null )
            return super.transform(opJoin, left, right) ;
        
        SqlNode sqlLeft = ((OpSQL)left).getSqlNode() ;
        SqlNode sqlRight = ((OpSQL)right).getSqlNode() ;
        
        // Check for coalesce.
        // Do optional variables on the right appear only as optional variables on the left?

        Set<ScopeEntry> scopes = sqlLeft.getIdScope().findScopes() ;
        
        // Find optional-on-left
        Set<ScopeEntry> scopes2 = toSet(filter(scopes, ScopeEntry.OptionalFilter)) ;
        Set<Var> leftOptVars = toSet(map(scopes2, ScopeEntry.ToVar)) ;              // Vars from left optionals.
        
        if ( false )
        {
            Iter<ScopeEntry> iter = Iter.iter(scopes) ;
            Set<Var> leftOptVars_ = iter.filter(ScopeEntry.OptionalFilter).map(ScopeEntry.ToVar).toSet() ;
        }
        
        // Find optional-on-right (easier - it's all variables) 
        Set<Var> rightOptVars = sqlRight.getIdScope().getVars() ;
        
        // And finally, calculate the intersection of the two.
        // SetUtils extension - one side could be an iterator  
        Set<Var> coalesceVars = intersection(leftOptVars, rightOptVars) ;
        
        // Future simplification : LeftJoinClassifier.nonLinearVars 
//        if ( ! coalesceVars.equals(LeftJoinClassifier.nonLinearVars( opJoin.getLeft(), opJoin.getRight() )) )
//        { unexpected }
        
        if ( coalesceVars.size() > 0  ) 
        {
            String alias = request.genId(AliasesSql.CoalesceAliasBase) ;
            SqlNode sqlNode = SqlBuilder.leftJoinCoalesce(request, alias,
                                                  sqlLeft, sqlRight, 
                                                  coalesceVars) ;
            return new OpSQL(sqlNode, opJoin, request) ;
            
            // Punt
            //return super.transform(opJoin, left, right) ;
        }
        return new OpSQL(SqlBuilder.leftJoin(request, sqlLeft, sqlRight, null), opJoin, request) ;
    }
    
    @Override
    public Op transform(OpFilter opFilter, Op op)
    {
//        SDBConstraint constraint = transformFilter(opFilter) ;
//        if ( constraint != null )
//            log.info("recognized: "+opFilter.getExprs()) ;
        return super.transform(opFilter, op) ;
    }
    
    @Override
    public Op transform(OpTable opTable)
    {
        if ( ! opTable.isJoinIdentity())
            log.fatal("OpTable : Not join identity") ;
        //return new OpSQL(null, opUnit, request) ;
        return super.transform(opTable) ;
    }
    
    // Modifiers: the structure is:
    //    slice
    //      distinct/reduced
    //        project
    //          order
    //            having
    //              group
    //                [toList]

    // modifier : having
    // modifier : group
  
    // ---- Modifiers
    
    @Override
    public Op transform(OpDistinct opDistinct, Op subOp)
    { 
        // TODO (distinct (project SQL)) 
        if ( ! QC.isOpSQL(subOp) )
            return super.transform(opDistinct, subOp) ;
        SqlNode sqlSubOp = ((OpSQL)subOp).getSqlNode() ;
        SqlNode n = SqlSelectBlock.distinct(sqlSubOp) ;
        return new OpSQL(n, opDistinct, request) ; 
    }
    
    @Override
    public Op transform(OpSlice opSlice, Op subOp)
    {
        if ( ! request.LimitOffsetTranslation )
            return super.transform(opSlice, subOp) ;
        
        // Two cases are currently handled:
        // (slice (sql expression))
        // (slice (project ... (sql expression)))
        
        boolean canHandle = false ;
        
        // Relies on the fact that isOpSQL(null) is false.
        if (  QC.isOpSQL(subOp) )
            canHandle = true ;
        else if ( QC.isOpSQL(sub(asProject(subOp))) )
        {
            return transformSliceProject(opSlice, (OpProject)subOp) ;
        }

        // Simple slice
        if ( ! QC.isOpSQL(subOp) )
            return super.transform(opSlice, subOp) ;

        return transformSlice(opSlice, ((OpSQL)subOp).getSqlNode()) ;
    }
        
    private Op transformSlice(OpSlice opSlice, SqlNode sqlSubOp)
    {
        SqlNode n = SqlSelectBlock.slice(sqlSubOp, opSlice.getStart(), opSlice.getLength()) ;
        Op x = new OpSQL(n, opSlice, request) ;
        return x ;
    }
    
    public Op transformSliceProject(OpSlice opSlice, OpProject opProject)
    {
        // (slice (project X))
        Op subOp = opProject.getSubOp() ;
        
        if ( ! QC.isOpSQL(subOp) )
            // Can't cope - just pass the slice to the general superclass. 
            return super.transform(opSlice, opProject) ;

        SqlNode sqlSubOp = ((OpSQL)subOp).getSqlNode() ;
        
        @SuppressWarnings("unchecked")
        List<Var> pv = opProject.getVars() ;
        
        // Do as (slice X)
        SqlNode n = SqlSelectBlock.slice(sqlSubOp, opSlice.getStart(), opSlice.getLength()) ;
        // Put back project - as an OpProject to leave for the bridge.
        Op x = new OpSQL(n, opProject, request) ;
        return new OpProject(x, pv) ;
    }

    // ----
    
    private boolean translateConstraints = true ;
    
    private SDBConstraint transformFilter(OpFilter opFilter)
    {
        if ( ! translateConstraints )
            return null ;
        
        ExprList exprs = opFilter.getExprs() ;
        @SuppressWarnings("unchecked")
        List<Expr> x = exprs.getList() ;
        for ( Expr  expr : x )
        {
            ConditionCompiler cc = new RegexCompiler() ;
            SDBConstraint psc = cc.recognize(expr) ;
            if ( psc != null )
                return psc ; 
        }
        return null ;
    }

    private Set<Var> getVarsInFilter(Expr expr)
    {
        @SuppressWarnings("unchecked")
        Set<Var> vars = expr.getVarsMentioned() ;
        return vars ;
    }
    
    // ---- Misc
    // Will migrate to ARQ.OpLib
    
    public static Op sub(Op1 op) { return op==null ? null : op.getSubOp() ; }
    
    public static boolean isProject(Op op) { return op instanceof OpProject ; } 
    public static OpProject asProject(Op op)
    {  return isProject(op) ? (OpProject)op : null ; }

    public static boolean isDistinct(Op op) { return op instanceof OpDistinct ; } 
    public static OpDistinct asDistinct(Op op)
    {  return isDistinct(op) ? (OpDistinct)op : null ; }

    public static boolean isReduced(Op op) { return op instanceof OpReduced ; } 
    public static OpReduced asReduced(Op op)
    {  return isReduced(op) ? (OpReduced)op : null ; }

    public static boolean isOrder(Op op) { return op instanceof OpOrder ; } 
    public static OpOrder asOrder(Op op)
    {  return isOrder(op) ? (OpOrder)op : null ; }

    public static boolean isSlice(Op op) { return op instanceof OpSlice ; } 
    public static OpSlice asSlice(Op op)
    {  return isSlice(op) ? (OpSlice)op : null ; }
}

/*
 * (c) Copyright 2006, 2007, 2008 Hewlett-Packard Development Company, LP
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