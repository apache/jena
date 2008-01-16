/*
 * (c) Copyright 2007, 2008 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sdb.core.sqlnode;


public class SqlTransformCopy implements SqlTransform
{
    public static final boolean COPY_ALWAYS         = true ;
    public static final boolean COPY_ONLY_ON_CHANGE = false ;
    private boolean alwaysCopy = false ;
    
    public SqlTransformCopy() { this(COPY_ONLY_ON_CHANGE) ; }
    public SqlTransformCopy(boolean alwaysDuplicate)   { this.alwaysCopy = alwaysDuplicate ; }
    
    public SqlNode transform(SqlProject sqlProject, SqlNode subNode)
    { return xform(sqlProject, subNode) ; }

    public SqlNode transform(SqlDistinct sqlDistinct, SqlNode subNode)
    { return xform(sqlDistinct, subNode) ; }

    public SqlNode transform(SqlRestrict sqlRestrict, SqlNode subNode)
    { return xform(sqlRestrict, subNode) ; }

    public SqlNode transform(SqlSlice sqlSlice, SqlNode subNode)
    { return xform(sqlSlice, subNode) ; }

    public SqlNode transform(SqlSelectBlock sqlSelectBlock, SqlNode subNode)
    { return xform(sqlSelectBlock, subNode) ; }

    public SqlNode transform(SqlJoinInner sqlJoinInner, SqlNode left, SqlNode right)
    { return xform(sqlJoinInner, left, right) ; }

    public SqlNode transform(SqlJoinLeftOuter sqlJoinLeftOuter, SqlNode left, SqlNode right)
    { return xform(sqlJoinLeftOuter, left, right) ; }

    public SqlNode transform(SqlUnion sqlUnion, SqlNode left, SqlNode right)
    { return xform(sqlUnion, left, right) ; } 

    public SqlNode transform(SqlTable sqlTable)
    { return xform(sqlTable) ; }

    public SqlNode transform(SqlRename sqlRename, SqlNode subNode)
    { return xform(sqlRename, subNode) ; } 

    public SqlNode transform(SqlCoalesce sqlCoalesce, SqlNode subNode)
    { return xform(sqlCoalesce, subNode) ; }
    
    // ---- Workers
    
    private SqlNode xform(SqlNodeBase0 sqlNode)
    { 
        if ( ! alwaysCopy )
            return sqlNode ;
        return sqlNode.copy() ;
    }

    private SqlNode xform(SqlNodeBase1 sqlNode, SqlNode subNode)
    { 
        if ( ! alwaysCopy && sqlNode.getSubNode() == subNode )
            return sqlNode ;
        return sqlNode.copy(subNode) ;
    }
    
    private SqlNode xform(SqlNodeBase2 sqlNode, SqlNode left, SqlNode right)
    {
        if ( ! alwaysCopy && sqlNode.getLeft() == left && sqlNode.getRight() == right )
            return sqlNode ;
        return sqlNode.copy(left, right) ;
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
