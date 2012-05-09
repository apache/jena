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

import com.hp.hpl.jena.sdb.core.Scope;

public class SqlUnion extends SqlNodeBase2
{
    public SqlUnion(SqlNode left, SqlNode right)
    {
        super(null, left, right) ;
    }

    @Override
    public Scope getIdScope()
    {
        return null ;
    }

    @Override
    public Scope getNodeScope()
    {
        return null ;
    }

    @Override
    public SqlNode apply(SqlTransform transform, SqlNode left, SqlNode right)
    { return transform.transform(this, left, right) ; }

    @Override
    public SqlNode copy(SqlNode left, SqlNode right)
    {
        return new SqlUnion(left, right) ;
    }

    @Override
    public void visit(SqlNodeVisitor visitor)   { visitor.visit(this) ; }
}
