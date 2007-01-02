/*
 * (c) Copyright 2006, 2007 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sdb.sql;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class TableUtils
{

    /** Does this table exist? 
     * 
     * @throws SQLException */
    public static boolean hasTable(Connection connection, String table, String... types) throws SQLException
    {
    	if (types.length == 0) types = null;
    	// MySQL bug -- doesn't see temporary tables!
    	// Postgres likes lowercase -- I'll try all options
    	ResultSet tableData = connection.getMetaData().getTables(null, null, table, types);
    	boolean hasTable = tableData.next();
    	tableData.close();
    	if (!hasTable) { // Try lowercase
    		tableData = connection.getMetaData().getTables(null, null, table.toLowerCase(), types);
    		hasTable = tableData.next();
    		tableData.close();
    	}
    	if (!hasTable) { // Try uppercase
    		tableData = connection.getMetaData().getTables(null, null, table.toUpperCase(), types);
    		hasTable = tableData.next();
    		tableData.close();
    	}
    	
    	return hasTable;
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
    	try {
    		ResultSet res = connection.createStatement().executeQuery("SELECT COUNT(*) AS size FROM " + table);
    		if (res.next())
    			size = res.getLong("size");
    		res.close();
    	} catch (SQLException e) { throw new SDBExceptionSQL(e) ; }
    
    	return size;
    }

    public static void dropTable(SDBConnection connection, String tableName)
    {
        try {
            if (TableUtils.hasTable(connection.getSqlConnection(), tableName))
                connection.exec("DROP TABLE "+tableName) ;
        } catch (SQLException ex)
        { throw new SDBExceptionSQL("SQLException : Can't drop table: "+tableName, ex) ; }
    }

}

/*
 * (c) Copyright 2006, 2007 Hewlett-Packard Development Company, LP
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