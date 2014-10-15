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

public interface SqlTransform
{
    public SqlNode transform(SqlProject sqlProject, SqlNode subNode) ;

    //public SqlNode transform(SqlDistinct sqlDistinct, SqlNode subNode) ;

    public SqlNode transform(SqlRestrict sqlRestrict, SqlNode subNode) ;

    public SqlNode transform(SqlRename sqlRename, SqlNode subNode) ;

    //public SqlNode transform(SqlSlice sqlSlice, SqlNode subNode) ;

    public SqlNode transform(SqlSelectBlock sqlSelectBlock, SqlNode newSubNode) ;

    public SqlNode transform(SqlCoalesce sqlCoalesce, SqlNode subNode) ;

    public SqlNode transform(SqlJoinInner sqlJoinInner, SqlNode left, SqlNode right) ;

    public SqlNode transform(SqlJoinLeftOuter sqlJoinLeftOuter, SqlNode left, SqlNode right) ;

    public SqlNode transform(SqlUnion sqlUnion, SqlNode left, SqlNode right) ;

    public SqlNode transform(SqlTable sqlTable) ;

}
