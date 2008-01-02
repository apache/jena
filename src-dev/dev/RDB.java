/*
 * (c) Copyright 2006, 2007, 2008 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package dev;

import com.hp.hpl.jena.sdb.sql.JDBC;
import com.hp.hpl.jena.sdb.store.DatabaseType;

/** Setup RDB properties */ 
public class RDB
{
    public static void setUserPassword(String user, String password)
    {
        set("jena.db.user",      user) ;
        set("jena.db.password",  password) ;
    }
    
    private static void set(String k, String v) {  System.getProperties().setProperty(k, v) ; }
    
    // Set the basics, not user/password
    
    public static void set_MySQL(String dbName)
    {
        set("jena.db.url",        "jdbc:mysql://localhost/"+dbName) ;
        set("jena.db.type",       DatabaseType.MySQL.getName()) ;
        set("jena.db.driver",     JDBC.getDriver(DatabaseType.MySQL)) ;
    }
    
    public static void set_PostgreSQL(String dbName)
    {
        set("jena.db.url",       "jdbc:postgresql://localhost/"+dbName) ;
        set("jena.db.type",      DatabaseType.PostgreSQL.getName()) ;
        set("jena.db.driver",    JDBC.getDriver(DatabaseType.PostgreSQL) ) ;
    }
    
    public static void set_SQLserver(String dbName)
    {
        set("jena.db.url",         "jdbc:sqlserver://localhost\\SQLEXPRESS;database="+dbName) ;
        set("jena.db.type",        DatabaseType.SQLServer.getName()) ;
        set("jena.db.driver",      JDBC.getDriver(DatabaseType.SQLServer) ) ;
        //set("jena.db.concurrent",  "false") ;
    }
    
    public static void set_HSQLDB(String dbName)
    {
        // "host" = "mem"
        set("jena.db.url",         "jdbc:hsqldb:mem:"+dbName) ;
        set("jena.db.type",        DatabaseType.HSQLDB.getName()) ;
        set("jena.db.driver",      JDBC.getDriver(DatabaseType.HSQLDB) ) ;
        set("jena.db.concurrent",  "false") ;
    }

    public static void set_Derby(String dbName)
    {
        set("jena.db.url",         "jdbc:derby:"+dbName) ;
        set("jena.db.type",        DatabaseType.Derby.getName()) ;
        set("jena.db.driver",      JDBC.getDriver(DatabaseType.Derby) ) ;
        set("jena.db.concurrent",  "false") ;
    }
    
    public static void init(boolean verbose)
    {
        setUserPassword("user", "password") ;
        set_MySQL("jenatest") ;
        
        System.out.println(String.format("%s // %s // %s\n"
                                         , System.getProperty("jena.db.url")
                                         , System.getProperty("jena.db.type")
                                         , System.getProperty("jena.db.driver")
                                         //, System.getProperty("jena.db.concurrent") 
                                         )) ;
        JDBC.loadDriver(System.getProperty("jena.db.driver")) ;
    }
    
}

/*
 * (c) Copyright 2006, 2007, 2008 Hewlett-Packard Development Company, LP
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
