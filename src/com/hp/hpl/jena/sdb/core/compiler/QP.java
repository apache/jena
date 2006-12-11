/*
 * (c) Copyright 2006 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sdb.core.compiler;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.LogFactory;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.engine.Binding;
import com.hp.hpl.jena.query.core.Var;
import com.hp.hpl.jena.query.engine.QueryIterator;
import com.hp.hpl.jena.query.engine1.ExecutionContext;
import com.hp.hpl.jena.sdb.core.SDBRequest;
import com.hp.hpl.jena.sdb.core.sqlnode.SqlNode;
import com.hp.hpl.jena.sdb.sql.RS;
import com.hp.hpl.jena.sdb.sql.SDBExceptionSQL;
import com.hp.hpl.jena.sdb.store.SQLBridge;

public class QP 
{
    public static boolean PrintSQL = false ;
    
    public static QueryIterator exec(OpSQL opSQL, SDBRequest request, Binding binding, ExecutionContext execCxt)
    {
        SQLBridge bridge = request.getStore().getSQLBridgeFactory().create() ;
        String sqlStmt = toSqlString(opSQL, request, bridge) ;
    
        if ( PrintSQL )
            System.out.println(sqlStmt) ;
        
        try {
            java.sql.ResultSet jdbcResultSet = request.getStore().getConnection().execQuery(sqlStmt) ;
            if ( false )
                // Destructive
                RS.printResultSet(jdbcResultSet) ;
            try {
                return bridge.assembleResults(jdbcResultSet, binding, execCxt) ;
            } finally { jdbcResultSet.close() ; }
        } catch (SQLException ex)
        {
            throw new SDBExceptionSQL("SQLException in executing SQL statement", ex) ;
        }
    }

    
    public static SqlNode toSqlTopNode(SqlNode sqlNode, 
                                       List<Var> projectVars,
                                       SQLBridge bridge)
    {
        bridge.init(sqlNode, projectVars) ;
        sqlNode = bridge.buildProject() ;
        return sqlNode ;
    }
    
    public static String toSqlString(OpSQL opSQL, 
                                     SDBRequest request, 
                                     SQLBridge bridge)
    {
        if ( bridge == null )
            // Direct call to produce SQL strings for printing 
            bridge = request.getStore().getSQLBridgeFactory().create() ;
        List<Var> projectVars = QP.projectVars(request.getQuery()) ;
        SqlNode sqlNode = QP.toSqlTopNode(opSQL.getSqlNode(), projectVars, bridge) ;
        String sqlStmt = request.getStore().getSQLGenerator().generateSQL(sqlNode) ;
        return sqlStmt ; 
    }
    
    public static List<Var> projectVars(Query query)
    {
        List<Var> vars = new ArrayList<Var>() ;
        @SuppressWarnings("unchecked")
        List<String> list = (List<String>)query.getResultVars() ;
        if ( list.size() == 0 )
            LogFactory.getLog(QP.class).warn("No project variables") ;
        for ( String vn  : list )
            vars.add(Var.alloc(vn)) ;
        return vars ;
    }
}

/*
 * (c) Copyright 2006 Hewlett-Packard Development Company, LP
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