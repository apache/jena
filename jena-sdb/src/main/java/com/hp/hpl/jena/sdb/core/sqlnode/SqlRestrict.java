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

import com.hp.hpl.jena.sdb.core.sqlexpr.SqlExpr;
import com.hp.hpl.jena.sdb.core.sqlexpr.SqlExprList;
import com.hp.hpl.jena.sdb.shared.SDBInternalError;

// Class no longer used/
public class SqlRestrict extends SqlNodeBase1
{
    private SqlExprList conditions = new SqlExprList() ;
    
    private static SqlNode restrict(SqlNode sqlNode, SqlExpr condition)
    {
        // Consider just making a Restriction node
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

    private static SqlNode restrict(SqlNode sqlNode, SqlExprList restrictions)
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

    @Override
    public void visit(SqlNodeVisitor visitor)
    { throw new SDBInternalError("SqlRestrict.visit") ; }
    //{ visitor.visit(this) ; }
    
    @Override
    public SqlNode apply(SqlTransform transform, SqlNode subNode)
    { throw new SDBInternalError("SqlRestrict.apply") ; }
    //{ return transform.transform(this, subNode) ; }

    @Override
    public SqlNode copy(SqlNode subNode)
    {
        return new SqlRestrict(this.getAliasName(), subNode, conditions) ;
    }
}
