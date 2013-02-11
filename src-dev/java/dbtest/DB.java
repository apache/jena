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

package dbtest;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;


public class DB
{
    static boolean verbose = false ;
    
    static public void setVerbose(boolean newValue) { verbose = newValue ; }
    
    static void execNoFail(Connection jdbc, String sql)
    {
        try { exec(jdbc, sql) ;
        } catch (SQLException ex) {}
    }

    static void exec(Connection jdbc, String sql) throws SQLException
    {
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
    
    static ResultSet execQuery(Connection jdbc, String sql) throws SQLException
    {
        if ( verbose )
            System.out.println(sql) ;
        Statement stmt = jdbc.createStatement() ;
        return stmt.executeQuery(sql) ;
    }
}
