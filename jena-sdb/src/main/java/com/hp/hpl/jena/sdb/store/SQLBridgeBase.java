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

package com.hp.hpl.jena.sdb.store;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.hp.hpl.jena.sdb.SDB;
import com.hp.hpl.jena.sdb.compiler.SqlBuilder;
import com.hp.hpl.jena.sdb.core.AliasesSql;
import com.hp.hpl.jena.sdb.core.Annotation1;
import com.hp.hpl.jena.sdb.core.SDBRequest;
import com.hp.hpl.jena.sdb.core.sqlexpr.SqlColumn;
import com.hp.hpl.jena.sdb.core.sqlnode.ColAlias;
import com.hp.hpl.jena.sdb.core.sqlnode.SqlNode;
import com.hp.hpl.jena.sdb.shared.SDBInternalError;
import com.hp.hpl.jena.sdb.sql.RS;
import com.hp.hpl.jena.sdb.sql.ResultSetJDBC;
import com.hp.hpl.jena.sdb.sql.SDBExceptionSQL;
import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.sparql.engine.ExecutionContext;
import com.hp.hpl.jena.sparql.engine.QueryIterator;
import com.hp.hpl.jena.sparql.engine.binding.Binding;
import com.hp.hpl.jena.sparql.engine.iterator.QueryIter;
import com.hp.hpl.jena.sparql.engine.iterator.QueryIterPlainWrapper;

/** Convert from whatever results a particular layout returns into
 *  an ARQ QueryIterator of Bindings.  An SQLBridge object
 *  is allocated for each query execution. 
 */  

public abstract class SQLBridgeBase implements SQLBridge
{
    // Always need to allocate a label for a column 
    // because can't access by table.column
    private Map<Var, String>varLabels = new HashMap<Var, String>() ;
    private Annotation1 annotation = new Annotation1() ;
    private Collection<Var> projectVars = null ;
    private SqlNode sqlNodeOriginal = null ;
    private SqlNode sqlNode = null ;                 // Subclass can mutate
    protected final SDBRequest request ;
    
    protected SQLBridgeBase(SDBRequest request, SqlNode sqlNode, Collection<Var> projectVars)
    {
        this.request = request ;
        this.sqlNodeOriginal = sqlNode ;
        this.projectVars = projectVars ;
        this.sqlNode = sqlNode ;
    }
    
    @Override
    public final
    void build()
    {
        buildValues() ;
        buildProject() ;
    }
    
    protected abstract void buildValues() ;
    protected abstract void buildProject() ;

    // Build next row from the JDBC ResultSet
    protected abstract Binding assembleBinding(ResultSetJDBC rs, Binding binding) ;

    @Override
    final
    public QueryIterator assembleResults(ResultSetJDBC rs, Binding binding, ExecutionContext execCxt)
    {
        if ( execCxt == null || execCxt.getContext().isTrueOrUndef(SDB.jdbcStream) )
        {
            // Stream
            return new QueryIterSQL(rs, binding, execCxt) ;
        }
        
        // Debugging or problems with unreleasing JDBC ResultSets - read all in now.
        QueryIterator qIter = new QueryIterSQL(rs, binding, execCxt) ;
        List<Binding> results = new ArrayList<Binding>() ;
        for ( ; qIter.hasNext() ; )
            results.add(qIter.nextBinding()) ;
        qIter.close() ;
        return new QueryIterPlainWrapper(results.iterator(), execCxt) ;
    }

    private void setProjectVars(Collection<Var> projectVars)
    {
        if ( this.projectVars != null )
            throw new SDBInternalError("SQLBridgeBase: Project vars already set") ;
        this.projectVars = projectVars ;
    }
    
    protected SqlNode getSqlExprNode()          { return sqlNodeOriginal ; }
    @Override
    public    SqlNode getSqlNode()              { return sqlNode ; }
    protected void setSqlNode(SqlNode sqlNode2) {  sqlNode = sqlNode2 ; }
    protected Collection<Var> getProject()      { return projectVars ; }

    // ---- value support
    
    // ---- project support
    
    protected void addProject(SqlColumn col, String colOutName)
    {
        sqlNode = SqlBuilder.project(request, 
                                     sqlNode, 
                                     new ColAlias(col, new SqlColumn(null, colOutName))) ;
    }
    
    protected void addAnnotation(String string)
    {
        annotation.addAnnotation(string) ;
    }

    protected void setAnnotation()
    {
        annotation.setAnnotation(sqlNode) ;
    }

    // ---- Var allocation
    
    protected String allocSqlName(Var v)
    {
        String sqlVarName = varLabels.get(v) ;
        if ( sqlVarName == null )
        {
            sqlVarName = request.genId(AliasesSql.VarBase) ;
            varLabels.put(v, sqlVarName) ;
        }
        return sqlVarName ;
    }
    
    protected String getSqlName(Var v) { return varLabels.get(v) ; }
    
    private class QueryIterSQL extends QueryIter
    {
        boolean ready = false ;
        boolean hasNext = false ;
        private ResultSetJDBC jdbcResultSet ;
        private Binding parent ;
        
        QueryIterSQL(ResultSetJDBC rs, Binding binding, ExecutionContext execCxt)
        {
            super(execCxt) ;
            this.jdbcResultSet = rs ;
            this.parent = binding ;
        }

        @Override
        protected void closeIterator()
        {
            RS.close(jdbcResultSet) ;
            jdbcResultSet = null ;
        }

        @Override
        protected boolean hasNextBinding()
        {
            if (!ready)
            {
                try
                {
                    hasNext = jdbcResultSet.get().next() ;
                } catch (SQLException ex)
                {
                    closeIterator() ;
                    throw new SDBExceptionSQL(ex) ;
                }
                ready = true ;
            }
            return hasNext ;
        }

        @Override
        protected Binding moveToNextBinding()
        {
//            if ( ! hasNextBinding() )
//                return null ;
            ready = false ; // Must set the state to 'unknown'.
            return assembleBinding(jdbcResultSet, parent) ;
        }

        // Asynchronous request to cancel.  ARQ 2.8.8 and later.
        @Override
        protected void requestCancel()
        {}
        
    }
}
