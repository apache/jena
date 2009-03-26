/*
 * (c) Copyright 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
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
 * 
 * @author skagels (Metatomix)
 *
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


/*
 * (c) Copyright 2009 Hewlett-Packard Development Company, LP
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