/*
 * (c) Copyright 2006, 2007 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sdb.core.sqlnode;

import com.hp.hpl.jena.sdb.core.sqlexpr.SqlExpr;
import com.hp.hpl.jena.sdb.core.sqlexpr.SqlExprList;


public class SqlRestrict extends SqlNodeBase1
{
    private SqlExprList conditions = new SqlExprList() ;
    
    public static SqlNode restrict(SqlNode sqlNode, SqlExpr condition)
    {
        // TODO Consider just making a Restriction node
        // and do moving into Joins as part of relational algrebra tree optimizations
        // c.f. the Join creation code that also moving restrictions around.
        
        if ( sqlNode.isJoin() )
        {
            sqlNode.asJoin().addCondition(condition) ;
            return sqlNode ;
        }
        
        if ( sqlNode.isRestrict() )
        {
            // Already a restriction - add to the restrictions already in place
            sqlNode.asRestrict().conditions.add(condition) ;
            return sqlNode ;
        }
        
        return new SqlRestrict(sqlNode.getAliasName(), sqlNode, condition) ;
    }

    public static SqlNode restrict(SqlNode sqlNode, SqlExprList restrictions)
    {
        if ( restrictions.size() == 0 )
            return sqlNode ;
        
        if ( sqlNode.isJoin() )
        {
            sqlNode.asJoin().addConditions(restrictions);
            return sqlNode ;
        }
        
        if ( sqlNode.isRestrict() )
        {
            // Already a restriction - add to the restrictions already in place
            sqlNode.asRestrict().conditions.addAll(restrictions) ;
            return sqlNode ;
        }
        
        return new SqlRestrict(sqlNode.getAliasName(), sqlNode, restrictions) ;
    }

    private SqlRestrict(SqlNode sqlNode, SqlExpr condition)
    { 
        super(null, sqlNode) ;
        this.conditions.add(condition) ; 
    }

    private SqlRestrict(String aliasName, SqlNode sqlNode, SqlExpr condition)
    { 
        super(aliasName, sqlNode) ;
        this.conditions.add(condition) ; 
    }
    
    private SqlRestrict(String aliasName, SqlNode sqlNode, SqlExprList conditions)
    { 
        super(aliasName, sqlNode) ;
        this.conditions = conditions ;
    }
    
    private SqlRestrict(SqlTable table, SqlExprList conditions)
    { 
        super(table.getAliasName(), table) ;
        this.conditions = conditions ;
    }

    @Override
    public boolean isRestrict() { return true ; }
    @Override
    public SqlRestrict asRestrict() { return this ; }

    public SqlExprList getConditions() { return conditions ; }

    public void visit(SqlNodeVisitor visitor)
    { visitor.visit(this) ; }

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