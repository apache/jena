/*
 * (c) Copyright 2006, 2007 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sdb.sql;

import java.util.HashMap;
import java.util.Map;

import com.hp.hpl.jena.sdb.Access;
import com.hp.hpl.jena.sdb.SDBException;
import com.hp.hpl.jena.sdb.shared.DBtype;
import com.hp.hpl.jena.sdb.shared.SDBNotFoundException;

public class JDBC
{
    // The "well known" not a JDBC connection really scheme
    public static final String jdbcNone = "jdbc:none" ;
    
    private static Map<DBtype, String> driver = new HashMap<DBtype, String>() ;
    static {
        driver.put(DBtype.MySQL,       "com.mysql.jdbc.Driver") ;
        driver.put(DBtype.PostgreSQL,  "org.postgresql.Driver") ;
        driver.put(DBtype.HSQL,        "org.hsqldb.jdbcDriver") ;
        driver.put(DBtype.Derby,       "org.apache.derby.jdbc.EmbeddedDriver") ;
        //driver.put(DBtype.Derby,       "org.apache.derby.jdbc.ClientDriver") ;
        driver.put(DBtype.SQLServer,   "com.microsoft.sqlserver.jdbc.SQLServerDriver") ;
        driver.put(DBtype.Oracle,      "oracle.jdbc.driver.OracleDriver") ;
    }
    
    static public String getDriver(DBtype dbType) { return driver.get(dbType) ; }
    
    static public void loadDriverHSQL()  { loadDriver(driver.get(DBtype.HSQL)) ; }
    static public void loadDriverMySQL() { loadDriver(driver.get(DBtype.MySQL)) ; }
    static public void loadDriverPGSQL() { loadDriver(driver.get(DBtype.PostgreSQL)); }
    static public void loadDriverDerby() { loadDriver(driver.get(DBtype.Derby)); }
    static public void loadDriverSQLServer() { loadDriver(driver.get(DBtype.SQLServer)); }
    static public void loadDriverOracle() { loadDriver(driver.get(DBtype.Oracle)); }
    static public void loadDriver(String className) { loadClass(className) ; }
    
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

    public static String makeURL(String type, String host, String dbName, String user, String password)
    { return makeURL(type, host, dbName, null, user, password) ; }

    // How to make URLs.
    public static String makeURL(String type, String host, String dbName, String argStr, String user, String password)
    {
        type = type.toLowerCase() ;
        
        if ( user == null )
            user = Access.getUser() ;
        if ( password == null )
            password = Access.getPassword() ;
        
        if ( type.equals("mysql") || type.equals("postgresql") || type.equals("pgsql") )
        {
            String s = String.format("jdbc:%s://%s/%s", type, host, dbName) ;
            if ( argStr != null && ! argStr.equals("") )
                s = s+ "?"+ argStr ;
            return s ;
        }
        
        if ( type.startsWith("hsql"))
        {
            String s = String.format("jdbc:%s:%s:%s", type, host, dbName) ;
            if ( argStr != null && ! argStr.equals("") )
                s = s+ "?"+ argStr ;
            return s ;
        }
        
        if ( type.startsWith("pgsql"))
        {
        	String s = String.format("jdbc:%s://%s/%s", type, host, dbName) ;
        	if (argStr != null && ! argStr.equals("") )
        		s = s+ "?"+ argStr ;
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
        
        if ( type.equals("oracle") || type.startsWith("oracle:") )
        {
        	String s = String.format("jdbc:%s:@%s:%s", type, host, dbName) ;
        	return s;
        }
        
        if ( type.equalsIgnoreCase("none") )
            return jdbcNone ;
        
        throw new SDBException("Don't know how to construct a JDBC URL for "+type) ;
    }
}

/*
 * (c) Copyright 2006, 2007 Hewlett-Packard Development Company, LP
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
