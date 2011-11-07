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

package com.hp.hpl.jena.db.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

import com.hp.hpl.jena.db.IDBConnection;
import com.hp.hpl.jena.db.RDFRDBException;

public class Driver_HSQL extends DriverRDB 
{
    public Driver_HSQL( ){
        super();

        String myPackageName = this.getClass().getPackage().getName();

        // Important!
        DB_NAMES_TO_UPPER = true ;
        
        DATABASE_TYPE = "HSQL";
        DRIVER_NAME = "org.hsqldb.jdbcDriver" ;

        ID_SQL_TYPE = "INTEGER";
        URI_COMPRESS = false;
        INDEX_KEY_LENGTH_MAX = INDEX_KEY_LENGTH = 250;
        LONG_OBJECT_LENGTH_MAX = LONG_OBJECT_LENGTH = 250;
        TABLE_NAME_LENGTH_MAX = 63;
        IS_XACT_DB = true ;
        PRE_ALLOCATE_ID = false;         // DB has auto increment fields - don't preallocate
        SKIP_DUPLICATE_CHECK = false;
        SQL_FILE = "etc/hsql.sql";
        QUOTE_CHAR = '\'';
        setTableNames(TABLE_NAME_PREFIX);

        m_psetClassName = myPackageName + ".PSet_TripleStore_RDB";
        m_psetReifierClassName = myPackageName + ".PSet_ReifStore_RDB";

        m_lsetClassName = myPackageName + ".SpecializedGraph_TripleStore_RDB";                      
        m_lsetReifierClassName = myPackageName + ".SpecializedGraphReifier_RDB";
    }

    @Override
    public void close()
    {
        // This cause problems with in-memory and on disk because there can be multiple models open.   
        //shutdown() ;
    }
    
    public void shutdown()
    {
        try {
            Connection c = getConnection().getConnection() ;
            Statement s = c.createStatement() ;
            s.execute("SHUTDOWN COMPACT") ;
        } catch (SQLException ex)
        { 
            System.err.println("HSQL shutdown exception") ;
            ex.printStackTrace(System.err) ;
        }
    }
    
    @Override
    String[] getDbInitTablesParams()
    {
        String [] res = new String[3];

        getTblParams(res);
        EOS_LEN = EOS.length();

        return res;
    }

    /**
     * Return the parameters for table creation.
     * 1) column type for subj, prop, obj.
     * 2) column type for head.
     * 3) table and index name prefix.
     * @param param array to hold table creation parameters. 
     */
    protected void getTblParams ( String [] param ) {
        String spoColType;
        String headColType;

        if ( LONG_OBJECT_LENGTH > 4000 )
            throw new RDFRDBException("Long object length specified (" + LONG_OBJECT_LENGTH +
            ") exceeds maximum sane length of 4000.");
        if ( INDEX_KEY_LENGTH > 4000 )
            throw new RDFRDBException("Index key length specified (" + INDEX_KEY_LENGTH +
            ") exceeds maximum sane length of 4000.");

        spoColType  = "VARCHAR(" + LONG_OBJECT_LENGTH + ")";
        headColType = "VARCHAR(" + INDEX_KEY_LENGTH + ")";
        STRINGS_TRIMMED = false;

        param[0] = spoColType;
        param[1] = headColType;
        param[2] = TABLE_NAME_PREFIX;
    }

    @Override
    protected String[] getCreateTableParams( int graphId, boolean isReif )
    {
        String [] parms = new String[3];
        String [] res = new String[2];
                
        getTblParams (parms);
        int tblCnt = getTableCount(graphId);
        res[0] = genTableName(graphId,tblCnt,isReif);
        res[1] = parms[0];
        return res;
    }
        
    @Override
    public int graphIdAlloc ( String graphName )
    {
        DBIDInt result = null;
        int dbid = 0;
        try {
            //dbid = getInsertID(GRAPH_TABLE);
            PreparedStatement ps = m_sql.getPreparedSQLStatement("insertGraph",GRAPH_TABLE);
            // Autoincr field
            //ps.setInt(1,dbid);
            //ps.setString(2,graphName);
            ps.setString(1,graphName);
            ps.executeUpdate();
            dbid = getInsertID(GRAPH_TABLE);
        } catch (SQLException e) {
            throw new RDFRDBException("Failed to get last inserted ID: " + e);
        }
        return dbid;
    }
    
    @Override
    public void graphIdDealloc ( int graphId ) {
        DBIDInt result = null;
        try {
            PreparedStatement ps = m_sql.getPreparedSQLStatement("deleteGraph",GRAPH_TABLE);
            ps.setInt(1,graphId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RDFRDBException("Failed to delete graph ID: " + e);
        }
        return;
    }

    // Now common code moved to DriverRDB - delete this anytime after Jena 2.5.2
//    public int getInsertID(String tableName)
//    {
//        DBIDInt result = null;
//        try {
//            PreparedStatement ps = m_sql.getPreparedSQLStatement("getInsertID",tableName);
//            ResultSet rs = ps.executeQuery();
//            if (rs.next()) {
//                result = wrapDBID(rs.getObject(1));
//            } else
//                throw new RDFRDBException("No insert ID");
//        } catch (SQLException e) {
//            throw new RDFRDBException("Failed to insert ID: " + e);
//        }
//        return result.getIntID();
//    }

    @Override
    public void setConnection(IDBConnection dbcon)
    {
        m_dbcon = dbcon;

        try {           
            // Properties defaultSQL = SQLCache.loadSQLFile(DEFAULT_SQL_FILE, null, ID_SQL_TYPE);
            // m_sql = new SQLCache(SQL_FILE, defaultSQL, dbcon, ID_SQL_TYPE);
            m_sql = new SQLCache(SQL_FILE, null, dbcon, ID_SQL_TYPE);
        } catch (Exception e) {
            e.printStackTrace( System.err );
            logger.error("Unable to set connection for Driver:", e);
        }
    }

}
