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

import java.util.HashSet ;
import java.util.Set ;

import org.apache.jena.atlas.lib.SetUtils ;
import org.slf4j.LoggerFactory ;

import com.hp.hpl.jena.sdb.core.AliasesSql ;
import com.hp.hpl.jena.sdb.core.Annotation1 ;
import com.hp.hpl.jena.sdb.core.SDBRequest ;
import com.hp.hpl.jena.sdb.core.Scope ;
import com.hp.hpl.jena.sdb.core.ScopeRename ;
import com.hp.hpl.jena.sdb.core.sqlexpr.SqlColumn ;
import com.hp.hpl.jena.sparql.core.Var ;

public class SqlCoalesce extends SqlNodeBase1
{
    /* A COALESCE is an operations that takes 
     * variables from the left and right sides of a join
     * and finds the first (left to right) that is defined (not NULL).
     * That's the COALESCE function in many SQL databases.
     */
    
    private SqlJoin join ;
    private Set<Var> coalesceVars ;
    private Set<Var> nonCoalesceVars = new HashSet<Var>() ;
    private ScopeRename idScope ;
    private ScopeRename nodeScope ;
    
    private static final String AliasBase = AliasesSql.VarCollasce ;
    
    public static SqlCoalesce create(SDBRequest request, String alias, SqlJoin join, Set<Var>coalesceVars) 
    {
        // This is not actually true in general.
        // But at the moment, it is a restriction so we test for it for now 
        // as a sanity check. Remove the test when the new situation arises 
        // as this class then needs to be checked. 
        if ( ! join.isLeftJoin() )
            LoggerFactory.getLogger(SqlCoalesce.class).warn("SqlCoalesce node is not a LeftJoin") ;
        
        return new SqlCoalesce(request, alias, join, coalesceVars) ;
    }
    
    private SqlCoalesce(String alias, SqlJoin join, Set<Var> coalesceVars)
    { 
        super(alias, join) ;
        this.join = join ;
        this.coalesceVars = coalesceVars ;
    }
    
    private SqlCoalesce(SDBRequest request, String alias, SqlJoin join, Set<Var> coalesceVars)
    { 
        this(alias, join, coalesceVars) ;
        Annotation1 annotation = new Annotation1(true) ;
        
        // ScopeCoalesce needed
        // Scope is:
        // new ScopeRename(oldScope, renames) ;
        // And ScopeBase ==> ScopeTable.
        
        idScope = new ScopeRename(join.getIdScope()) ;
        nodeScope = new ScopeRename(join.getNodeScope()) ;
        SqlTable table = new SqlTable(alias) ;
        
        nonCoalesceVars = SetUtils.difference(join.getIdScope().getVars(),
                                              coalesceVars) ;

        // In layout1, NodeScope is the same as IdScope
//        if ( join.getNodeScope().getVars().size() != 0 )
//            LoggerFactory.getLogger(SqlCoalesce.class).warn("NodeScope is not empty") ;
        
        for ( Var v : coalesceVars )
        {
            String sqlColName = request.genId(AliasBase) ;
            SqlColumn col = new SqlColumn(table, sqlColName) ;
            idScope.setAlias(v, col) ;
            annotation.addAnnotation(v+" as "+col) ;
            // TODO Value
        }
        
        // Aliases.
        // Not coalesce variables.
        for ( Var v : nonCoalesceVars )
        {
            if ( coalesceVars.contains(v) )
            {
                LoggerFactory.getLogger(SqlCoalesce.class).warn("Variable in coalesce and non-coalesce sets: "+v) ;
                continue ;
            }
            String sqlColName = request.genId(AliasBase) ;
            SqlColumn col = new SqlColumn(table, sqlColName) ;
            idScope.setAlias(v, col) ;
            annotation.addAnnotation(v+" as "+col) ;
            // TODO Value
        }
        annotation.setAnnotation(this) ;
    }
    
    public Set<Var> getCoalesceVars()       { return coalesceVars ; }
    public Set<Var> getNonCoalesceVars()    { return nonCoalesceVars ; }
    
    @Override
    public boolean      isCoalesce()    { return true ; }
    @Override
    public SqlCoalesce  asCoalesce()    { return this ; }

    @Override
    public Scope getIdScope()           { return idScope ; }
    
    @Override
    public Scope getNodeScope()         { return nodeScope ; }
    
    public SqlJoin getJoinNode()        { return join ; }
    
    @Override
    public void visit(SqlNodeVisitor visitor)
    { visitor.visit(this) ; }

    @Override
    public SqlNode apply(SqlTransform transform, SqlNode newSubNode)
    {
        return transform.transform(this, newSubNode) ;
    }

    @Override
    public SqlNode copy(SqlNode subNode)
    {
        // May need to do a deeper copy.
        SqlCoalesce s = new SqlCoalesce(getAliasName(), subNode.asJoin(), this.coalesceVars) ;
        s.nonCoalesceVars  = this.nonCoalesceVars ;
        s.idScope = this.idScope ;
        s.nodeScope = this.nodeScope ;
        return s ;
    }
}
