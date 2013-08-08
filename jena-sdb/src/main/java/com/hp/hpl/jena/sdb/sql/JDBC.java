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
/* H2 contribution from Martin HEIN (m#)/March 2008 */
/* SAP contribution from Fergal Monaghan (m#)/May 2012 */

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Locale ;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.sdb.SDBException;
import com.hp.hpl.jena.sdb.shared.Access;
import com.hp.hpl.jena.sdb.shared.SDBNotFoundException;
import com.hp.hpl.jena.sdb.store.DatabaseType;

public class JDBC
{
    private static Logger log = LoggerFactory.getLogger(JDBC.class) ; 
    // The "well known" not a JDBC connection really scheme
    public static final String jdbcNone = "jdbc:none" ;

    private static Map<DatabaseType, String> driver = new HashMap<DatabaseType, String>() ;
    static {
        driver.put(DatabaseType.MySQL,      "com.mysql.jdbc.Driver") ;
        driver.put(DatabaseType.PostgreSQL, "org.postgresql.Driver") ;
        driver.put(DatabaseType.H2,         "org.h2.Driver") ;
        driver.put(DatabaseType.HSQLDB,     "org.hsqldb.jdbcDriver") ;
        driver.put(DatabaseType.Derby,      "org.apache.derby.jdbc.EmbeddedDriver") ;
        //driver.put(DatabaseType.Derby,       "org.apache.derby.jdbc.ClientDriver") ;
        driver.put(DatabaseType.SQLServer,  "com.microsoft.sqlserver.jdbc.SQLServerDriver") ;
        driver.put(DatabaseType.Oracle,     "oracle.jdbc.driver.OracleDriver") ;
        driver.put(DatabaseType.DB2,        "com.ibm.db2.jcc.DB2Driver") ;
        driver.put(DatabaseType.SAP,        "com.sap.db.jdbc.Driver") ;
    }
    
    static public String getDriver(DatabaseType dbType) { return driver.get(dbType) ; }
    
    /** Explicitly load the HSQLDB driver */ 
    static public void loadDriverHSQL()  { loadDriver(driver.get(DatabaseType.HSQLDB)) ; }
    /** Explicitly load the H2 driver */ 
    static public void loadDriverH2()  { loadDriver(driver.get(DatabaseType.H2)) ; }
    /** Explicitly load the MySQL driver */ 
    static public void loadDriverMySQL() { loadDriver(driver.get(DatabaseType.MySQL)) ; }
    /** Explicitly load the PostgreSQL driver */ 
    static public void loadDriverPGSQL() { loadDriver(driver.get(DatabaseType.PostgreSQL)); }
    /** Explicitly load the Derby driver */ 
    static public void loadDriverDerby() { loadDriver(driver.get(DatabaseType.Derby)); }
    /** Explicitly load the SQLServer driver */ 
    static public void loadDriverSQLServer() { loadDriver(driver.get(DatabaseType.SQLServer)); }
    /** Explicitly load the Oracle driver */ 
    static public void loadDriverOracle() { loadDriver(driver.get(DatabaseType.Oracle)); }
    /** Explicitly load the DB2 driver */ 
    static public void loadDriverDB2() { loadDriver(driver.get(DatabaseType.DB2)); }
    /** Explicitly load the SAP driver */ 
    static public void loadDriverSAP() { loadDriver(driver.get(DatabaseType.SAP)); }
    
    static public void loadDriver(String className) { loadClass(className) ; }
    
    static public String guessDriver(String type)
    { 
        return getDriver(DatabaseType.fetch(type)) ;
    }
    
    // This is the only place a driver is created.
    public static Connection createConnection(String url, String user, String password) throws SQLException
    {
        if ( log.isDebugEnabled() )
            log.debug("Create JDBC connection: "+url);
        
        if ( url.equals(jdbcNone) )
            return null ;
        
        if ( user == null )
            user = Access.getUser() ;
        if ( password == null )
            password = Access.getPassword() ;

        return DriverManager.getConnection(url, user, password) ;
    }
    
//    static public void loadClass(String className)
//    { Loader.loadClass(className) ; }

    static private void loadClass(String className)
    { 
        try { Class.forName(className); }
        catch (ClassNotFoundException ex)
        { throw new SDBNotFoundException("Class.forName("+className+")", ex) ; } 
    }

    public static String makeURL(String type, String host, String dbName)
    { return makeURL(type, host, dbName, null, null) ; }

    // How to make URLs.
    public static String makeURL(String type, String host, String dbName, String user, String password)
    {
        if ( log.isDebugEnabled() )
            log.debug(String.format("Create JDBC URL: (type=%s, host=%s, dbName=%s)", type, host, dbName)) ;
        
        type = type.toLowerCase(Locale.ENGLISH) ;
        
        if ( user == null )
            user = Access.getUser() ;
        if ( password == null )
            password = Access.getPassword() ;
        
        if ( type.equals("mysql") )
        {
            String s = String.format("jdbc:%s://%s/%s", type, host, dbName) ;
            return s ;
        }
        
        if ( type.startsWith("hsql"))
        {
            String s = String.format("jdbc:%s:%s:%s", type, host, dbName) ;
            return s ;
        }
        
        if ( type.startsWith("h2"))
        {
            if ( type.startsWith("h2:tcp") || type.startsWith("h2:ssl"))
            {
                String s = String.format("jdbc:%s://%s/%s", type, host, dbName) ;
                return s ;
            }

            // The rest including -- h2:file, h2:mem or h2
            // Ignores host
            String s = String.format("jdbc:%s:%s", type, dbName) ;
            return s ;
        }

        if ( type.startsWith("pgsql") || type.equals("postgresql") )
        {
        	String s = String.format("jdbc:%s://%s/%s", type, host, dbName) ;
        	return s ;
        }
        
        if ( type.equals("derby") )
        {
            //jdbc:derby:sdb2;create=true
            String s = String.format("jdbc:%s:%s;create=true", type, dbName) ;
            return s ;
        }
        
        if ( type.equals("mssqlserver") || type.equals("sqlserver") )
        {
            //jdbc:sqlserver://localhost;databaseName=sdb_layout1
            String s = String.format("jdbc:%s://%s;databaseName=%s", "sqlserver", host, dbName) ;
            return s ;
        }
        
        if ( type.equals("mssqlserverexpress") || type.equals("sqlserverexpress"))
        {
            //jdbc:sqlserver://${TESTHOST}\\SQLEXPRESS;databaseName=jenatest"
            String s = String.format("jdbc:%s://%s\\SQLEXPRESS;databaseName=%s","sqlserver",host, dbName) ;
            return s ;
        }
        
        if ( type.startsWith("oracle:") )
        {
        	String s = String.format("jdbc:%s:@%s:%s", type, host, dbName) ;
        	return s;
        }
        
        if ( type.equals("oracle") )
        {
            // If not specified, use the thin driver.
            String s = String.format("jdbc:%s:thin:@%s:%s", type, host, dbName) ;
            return s;
        }
        
        if ( type.equals("db2") )
        {
            String s = String.format("jdbc:%s://%s/%s", type, host, dbName) ;
            return s;
        }

        if ( type.equals("sap") )
        {
        	String s = String.format("jdbc:%s://%s:3%s15", type, host, dbName) ;
        	return s ;
        }

        if ( type.equals("none") )
            return jdbcNone ;
        
        throw new SDBException("Don't know how to construct a JDBC URL for "+type) ;
    }
}
