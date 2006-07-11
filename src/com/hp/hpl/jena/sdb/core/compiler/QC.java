/*
 * (c) Copyright 2006 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sdb.core.compiler;

import static com.hp.hpl.jena.sdb.core.JoinType.INNER;
import static com.hp.hpl.jena.sdb.core.JoinType.LEFT;
import org.apache.commons.logging.LogFactory;

import com.hp.hpl.jena.query.core.Var;
import com.hp.hpl.jena.sdb.core.CompileContext;
import com.hp.hpl.jena.sdb.core.JoinType;
import com.hp.hpl.jena.sdb.core.sqlexpr.S_Equal;
import com.hp.hpl.jena.sdb.core.sqlexpr.SqlExpr;
import com.hp.hpl.jena.sdb.core.sqlexpr.SqlExprList;
import com.hp.hpl.jena.sdb.core.sqlnode.*;

public class QC
{
    public static SqlNode innerJoin(CompileContext context, SqlNode left, SqlNode right)
    {
        return join(context, left, right, INNER) ; 
    }

    public static SqlNode leftJoin(CompileContext context, SqlNode left, SqlNode right)
    {
        return join(context, left, right, LEFT) ; 
    }

    
    // Put somewhere useful
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
    
    public static SqlNode join(CompileContext context, SqlNode left, SqlNode right, JoinType joinType)
    {
        if ( left == null )
            return right ; 

        SqlExprList conditions = new SqlExprList() ;
        
        // Flatten some cases.
        if ( left.isRestrict() && joinType == JoinType.INNER )
        {
            SqlNode sub = left.getRestrict().getSubNode() ;
            if ( sub.isTable() || 
                 ( sub.isJoin() && sub.getJoin().getJoinType() == JoinType.INNER ) )
            {
                SqlRestrict r = left.getRestrict() ; 
                left = removeRestrict(r, conditions) ;
            }
        }
            
        if ( right.isRestrict() )
        {
            if ( right.getRestrict().getSubNode().isTable() )
            {
                SqlRestrict r = right.getRestrict() ; 
                right = removeRestrict(r, conditions) ;
            }
            else
                LogFactory.getLog(QC.class).info("join: restriction not over a table") ;
        }
        
        for ( Var v : left.getScope().getVars() )
        {
            if ( right.getScope().hasColumnForVar(v) ) 
            {
                SqlExpr c = new S_Equal(left.getScope().getColumnForVar(v), right.getScope().getColumnForVar(v)) ;
                conditions.add(c) ;
                c.addNote("Join var: "+v) ; 
            }
        }
        
        SqlJoin join = SqlJoin.create(joinType, left, right, null) ;
        join.addConditions(conditions) ;
        return join ;
    }
    
    private static SqlNode removeRestrict(SqlRestrict restrict, SqlExprList conditions)
    {
        // Move the conditions up.
        conditions.addAll(restrict.getConditions()) ;
        // Loose the restriction
        SqlNode sqlNode = restrict.getSubNode() ;
        sqlNode.addNotes(restrict.getNotes()) ;
        return sqlNode ;
    }
    
 
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