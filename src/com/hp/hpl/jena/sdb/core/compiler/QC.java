/*
 * (c) Copyright 2006 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sdb.core.compiler;

import static com.hp.hpl.jena.sdb.core.JoinType.INNER;
import static com.hp.hpl.jena.sdb.core.JoinType.LEFT;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.core.Var;
import com.hp.hpl.jena.query.engine.Binding;
import com.hp.hpl.jena.query.engine.QueryIterator;
import com.hp.hpl.jena.query.engine1.ExecutionContext;

import com.hp.hpl.jena.sdb.core.JoinType;
import com.hp.hpl.jena.sdb.core.SDBRequest;
import com.hp.hpl.jena.sdb.core.sqlexpr.S_Equal;
import com.hp.hpl.jena.sdb.core.sqlexpr.SqlExpr;
import com.hp.hpl.jena.sdb.core.sqlexpr.SqlExprList;
import com.hp.hpl.jena.sdb.core.sqlnode.*;
import com.hp.hpl.jena.sdb.sql.RS;
import com.hp.hpl.jena.sdb.sql.SDBExceptionSQL;
import com.hp.hpl.jena.sdb.store.SQLBridge;

public class QC
{
    private static Log log = LogFactory.getLog(QC.class) ;
    
    public static SqlNode innerJoin(SDBRequest request, SqlNode left, SqlNode right)
    {
        return join(request, INNER, left, right, null) ; 
    }

    public static SqlNode leftJoin(SDBRequest request, SqlNode left, SqlNode right)
    {
        return join(request, LEFT, left, right, null) ; 
    }

    public static SqlNode leftJoinCoalesc(SDBRequest request, SqlNode left, SqlNode right, Set<Var> coalesceVars)
    {
        return join(request, LEFT, left, right, coalesceVars) ; 
    }
    
    private static String sqlNodeName(SqlNode sNode)
    {
        if ( sNode == null ) return "<null>" ;
        if ( sNode instanceof SqlProject )       return "Project" ;
        if ( sNode instanceof SqlRestrict )      return "Restrict/"+sqlNodeName(sNode.getRestrict().getSubNode()) ;
        if ( sNode instanceof SqlTable )         return "Table" ;
        if ( sNode instanceof SqlJoinInner )     return "JoinInner" ;
        if ( sNode instanceof SqlJoinLeftOuter ) return "Joinleft" ;
        return "<unknown>" ;
    }
    
    // Join/LeftJoin two subexpressions, calculating the join conditions in the process
    // If a coalesce (LeftJoin) then don't equate left and right vars of the same name.
    private static SqlNode join(SDBRequest request, 
                                JoinType joinType, 
                                SqlNode left, SqlNode right,
                                Set<Var> coalesceVars)
    {
        if ( left == null )
            return right ; 

        SqlExprList conditions = new SqlExprList() ;
        
        if ( joinType == JoinType.INNER )
            // If it's a LeftJoin, leave the left filter on the LHS.
            left = removeRestrict(left, conditions) ;
        
        right = removeRestrict(right, conditions) ;
        
        for ( Var v : left.getIdScope().getVars() )
        {
            if ( right.getIdScope().hasColumnForVar(v) )
            {
                if (coalesceVars == null || ! coalesceVars.contains(v) )
                {
                    SqlExpr c = new S_Equal(left.getIdScope().getColumnForVar(v), right.getIdScope().getColumnForVar(v)) ;
                    conditions.add(c) ;
                    c.addNote("Join var: "+v) ; 
                }
                else
                    log.warn("Skip coalesce var") ;
            }
        }
        
        SqlJoin join = SqlJoin.create(joinType, left, right, null) ;
        return SqlRestrict.restrict(join, conditions) ;
    }
    
    private static SqlNode removeRestrict(SqlNode sqlNode, SqlExprList conditions)
    {
        if ( ! sqlNode.isRestrict() ) 
            return sqlNode ;
        
        SqlRestrict restrict = sqlNode.getRestrict() ;
        SqlNode subNode = restrict.getSubNode() ;
        if ( ! subNode.isTable() && ! subNode.isInnerJoin() )
            return sqlNode ;
        conditions.addAll(restrict.getConditions()) ;
        subNode.addNotes(restrict.getNotes()) ;
        return subNode ;
    }
    
    public static boolean PrintSQL = false ;
    
    public static QueryIterator exec(OpSQL opSQL, SDBRequest request, Binding binding, ExecutionContext execCxt)
    {
        String sqlStmt = toSqlString(opSQL, request) ;
        
        if ( PrintSQL )
            System.out.println(sqlStmt) ;
        
        try {
            java.sql.ResultSet jdbcResultSet = request.getStore().getConnection().execQuery(sqlStmt) ;
            if ( false )
                // Destructive
                RS.printResultSet(jdbcResultSet) ;
            try {
                return opSQL.getBridge().assembleResults(jdbcResultSet, binding, execCxt) ;
            } finally { jdbcResultSet.close() ; }
        } catch (SQLException ex)
        {
            throw new SDBExceptionSQL("SQLException in executing SQL statement", ex) ;
        }
    }

    
    public static SqlNode toSqlTopNode(SqlNode sqlNode, 
                                       List<Var> projectVars,
                                       SQLBridge bridge)
    {
        bridge.init(sqlNode, projectVars) ;
        bridge.buildValues() ;
        bridge.buildProject() ;
        return bridge.getSqlNode() ;
    }
    
    public static String toSqlString(OpSQL opSQL, 
                                     SDBRequest request)
    {
        SqlNode sqlNode = opSQL.getSqlNode() ;
        String sqlStmt = request.getStore().getSQLGenerator().generateSQL(sqlNode) ;
        return sqlStmt ; 
    }
    
    public static List<Var> projectVars(Query query)
    {
        List<Var> vars = new ArrayList<Var>() ;
        @SuppressWarnings("unchecked")
        List<String> list = (List<String>)query.getResultVars() ;
        if ( list.size() == 0 )
            LogFactory.getLog(QC.class).warn("No project variables") ;
        for ( String vn  : list )
            vars.add(Var.alloc(vn)) ;
        return vars ;
    }
    
//    public static Set<Var> exitVariables(Block block)
//    {
//        Set<Var> x = block.getProjectVars() ;
//        if ( x == null )
//        {
//            if ( block.isCompletePattern() )
//                log.warn("Null for projection variables - but it's a single block") ;
//            x = block.getDefinedVars() ;
//        }
//        if ( x == null )
//            log.warn("Null for defined variables") ;
//        return x ; 
//    }
}

/*
 * (c) Copyright 2006 Hewlett-Packard Development Company, LP
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