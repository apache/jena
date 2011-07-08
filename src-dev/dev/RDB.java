/**
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
