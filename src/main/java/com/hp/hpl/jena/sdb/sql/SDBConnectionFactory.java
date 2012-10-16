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

package com.hp.hpl.jena.sdb.sql;

import java.sql.Connection;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.rdf.model.Model;

import com.hp.hpl.jena.sdb.SDBException;

/*
 * An SDBConnection is the abstraction of the link between client
 * application and the database.
 *  There can be many Store's per connection.
 */  

public class SDBConnectionFactory
{
    private static Logger log = LoggerFactory.getLogger(SDBConnectionFactory.class) ;
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
    
    public static DataSource createDataSource(String configFile)
    {
        // XXX Use this through out?
        SDBConnectionDesc desc = SDBConnectionDesc.read(configFile) ;
        return new DataSourceSDB(desc) ;
    }
    
    // --------
    
    /** Create a new SDB connection from the description. */ 
    private static SDBConnection worker(SDBConnectionDesc desc)
    {
        // Pooling?
        return makeSDBConnection(desc) ;
    }

    private static SDBConnection makeSDBConnection(SDBConnectionDesc desc)
    {
        java.sql.Connection sqlConnection = createSqlConnection(desc) ;
        // Only place a new SDBConnection is made from a description.
        SDBConnection c = new SDBConnection(sqlConnection, desc.getJdbcURL()) ;
        if ( desc.getLabel() != null )
            c.setLabel(desc.getLabel()) ;
        else
            c.setLabel(desc.getJdbcURL()) ;
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
        {
            SQLException e2 = e.getNextException() ;
            String more = "" ;
            if ( e2 != null )
                more = " : "+e2.getMessage() ;
            throw new SDBException("SQL Exception while connecting to database: "+jdbcURL+" : "+e.getMessage()+more) ;
        }
    }
    
}
