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

package org.apache.jena.sdb.core.sqlnode;

import java.util.ArrayList;
import java.util.List;

import org.apache.jena.sdb.core.* ;
import org.apache.jena.sdb.shared.SDBInternalError ;

/** SQL rename */
//Not used - may be removed
/*public*/ class SqlRename extends SqlNodeBase1
{
    private ScopeBase idScope ;
    private ScopeBase nodeScope ;
    private SqlTable vTable ;       // Our column naming space.
    private List<ColAlias> columns ;
    
    //---

    private SqlRename(SqlTable here, SqlNode sqlNode)
    {
        super(here.getAliasName(), sqlNode) ;
        this.vTable = here ;
        this.idScope = new ScopeBase() ;
        this.nodeScope = new ScopeBase() ;
        this.columns = new ArrayList<ColAlias>() ;
    }

    private SqlRename(SqlRename other)
    {
        super(other.vTable.getAliasName(), other.getSubNode()) ;
        this.vTable = other.vTable ;
        this.idScope = other.idScope ;
        this.nodeScope = other.nodeScope ;
        this.columns = new ArrayList<ColAlias>(other.columns) ;
    }

    @Override
    public Scope getIdScope()   { return idScope ; }
    @Override
    public Scope getNodeScope() { return nodeScope ; }
    
    @Override
    public void visit(SqlNodeVisitor visitor)
    { throw new SDBInternalError("SqlRename.visit") ; }
    // { visitor.visit(this) ; }

    @Override
    public SqlNode apply(SqlTransform transform, SqlNode subNode)
    { throw new SDBInternalError("SqlRename.apply") ; }
    // { return transform.transform(this, subNode) ; }

    @Override
    public SqlNode copy(SqlNode subNode)
    { return new SqlRename(this) ; }
}
