/*
 * (c) Copyright 2006 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package sdb.test;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class TestDB
{
    protected Connection jdbc = null ;
    boolean verbose = false ;
    

    public TestDB(Connection jdbc, boolean verbose)
    { 
        this.jdbc = jdbc ;
        this.verbose = verbose ;
    }
    
    public void setVerbose(boolean newValue) { verbose = newValue ; }
    
    protected String sqlFormat(String sql, Object... args)
    {
        return String.format(sql, args) ;
    }
    
    protected void execNoFail(String sql, Object... args)
    {
        try { exec(sql, args) ;
        } catch (SQLException ex) {}
    }

    protected void exec(String sql, Object... args) throws SQLException
    {
        sql = sqlFormat(sql, args) ;
        Statement stmt = null ;
        try {
            stmt = jdbc.createStatement() ;
            if ( verbose )
                System.out.println(sql) ;
            stmt.execute(sql) ;
        } finally {
            if ( stmt != null ) stmt.close() ;
        }
    }
    
    protected ResultSet execQuery(String sql, Object... args) throws SQLException
    {
        sql = sqlFormat(sql, args) ;
        if ( verbose )
            System.out.println(sql) ;
        Statement stmt = jdbc.createStatement() ;
        return stmt.executeQuery(sql) ;
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