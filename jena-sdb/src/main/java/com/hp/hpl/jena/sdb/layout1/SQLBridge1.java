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

package com.hp.hpl.jena.sdb.layout1;

import java.sql.SQLException;
import java.util.List;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.sdb.core.SDBRequest;
import com.hp.hpl.jena.sdb.core.ScopeEntry;
import com.hp.hpl.jena.sdb.core.sqlexpr.SqlColumn;
import com.hp.hpl.jena.sdb.core.sqlnode.SqlNode;
import com.hp.hpl.jena.sdb.sql.ResultSetJDBC;
import com.hp.hpl.jena.sdb.sql.SQLUtils;
import com.hp.hpl.jena.sdb.store.SQLBridgeBase;
import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.sparql.engine.binding.Binding;
import com.hp.hpl.jena.sparql.engine.binding.BindingFactory ;
import com.hp.hpl.jena.sparql.engine.binding.BindingMap;

public class SQLBridge1 extends SQLBridgeBase
{
    private EncoderDecoder codec ;
    
    public SQLBridge1(SDBRequest request, SqlNode sqlNode, List<Var> projectVars, EncoderDecoder codec)
    { 
        super(request, sqlNode, projectVars) ;
        this.codec = codec ;
    }
    
    @Override
    protected void buildValues()
    { 
        // No added SQL operations to get the values.
        // If it is a SelectBlock for renaming purposes, strip the
        // renames out as they are unnecessary.  Affects scopes.
        if ( getSqlNode().isSelectBlock() )
            setSqlNode(getSqlNode().asSelectBlock().clearView()) ;
    }

    @Override
    protected void buildProject()
    {
        for ( Var v : getProject() )
        {
            if ( ! v.isNamedVar() )
                continue ;
            // Value scope == IdScope for layout1
            // CHECK
            ScopeEntry e = getSqlExprNode().getIdScope().findScopeForVar(v) ; 
            if ( e == null )
                continue ;
            SqlColumn c = e.getColumn() ;
            String sqlVarName = allocSqlName(v) ;
            addProject(c, sqlVarName) ;
            addAnnotation(sqlVarName+"="+v.toString()) ;
        }
        setAnnotation() ;
    }
    
    @Override
    protected Binding assembleBinding(ResultSetJDBC rs, Binding parent)
    {
        BindingMap b = BindingFactory.create(parent) ;
        for ( Var v : getProject() )
        {
            String sqlVarName = getSqlName(v) ;
            
            if ( sqlVarName == null )
                // Not mentioned in query.
                continue ;
            try {
                // because of encoding into SPARQL terms, this is never the empty string.
                String s = rs.get().getString(sqlVarName) ;
                // Same as rs.wasNull() for things that can return Java nulls.
                if ( s == null )
                    continue ;
                // TupleLoaderSimple used SqlConstant which made the string SQL-safe
                // so it could be embedded in a non-prepared statement.  
                s = SQLUtils.unescapeStr(s) ;
                Node n = codec.decode(s) ;
                b.add(v, n) ;
                // Ignore any access error (variable requested not in results)
            } catch (SQLException ex) {}
        }
        return b ;
    }
}
