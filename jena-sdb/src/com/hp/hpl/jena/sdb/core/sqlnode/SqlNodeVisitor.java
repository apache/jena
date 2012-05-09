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

public interface SqlNodeVisitor
{
    public void visit(SqlTable          sqlTable) ;
    public void visit(SqlSelectBlock    sqlSelectBlock) ;
    
    // TO GO
    //public void visit(SqlProject        sqlProject) ;       // Removeable?

    //public void visit(SqlSlice          sqlSlice) ;
    //public void visit(SqlRestrict       sqlRestrict) ;
    //public void visit(SqlDistinct       sqlDistinct) ;
    //public void visit(SqlRename         sqlRename) ;
    
    public void visit(SqlJoinInner      sqlJoin) ;
    public void visit(SqlJoinLeftOuter  sqlJoin) ;
    public void visit(SqlUnion          sqlUnion) ;
    public void visit(SqlCoalesce       sqlCoalesce) ;
    
}
