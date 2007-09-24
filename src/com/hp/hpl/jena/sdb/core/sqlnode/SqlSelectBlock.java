/*
 * (c) Copyright 2007 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sdb.core.sqlnode;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.hp.hpl.jena.sdb.core.Scope;
import com.hp.hpl.jena.sdb.core.sqlexpr.SqlColumn;
import com.hp.hpl.jena.sdb.core.sqlexpr.SqlExprList;
import com.hp.hpl.jena.sdb.shared.SDBInternalError;

/** A unit that generates an SQL SELECT Statement.
 *  The SQL generation process is a pass over the SqlNdoe structure to generate SelectBlocks,
 *  then to generate the SQL strings.
 * 
 * @author Andy Seaborne
 * @version $Id$
 */

public class SqlSelectBlock extends SqlNodeBase1
{
    // Need: code to take an SqlNode and produce a rename map. 
    // Already in SqlRename.  Use SqlRename.view.
    // SqlRename is currently unused.
    // Is SqlRename > SqlProject?
    //   with Map<Var, String> = List<VarCol>
    
    // Mapping of names
    // projection
    // Joins (inner and left)
    // Restriction.
    // Group
    // aggregrate
    // order
    // limit/offset
    
    private List<ColAlias> cols = new ArrayList<ColAlias>() ;
    
    private SqlExprList exprs = new SqlExprList() ;
    private long start = -1 ;
    private long length = -1 ;
    private boolean distinct = false ;
    
    private Scope idScope = null ;      // Scopes are as the wrapped SqlNode unless explicitly changed.
    private Scope nodeScope = null ;
    
    /**
     * @param aliasName
     * @param sqlNode
     */
    public SqlSelectBlock(String aliasName, SqlNode sqlNode)
    {
        super(aliasName, sqlNode) ;
        
    }

    public void setBlockAlias(String alias)      { super.aliasName = alias ; }
    
    public List<ColAlias> getCols()       { return cols ; }
    public void add(ColAlias c)           { _add(c) ; }
    public void addAll(Collection<ColAlias> vc)    
    { 
        for ( ColAlias c : vc )
            _add(c) ;
    }
    
    private void _add(ColAlias c)
    { 
        SqlColumn col = c.getColumn() ;
        SqlColumn aliasCol = c.getAlias() ;
        c.check(getAliasName()) ;
//        
//        if ( aliasCol.getTable() != null && aliasCol.getTable().getAliasName().equals(getAliasName()) )
//            throw new SDBInternalError("Attempt to project to a column with different alias: "+col+" -> "+aliasCol) ;
        cols.add(c) ;
    }    
    
    public SqlExprList getWhere()       { return exprs ; }

    public long getStart()              { return start ; }
    public void setStart(long start)    { this.start = start ; }

    public long getLength()             { return length ; }
    public void setLength(long length)  { this.length = length ; }
    
    @Override
    public Scope getIdScope()           { return idScope != null ? idScope : super.getIdScope() ; } 

    @Override
    public Scope getNodeScope()         { return nodeScope != null ? nodeScope : super.getNodeScope() ; } 

    public void setIdScope(Scope scope)     { idScope = scope ; }
    public void setNodeScope(Scope scope)   { nodeScope = scope ; }
    
    @Override
    public SqlNode apply(SqlTransform transform, SqlNode newSubNode)
    { throw new SDBInternalError("SqlSelectBlock.apply") ; }
    @Override
    public SqlNode copy(SqlNode subNode)
    { throw new SDBInternalError("SqlSelectBlock.copy") ; }
    
    public void visit(SqlNodeVisitor visitor)
    { visitor.visit(this) ; }

    // Not "isDistinct" 
    public boolean getDistinct()
    {
        return distinct ;
    }

    public void setDistinct(boolean isDistinct)
    {
        this.distinct = isDistinct ;
    }
}

/*
 * (c) Copyright 2007 Hewlett-Packard Development Company, LP
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