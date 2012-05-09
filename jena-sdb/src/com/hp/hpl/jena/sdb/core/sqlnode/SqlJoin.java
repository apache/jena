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

package com.hp.hpl.jena.sdb.core.sqlnode;

import org.slf4j.LoggerFactory;

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
    
    public static SqlJoin create(JoinType joinType, SqlNode left, SqlNode right)
    {
        switch (joinType)
        {
            case INNER: return new SqlJoinInner(left, right) ;
            case LEFT: return new SqlJoinLeftOuter(left, right) ;
        }
        LoggerFactory.getLogger(SqlJoin.class).warn("Unknown join type: "+joinType.printName()) ;
        return null ;
    }

    protected SqlJoin(JoinType joinType, SqlNode left, SqlNode right)
    { 
        // Does not have an alias.
        super(null, left, right) ;
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
    
    @Override
    public Scope getIdScope()     { return idScope ; }
    @Override
    public Scope getNodeScope()   { return nodeScope ; }
}
