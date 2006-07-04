/*
 * (c) Copyright 2006 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sdb.core.sqlnode;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import com.hp.hpl.jena.query.core.Var;
import com.hp.hpl.jena.sdb.SDBException;
import com.hp.hpl.jena.sdb.core.sqlexpr.SqlColumn;

/** Root of all concrete tables */

public class SqlTable extends SqlNodeBase
{
    private String tableName ;
    private Map<Var, SqlColumn> cols = new HashMap<Var, SqlColumn>() ;
    
    protected SqlTable(String tableName, String aliasName) { super(aliasName) ; this.tableName = tableName ; }
    
    @Override
    public boolean isTable() { return true ; }
    @Override
    public SqlTable getTable() { return this ; }
    
    public Collection<Var> getVars() { return cols.keySet() ; }

    @Override
    public boolean usesColumn(SqlColumn c) { return c.getTable() == this ; }

    public String getTableName()  { return tableName ; }
    
    public SqlColumn getColumnForVar(Var var)
    {
        return cols.get(var) ; 
    }

    public void setColumnForVar(Var var, SqlColumn col)
    {
        if ( !col.getTable().getTableName().equals(getTableName()) )
            throw new SDBException("Attempt to set column in wrong table: Table="+this.getTableName()+" Column: "+col.getFullColumnName()) ;
        cols.put(var, col) ;
    }
    
    public void visit(SqlNodeVisitor visitor)
    { visitor.visit(this) ; }
}

/*
 * (c) Copyright 2006 Hewlett-Packard Development Company, LP
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