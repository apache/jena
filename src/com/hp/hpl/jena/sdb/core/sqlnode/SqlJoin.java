/*
 * (c) Copyright 2005, 2006, 2007 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sdb.core.sqlnode;

import org.apache.commons.logging.LogFactory;

import com.hp.hpl.jena.sdb.core.JoinType;
import com.hp.hpl.jena.sdb.core.Scope;
import com.hp.hpl.jena.sdb.core.Scope2;
import com.hp.hpl.jena.sdb.core.ScopeOptional;
import com.hp.hpl.jena.sdb.core.sqlexpr.SqlExpr;
import com.hp.hpl.jena.sdb.core.sqlexpr.SqlExprList;

public abstract class SqlJoin extends SqlNodeBase2
{
    private JoinType joinType ;
    private Scope idScope ;
    private Scope nodeScope ;
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
    { this(null, joinType, left, right) ; }

    protected SqlJoin(String alias, JoinType joinType, SqlNode left, SqlNode right)
    { 
        super(alias, left, right) ;
        this.joinType = joinType ;
        
        if ( joinType == JoinType.LEFT )
        {
            // If a left join, the RHS may be null. 
            idScope = new Scope2(left.getIdScope(), new ScopeOptional(right.getIdScope())) ;
            nodeScope = new Scope2(left.getNodeScope(),  new ScopeOptional(right.getNodeScope())) ;
        }
        else
        {
            idScope = new Scope2(left.getIdScope(), right.getIdScope()) ;
            nodeScope = new Scope2(left.getNodeScope(), right.getNodeScope()) ;
        }
    } 
    
    public JoinType  getJoinType() { return joinType ; }
    
    @Override 
    public boolean   isJoin()           { return true ; }

//    @Override 
//    public boolean   isInnerJoin()      { return joinType == JoinType.INNER ; }
//
//    @Override 
//    public boolean   isLeftJoin()       { return joinType == JoinType.LEFT ; }

    @Override 
    public SqlJoin   asJoin()           { return this ; }
    
    public SqlExprList getConditions() { return conditions ; }
    public void addCondition(SqlExpr e) { conditions.add(e) ; }
    public void addConditions(SqlExprList exprs) { conditions.addAll(exprs) ; }
    
    public Scope getIdScope()     { return idScope ; }
    public Scope getNodeScope()   { return nodeScope ; }
}

/*
 * (c) Copyright 2005, 2006, 2007 Hewlett-Packard Development Company, LP
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