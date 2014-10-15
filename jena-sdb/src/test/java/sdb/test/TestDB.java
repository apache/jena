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
    protected void setConnection(Connection jdbc) { this.jdbc = jdbc ; }
    
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
        try ( Statement stmt = jdbc.createStatement() ) {
            if ( verbose )
                System.out.println(sql) ;
            stmt.execute(sql) ;
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
