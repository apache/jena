/*
 * (c) Copyright 2006, 2007 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sdb.core.sqlnode;

import java.util.ArrayList;
import java.util.List;

import com.hp.hpl.jena.sdb.core.sqlexpr.SqlColumn;

public class SqlProject extends SqlNodeBase1
{
    List <ColAlias> cols = null ; 
    
    // ---- Factory methods
    /** make sure this node is a projection - add the column */
    public static SqlNode project(SqlNode sqlNode, ColAlias col)
    {
        SqlProject p = ensure(sqlNode) ;

        if ( col != null )
        {
            col.check(sqlNode.getAliasName()) ;
            p.cols.add(col) ;
        }
        return p ;
    }
    
    public static SqlNode project(SqlNode sqlNode)
    {
        return project(sqlNode, null) ;
    }
    
    private static SqlProject ensure(SqlNode sqlNode)
    {
        if ( sqlNode.isProject() )
            return sqlNode.asProject() ;
        else
            return new SqlProject(sqlNode) ;
    }
    
    // ----
    
    private SqlProject(SqlNode sqlNode)
    { this(sqlNode, new ArrayList<ColAlias>()) ; }
    
    private SqlProject(SqlNode sqlNode, List<ColAlias> cols)
    { 
        // Same alias as the underlying node.
        super(sqlNode.getAliasName(), sqlNode) ;
        this.cols = cols ; 
    }
    
    @Override
    public boolean isProject() { return true ; }
    @Override
    public SqlProject asProject() { return this ; }
    
    @Override 
    public boolean usesColumn(SqlColumn c) { return cols.contains(c) ; }

    public List<ColAlias> getCols() { return cols ; }

    public void visit(SqlNodeVisitor visitor)
    { visitor.visit(this) ; }
    
    @Override
    public SqlNode apply(SqlTransform transform, SqlNode subNode)
    { return transform.transform(this, subNode) ; }

    @Override
    public SqlNode copy(SqlNode subNode)
    {
        return new SqlProject(subNode, this.getCols()) ;
    }
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