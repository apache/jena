/*
 * (c) Copyright 2006 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sdb.store;

import org.apache.commons.logging.LogFactory;

import com.hp.hpl.jena.query.util.Named;
import com.hp.hpl.jena.sdb.SDBException;

/** Symbolic names for databases supported
 *  (can still add new databases without needing
 *  to be in this fixed list).
 *  
 * @author Andy Seaborne
 * @version $Id: DatabaseType.java,v 1.4 2006/05/07 19:19:24 andy_seaborne Exp $
 */

public enum DatabaseType implements Named {
    // These should be compatible with the DBnames that Jena uses for ModelRDB
    MySQL5     { public String getName() { return "MySQL5" ; } } ,
    MySQL41    { public String getName() { return "MySQL41" ; } } ,
    PostgreSQL { public String getName() { return "PostgreSQL" ; } } ,
    Oracle10   { public String getName() { return "Oracle10" ; } } ,
    SQLServer  { public String getName() { return "MSSQLServer" ; } } ,
    HSQLDB     { public String getName() { return "HSQLDB" ; } } ,
    Derby     { public String getName() { return "Derby" ; } } ,
    ;
    
    private DatabaseType() {}
    
    public static DatabaseType convert(String databaseTypeName)
    {
        if ( databaseTypeName.equalsIgnoreCase("MySQL") )         return MySQL5 ;
        if ( databaseTypeName.equalsIgnoreCase("MySQL4") )        return MySQL41 ;
        if ( databaseTypeName.equalsIgnoreCase("MySQL5") )        return MySQL5 ;
        
        if ( databaseTypeName.equalsIgnoreCase("PostgreSQL") )    return PostgreSQL ;
        if ( databaseTypeName.equalsIgnoreCase("Oracle10") )      return Oracle10 ;
        if ( databaseTypeName.equalsIgnoreCase("SQLServer") )     return SQLServer ;
        if ( databaseTypeName.equalsIgnoreCase("MSSQLServer") )   return SQLServer ;
        
        if ( databaseTypeName.equalsIgnoreCase("hsqldb") )        return HSQLDB ;
        if ( databaseTypeName.equalsIgnoreCase("hsql") )          return HSQLDB ;
        
        if ( databaseTypeName.equalsIgnoreCase("Derby") )          return Derby ;
        if ( databaseTypeName.equalsIgnoreCase("JavaDB") )         return Derby ;
        
        LogFactory.getLog(DatabaseType.class).warn("Can't turn '"+databaseTypeName+"' into a database type") ;
        throw new SDBException("Can't turn '"+databaseTypeName+"' into a database type") ; 
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