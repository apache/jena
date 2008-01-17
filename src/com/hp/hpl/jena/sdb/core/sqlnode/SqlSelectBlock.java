/*
 * (c) Copyright 2007, 2008 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sdb.core.sqlnode;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.hp.hpl.jena.sdb.core.Generator;
import com.hp.hpl.jena.sdb.core.Gensym;
import com.hp.hpl.jena.sdb.core.Scope;
import com.hp.hpl.jena.sdb.core.ScopeBase;
import com.hp.hpl.jena.sdb.core.ScopeEntry;
import com.hp.hpl.jena.sdb.core.sqlexpr.SqlColumn;
import com.hp.hpl.jena.sdb.core.sqlexpr.SqlExpr;
import com.hp.hpl.jena.sdb.core.sqlexpr.SqlExprList;
import com.hp.hpl.jena.sdb.shared.SDBInternalError;
import com.hp.hpl.jena.sparql.core.Var;

/** A unit that generates an SQL SELECT Statement.
 *  The SQL generation process is a pass over the SqlNdoe structure to generate SelectBlocks,
 *  then to generate the SQL strings.
 * 
 * @author Andy Seaborne
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
    private static final int NOT_SET = -9 ; 
    private long start = NOT_SET ;
    private long length = NOT_SET ;
    private boolean distinct = false ;
    
    private SqlTable vTable ;           // Naming base for renamed columns
    private Scope idScope = null ;      // Scopes are as the wrapped SqlNode unless explicitly changed.
    private Scope nodeScope = null ;
    
    static public SqlNode distinct(SqlNode sqlNode)
    { 
        SqlSelectBlock block = blockWithView(sqlNode) ;
        block.setDistinct(true) ;
        return block ;
    }
    
    static public SqlNode project(SqlNode sqlNode)
    { return project(sqlNode, (ColAlias)null) ; }
    
    static public SqlNode project(SqlNode sqlNode, Collection<ColAlias> cols)
    {
        // If already a view, not via a project, - think harder
        
        SqlSelectBlock block = blockNoView(sqlNode) ;
        if ( block.idScope != null || block.nodeScope != null )
            System.err.println("SqlSelectBlock.project : already a view") ; 
        
        if ( cols != null )
            block.addAll(cols) ;
        return block ;
    }
    
    static public SqlNode project(SqlNode sqlNode, ColAlias col)
    {
        SqlSelectBlock block = blockNoView(sqlNode) ;
        if ( col != null )
            block.add(col) ;
        return block ;
    }

    static public SqlNode slice(SqlNode sqlNode, long start, long length)
    {
        SqlSelectBlock block = blockWithView(sqlNode) ;
        
        if ( start >= 0 )
        {
            if (  block.getStart() > 0 )
                start = start + block.getStart() ;
            block.setStart(start) ;
        }
        
        if ( length >= 0 )
        {
            if ( block.getLength() >= 0 )
                length = Math.min(length, block.getLength()) ;
            block.setLength(length) ;
        }
        return block ;
    }
    
    static public SqlNode view(SqlNode sqlNode)
    {
        SqlSelectBlock block = blockWithView(sqlNode) ;
        return block ;
    }

    static public SqlNode restrict(SqlNode sqlNode, SqlExprList exprs)
    {
        if ( exprs.size() == 0 )
            return sqlNode ;
        
        // Single table does not need renaming of columns 
        SqlSelectBlock block = (sqlNode.isTable() ? blockPlain(sqlNode) : blockWithView(sqlNode)) ;
        block.getWhere().addAll(exprs) ;
        return block ;
    }
    
    static public SqlNode restrict(SqlNode sqlNode, SqlExpr expr)
    {
        SqlSelectBlock block = (sqlNode.isTable() ? blockPlain(sqlNode) : blockWithView(sqlNode)) ;
        block.getWhere().add(expr) ;
        return block ;
    }
     
    /**
     * @param aliasName
     * @param sqlNode
     */
    private SqlSelectBlock(String aliasName, SqlNode sqlNode)
    {
        super(aliasName, sqlNode) ;
        if ( aliasName != null )
            vTable = new SqlTable(aliasName) ;
    }
    
    @Override
    public boolean         isSelectBlock() { return true ; }
    @Override
    public SqlSelectBlock  asSelectBlock() { return this  ; }
    
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

    public boolean hasSlice()           { return (start != NOT_SET )  || ( length != NOT_SET ) ; }
    
    public long getStart()              { return start ; }
    private void setStart(long start)    { this.start = start ; }

    public long getLength()             { return length ; }
    private void setLength(long length)  { this.length = length ; }
    
    @Override
    public Scope getIdScope()           { return idScope != null ? idScope : super.getIdScope() ; } 

    @Override
    public Scope getNodeScope()         { return nodeScope != null ? nodeScope : super.getNodeScope() ; } 

    @Override
    public SqlNode apply(SqlTransform transform, SqlNode newSubNode)
    { return transform.transform(this, newSubNode) ; }
    
    @Override
    public SqlNode copy(SqlNode subNode)
    { return new SqlSelectBlock(this.getAliasName(), subNode) ; }
    
    public void visit(SqlNodeVisitor visitor)
    { visitor.visit(this) ; }

    // Not "isDistinct" 
    public boolean getDistinct()
    {
        return distinct ;
    }

    private void setDistinct(boolean isDistinct)
    {
        this.distinct = isDistinct ;
    }

    // TODO Make this per-request
    static private Generator genTableAlias = Gensym.create("SB") ;
    
    private static SqlSelectBlock blockWithView(SqlNode sqlNode)
    {
        if ( sqlNode instanceof SqlSelectBlock )
        {
            SqlSelectBlock block = (SqlSelectBlock)sqlNode ;
            if ( block.cols.size() == 0 )
            {
                // Didn't have a column view - force it
                calcView(block) ;
            }
            
            return (SqlSelectBlock)sqlNode ;
        }
            
        SqlSelectBlock block = _create(sqlNode) ;
        if ( block.getCols().size() != 0 )
            throw new SDBInternalError("Can't set a view on Select block which is already had columns set") ; 
        
        calcView(block) ;
        return block ;
    }
    
    private static SqlSelectBlock blockPlain(SqlNode sqlNode)
    {
        if ( sqlNode instanceof SqlSelectBlock )
            return (SqlSelectBlock)sqlNode ;
        // Reuse alias (typically, sqlNode is a table or view and this is the table name) 
        SqlSelectBlock block = new SqlSelectBlock(sqlNode.getAliasName(), sqlNode) ;
        //addNotes(block, sqlNode) ;
        return block ;
    }
    
    private static SqlSelectBlock blockNoView(SqlNode sqlNode)
    {
        if ( sqlNode instanceof SqlSelectBlock )
            return (SqlSelectBlock)sqlNode ;
        return _create(sqlNode) ;
    }
        
    private static SqlSelectBlock _create(SqlNode sqlNode)
    {
        String alias = sqlNode.getAliasName() ;
        
        // XXX Remove
//        if ( alias == null )
//            alias = genTableAlias.next() ;
        
        // Always have a new name.
        alias = genTableAlias.next() ;
        SqlSelectBlock block = new SqlSelectBlock(alias, sqlNode) ;
        addNotes(block, sqlNode) ;
        return block ;
    }
    
    private static void addNotes(SqlSelectBlock block, SqlNode sqlNode)
    {
        block.addNotes(sqlNode.getNotes()) ;
    }

    static private void calcView(SqlSelectBlock block)
    {
        SqlNode sqlNode = block.getSubNode() ;
        ScopeBase idScopeRename = new ScopeBase() ;
        ScopeBase nodeScopeRename = new ScopeBase() ;
        Generator gen = Gensym.create("X") ;    // Column names.  Not global.
    
        block.merge(sqlNode.getIdScope(), idScopeRename, gen) ;
        block.merge(sqlNode.getNodeScope(), nodeScopeRename, gen) ;
    
        block.nodeScope = nodeScopeRename ;
        block.idScope = idScopeRename ;
    }

    // Calculate renames
    // Map all vars in the scope to names in the rename.
    private void merge(Scope scope, ScopeBase newScope, Generator gen)
    {
        String x = "" ;
        String sep = "" ;
    
        for ( ScopeEntry e : scope.findScopes() )
        {
            SqlColumn oldCol = e.getColumn() ;
            Var v = e.getVar() ;
            String colName = gen.next() ;
            SqlColumn newCol = new SqlColumn(vTable, colName) ;
            this.add(new ColAlias(oldCol, newCol)) ;
            newScope.setColumnForVar(v, newCol) ;
            // Annotations
            x = String.format("%s%s%s:(%s=>%s)", x, sep, v, oldCol, newCol) ;
            sep = " " ;
        }
        if ( x.length() > 0 )
            addNote(x) ;
    }
}

/*
 * (c) Copyright 2007, 2008 Hewlett-Packard Development Company, LP
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