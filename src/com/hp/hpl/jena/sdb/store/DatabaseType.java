/*
 * (c) Copyright 2007, 2008 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sdb.store;

import java.util.List;

import org.apache.commons.logging.LogFactory;

import com.hp.hpl.jena.sdb.SDBException;
import com.hp.hpl.jena.sdb.shared.SymbolRegistry;
import com.hp.hpl.jena.sparql.util.Named;
import com.hp.hpl.jena.sparql.util.Symbol;

// Common super class with LayoutType
public class DatabaseType extends Symbol implements Named
{
    public static final DatabaseType Derby           = new DatabaseType("derby") ;
    public static final DatabaseType HSQLDB          = new DatabaseType("HSQLDB") ;
    public static final DatabaseType MySQL           = new DatabaseType("MySQL") ;
    public static final DatabaseType PostgreSQL      = new DatabaseType("PostgreSQL") ;
    public static final DatabaseType SQLServer       = new DatabaseType("SQLServer") ;
    public static final DatabaseType Oracle          = new DatabaseType("Oracle") ;
    public static final DatabaseType DB2             = new DatabaseType("DB2") ;
    
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

        LogFactory.getLog(DatabaseType.class).warn("Can't turn '"+databaseTypeName+"' into a database type") ;
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
        
        // Open source DBs
        register(MySQL) ;
        registerName("MySQL5", HSQLDB) ;
        
        register(PostgreSQL) ;
        
        // Commercial DBs
        register(SQLServer) ;
        registerName("MSSQLServer" , SQLServer) ;
        registerName("MSSQLServerExpress" , SQLServer) ;
        
        register(Oracle) ;
        
        register(DB2) ;
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

    public String getName()
    {
        return super.getSymbol() ;
    }
}

/*
 * (c) Copyright 2007, 2008 Hewlett-Packard Development Company, LP
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