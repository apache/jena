/*
 * (c) Copyright 2006, 2007 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sdb.engine.compiler;

import static com.hp.hpl.jena.sdb.util.SetUtils.convert;
import static com.hp.hpl.jena.sdb.util.SetUtils.filter;
import static com.hp.hpl.jena.sdb.util.SetUtils.intersection;

import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hp.hpl.jena.query.algebra.Op;
import com.hp.hpl.jena.query.algebra.op.*;
import com.hp.hpl.jena.query.core.Var;
import com.hp.hpl.jena.query.expr.Expr;
import com.hp.hpl.jena.query.expr.ExprList;
import com.hp.hpl.jena.sdb.SDBException;
import com.hp.hpl.jena.sdb.core.*;
import com.hp.hpl.jena.sdb.core.sqlnode.SqlNode;


public class TransformSDB extends TransformCopy
{
    private static Log log = LogFactory.getLog(TransformSDB.class) ;
    private SDBRequest request ;
    private QuadBlockCompiler quadBlockCompiler ;
    //private boolean doLeftJoin = true ;
    
    private Generator genCoalesceAlias = Gensym.create(Aliases.CoalesceAliasBase) ;
    
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

        // --- Algorithm -- inspired by http://jga.sourceforge.net/
        // Except we want it working on Sets. 
        
        Set<ScopeEntry> scopes = sqlLeft.getIdScope().findScopes() ;
        Set<ScopeEntry> scopes2 = filter(scopes, ScopeEntry.OptionalFilter) ;

        Set<Var> leftOptVars = convert(scopes2, ScopeEntry.ToVar) ;             // Vars from left optionals.
        
        Set<Var> rightOptVars = sqlRight.getIdScope().getVars() ;               // Why is this the opt vars?
        Set<Var> coalesceVars = intersection(leftOptVars, rightOptVars) ;
        
        // Future simplification : LeftJoinClassifier.nonLinearVars 
//        if ( ! coalesceVars.equals(LeftJoinClassifier.nonLinearVars( opJoin.getLeft(), opJoin.getRight() )) )
//        { unexpected }
        
        if ( coalesceVars.size() > 0  ) 
        {
            SqlNode sqlNode = QC.leftJoinCoalesce(request, genCoalesceAlias.next(),
                                                  sqlLeft, sqlRight, coalesceVars) ;
            return new OpSQL(sqlNode, opJoin, request) ;
            
            // Punt
            //return super.transform(opJoin, left, right) ;
        }
        return new OpSQL(QC.leftJoin(request, sqlLeft, sqlRight), opJoin, request) ;
    }
    
    @Override
    public Op transform(OpFilter opFilter, Op op)
    {
        return super.transform(opFilter, op) ;
    }
    
    @Override
    public Op transform(OpUnit opUnit)
    {
        //return new OpSQL(null, opUnit, request) ;
        return super.transform(opUnit) ;
    }
    
    private boolean translateConstraints = false ;
    
    
    private SDBConstraint transformFilter(OpFilter opFilter)
    {
        if ( ! translateConstraints )
            return null ;
        
        ExprList exprs = opFilter.getExprs() ;
        @SuppressWarnings("unchecked")
        List<Expr> x = (List<Expr>)exprs.getList() ;
        for ( Expr  expr : x )
        {
            ConditionCompiler cc = null ;
            SDBConstraint psc = cc.recognize(expr) ;
            // Maybe null (not recognized)
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