/**
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
