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

package com.hp.hpl.jena.sdb.core.sqlnode;


public class SqlTransformCopy implements SqlTransform
{
    public static final boolean COPY_ALWAYS         = true ;
    public static final boolean COPY_ONLY_ON_CHANGE = false ;
    private boolean alwaysCopy = false ;
    
    public SqlTransformCopy() { this(COPY_ONLY_ON_CHANGE) ; }
    public SqlTransformCopy(boolean alwaysDuplicate)   { this.alwaysCopy = alwaysDuplicate ; }
    
    @Override
    public SqlNode transform(SqlProject sqlProject, SqlNode subNode)
    { return xform(sqlProject, subNode) ; }

    public SqlNode transform(SqlDistinct sqlDistinct, SqlNode subNode)
    { return xform(sqlDistinct, subNode) ; }

    @Override
    public SqlNode transform(SqlRestrict sqlRestrict, SqlNode subNode)
    { return xform(sqlRestrict, subNode) ; }

    public SqlNode transform(SqlSlice sqlSlice, SqlNode subNode)
    { return xform(sqlSlice, subNode) ; }

    @Override
    public SqlNode transform(SqlSelectBlock sqlSelectBlock, SqlNode subNode)
    { return xform(sqlSelectBlock, subNode) ; }

    @Override
    public SqlNode transform(SqlJoinInner sqlJoinInner, SqlNode left, SqlNode right)
    { return xform(sqlJoinInner, left, right) ; }

    @Override
    public SqlNode transform(SqlJoinLeftOuter sqlJoinLeftOuter, SqlNode left, SqlNode right)
    { return xform(sqlJoinLeftOuter, left, right) ; }

    @Override
    public SqlNode transform(SqlUnion sqlUnion, SqlNode left, SqlNode right)
    { return xform(sqlUnion, left, right) ; } 

    @Override
    public SqlNode transform(SqlTable sqlTable)
    { return xform(sqlTable) ; }

    @Override
    public SqlNode transform(SqlRename sqlRename, SqlNode subNode)
    { return xform(sqlRename, subNode) ; } 

    @Override
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
