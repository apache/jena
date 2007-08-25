/*
 * (c) Copyright 2006, 2007 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sdb.compiler;

import static com.hp.hpl.jena.sdb.iterator.Streams.filter;
import static com.hp.hpl.jena.sdb.iterator.Streams.map;
import static com.hp.hpl.jena.sdb.iterator.Streams.toSet;
import static com.hp.hpl.jena.sdb.iterator.SetUtils.intersection ;

import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hp.hpl.jena.sdb.SDBException;
import com.hp.hpl.jena.sdb.core.AliasesSql;
import com.hp.hpl.jena.sdb.core.SDBRequest;
import com.hp.hpl.jena.sdb.core.ScopeEntry;
import com.hp.hpl.jena.sdb.core.sqlnode.SqlNode;
import com.hp.hpl.jena.sdb.core.sqlnode.SqlSlice;
import com.hp.hpl.jena.sdb.iterator.Iter;
import com.hp.hpl.jena.sdb.layout2.expr.RegexCompiler;
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
    }
    
    @Override
    public Op transform(OpBGP opBGP)
    { throw new SDBException("OpBGP should not appear") ; }

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
        return new OpSQL(QC.innerJoin(request, sqlLeft, sqlRight), opJoin, request) ;
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
            SqlNode sqlNode = QC.leftJoinCoalesce(request, alias,
                                                  sqlLeft, sqlRight, 
                                                  coalesceVars) ;
            return new OpSQL(sqlNode, opJoin, request) ;
            
            // Punt
            //return super.transform(opJoin, left, right) ;
        }
        return new OpSQL(QC.leftJoin(request, sqlLeft, sqlRight), opJoin, request) ;
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
    //      project
    //        order
    //          [toList]
    
    
    @Override
    public Op transform(OpSlice opSlice, Op subOp)
    {
        if ( ! request.LimitOffsetTranslation )
            return super.transform(opSlice, subOp) ;
        
        List<Var> project = null ; 
        
        // Either OpSlice(SQL) or OpSlice(OpProject(SQL))
        // Note: OpSlice(OpProject(SQL)) => OpProject(OpSlice(SQL))
        //  iff no other modifiers

        if ( subOp instanceof OpProject )
        {
            OpProject p = (OpProject)subOp ;
            @SuppressWarnings("unchecked")
            List<Var> pv = (List<Var>)p.getVars() ;
            project = pv ;
            subOp = ((OpProject)subOp).getSubOp() ;
        }
        
        if ( ! QC.isOpSQL(subOp) )
            return super.transform(opSlice, subOp) ; 
        
        SqlNode sqlSubOp = ((OpSQL)subOp).getSqlNode() ;
        SqlNode sqlSlice = new SqlSlice(sqlSubOp, opSlice.getStart(), opSlice.getLength()) ;
        
        Op x = new OpSQL(sqlSlice, opSlice, request) ;
        if ( project != null )
            x = new OpProject(x, project) ;
        return x ;
    }
    
    private boolean translateConstraints = true ;
    
    private SDBConstraint transformFilter(OpFilter opFilter)
    {
        if ( ! translateConstraints )
            return null ;
        
        ExprList exprs = opFilter.getExprs() ;
        @SuppressWarnings("unchecked")
        List<Expr> x = (List<Expr>)exprs.getList() ;
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
        Set<Var> vars = (Set<Var>)expr.getVarsMentioned() ;
        return vars ;
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