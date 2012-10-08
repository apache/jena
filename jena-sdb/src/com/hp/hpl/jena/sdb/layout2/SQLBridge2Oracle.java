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

package com.hp.hpl.jena.sdb.layout2;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;

import com.hp.hpl.jena.sdb.core.SDBRequest;
import com.hp.hpl.jena.sdb.core.sqlexpr.SqlColumn;
import com.hp.hpl.jena.sdb.core.sqlnode.SqlNode;
import com.hp.hpl.jena.sdb.core.sqlnode.SqlTable;
import com.hp.hpl.jena.sdb.sql.SQLUtils;
import com.hp.hpl.jena.sparql.core.Var;

/**
 * Added to specialize lex column selection for Oracle.
 * The lex column is an nclob that should get converted to a string for performance 
 * reasons.
 */
public class SQLBridge2Oracle extends SQLBridge2 {
    
    public SQLBridge2Oracle(SDBRequest request, SqlNode sqlNode, Collection<Var> projectVars)
    { 
        super(request, sqlNode, projectVars) ;
    }
    
    @Override
    protected SqlColumn getLexSqlColumn(SqlTable table) {
        
        return new SqlColumn(table, "lex") {
            @Override
            public String getFullColumnName() { 
                String col = getTable().getAliasName()+"."+getColumnName();
                return  "CASE WHEN LENGTH("+col+
                        ") <= 2000 THEN NULL ELSE "+col+" END";              
            }
        } ;
    }
    
    @Override
    protected SqlColumn getLexNCharSqlColumn(SqlTable table) {
        
        return new SqlColumn(table, "lexNChar") {
            @Override
            public String getFullColumnName() { 
                String col = getTable().getAliasName()+".lex";
                return "CASE WHEN LENGTH("+col+") <= 2000 THEN TO_NCHAR("+
                       col+") ELSE NULL END ";              
            }
        } ;
    }
    
    @Override
    protected String getLexFromResultSet(ResultSet rs, String codename) 
    throws SQLException
    {
        String lex = rs.getString(SQLUtils.gen(codename, "lexNChar"));
        if (lex == null)            
            rs.getString(SQLUtils.gen(codename,"lex")) ;                
        if ( lex == null )
            lex = "" ;
        return lex;
    }

}
