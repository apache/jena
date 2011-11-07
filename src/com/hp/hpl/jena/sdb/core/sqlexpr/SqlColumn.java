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

package com.hp.hpl.jena.sdb.core.sqlexpr;

import com.hp.hpl.jena.sdb.core.sqlnode.SqlTable;

/** A table column, whether in an expression or a column name of a table */ 
public class SqlColumn extends SqlExprBase
{
    // If we had multiple inheritance, the two concepts migh tbe worth separating ...
    // We don't so the slight pun is a worthwhile convenience.  

    SqlTable  table ;
    String columnName ;
    public SqlColumn(SqlTable sqlTable, String colName) { this.table = sqlTable ; this.columnName = colName ; }

    public String getColumnName() { return columnName ; }
    public SqlTable getTable()  { return table ;  }

    public String getFullColumnName()
    { 
        if ( getTable() != null  )
            return getTable().getAliasName()+"."+columnName ;
        else
            return columnName ;
    }
        
    
    @Override
    public int hashCode()
    {
        return table.hashCode() ^ columnName.hashCode() << 1 ;
    }
    
    @Override
    public boolean equals(Object other)
    {
        if ( this == other ) return true ;
        if ( ! ( other instanceof SqlColumn ) ) 
            return false ;
        SqlColumn col = (SqlColumn)other ;
        
        return table.equals(col.getTable()) && columnName.equals(col.getColumnName()) ;
    }
    
    @Override
    public boolean isColumn()   { return true ; }
    
    public String asString() { return getFullColumnName() ; }
    
    @Override
    public void visit(SqlExprVisitor visitor) { visitor.visit(this) ; }
}
