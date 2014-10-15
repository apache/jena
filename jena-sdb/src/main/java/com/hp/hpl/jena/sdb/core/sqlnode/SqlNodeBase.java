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

import java.util.LinkedHashSet;
import java.util.Set;

import com.hp.hpl.jena.sdb.core.AnnotationsBase;
import com.hp.hpl.jena.sdb.core.sqlexpr.SqlColumn;
import org.apache.jena.atlas.io.IndentedLineBuffer;
import org.apache.jena.atlas.io.IndentedWriter;
import com.hp.hpl.jena.sparql.util.Utils;


public abstract class SqlNodeBase extends AnnotationsBase implements SqlNode
{
    protected String aliasName ;
    
    public SqlNodeBase(String aliasName) { this.aliasName = aliasName ; }
    
    @Override
    public boolean      isJoin()      { return false ; }
    @Override
    public boolean      isInnerJoin() { return false ; }
    @Override
    public boolean      isLeftJoin()  { return false ; }
//    public boolean isRightJoin() { return false ; }
//    public boolean isOuterJoin() { return false ; }

    @Override
    public SqlJoin      asJoin()        { classError(SqlJoin.class) ; return null  ; }
    @Override
    public SqlJoinLeftOuter     asLeftJoin() { classError(SqlJoinLeftOuter.class) ; return null  ; }
    @Override
    public SqlJoinInner         asInnerJoin(){ classError(SqlJoinInner.class) ; return null  ; }

    @Override
    public boolean      isRestrict()    { return false ; }
    @Override
    public SqlRestrict  asRestrict()    { classError(SqlRestrict.class) ; return null  ; }

    @Override
    public boolean      isProject()     { return false ; }
    @Override
    public SqlProject   asProject()     { classError(SqlProject.class) ; return null  ; }

    @Override
    public boolean      isDistinct()     { return false ; }
    @Override
    public SqlDistinct  asDistinct()     { classError(SqlDistinct.class) ; return null  ; }

    @Override
    public boolean      isCoalesce()    { return false ; }
    @Override
    public SqlCoalesce  asCoalesce()    { classError(SqlCoalesce.class) ; return null  ; }
    
    @Override
    public boolean      isTable()       { return false ; }
    @Override
    public SqlTable     asTable()       { classError(SqlTable.class) ; return null  ; }

    @Override
    public boolean         isSelectBlock() { return false ; }
    @Override
    public SqlSelectBlock  asSelectBlock() { classError(SqlSelectBlock.class) ; return null  ; }

    @Override
    public void output(IndentedWriter out)  { output(out, true) ; }
    
    public void output(IndentedWriter out, boolean withAnnotations)
    { this.visit(new SqlNodeTextVisitor(out, withAnnotations)) ; }
    
    // Scope
    
//    public boolean hasColumnForVar(Var var) { return getColumnForVar(var) != null ; }
//    public Iterator<Var> vars()
//    { return getVars().iterator() ; }
    
    public boolean usesColumn(SqlColumn c) { return false ; }
    
    @Override
    final
    public String getAliasName() { return aliasName ; }

    private void classError(Class<?> wanted)
    {
        throw new ClassCastException("Wanted class: "+Utils.className(wanted)+" :: Actual class "+Utils.className(this) ) ;
    }
    
    @Override
    public Set<SqlTable> tablesInvolved()
    {
        TableFinder t = new TableFinder() ;
        SqlNodeWalker.walk(this, t) ;
        return t.acc ;
    }

    @Override public String toString()
    {
        IndentedLineBuffer buff = new IndentedLineBuffer() ;
        output(buff, true) ;
        return buff.asString() ;
    }
}

class TableFinder extends SqlNodeVisitorBase
{
    Set<SqlTable> acc = new LinkedHashSet<SqlTable>() ;
    
    @Override
    public void visit(SqlTable sqlNode)
    {
        acc.add(sqlNode) ;
    }
}
