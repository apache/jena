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

import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.sdb.core.Scope;
import com.hp.hpl.jena.sdb.core.ScopeBase;
import com.hp.hpl.jena.sdb.core.sqlexpr.SqlColumn;

/** Root of all tables (roughly, a group of columns) */

public class SqlTable extends SqlNodeBase0
{
    private String tableName ;
    protected ScopeBase idScope = null ;
    protected ScopeBase nodeScope = null ;
    
    public SqlTable(String name)
    {
        this(name, name) ;
    }

    public SqlTable(String aliasName, String tableName)
    {
        this(aliasName, tableName, new ScopeBase(), new ScopeBase()) ;
    }
    
    private SqlTable(String aliasName, String tableName, ScopeBase idScope, ScopeBase nodeScope)
    {
        super(aliasName) ;
        this.tableName = tableName ;
        this.idScope = idScope ;
        this.nodeScope = nodeScope ;
    }

    @Override
    public boolean isTable() { return true ; }
    @Override
    public SqlTable asTable() { return this ; }
    
    @Override
    public boolean usesColumn(SqlColumn c) { return c.getTable() == this ; }

    public String getTableName()  { return tableName ; }
    
    @Override
    public void visit(SqlNodeVisitor visitor)
    { visitor.visit(this) ; }

    @Override
    public Scope getIdScope() { return idScope ; }

    @Override
    public Scope getNodeScope() { return nodeScope ; }
    
    public void setIdColumnForVar(Var var, SqlColumn thisCol)
    {
        idScope.setColumnForVar(var, thisCol) ;
    }
    
    public void setValueColumnForVar(Var var, SqlColumn thisCol)
    {
        if ( nodeScope == null )
            nodeScope = new ScopeBase() ;
        nodeScope.setColumnForVar(var, thisCol) ;
    }
    
    @Override
    public int hashCode()
    {
        int h = 981 ;
        if ( tableName != null )
            h = h ^ tableName.hashCode() ;
        if ( getAliasName() != null )
            h = h ^ getAliasName().hashCode() << 1 ;
        return h ;
    }
    
    @Override
    public boolean equals(Object other)
    {
        if ( this == other ) return true ;

        if ( ! ( other instanceof SqlTable ) ) 
            return false ;
        SqlTable table = (SqlTable)other ;
        
        if ( ! tableName.equals(table.tableName) )
            return false ;

        if ( getAliasName() == null && table.getAliasName() == null )
            return true ;
        if ( getAliasName() == null || table.getAliasName() == null )
            return false ;
        return getAliasName().equals(table.getAliasName()) ;
    }

    @Override
    public SqlNode apply(SqlTransform transform)
    {
        return transform.transform(this) ;
    }

    @Override
    public SqlNode copy()
    {
        return new SqlTable(tableName, getAliasName(), this.idScope, this.nodeScope) ;
    }
}
