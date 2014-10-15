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

import java.util.HashSet;
import java.util.Set;

import com.hp.hpl.jena.sdb.core.AnnotationsBase;
import org.apache.jena.atlas.io.IndentedLineBuffer;

public abstract class SqlExprBase extends AnnotationsBase implements SqlExpr
{
    @Override
    public final String toString()
    {
        return asSQL(this) ;
    }
    
    @Override
    public String asSQL() { return asSQL(this) ; } 
    
    public static String asSQL(SqlExpr expr)
    {
        IndentedLineBuffer buff = new IndentedLineBuffer() ;
        SqlExprVisitor v = new SqlExprGenerateSQL(buff) ;
        expr.visit(v) ;
        return buff.toString() ; 
    }
    
    @Override
    public Set<SqlColumn> getColumnsNeeded()
    {
        Set<SqlColumn> acc = new HashSet<SqlColumn>() ;
        SqlExprVisitor v = new SqlExprColumnsUsed(acc) ;
        SqlExprWalker.walk(this, v) ;
        return acc ;
    }
    
    @Override
    public boolean isColumn()   { return false ; }
    @Override
    public boolean isConstant() { return false ; }

}
