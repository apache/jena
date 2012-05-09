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

import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException ;
import java.util.logging.Logger ;

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
    
    @Override
    public Connection getConnection() throws SQLException
    {
        return getConnection(null, null) ;
    }

    @Override
    public Connection getConnection(String username, String password) throws SQLException
    {
        // All the work!
        return SDBConnectionFactory.create(sdbConnDesc).getSqlConnection() ;
    }

    // This was added at Java7 so we have play games to compile cleanly on Java6 and Java7
    //@Override
    @SuppressWarnings("all")
    public Logger getParentLogger() throws SQLFeatureNotSupportedException
    { throw new SQLFeatureNotSupportedException() ; }
    
    @Override
    public PrintWriter getLogWriter() throws SQLException
    { return printWriter ; }

    @Override
    public void setLogWriter(PrintWriter out) throws SQLException
    { printWriter = out ; }

    
    @Override
    public int getLoginTimeout() throws SQLException
    {
        // 0 means default to "system timeout"
        return 0 ;
    }


    @Override
    public void setLoginTimeout(int seconds) throws SQLException
    {
        // Ignore.  Efficiently.
    }

    @Override
    public boolean isWrapperFor(Class<? > iface) throws SQLException
    {
        // We do not wrap anything.
        return false ;
    }

    @Override
    public <T> T unwrap(Class<T> iface) throws SQLException
    {
        throw new SQLException("Not wrapped: "+iface.getCanonicalName()) ;
    }

}
