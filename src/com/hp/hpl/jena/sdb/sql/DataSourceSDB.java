/*
 * (c) Copyright 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sdb.sql;

import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;

import javax.sql.DataSource;

/** A simple datasource that uses SDBConnectionDesc and hence works with the SDB assembler descriptions */ 

public class DataSourceSDB implements DataSource
{
    private static PrintWriter printWriter = new PrintWriter(System.out) ; 
    private SDBConnectionDesc sdbConnDesc ;
    
    public DataSourceSDB(SDBConnectionDesc sdbConnDesc)
    {
        this.sdbConnDesc = sdbConnDesc ;
    }
    
    public Connection getConnection() throws SQLException
    {
        return getConnection(null, null) ;
    }

    public Connection getConnection(String username, String password) throws SQLException
    {
        // All the work!
        return SDBConnectionFactory.create(sdbConnDesc).getSqlConnection() ;
    }

    public PrintWriter getLogWriter() throws SQLException
    { return printWriter ; }

    public void setLogWriter(PrintWriter out) throws SQLException
    { printWriter = out ; }

    
    public int getLoginTimeout() throws SQLException
    {
        // 0 means default to "system timeout"
        return 0 ;
    }


    public void setLoginTimeout(int seconds) throws SQLException
    {
        // Ignore.  Efficiently.
    }

    public boolean isWrapperFor(Class<? > iface) throws SQLException
    {
        // We do not wrap anything.
        return false ;
    }

    public <T> T unwrap(Class<T> iface) throws SQLException
    {
        throw new SQLException("Not wrapped: "+iface.getCanonicalName()) ;
    }

}

/*
 * (c) Copyright 2008, 2009 Hewlett-Packard Development Company, LP
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