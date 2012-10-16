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

package com.hp.hpl.jena.sdb.layout2;

import com.hp.hpl.jena.sdb.compiler.OpSQL ;
import com.hp.hpl.jena.sdb.compiler.QueryCompilerMain ;
import com.hp.hpl.jena.sdb.compiler.SDB_QC ;
import com.hp.hpl.jena.sdb.core.SDBRequest ;
import com.hp.hpl.jena.sdb.core.ScopeEntry ;
import com.hp.hpl.jena.sdb.core.sqlexpr.S_Like ;
import com.hp.hpl.jena.sdb.core.sqlexpr.SqlColumn ;
import com.hp.hpl.jena.sdb.core.sqlnode.SqlNode ;
import com.hp.hpl.jena.sdb.core.sqlnode.SqlSelectBlock ;
import com.hp.hpl.jena.sdb.util.RegexUtils ;
import com.hp.hpl.jena.sparql.algebra.Op ;
import com.hp.hpl.jena.sparql.algebra.Transform ;
import com.hp.hpl.jena.sparql.algebra.TransformCopy ;
import com.hp.hpl.jena.sparql.algebra.Transformer ;
import com.hp.hpl.jena.sparql.algebra.op.OpFilter ;
import com.hp.hpl.jena.sparql.core.Var ;
import com.hp.hpl.jena.sparql.expr.E_Regex ;
import com.hp.hpl.jena.sparql.expr.Expr ;
import com.hp.hpl.jena.sparql.expr.ExprList ;

// Common processing for all layout2 forms. 
public abstract class QueryCompiler2 extends QueryCompilerMain
{
    
    public QueryCompiler2(SDBRequest request)
    { 
        super(request) ;
    }
    
    @Override
    protected Op postProcessSQL(Op op)
    {
        // Modifiers: the structure is:
        //    slice
        //      distinct/reduced
        //        project
        //          order
        //            having
        //              group
        //                [toList]
        
        // Look for certain structures we can do especially well.
        // (slice (filter regex (...)))
        // (filter regex)
        // Must be done filter changes then limit/offset changes.
        
        // Looks for (filter ... (sql ...))
        op = rewriteFilters(op, request) ;

        // (slice (distinct ...)) done as two steps. 
        
        // (distinct ....
        op = rewriteDistinct(op, request) ;

        // Look for (slice ...) or (slice (project ...))
        op = rewriteLimitOffset(op, request) ;
        
        return op ;
    }

    // See if theer are any filter operations we can move all or part of into SQL.
    protected static Op rewriteFilters(Op op, SDBRequest request)
    {
        Transform t = new FilterOptimizer(request) ;
        return Transformer.transform(t, op) ;
    }
    
    // Find variables that need to be returned. 
    private static class FilterOptimizer extends TransformCopy
    {
        private final SDBRequest request ;

        public FilterOptimizer(SDBRequest request)
        { this.request = request ; }

        @Override
        public Op transform(OpFilter opFilter, Op op)
        {
            if ( ! SDB_QC.isOpSQL(op) )
                return super.transform(opFilter, op) ;
            SqlNode sqlNode = ((OpSQL)op).getSqlNode() ;
            if ( ! sqlNode.isSelectBlock() )
                return super.transform(opFilter, op) ;
            
            SqlSelectBlock ssb = sqlNode.asSelectBlock() ;
            
            ExprList exprs = opFilter.getExprs() ;
            ExprList exprs2 =  new ExprList() ;

            for ( Object obj : exprs.getList() )
            {
                Expr expr = (Expr)obj ;
                Expr expr2 = convert(expr, ssb, (OpSQL)op) ;
                // Not all filter conversions are complete - only a partial constraint
                // may have been added to the SQL meaning the expr must still be done
                // in ARQ to get exact semantic but hopefully on much less data
                
                if ( expr2 != null )
                    exprs2.add(expr2) ;
            }

            return OpFilter.filter(exprs2, op) ; 
        }

        // return null for don't need an expr anymore.
        private Expr convert(Expr expr, SqlSelectBlock ssb, OpSQL opSQL)
        {
            if ( ! ( expr instanceof E_Regex ) )
                return expr ;
            E_Regex regex = (E_Regex)expr ;
            
            Expr exprPattern = regex.getArg(2) ;
            Expr exprFlags = ( regex.getArg(3) == null ? null : regex.getArg(3) ) ;
            
            boolean caseInsensitive = false ;
            
            // Any flags?
            if ( exprFlags != null ) 
            {
                // Expression for flags?
                if ( ! exprFlags.isConstant() ) return expr ;
                String flags = exprFlags.getConstant().asString() ;
                if ( ! "i".equals(flags) ) return expr ;
                caseInsensitive = true ;
            }
            
            // Check pattern - must be a constant string
            if ( !exprPattern.isConstant() ) return expr ;
            String pattern =  exprPattern.getConstant().asString() ;
            if ( pattern == null ) return expr ;
            
            // See if it's LIKEable.
            String patternLike = RegexUtils.regexToLike(pattern) ;
            if ( patternLike == null )
                return expr ;
            
            // Check target
            // BUG in old ARQ getRegexExpr() returns the wrong thing.
            if ( ! regex.getArg(1).isVariable() ) return expr  ;
            Var v = regex.getArg(1).asVar() ;

            // Scope check
            if ( ! ssb.getNodeScope().hasColumnForVar(v) )
                // Probably out of scope - or just not in the graph apttern. 
                return expr ;

            // Find the column for this variable.
            ScopeEntry sc = ssb.getNodeScope().findScopeForVar(v) ;
            // Get the lexical column : this is layout2 specific.
            SqlColumn col = new SqlColumn(sc.getColumn().getTable(), "lex") ;
            
            S_Like sreg = new S_Like(col, patternLike, caseInsensitive) ;
            ssb.getConditions().add(sreg) ;
            
            // Completely replace the filter - so remove from OpFilter.
            return null ;
        }
    }
    

}
