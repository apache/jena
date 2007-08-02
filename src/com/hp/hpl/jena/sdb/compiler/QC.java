/*
 * (c) Copyright 2006, 2007 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sdb.compiler;

import static com.hp.hpl.jena.sdb.core.JoinType.INNER;
import static com.hp.hpl.jena.sdb.core.JoinType.LEFT;

import java.sql.SQLException;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.SortCondition;
import com.hp.hpl.jena.sdb.SDB;
import com.hp.hpl.jena.sdb.core.JoinType;
import com.hp.hpl.jena.sdb.core.SDBRequest;
import com.hp.hpl.jena.sdb.core.ScopeEntry;
import com.hp.hpl.jena.sdb.core.sqlexpr.*;
import com.hp.hpl.jena.sdb.core.sqlnode.SqlCoalesce;
import com.hp.hpl.jena.sdb.core.sqlnode.SqlJoin;
import com.hp.hpl.jena.sdb.core.sqlnode.SqlNode;
import com.hp.hpl.jena.sdb.core.sqlnode.SqlRestrict;
import com.hp.hpl.jena.sdb.sql.SDBExceptionSQL;
import static com.hp.hpl.jena.sdb.util.Alg.* ;
import com.hp.hpl.jena.sdb.util.alg.Transform;
import com.hp.hpl.jena.sparql.algebra.Op;
import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.sparql.engine.ExecutionContext;
import com.hp.hpl.jena.sparql.engine.QueryIterator;
import com.hp.hpl.jena.sparql.engine.binding.Binding;

public class QC
{
    private static Log log = LogFactory.getLog(QC.class) ;
    
    public static SqlNode innerJoin(SDBRequest request, SqlNode left, SqlNode right)
    {
        if ( left == null )
            return right ; 
        
        
        // Try to make things a left tree join(join(table, table), table)
        
        return join(request, INNER, left, right, null) ; 
    }

    public static SqlNode leftJoin(SDBRequest request, SqlNode left, SqlNode right)
    {
        if ( left == null )
            return right ; 
        return join(request, LEFT, left, right, null) ; 
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
            left = removeRestrict(left, conditions) ;

        right = removeRestrict(right, conditions) ;
        
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
        
        SqlJoin join = SqlJoin.create(joinType, left, right, null) ;
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

    private static SqlNode removeRestrict(SqlNode sqlNode, SqlExprList conditions)
    {
        if ( ! sqlNode.isRestrict() ) 
            return sqlNode ;
        
        SqlRestrict restrict = sqlNode.asRestrict() ;
        SqlNode subNode = restrict.getSubNode() ;
        if ( ! subNode.isTable() && ! subNode.isInnerJoin() )
            return sqlNode ;
        conditions.addAll(restrict.getConditions()) ;
        subNode.addNotes(restrict.getNotes()) ;
        return subNode ;
    }
    
    public static boolean fetchPrint = false ;
    public static boolean PrintSQL = false ;
    
    public static QueryIterator exec(OpSQL opSQL, SDBRequest request, Binding binding, ExecutionContext execCxt)
    {
        String sqlStmtStr = toSqlString(opSQL, request) ;
        
        if ( PrintSQL )
            System.out.println(sqlStmtStr) ;
        
        String str = null ;
        if ( execCxt != null )
            str = execCxt.getContext().getAsString(SDB.jdbcFetchSize) ;
        
        int fetchSize = Integer.MIN_VALUE ;
        
        if ( str != null )
            try { fetchSize = Integer.parseInt(str) ; }
            catch (NumberFormatException ex)
            { log.warn("Bad number for fetch size: "+str) ; }
        
        try {
            java.sql.ResultSet jdbcResultSet = request.getStore().getConnection().execQuery(sqlStmtStr, fetchSize) ;
            try {
                // And check this is called once per SQL.
                if ( opSQL.getBridge() == null )
                    log.fatal("Null bridge") ;
                return opSQL.getBridge().assembleResults(jdbcResultSet, binding, execCxt) ;
            } finally {
                // ResultSet closed inside assembleResults or by the iterator returned.
                jdbcResultSet = null ;
            }
        } catch (SQLException ex)
        {
            throw new SDBExceptionSQL("SQLException in executing SQL statement", ex) ;
        }
    }

    public static String toSqlString(OpSQL opSQL, 
                                     SDBRequest request)
    {
        SqlNode sqlNode = opSQL.getSqlNode() ;
        String sqlStmt = request.getStore().getSQLGenerator().generateSQL(sqlNode) ;
        return sqlStmt ; 
    }
    
    /** Find the variables needed out of this query.
     * If we don't do sorting in-DB, then we need the ORDER BY variables as well. 
     * @param query
     * @return
     */
    @SuppressWarnings("unchecked")
    public static List<Var> queryOutVars(Query query)
    {
        // If part query, need all variables. 
        
        // Project variables
        List<Var> vars = toList(map((List<String>)query.getResultVars(), StringToVar)) ;
        
        if ( vars.size() == 0 )
            // SELECT * {}
            LogFactory.getLog(QC.class).warn("No project variables") ;
        
        // Add the ORDER BY variables
        List<SortCondition> orderConditions = (List<SortCondition>)query.getOrderBy() ;
        if ( orderConditions != null )
        {
            for ( SortCondition sc : orderConditions )
            {
                Set<Var> x = (Set<Var>)sc.getExpression().getVarsMentioned() ;
                for ( Var v :  x )
                {
                    if ( ! vars.contains(v) )
                        vars.add(v) ;
                }
            }
        }
        return vars ;
    }
    
    
    public static boolean isOpSQL(Op x)
    {
        return ( x instanceof OpSQL ) ;
    }

    
    private static Transform<String, Var> StringToVar = new Transform<String, Var>(){
        public Var convert(String varName)
        {
            return Var.alloc(varName) ;
        }} ;
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