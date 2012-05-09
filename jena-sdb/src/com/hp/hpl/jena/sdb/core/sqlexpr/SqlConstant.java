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

package com.hp.hpl.jena.sdb.core.sqlexpr;

import com.hp.hpl.jena.sdb.sql.SQLUtils;

public class SqlConstant extends SqlExprBase
{
    private String str ;
    
    // Form already encoded for this schema
    public SqlConstant(String str) { this.str = SQLUtils.quoteStr(str) ; }
    public SqlConstant(long number) { this.str = Long.toString(number) ; }
    
    public String  asSqlString()
    { return str ; }
    
    @Override
    public boolean isConstant() { return true ; }
    
    @Override
    public void visit(SqlExprVisitor visitor) { visitor.visit(this) ; }
}
