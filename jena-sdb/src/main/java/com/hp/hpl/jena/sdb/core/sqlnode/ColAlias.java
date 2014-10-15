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

import com.hp.hpl.jena.sdb.core.sqlexpr.SqlColumn;
import com.hp.hpl.jena.sdb.shared.SDBInternalError;

public class ColAlias
{
    // "extends Pair" does not give nice names.
    private SqlColumn column ;
    private SqlColumn alias ;
    
    public ColAlias(SqlColumn column, SqlColumn alias)
    {
        this.column = column ;
        this.alias = alias ;
    }

    public SqlColumn getColumn()
    {
        return column ;
    }

    public SqlColumn getAlias()
    {
        return alias ;
    }
    
    public void check(String requiredName)
    {
        if ( getAlias() == null )
            return ;
        if ( getAlias().getTable() == null )
            return ;
        if ( ! getAlias().getTable().getAliasName().equals(requiredName) )
            throw new SDBInternalError("Alias name error: "+getColumn()+"/"+getAlias()+": required: "+requiredName) ;
    }
    
    @Override
    public String toString()
    {
        
        StringBuilder b = new StringBuilder() ;
        b.append("(") ;
        b.append(  (column == null) ? "??" : column ) ;
        b.append(",") ;
        b.append(  (alias == null) ? "??" : alias ) ;
        b.append(")") ;
        return b.toString() ; 
    }
}
