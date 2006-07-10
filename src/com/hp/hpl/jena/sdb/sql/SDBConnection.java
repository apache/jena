/*
 * (c) Copyright 2005, 2006 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sdb.sql;

/** The abstraction of a connection to an SQL-backed Jena graph for SDB */

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.sql.DataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hp.hpl.jena.graph.TransactionHandler;
import com.hp.hpl.jena.sdb.Access;
import com.hp.hpl.jena.sdb.SDBException;
import com.hp.hpl.jena.sdb.graph.TransactionHandlerSDB;
import com.hp.hpl.jena.shared.Command;

public class SDBConnection
{
    static private Log log = LogFactory.getLog(SDBConnection.class) ;
    private Connection sqlConnection = null ;
    boolean inTransaction = false ;
    TransactionHandler transactionHandler = null ;
    String label = "<unset>" ;
    
    // TODO (here or TransactionHandler) counting transactions.
    //   record transaction state
    //   start transation on 0 -> 1
    //   commit transaction on 1 -> 0
    //   abort on any exception
    // Hard and soft forms of executeInTransaction 
    // begin/commit/abort call-throughs
    
    // Make per-connection 
    public static boolean logSQLExceptions = false ;
    public static boolean logSQLStatements = false ;
    public static boolean logSQLQueries    = false ;

    public SDBConnection(DataSource ds) throws SQLException
    {
        this(ds.getConnection()) ;
    }
    
    public SDBConnection(SDBConnectionDesc desc)
    {
        desc.initJDBC() ;
        if ( desc.driver != null )
            JDBC.loadDriver(desc.driver) ;
        init(desc.jdbcURL, desc.user, desc.password) ;
    }
    
    public SDBConnection(String url, String user, String password)
    { init(url, user, password) ; }
    
    public SDBConnection(Connection jdbcConnection)
    { 
        sqlConnection = jdbcConnection ;
        transactionHandler = new TransactionHandlerSDB(this) ;
    }

    private void init(String url, String user, String password)
    {
        if ( user == null )
            user = Access.getUser() ;
        if ( password == null )
            password = Access.getPassword() ;
        try
        {
            sqlConnection = DriverManager.getConnection(url, user, password) ;
            label = url ;
        } catch (SQLException e)
        {
            //exception("SDBConnection",e ) ;
            throw new SDBException("SQL Exception while connecting to database: "+url+" : "+e.getMessage()) ;
        }
        transactionHandler = new TransactionHandlerSDB(this) ;
    }
    
    public TransactionHandler getTransactionHandler() { return transactionHandler ; } 
    
    public ResultSet execQuery(String sqlString) throws SQLException
    {
        if ( logSQLStatements || logSQLQueries )
            log.info("execQuery\n\n"+sqlString+"\n") ;
        
        Connection conn = getSqlConnection() ;

        try {
            Statement s = conn.createStatement() ; // Not closed - happens when result set closed
            return s.executeQuery(sqlString) ;
        } catch (SQLException ex)
        {
            exception("execQuery", ex, sqlString) ;
            throw ex ;
        }
    }

    public Object executeInTransaction(Command c) { return getTransactionHandler().executeInTransaction(c) ; }
    
    public Object executeSQL(final SQLCommand c)
    {
        return executeInTransaction(new Command(){
            public Object execute()
            {
                try {
                    return c.execute() ;
                } catch (SQLException ex)
                { 
                    exception("execQuery", ex) ;
                    throw new SDBExceptionSQL("execute", ex) ; } 
            }}) ;
    }

    
    public int execUpdate(String sqlString) throws SQLException
    {
        if ( logSQLStatements )
            log.info("execUpdate\n\n"+sqlString+"\n") ;
        
        Connection conn = getSqlConnection() ;
        try {
            Statement s = conn.createStatement() ;
            int rc = s.executeUpdate(sqlString) ;
            s.close() ;
            return rc ;
        } catch (SQLException ex)
        {
            exception("execUpdate", ex, sqlString) ;
            throw ex ;
        }
    }

    public void execAny(String sqlString) throws SQLException
    {
        if ( logSQLStatements )
            log.info("execAny\n\n"+sqlString+"\n") ;
        
        Connection conn = getSqlConnection() ;
        
        try {
            Statement s = conn.createStatement() ;
            s.execute(sqlString) ;
            s.close() ;
        } catch (SQLException ex)
        {
            exception("execAny", ex, sqlString) ;
            throw ex ;
        }

    }

    private void exception(String who, SQLException ex, String sqlString)
    {
        if ( logSQLExceptions )
            log.warn(who+": SQLException\n"+ex.getMessage()+"\n"+sqlString) ;
    }

    private void exception(String who, SQLException ex)
    {
        if ( logSQLExceptions )
            log.warn(who+": SQLException\n"+ex.getMessage()) ;
    }

    public ResultSet metaData(String sqlString) throws SQLException
    {
        try {
            Connection conn = getSqlConnection() ;
            DatabaseMetaData dbmd = conn.getMetaData() ;
            ResultSet rsMD = dbmd.getTables(null, null, null, null) ;
            return rsMD ;
        } catch (SQLException e)
        {
            exception("metaData", e) ;
            throw e ;
        }
    }
    
    public Connection getSqlConnection()
    {
        return sqlConnection ;
    }
    
    @Override
    public String toString() { return label ; }

}

/*
 * (c) Copyright 2005, 2006 Hewlett-Packard Development Company, LP
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