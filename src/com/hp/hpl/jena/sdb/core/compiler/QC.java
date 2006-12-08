/*
 * (c) Copyright 2006 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sdb.core.compiler;

import static com.hp.hpl.jena.sdb.core.JoinType.INNER;
import static com.hp.hpl.jena.sdb.core.JoinType.LEFT;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hp.hpl.jena.query.core.Var;
import com.hp.hpl.jena.sdb.core.SDBRequest;
import com.hp.hpl.jena.sdb.core.JoinType;
import com.hp.hpl.jena.sdb.core.sqlexpr.S_Equal;
import com.hp.hpl.jena.sdb.core.sqlexpr.SqlExpr;
import com.hp.hpl.jena.sdb.core.sqlexpr.SqlExprList;
import com.hp.hpl.jena.sdb.core.sqlnode.*;

public class QC
{
    // TODO Combine with QP
    
    private static Log log = LogFactory.getLog(QC.class) ;
    
    public static SqlNode innerJoin(SDBRequest request, SqlNode left, SqlNode right)
    {
        return join(request, left, right, INNER) ; 
    }

    public static SqlNode leftJoin(SDBRequest request, SqlNode left, SqlNode right)
    {
        return join(request, left, right, LEFT) ; 
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
    
    public static SqlNode join(SDBRequest request, SqlNode left, SqlNode right, JoinType joinType)
    {
        if ( left == null )
            return right ; 

        SqlExprList conditions = new SqlExprList() ;
        
        if ( joinType == JoinType.INNER )
            left = removeRestrict(left, conditions) ;
        
        right = removeRestrict(right, conditions) ;
        
        for ( Var v : left.getIdScope().getVars() )
        {
            if ( right.getIdScope().hasColumnForVar(v) ) 
            {
                SqlExpr c = new S_Equal(left.getIdScope().getColumnForVar(v), right.getIdScope().getColumnForVar(v)) ;
                conditions.add(c) ;
                c.addNote("Join var: "+v) ; 
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