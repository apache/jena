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

package com.hp.hpl.jena.sdb.store;
/* H2 contribution from Martin HEIN (m#)/March 2008 */
/* SAP contribution from Fergal Monaghan (m#)/May 2012 */

import java.util.List;

import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.sdb.SDBException;
import com.hp.hpl.jena.sdb.shared.SymbolRegistry;
import com.hp.hpl.jena.sparql.util.Named;
import com.hp.hpl.jena.sparql.util.Symbol;

// Common super class with LayoutType
public class DatabaseType extends Symbol implements Named
{
    public static final DatabaseType Derby           = new DatabaseType("derby") ;
    public static final DatabaseType H2              = new DatabaseType("H2") ;
    public static final DatabaseType HSQLDB          = new DatabaseType("HSQLDB") ;
    public static final DatabaseType MySQL           = new DatabaseType("MySQL") ;
    public static final DatabaseType PostgreSQL      = new DatabaseType("PostgreSQL") ;
    public static final DatabaseType SQLServer       = new DatabaseType("SQLServer") ;
    public static final DatabaseType Oracle          = new DatabaseType("Oracle") ;
    public static final DatabaseType DB2             = new DatabaseType("DB2") ;
    public static final DatabaseType SAP             = new DatabaseType("sap") ;
    
    static SymbolRegistry<DatabaseType> registry = new SymbolRegistry<DatabaseType>() ;
    static { init() ; }
    
    public static DatabaseType fetch(String databaseTypeName)
    {
        if ( databaseTypeName == null )
            throw new IllegalArgumentException("DatabaseType.convert: null not allowed") ;

        DatabaseType t = registry.lookup(databaseTypeName) ;
        if ( t != null )
            return t ;
        
        // Hack?
        if ( databaseTypeName.startsWith("oracle:") )
            return Oracle ;

        LoggerFactory.getLogger(DatabaseType.class).warn("Can't turn '"+databaseTypeName+"' into a database type") ;
        throw new SDBException("Can't turn '"+databaseTypeName+"' into a database type") ; 
    }
    
    static void init()
    {
        // Java databases
        register(Derby) ;
        registerName("JavaDB", Derby) ;
        
        register(HSQLDB) ;
        registerName("hsqldb:file", HSQLDB) ;
        registerName("hsqldb:mem", HSQLDB) ;
        registerName("hsql", HSQLDB) ;
        
        register(H2) ;
        registerName("h2:file", H2) ;
        registerName("h2:mem", H2) ;
        registerName("h2:tcp", H2) ;
        registerName("h2", H2) ;
        
        // Open source DBs
        register(MySQL) ;
        // registerName("MySQL5", HSQLDB) ;      // am I right in assuming that "HSQLDB" should actually be "MySQL"
        registerName("MySQL5", MySQL) ;
        
        register(PostgreSQL) ;
        
        // Commercial DBs
        register(SQLServer) ;
        registerName("MSSQLServer" , SQLServer) ;
        registerName("MSSQLServerExpress" , SQLServer) ;
        
        register(Oracle) ;
        
        register(DB2) ;
        
        register(SAP);
    }
    
    static public List<String> allNames() { return registry.allNames() ; }
    static public List<DatabaseType> allTypes() { return registry.allSymbols() ; }
    
    static public void register(String name)
    {
        if ( name == null )
            throw new IllegalArgumentException("DatabaseType.register(String): null not allowed") ;
        register(new DatabaseType(name)) ; 
    }
    
    static public void register(DatabaseType dbType)
    {
        if ( dbType == null )
            throw new IllegalArgumentException("DatabaseType.register(DatabaseType): null not allowed") ;
        registry.register(dbType) ;
    }

    static public void registerName(String databaseName, DatabaseType dbType)
    {
        if ( dbType == null )
            throw new IllegalArgumentException("DatabaseType.registerName: null not allowed") ;
        registry.register(databaseName, dbType) ; 
    }
    
    private DatabaseType(String layoutName)
    {
        super(layoutName) ;
    }

    @Override
    public String getName()
    {
        return super.getSymbol() ;
    }
}
