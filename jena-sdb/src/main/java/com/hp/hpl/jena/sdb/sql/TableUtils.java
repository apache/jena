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
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class TableUtils
{
    public static void dump(SDBConnection conn, String tableName)
    {
        ResultSetJDBC tableData = null ;
        try {
            tableData = conn.execQuery("SELECT * FROM "+tableName) ;
            RS.printResultSet(tableData.get()) ;
            tableData.close() ;
        } catch (SQLException ex)
        { throw new SDBExceptionSQL(ex) ; }
        finally { RS.close(tableData) ; }
    }
    
    public static void dump(Connection conn, String tableName)
    {
        try {
            Statement s = conn.createStatement() ; // Not closed - happens when result set closed
            try ( ResultSet tableData = s.executeQuery("SELECT * FROM "+tableName) ) {
                RS.printResultSet(tableData) ;
                tableData.close() ;
            }
        } catch (SQLException ex)
        { throw new SDBExceptionSQL(ex) ; }
    }
    

    /** Does this table exist? 
     * 
     * @throws SQLException */
    public static boolean hasTable(Connection connection, String table, String... types) throws SQLException
    {
    	if (types.length == 0) types = null;
    	// MySQL bug -- doesn't see temporary tables!
    	// Postgres likes lowercase -- I'll try all options
    	boolean hasTable = false ;
    	try ( ResultSet tableData = connection.getMetaData().getTables(null, null, table, types)) {
    	    hasTable = tableData.next();
    	} 
    	if (!hasTable) { // Try lowercase
    		try ( ResultSet tableData = connection.getMetaData().getTables(null, null, table.toLowerCase(), types) ) {
    		    hasTable = tableData.next();
    		}
    	}
    	if (!hasTable) { // Try uppercase
    	    try ( ResultSet tableData = connection.getMetaData().getTables(null, null, table.toUpperCase(), types) ) {
    	        hasTable = tableData.next();
    	    }
    	}
    	
    	return hasTable;
    }

    public static boolean hasTable(SDBConnectionHolder holder, String table, String... types) throws SQLException {
		return hasTable(holder.connection().getSqlConnection(), table, types);
	}

	/** Get the names of the application tables */
    public static List<String> getTableNames(Connection connection)
    {
        return getTableNames(connection, "TABLE") ;
    }

    /** Get the names of the tables of a particular type*/
    public static List<String> getTableNames(Connection connection, String tableTypeName)
    {
        try {
            List<String> tableNames = new ArrayList<String>() ;
            
            ResultSet rs = connection.getMetaData().getTables(null, null, null, new String[]{tableTypeName});
    
            while(rs.next())
            {
                String tableName = rs.getString("TABLE_NAME");
    //            String tableType = rs.getString("TABLE_TYPE");
    //            if ( tableType.equalsIgnoreCase("TABLE") )
                    tableNames.add(tableName) ;
            }
            return tableNames ;
        } catch (SQLException ex) { throw new SDBExceptionSQL(ex) ; } 
    }

    /** Get the size of a table (usually called 'Triples') **/
    public static long getTableSize(Connection connection, String table)
    {
        long size = -1;
        try ( ResultSet res = connection.createStatement().executeQuery("SELECT COUNT(*) FROM " + table) ) {
            if (res.next())
                size = res.getLong(1);
        } catch (SQLException e) { throw new SDBExceptionSQL(e) ; }
    
    	return size;
    }

    public static void dropTable(SDBConnection connection, String tableName)
    {
        try {
            if (TableUtils.hasTable(connection.getSqlConnection(), tableName))
                connection.execSilent("DROP TABLE "+tableName) ;
        } catch (SQLException ex)
        { throw new SDBExceptionSQL("SQLException : Can't drop table: "+tableName, ex) ; }
    }

    public static void dropTableSilent(SDBConnection connection, String tableName)
    {
        connection.execSilent("DROP TABLE "+tableName) ;
    }
}
