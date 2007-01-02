/*
 * (c) Copyright 2006, 2007 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sdb.core.sqlnode;

import java.util.ArrayList;
import java.util.List;

import com.hp.hpl.jena.query.core.Var;
import com.hp.hpl.jena.sdb.core.sqlexpr.SqlColumn;
import com.hp.hpl.jena.sdb.util.Pair;

public class SqlProject extends SqlNodeBase1
{
    public static SqlNode project(SqlNode sqlNode, Pair<Var, SqlColumn> col)
    {
        SqlProject p = null ;
        if ( sqlNode.isProject() )
            p = sqlNode.getProject() ;
        else
            p = new SqlProject(sqlNode) ;
        if ( col != null )
            p.cols.add(col) ;
        return p ;
    }
    
    public static SqlNode project(SqlNode sqlNode)
    {
        return project(sqlNode, null) ;
    }
    
    private List<Pair<Var, SqlColumn>> cols = null ; 
    
    private SqlProject(SqlNode sqlNode)
    { 
        super(sqlNode.getAliasName(), sqlNode) ;
        this.cols = new ArrayList<Pair<Var, SqlColumn>>() ; 
    }
    
    @Override
    public boolean isProject() { return true ; }
    @Override
    public SqlProject getProject() { return this ; }
    
    @Override 
    public boolean usesColumn(SqlColumn c) { return cols.contains(c) ; }

    public List<Pair<Var, SqlColumn>> getCols() { return cols ; }

    public void visit(SqlNodeVisitor visitor)
    { visitor.visit(this) ; }
}

/*
 * (c) Copyright 2006, 2007 Hewlett-Packard Development Company, LP
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