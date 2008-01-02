/*
 * (c) Copyright 2005, 2006, 2007, 2008 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sdb.sql;

import java.sql.Connection;
import java.sql.SQLException;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.sdb.SDBException;

/*
 * An SDBConnection is the abstraction of the link between client
 * application and the database.
 *  There can be many Store's per connection.
 */  

public class SDBConnectionFactory
{
    public static SDBConnection create(SDBConnectionDesc desc){ return worker(desc) ; }

    public static SDBConnection create(String configFile)
    { 
        SDBConnectionDesc desc = SDBConnectionDesc.read(configFile) ;
        return create(desc) ;
    }

    public static SDBConnection create(String url, String user, String password)
    {
        return new SDBConnection(createSqlConnection(url, user, password)) ;
    }
    
    public static Connection createJDBC(String configFile)
    { 
        SDBConnectionDesc desc = SDBConnectionDesc.read(configFile) ;
        return createSqlConnection(desc) ;
    }

    public static Connection createJDBC(Model model)
    { 
        SDBConnectionDesc desc = SDBConnectionDesc.read(model) ;
        return createSqlConnection(desc) ;
    }
    
    public static SDBConnection create(Connection sqlConnection)
    {
        return new SDBConnection(sqlConnection) ;
    }
    
    // --------
    
    /** Create a new SDB connection from the description. */ 
    private static SDBConnection worker(SDBConnectionDesc desc)
    {
        java.sql.Connection sqlConnection = createSqlConnection(desc) ;
        SDBConnection c = new SDBConnection(sqlConnection) ;
        if ( desc.getLabel() != null )
            c.setLabel(desc.getLabel()) ;
        return c ;
    }

    /** Create a new, plain JDBC SQL connection from the description. */ 
    public static Connection createSqlConnection(SDBConnectionDesc desc)
    {
        if ( desc.getDriver() != null )
            JDBC.loadDriver(desc.getDriver()) ;
        else if ( ! desc.getJdbcURL().equals(JDBC.jdbcNone) )
        {
            String driver = desc.getDriver() ;
            if ( driver == null )
                driver = JDBC.guessDriver(desc.getType()) ;
            if ( driver != null )
                JDBC.loadDriver(driver) ;
        }
        
        return createSqlConnection(desc.getJdbcURL(), desc.getUser(), desc.getPassword()) ;
    }

    public static Connection createSqlConnection(String jdbcURL, String user, String password)
    {
        try {
            return JDBC.createConnection(jdbcURL, user, password) ;
        } catch (SQLException e)
        { throw new SDBException("SQL Exception while connecting to database: "+jdbcURL+" : "+e.getMessage()) ; }
    }
    
}

/*
 * (c) Copyright 2005, 2006, 2007, 2008 Hewlett-Packard Development Company, LP
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