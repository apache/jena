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

package com.hp.hpl.jena.sdb.sql;


import static java.sql.ResultSet.CONCUR_READ_ONLY;
import static java.sql.ResultSet.TYPE_FORWARD_ONLY;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.graph.TransactionHandler;
import com.hp.hpl.jena.shared.Command;

import com.hp.hpl.jena.sdb.core.Generator;
import com.hp.hpl.jena.sdb.core.Gensym;
import com.hp.hpl.jena.sdb.core.SDBConstants;
import com.hp.hpl.jena.sdb.graph.TransactionHandlerSDB;

/*
 * An SDBConnection is the abstraction of the link between client
 * application and the database.
 *  There can be many Store's per connection.
 */  

public class SDBConnection
{
    static private Logger log = LoggerFactory.getLogger(SDBConnection.class) ;
    static private Generator gen = Gensym.create("connection-") ;

    private Connection sqlConnection = null ;
    TransactionHandler transactionHandler = null ;
    String label = gen.next() ;
    
    String jdbcURL = "unset" ;
    
    // Defaults 
    public static boolean logSQLExceptions = true ;
    public static boolean logSQLStatements = false ;
    public static boolean logSQLQueries    = false ;
    
    private boolean thisLogSQLExceptions = logSQLExceptions ;
    private boolean thisLogSQLStatements = logSQLStatements ;
    private boolean thisLogSQLQueries    = logSQLQueries ;
    
    public SDBConnection(DataSource ds) throws SQLException
    {
        this(ds.getConnection()) ;
    }
    
    public SDBConnection(String url, String user, String password)
    { 
        this(SDBConnectionFactory.createSqlConnection(url, user, password)) ;
        setLabel(url) ;
        setJdbcURL(url) ;
    }
    
    public SDBConnection(Connection jdbcConnection)
    { 
        this(jdbcConnection, null) ;
    }
    
    public SDBConnection(Connection jdbcConnection, String url)
    { 
        sqlConnection = jdbcConnection ;
        transactionHandler = new TransactionHandlerSDB(this) ;
        if ( url != null ) setJdbcURL(url) ;
    }

    public static SDBConnection none()
    {
        return new SDBConnection(JDBC.jdbcNone, null, null) ;
    }

    
    public boolean hasSQLConnection() { return sqlConnection != null ; }
    
    public TransactionHandler getTransactionHandler() { return transactionHandler ; } 
    
    public ResultSetJDBC execQuery(String sqlString) throws SQLException
    { return execQuery(sqlString, SDBConstants.jdbcFetchSizeOff) ; }
    
    public ResultSetJDBC execQuery(String sqlString, int fetchSize) throws SQLException
    {
        if ( loggingSQLStatements() || loggingSQLQueries() )
            writeLog("execQuery", sqlString) ;
        
        Connection conn = getSqlConnection() ;

        try {
            //Statement s = conn.createStatement() ; // Managed by ResultSetJDBC
            
            // These are needed for MySQL when trying for row-by-row fetching
            // and they are gemnerally true so set them always.
            Statement s = conn.createStatement(TYPE_FORWARD_ONLY, CONCUR_READ_ONLY) ; // Managed by ResultSetJDBC
            
            // MySQL : Integer.MIN_VALUE
            if ( fetchSize != SDBConstants.jdbcFetchSizeOff )
            {
                /* MySQL: streaming if:
                 * stmt = conn.createStatement(java.sql.ResultSet.TYPE_FORWARD_ONLY,
                            java.sql.ResultSet.CONCUR_READ_ONLY);
                   stmt.setFetchSize(Integer.MIN_VALUE);
                 */
                s.setFetchSize(fetchSize) ;
            }
            return new ResultSetJDBC(s, s.executeQuery(sqlString)) ;
        } catch (SQLException ex)
        {
            exception("execQuery", ex, sqlString) ;
            throw ex ;
        }
        catch (RuntimeException ex)
        { throw ex ; }
    }

    public Object executeInTransaction(Command c) { return getTransactionHandler().executeInTransaction(c) ; }
    
    public Object executeSQL(final SQLCommand c)
    {
        try {
            return c.execute(getSqlConnection()) ;
        } catch (SQLException ex)
        { 
            exception("SQL", ex) ;
            throw new SDBExceptionSQL(ex) ;
        } 
    }

    
    public int execUpdate(String sqlString) throws SQLException
    {
        if ( loggingSQLStatements() )
            writeLog("execUpdate", sqlString) ;
        
        Connection conn = getSqlConnection() ;
        try ( Statement s = conn.createStatement() ) {
            return s.executeUpdate(sqlString) ;
        } catch (SQLException ex)
        {
            exception("execUpdate", ex, sqlString) ;
            throw ex ;
        }
    }

    /** Execute a statement, return the result set if there was one, else null */
    public ResultSetJDBC exec(String sqlString) throws SQLException
    {
        if ( loggingSQLStatements() )
            writeLog("exec", sqlString) ;
        
        Connection conn = getSqlConnection() ;
        Statement s = null ;
        try {
            s = conn.createStatement() ;
            boolean r = s.execute(sqlString) ;
            if ( r )
                return new ResultSetJDBC(s, s.getResultSet()) ; 
            RS.close(s) ;
            return null ;
        }
//        catch (SQLSyntaxErrorException ex)  // Java 6
//        {
//            exception("exec", ex, sqlString) ;
//            throw ex ;
//        }
        catch (SQLException ex)
        {
            RS.close(s) ;
            exception("exec", ex, sqlString) ;
            throw ex ;
        }
    }

    /** Execute a statement, return the result set if there was one, else null.  */
    public ResultSetJDBC execSilent(String sqlString)
    {
        if ( loggingSQLStatements() )
            writeLog("execSilent", sqlString) ;
        
        Connection conn = getSqlConnection() ;
        Statement s = null ;
        try {
            s = conn.createStatement() ;
            boolean r = s.execute(sqlString) ;
            if ( r )
                return new ResultSetJDBC(s, s.getResultSet()) ;
        } catch (SQLException ex) {}
        // Close if did not return a ResultSetJDBC
        RS.close(s) ;
        return null ; 
    }

    /** Prepare a statement **/
    public PreparedStatement prepareStatement(String sqlString) throws SQLException {
    	if ( loggingSQLStatements() )
    		writeLog("prepareStatement", sqlString) ;
    	
    	Connection conn = getSqlConnection() ;
    	try {
    		PreparedStatement ps = conn.prepareStatement(sqlString);
    		return ps;
    	} catch (SQLException ex) {
    		exception("prepareStatement", ex, sqlString) ;
    		throw ex;
    	}
    }
    
    /** Close a prepared statement **/
    public void closePreparedStatement(PreparedStatement ps) throws SQLException {
        if ( loggingSQLStatements() )
            writeLog("closePrepareStatement", ps.toString()) ;
        if ( ps == null )
            return ;
        try {
            ps.close() ;
        } catch (SQLException ex) {
            exception("closePrepareStatement", ex, ps.toString()) ;
            throw ex;
        }
    }
    
    /** Get the names of the application tables */
    public List<String> getTableNames()
    {
        return TableUtils.getTableNames(getSqlConnection()) ;
    }
    
    
//    public ResultSet metaData(String sqlString) throws SQLException
//    {
//        try {
//            Connection conn = getSqlConnection() ;
//            DatabaseMetaData dbmd = conn.getMetaData() ;
//            ResultSet rsMD = dbmd.getTables(null, null, null, null) ;
//            return rsMD ;
//        } catch (SQLException e)
//        {
//            exception("metaData", e) ;
//            throw e ;
//        }
//    }
    
    public Connection getSqlConnection()
    {
        // Potential pool point.
        return sqlConnection ;
    }
    
    public void close()
    {
        Connection connection = getSqlConnection() ;
        try {
            if ( connection != null && ! connection.isClosed() )
                connection.close() ;
        } catch (SQLException ex){
            log.warn("Problems closing SQL connection", ex) ;
        }
    }
    
    @Override
    public String toString() { return getLabel() ; }

    public boolean loggingSQLExceptions() { return thisLogSQLExceptions ;
    }

    public void setLogSQLExceptions(boolean thisLogSQLExceptions)
    {
        this.thisLogSQLExceptions = thisLogSQLExceptions ;
    }

    public boolean loggingSQLQueries() { return thisLogSQLQueries ; }

    public void setLogSQLQueries(boolean thisLogSQLQueries)
    {
        this.thisLogSQLQueries = thisLogSQLQueries ;
    }

    public boolean loggingSQLStatements() { return thisLogSQLStatements ; }
    
    public void setLogSQLStatements(boolean thisLogSQLStatements)
    {
        this.thisLogSQLStatements = thisLogSQLStatements ;
    }

    public String getLabel()
    {
        return label ;
    }

    public void setLabel(String label)
    {
        this.label = label ;
    }
    
    public String getJdbcURL()
    {
        return jdbcURL ;
    }

    public void setJdbcURL(String jdbcURL)
    {
        this.jdbcURL = jdbcURL ;
    }

    static Logger sqlLog = log ; // LoggerFactory.getLogger("SQL") ; // Remember to turn on in log4j.properties.
    
    private void exception(String who, SQLException ex, String sqlString)
    {
        if ( this.loggingSQLExceptions() )
            sqlLog.warn(who+": SQLException\n"+ex.getMessage()+"\n"+sqlString+"\n") ;
    }

    private void exception(String who, SQLException ex)
    {
        if ( this.loggingSQLExceptions() )
            sqlLog.warn(who+": SQLException\n"+ex.getMessage()) ;
    }
    
    private void writeLog(String who, String sqlString)
    {
        if ( sqlLog.isInfoEnabled() )
            sqlLog.info(who+"\n\n"+sqlString+"\n") ;
    }
}
