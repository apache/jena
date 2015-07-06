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

package org.apache.jena.sdb.core;

import java.util.function.Consumer;
import java.util.function.Predicate;

import org.apache.jena.sdb.core.sqlexpr.SqlColumn ;
import org.apache.jena.sparql.core.Var ;

public class ScopeEntry
{
    Var var;
    SqlColumn column;
    ScopeStatus status ;
    
    public static Predicate<ScopeEntry> OptionalFilter = item -> item.getStatus() == ScopeStatus.OPTIONAL;
    
    public static Consumer<ScopeEntry> SetOpt = item -> item.setStatus(ScopeStatus.OPTIONAL) ;
    
    public ScopeEntry(Var var, SqlColumn column)
    { this(var, column, ScopeStatus.FIXED) ; }

    private ScopeEntry(Var var, SqlColumn column, ScopeStatus status)
    {
        this.var = var ; 
        this.column = column ;
        this.status = status ;
    }

    public void reset(Var var, SqlColumn column, ScopeStatus status)
    {
        this.var = var ; 
        this.column = column ;
        this.status = status ;
    }
    
    public ScopeEntry duplicate()
    {
        return new ScopeEntry(var, column, status) ;
    }
    
    
    public SqlColumn getColumn()
    {
        return column ;
    }

    public ScopeStatus getStatus()
    {
        return status ;
    }

    public void setStatus(ScopeStatus newStatus)
    {
        status = newStatus ;
    }

    public boolean isOptional()     { return hasStatus(ScopeStatus.OPTIONAL) ; }
    public boolean isFixed()        { return hasStatus(ScopeStatus.FIXED) ; }
    
    public boolean hasStatus(ScopeStatus testStatus2)
    { return status == testStatus2 ; }
    
    public Var getVar()
    {
        return var ;
    }
    
    @Override
    public String toString() { return "("+var+", "+column+"/"+status.name()+")" ; }
}
