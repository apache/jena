/*
 * (c) Copyright 2007 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sdb.core.sqlnode;

import com.hp.hpl.jena.sdb.core.AliasesSql;
import com.hp.hpl.jena.sdb.core.Generator;
import com.hp.hpl.jena.sdb.core.Gensym;
import com.hp.hpl.jena.sdb.core.Scope;
import com.hp.hpl.jena.sdb.core.ScopeEntry;
import com.hp.hpl.jena.sdb.shared.SDBInternalError;

public class TransformSelectBlock extends SqlTransformCopy
{
    Generator gen = Gensym.create(AliasesSql.SelectBlock) ;
    
    public TransformSelectBlock() {}
    
    // Pull-in various features of a SELECT statement. 
    
    @Override
    public SqlNode transform(SqlProject sqlProject, SqlNode subNode)
    { 
        SqlSelectBlock block = block(subNode) ;
        addNotes(block, sqlProject) ;
        block.addAll(sqlProject.getCols()) ;
        return block ;
    }

    private void addNotes(SqlSelectBlock block, SqlNode sqlNode)
    {
        block.addNotes(sqlNode.getNotes()) ;
    }

    @Override
    public SqlNode transform(SqlDistinct sqlDistinct, SqlNode subNode)
    {
        SqlSelectBlock block = block(subNode) ;
        addNotes(block, sqlDistinct) ;
        block.setDistinct(true) ;
        return block ;
    }
    
    @Override
    public SqlNode transform(SqlRestrict sqlRestrict, SqlNode subNode)
    { 
        SqlSelectBlock block = block(subNode) ;
        addNotes(block, sqlRestrict) ;
        block.getWhere().addAll(sqlRestrict.getConditions()) ;
        return block ;
    }

    @Override
    public SqlNode transform(SqlSlice sqlSlice, SqlNode subNode)
    { 
        SqlSelectBlock block = block(subNode) ;
        addNotes(block, sqlSlice) ;
        
        long start = block.getStart() ;
        if ( start == -1 )
            start = sqlSlice.getStart() ;           // start was unset.
        else
            start = start + sqlSlice.getStart() ;   // start of the underlying sequence 
        block.setStart(start) ;

        // Note sure what's the best to do here.  Not that ist should occur.
        long length = block.getLength() ;
        if ( length == -1 )
            length = sqlSlice.getLength() ;         // Length was unset.
        else
            length = Math.min(length, sqlSlice.getLength()) ;
        block.setLength(length) ;
        return block ;
    }

//    @Override
//    public SqlNode transform(SqlJoinInner sqlJoinInner, SqlNode left, SqlNode right)
//    { return null ; }
//
//    @Override
//    public SqlNode transform(SqlJoinLeftOuter sqlJoinLeftOuter, SqlNode left, SqlNode right)
//    { return null ; }
//
//    @Override
//    public SqlNode transform(SqlTable sqlTable)
//    { return null ; }
//
    @Override
    public SqlNode transform(SqlRename sqlRename, SqlNode subNode)
    { 
        SqlSelectBlock block = block(subNode) ;

        if ( sqlRename.getAliasName() != null )
            block.setBlockAlias(sqlRename.getAliasName()) ;
        addNotes(block, sqlRename) ;
        block.setIdScope(sqlRename.getIdScope()) ;
        block.setNodeScope(sqlRename.getNodeScope()) ;
        
        // Need to add X AS Y
        // for X as subnode and Y as rename. 
        addProject(block, sqlRename.getIdScope(), sqlRename.getSubNode().getIdScope()) ;
        addProject(block, sqlRename.getNodeScope(), sqlRename.getSubNode().getNodeScope()) ;
        return block ;
    }
    
    private void addProject(SqlSelectBlock block, Scope scope, Scope subScope)
    {
        for ( ScopeEntry e : scope.findScopes() )
        {
            ScopeEntry sub = subScope.findScopeForVar(e.getVar()) ;
            if ( sub == null )
                throw new SDBInternalError("Internal error: column for renamed var not found: "+e.getVar()) ;
            ColAlias colAlias = new ColAlias(sub.getColumn(), e.getColumn()) ;
            colAlias.check(block.getAliasName()) ;
            block.add(colAlias) ;
        }
    }
    
    // XXX Aliasing.  Need to propagate the aliases down if we introduce one. 
    
    private SqlSelectBlock block(SqlNode sqlNode)
    {
        if ( sqlNode instanceof SqlSelectBlock )
            return (SqlSelectBlock)sqlNode ;
        
        String alias = sqlNode.getAliasName() ;
//        if ( alias == null )
//            alias = gen.next() ;
        return new SqlSelectBlock(alias, sqlNode) ;
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