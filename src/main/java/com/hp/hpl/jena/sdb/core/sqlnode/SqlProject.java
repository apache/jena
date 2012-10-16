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

import java.util.ArrayList;
import java.util.List;

import com.hp.hpl.jena.sdb.core.sqlexpr.SqlColumn;
import com.hp.hpl.jena.sdb.shared.SDBInternalError;

// Unused - see SqlSelectBlock 
public class SqlProject extends SqlNodeBase1
{
    // Development:
    // + SqlRename is an alias and just SqlProject of pre-calculated
    //   columns because SQL uses SELECT for both.
    // + SqlCoalesce is a SqlProject with a COALESCE function 
    
    List <ColAlias> cols = null ; 
    
    // ---- Factory methods
    
    /** make sure this node is a projection */
    
    // This is not quite SqlSelectBlock.
    /*public*/private static SqlNode project(SqlNode sqlNode)
    {
        return project(sqlNode, null) ;
    }
    
    /** make sure this node is a projection and add a column */

    /*public*/private static SqlNode project(SqlNode sqlNode, SqlColumn col, String colOutName)
    {
        SqlColumn asCol = new SqlColumn(null, colOutName) ; 
        ColAlias colAlias = new ColAlias(col, asCol) ;
        return SqlProject.project(sqlNode, colAlias) ;
    }
    
    /** make sure this node is a projection and add a column */

    /*public*/public static SqlNode project(SqlNode sqlNode, ColAlias col)
    {
        // Not if to be left to the bridge.
        //return SqlSelectBlock.project(sqlNode, col) ;
        
        SqlProject p = ensure(sqlNode) ;
        
        if ( col != null )
        {
            verify(p, col) ;
            col.check(sqlNode.getAliasName()) ;
            p.cols.add(col) ;
        }
        return p ;
    }
    
    private static void verify(SqlProject p, ColAlias col)
    {
        String newColName = col.getAlias().getColumnName() ;
        
        for ( ColAlias a : p.getCols() )
            if ( a.getAlias().getColumnName().equals(newColName) )
                throw new SDBInternalError("Attempt to use same alias twice") ;
    }

    private static SqlProject ensure(SqlNode sqlNode)
    {
        if ( sqlNode.isProject() )
            return sqlNode.asProject() ;
        else
            return new SqlProject(sqlNode) ;
    }
    
    // ----
    
    private SqlProject(SqlNode sqlNode)
    { this(sqlNode, new ArrayList<ColAlias>()) ; }
    
    private SqlProject(SqlNode sqlNode, List<ColAlias> cols)
    { 
        super(null, sqlNode) ;
        this.cols = cols ; 
    }
    
    @Override
    public boolean isProject() { return true ; }
    @Override
    public SqlProject asProject() { return this ; }
    
    @Override 
    public boolean usesColumn(SqlColumn c) { return cols.contains(c) ; }

    public List<ColAlias> getCols() { return cols ; }

    @Override
    public void visit(SqlNodeVisitor visitor)
    { throw new SDBInternalError("SqlProject.visit") ; }
    //{ visitor.visit(this) ; }
    
    @Override
    public SqlNode apply(SqlTransform transform, SqlNode subNode)
    { throw new SDBInternalError("SqlProject.apply") ; }
    //{ return transform.transform(this, subNode) ; }

    @Override
    public SqlNode copy(SqlNode subNode)
    {
        return new SqlProject(subNode, this.getCols()) ;
    }
}
