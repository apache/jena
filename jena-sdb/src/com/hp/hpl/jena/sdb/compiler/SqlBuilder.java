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

package com.hp.hpl.jena.sdb.compiler;

import static com.hp.hpl.jena.sdb.core.JoinType.INNER;
import static com.hp.hpl.jena.sdb.core.JoinType.LEFT;

import java.util.Collection;
import java.util.Set;

import com.hp.hpl.jena.sparql.core.Var;

import com.hp.hpl.jena.sdb.core.JoinType;
import com.hp.hpl.jena.sdb.core.SDBRequest;
import com.hp.hpl.jena.sdb.core.ScopeEntry;
import com.hp.hpl.jena.sdb.core.sqlexpr.*;
import com.hp.hpl.jena.sdb.core.sqlnode.*;
import com.hp.hpl.jena.sdb.shared.SDBInternalError;

public class SqlBuilder
{
    static public SqlNode distinct(SDBRequest request, SqlNode sqlNode)
    { return SqlSelectBlock.distinct(request, sqlNode) ; }

    static public SqlNode slice(SDBRequest request, SqlNode sqlNode, long start, long length)
    { return SqlSelectBlock.slice(request, sqlNode, start, length) ; }

    static public SqlNode project(SDBRequest request, SqlNode sqlNode, Collection<ColAlias> cols)
    { return SqlSelectBlock.project(request, sqlNode, cols) ; }
    
    static public SqlNode project(SDBRequest request, SqlNode sqlNode, ColAlias col)
    { return SqlSelectBlock.project(request, sqlNode, col) ; }

    static public SqlNode view(SDBRequest request, SqlNode sqlNode)
    { return SqlSelectBlock.view(request, sqlNode) ; }
    
    static public SqlNode restrict(SDBRequest request, SqlNode sqlNode, SqlExprList conditions)
    {
        for ( SqlExpr e : conditions )
            sqlNode = SqlSelectBlock.restrict(request, sqlNode, e) ;
        return sqlNode ;
    }
    
    static public SqlNode restrict(SDBRequest request, SqlNode sqlNode, SqlExpr expr)
    {
        if ( sqlNode.isInnerJoin() )
        {
            sqlNode.asInnerJoin().addCondition(expr) ;
            return sqlNode ;
        }
        return SqlSelectBlock.restrict(request, sqlNode, expr) ;
    }
    
    // -----  Making join nodes
    
    public static SqlNode innerJoin(SDBRequest request, SqlNode left, SqlNode right)
    {
        if ( left == null )
            return right ; 
        
        return join(request, INNER, left, right, null) ; 
    }

    public static SqlNode leftJoin(SDBRequest request, SqlNode left, SqlNode right, SqlExpr expr)
    {
        if ( left == null )
            throw new SDBInternalError("Attempt to leftJoin to null") ;
        SqlJoin j = join(request, LEFT, left, right, null) ;
        if ( expr != null )
            j.addCondition(expr) ;
        return j ;
    }
    
    public static SqlNode leftJoinCoalesce(SDBRequest request, String alias,
                                           SqlNode left, SqlNode right,
                                           Set<Var> coalesceVars)
    {
        SqlJoin sqlJoin = join(request, LEFT, left, right, coalesceVars) ;
        return SqlCoalesce.create(request, alias, sqlJoin, coalesceVars) ;
    }
    
//    private static String sqlNodeName(SqlNode sNode)
//    {
//        if ( sNode == null )            return "<null>" ;
//        if ( sNode.isProject() )        return "Project" ;
//        if ( sNode.isRestrict() )       return "Restrict/"+sqlNodeName(sNode.asRestrict().getSubNode()) ;
//        if ( sNode.isTable() )          return "Table" ;
//        if ( sNode.isInnerJoin() )      return "JoinInner" ;
//        if ( sNode.isLeftJoin() )       return "Joinleft" ;
//        if ( sNode.isCoalesce() )       return "Coalesce" ;
//        return "<unknown>" ;
//    }
    
    // Join/LeftJoin two subexpressions, calculating the join conditions in the process
    // If a coalesce (LeftJoin) then don't equate left and right vars of the same name.
    // A SqlCoalesce is a special case of LeftJoin where ignoreVars!=null
    
    private static SqlJoin join(SDBRequest request, 
                                JoinType joinType, 
                                SqlNode left, SqlNode right,
                                Set<Var> ignoreVars)
    {
        SqlExprList conditions = new SqlExprList() ;

        if ( joinType == INNER )
            // Put any left filter into the join conditions.
            // Does not apply to LEFT because the LHS filter does not apply to the right in the same way. 
            left = extractRestrict(left, conditions) ;

        right = extractRestrict(right, conditions) ;
        
        for ( Var v : left.getIdScope().getVars() )
        {
            if ( right.getIdScope().hasColumnForVar(v) )
            {
                ScopeEntry sLeft = left.getIdScope().findScopeForVar(v) ;
                ScopeEntry sRight = right.getIdScope().findScopeForVar(v) ;
                
                SqlExpr c = joinCondition(joinType, sLeft, sRight) ;
                conditions.add(c) ;
                c.addNote("Join var: "+v) ; 
            }
        }
        
        SqlJoin join = SqlJoin.create(joinType, left, right) ;
        join.addConditions(conditions) ;
        return join ;
    }
    
    private static SqlExpr joinCondition(JoinType joinType, ScopeEntry sLeft, ScopeEntry sRight)
    {
        SqlExpr c = null ;
        SqlColumn leftCol = sLeft.getColumn() ;
        SqlColumn rightCol = sRight.getColumn() ;
        
        // SPARQL join condition is join if "undef or same"
        // Soft null handling : need to insert "IsNull OR"
        // if the column can be a null.
        // The order of the OR conditions matters.
        
        if ( sLeft.isOptional() )
            c = makeOr(c, new S_IsNull(leftCol)) ;
        
        if ( sRight.isOptional() )
            c = makeOr(c, new S_IsNull(rightCol)) ;
        
        c = makeOr(c, new S_Equal(leftCol, rightCol)) ;
        return c ;
    }
   // ---- Expressions
    
    private static SqlExpr makeOr(SqlExpr c, SqlExpr expr)
    {
        if ( c == null )
            return expr ;
       
        return new S_Or(c, expr) ;
    }

    private static SqlExpr makeAnd(SqlExpr c, SqlExpr expr)
    {
        if ( c == null )
            return expr ;
       
        return new S_And(c, expr) ;
    }

    private static SqlNode extractRestrict(SqlNode sqlNode, SqlExprList conditions)
    {
        // SqlSelectBlocks as simple restrictions.
        
        if ( sqlNode.isSelectBlock() )
        {
            SqlSelectBlock block = sqlNode.asSelectBlock() ;
            if ( block.getDistinct() )
                return sqlNode ;
            if ( block.hasSlice() )
                return sqlNode ;
            // If a restriction of a table.
            if ( block.getSubNode().isTable() )
            {
                SqlTable t = block.getSubNode().asTable() ;
                conditions.addAll(block.getConditions()) ;
                return t ;
            }
        }
        return sqlNode ;
        
//        if ( ! sqlNode.isRestrict() ) 
//            return sqlNode ;
//        
//        SqlRestrict restrict = sqlNode.asRestrict() ;
//        SqlNode subNode = restrict.getSubNode() ;
//        if ( ! subNode.isTable() && ! subNode.isInnerJoin() )
//            return sqlNode ;
//        conditions.addAll(restrict.getConditions()) ;
//        subNode.addNotes(restrict.getNotes()) ;
//        return subNode ;
    }
}
