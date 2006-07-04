/*
 * (c) Copyright 2005, 2006 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sdb.core.sqlnode;

import com.hp.hpl.jena.query.core.Var;
import com.hp.hpl.jena.sdb.core.JoinType;
import com.hp.hpl.jena.sdb.core.sqlexpr.SqlColumn;
import com.hp.hpl.jena.sdb.core.sqlexpr.SqlExprList;

import org.apache.commons.logging.LogFactory;

public abstract class SqlJoin extends SqlNodeBase
{
    private JoinType joinType ;
    private SqlNode left ;
    private SqlNode right ;
    private SqlExprList conditions = new SqlExprList() ;

//    public static SqlJoin create(JoinType joinType, SqlNode left, SqlNode right)
//    { return create(joinType, left, right) ; }
    
    public static SqlJoin create(JoinType joinType, SqlNode left, SqlNode right, String alias)
    {
        switch (joinType)
        {
            case INNER: return new SqlJoinInner(left, right, alias) ;
            case LEFT: return new SqlJoinLeftOuter(left, right, alias) ;
        }
        LogFactory.getLog(SqlJoin.class).warn("Unknown join type: "+joinType.printName()) ;
        return null ;
    }


    protected SqlJoin(JoinType joinType, SqlNode left, SqlNode right)
    { this(joinType, left, right, null) ; }

    protected SqlJoin(JoinType joinType, SqlNode left, SqlNode right, String alias)
    { 
        super(alias) ;
        this.joinType = joinType ;
        this.left = left ;
        this.right = right ;
    } 
    
    public SqlNode   getLeft()   { return left ; }
    public SqlNode   getRight()  { return right ; }
    
    public JoinType  getJoinType() { return joinType ; }
    
    @Override 
    public boolean   isJoin()             { return true ; }
    @Override 
    public SqlJoin   getJoin()            { return this ; }
    
    public SqlExprList getConditions() { return conditions ; }
    @Override
    public boolean usesColumn(SqlColumn c)
    {
        return getLeft().usesColumn(c) || getRight().usesColumn(c) ; 
    }
    
    public SqlColumn getColumnForVar(Var var)
    {
        SqlColumn col = getLeft().getColumnForVar(var) ;
        if ( col != null )
            return col ;
        return getRight().getColumnForVar(var) ;
    }
}

/*
 * (c) Copyright 2005, 2006 Hewlett-Packard Development Company, LP
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