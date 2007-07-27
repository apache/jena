/*
 * (c) Copyright 2007 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package dev.db;

import java.util.HashSet;
import java.util.Set;

import org.apache.commons.logging.LogFactory;

import com.hp.hpl.jena.sparql.util.Named;
import com.hp.hpl.jena.sparql.util.Symbol;

import com.hp.hpl.jena.sdb.SDBException;

public class DatabaseType2 extends Symbol implements Named
{
    // URIs.
    static Set<DatabaseType2> registeredTypes = new HashSet<DatabaseType2>() ;
    
    public static DatabaseType2 convert(String databaseTypeName)
    {
        // Canonicalize name.
        if ( databaseTypeName.equalsIgnoreCase("MySQL") )           return MySQL ;
        if ( databaseTypeName.equalsIgnoreCase("MySQL5") )          return MySQL ;

        if ( databaseTypeName.equalsIgnoreCase("PostgreSQL") )      return PostgreSQL ;
        if ( databaseTypeName.equalsIgnoreCase("oracle") )          return Oracle ;
        if ( databaseTypeName.startsWith("oracle:"))                return Oracle ;
        if ( databaseTypeName.equalsIgnoreCase("SQLServer") )       return SQLServer ;
        if ( databaseTypeName.equalsIgnoreCase("MSSQLServer") )     return SQLServer ;
        if ( databaseTypeName.equalsIgnoreCase("MSSQLServerExpress") )   return SQLServer ;

        if ( databaseTypeName.equalsIgnoreCase("hsqldb") )          return HSQLDB ;
        if ( databaseTypeName.equalsIgnoreCase("hsqldb:file") )     return HSQLDB ;
        if ( databaseTypeName.equalsIgnoreCase("hsqldb:mem") )      return HSQLDB ;
        if ( databaseTypeName.equalsIgnoreCase("hsql") )            return HSQLDB ;

        if ( databaseTypeName.equalsIgnoreCase("Derby") )           return Derby ;
        if ( databaseTypeName.equalsIgnoreCase("JavaDB") )          return Derby ;
        
        // Symbol provides .equals
        DatabaseType2 t = new DatabaseType2(databaseTypeName) ;
        if ( registeredTypes.contains(t) )
            return t ;
        LogFactory.getLog(DatabaseType2.class).warn("Can't turn '"+databaseTypeName+"' into a database type") ;
        throw new SDBException("Can't turn '"+databaseTypeName+"' into a database type") ; 
    }
    
    
    //private static String[] dbNames = { "Derby", "HSQLDB", "MySQL", "PostgreSQL", "Oracle", "SQLServer" } ;
    
    public static final DatabaseType2 Derby           = new DatabaseType2("derby") ;
    public static final DatabaseType2 HSQLDB          = new DatabaseType2("HSQLDB") ;

    public static final DatabaseType2 MySQL           = new DatabaseType2("MySQL") ;
    public static final DatabaseType2 PostgreSQL      = new DatabaseType2("PostgreSQL") ;
    public static final DatabaseType2 SQLServer       = new DatabaseType2("PostgreSQL") ;
    public static final DatabaseType2 Oracle          = new DatabaseType2("Oracle") ;
    
    static void init()
    {
        register(Derby) ;
        register(HSQLDB) ;
        register(MySQL) ;
        register(PostgreSQL) ;
        register(SQLServer) ;
        register(Oracle) ;
    }
    
    static public void register(String name)
    {
        register(new DatabaseType2(name)) ; 
    }
    
    static public void register(DatabaseType2 dbType)
    {
        registeredTypes.add(dbType) ; 
    }

    private DatabaseType2(String layoutName)
    {
        super(layoutName) ;
    }

    public String getName()
    {
        return super.getSymbol() ;
    }
}

/*
 * (c) Copyright 2007 Hewlett-Packard Development Company, LP
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