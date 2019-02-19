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

package org.apache.jena.sdb.compiler;

import static org.apache.jena.atlas.iterator.Iter.* ;

import java.sql.SQLException;
import java.util.List;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.jena.query.Query ;
import org.apache.jena.query.SortCondition ;
import org.apache.jena.sdb.SDB ;
import org.apache.jena.sdb.core.SDBConstants ;
import org.apache.jena.sdb.core.SDBRequest ;
import org.apache.jena.sdb.core.sqlnode.SqlNode ;
import org.apache.jena.sdb.sql.ResultSetJDBC ;
import org.apache.jena.sdb.sql.SDBExceptionSQL ;
import org.apache.jena.sparql.algebra.Op ;
import org.apache.jena.sparql.core.Var ;
import org.apache.jena.sparql.engine.ExecutionContext ;
import org.apache.jena.sparql.engine.QueryIterator ;
import org.apache.jena.sparql.engine.binding.Binding ;

public class SDB_QC
{
    private static Logger log = LoggerFactory.getLogger(SDB_QC.class) ;
    
    public static boolean fetchPrint = false ;
    public static boolean PrintSQL = false ;
    
    // ---- Execute an OpSQL.
    
    public static QueryIterator exec(OpSQL opSQL, SDBRequest request, Binding binding, ExecutionContext execCxt)
    {
        String sqlStmtStr = toSqlString(opSQL, request) ;
        
        if ( PrintSQL )
            System.out.println(sqlStmtStr) ;
        
        String str = null ;
        if ( execCxt != null )
            str = execCxt.getContext().getAsString(SDB.jdbcFetchSize) ;
        
        int fetchSize = SDBConstants.jdbcFetchSizeOff;
        
        if ( str != null )
            try { fetchSize = Integer.parseInt(str) ; }
            catch (NumberFormatException ex)
            { log.warn("Bad number for fetch size: "+str) ; }
        
        try {
            ResultSetJDBC jdbcResultSet = request.getStore().getConnection().execQuery(sqlStmtStr, fetchSize) ;
            try {
                // And check this is called once per SQL.
                if ( opSQL.getBridge() == null )
                    log.error("Null bridge") ;
                return opSQL.getBridge().assembleResults(jdbcResultSet, binding, execCxt) ;
            } finally {
                // ResultSet closed inside assembleResults or by the iterator returned.
                jdbcResultSet = null ;
            }
        } catch (SQLException ex)
        {
            throw new SDBExceptionSQL("SQLException in executing SQL statement", ex) ;
        }
    }

    public static String toSqlString(OpSQL opSQL, 
                                     SDBRequest request)
    {
        SqlNode sqlNode = opSQL.getSqlNode() ;
        String sqlStmt = request.getStore().getSQLGenerator().generateSQL(request, sqlNode) ;
        return sqlStmt ; 
    }
    
    /** Find the variables needed out of this query.
     * If we don't do sorting in-DB, then we need the ORDER BY variables as well. 
     * @param query
     */
    public static List<Var> queryOutVars(Query query)
    {
        // If part query, need all variables. 
        
        // Project variables
        List<Var> vars = toList(map(query.getResultVars().iterator(), Var::alloc)) ;
        
        // Add the ORDER BY variables
        List<SortCondition> orderConditions = query.getOrderBy() ;
        if ( orderConditions != null )
        {
            for ( SortCondition sc : orderConditions )
            {
                Set<Var> x = sc.getExpression().getVarsMentioned() ;
                for ( Var v :  x )
                {
                    if ( ! vars.contains(v) )
                        vars.add(v) ;
                }
            }
        }
        return vars ;
    }
    
    
    public static boolean isOpSQL(Op x)
    {
        return ( x instanceof OpSQL ) ;
    }
}
