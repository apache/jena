/*
 * (c) Copyright 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sdb.compiler;

import static org.openjena.atlas.iterator.Iter.* ;
import java.sql.SQLException;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.SortCondition;
import com.hp.hpl.jena.sdb.SDB;
import com.hp.hpl.jena.sdb.core.SDBConstants;
import com.hp.hpl.jena.sdb.core.SDBRequest;
import com.hp.hpl.jena.sdb.core.sqlnode.SqlNode;
import com.hp.hpl.jena.sdb.sql.ResultSetJDBC;
import com.hp.hpl.jena.sdb.sql.SDBExceptionSQL;
import com.hp.hpl.jena.sparql.algebra.Op;
import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.sparql.engine.ExecutionContext;
import com.hp.hpl.jena.sparql.engine.QueryIterator;
import com.hp.hpl.jena.sparql.engine.binding.Binding;
import org.openjena.atlas.iterator.Transform;

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
        List<Var> vars = toList(map(query.getResultVars(), StringToVar)) ;
        
        if ( vars.size() == 0 )
        {
            // This works around a bug in ARQ 2.7.0 where a SPARQL/Update is made programmtically.
            // The query is not created fully - this worksaround that.
            // TODO Remove when ARQ 2.7.1/2.8.0 released.
            // Can occur with programmatically created patterns that are not fully setup.
            Query q2 = new Query() ;
            q2.setQueryPattern(query.getQueryPattern()) ;
            q2.setQuerySelectType() ;
            q2.setQueryResultStar(true) ;
            vars = toList(map(q2.getResultVars(), StringToVar)) ;
        }
        
        
        if ( vars.size() == 0 )
        {
            // SELECT * {} or SPARQL Update with WHERE {}
            // LoggerFactory.getLogger(SDB_QC.class).warn("No project variables") ;
        }
        
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

    
    private static Transform<String, Var> StringToVar = new Transform<String, Var>(){
        public Var convert(String varName)
        {
            return Var.alloc(varName) ;
        }} ;
}

/*
 * (c) Copyright 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. The name of the author may not be used to endorse or promote products
 *    derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */