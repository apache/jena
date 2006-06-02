/*
 * (c) Copyright 2006 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sdb.sql;

import com.hp.hpl.jena.sdb.SDBException;

public enum MySQLEngineType
{
    MyISAM    { @Override public String getEngineName() { return "MyISAM" ; } } ,
    InnoDB    { @Override public String getEngineName() { return "InnoDB" ; } } ,
    Memory    { @Override public String getEngineName() { return "MEMORY" ; } } ,
    BDB       { @Override public String getEngineName() { return "BDB" ; } } ,
    NDB       { @Override public String getEngineName() { return "NDB" ; } } ,
    ;
    abstract public String getEngineName() ;
    
    public static MySQLEngineType convert(String engineName)
    {
        if ( check(engineName, MyISAM) )  return MyISAM ;
        if ( check(engineName, InnoDB) )  return InnoDB ;
        if ( check(engineName, Memory) )  return Memory ;
        if ( check(engineName, BDB) )     return BDB ;
        if ( check(engineName, NDB) )     return NDB ;
        throw new SDBException("Can't turn '"+engineName+"' into an engine type") ; 
    }
    
    private static boolean check(String engineName, MySQLEngineType t)
    { return  engineName.equalsIgnoreCase(t.getEngineName()) ; }
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