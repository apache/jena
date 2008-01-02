/******************************************************************
 * File:        Driver_MsSQL.java
 * Created by:  Dave Reynolds
 * Created on:  02-Nov-2005
 * 
 * (c) Copyright 2005, Hewlett-Packard Development Company, LP
 * [See end of file]
 * $Id: Driver_MsSQL.java,v 1.6 2008-01-02 12:08:23 andy_seaborne Exp $
 *****************************************************************/

package com.hp.hpl.jena.db.impl;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Properties;

import com.hp.hpl.jena.db.IDBConnection;
import com.hp.hpl.jena.db.RDFRDBException;

/**
 * This is a driver file for MS SQL Server 2000, MSDE 2000 and 
 * SQL Server 2005 (inc MS SQL Server Express).
 * <p>
 * There is very little difference from the postgres driver except for the
 * use of script inheritance to override some of the postgresql SQL commands and
 * a small difference in the use of ID allocation.
 * <p>
 * The id allocation approach was adopted from an earlier driver by Erik Barke (eba@ur.se)
 * <p>
 * N.B. If the postgresql driver file is changed this should be reviewed
 * for impact.
 * 
 * @author <a href="mailto:der@hplb.hpl.hp.com">Dave Reynolds</a>
 * @version $Revision: 1.6 $
 */

public class Driver_MsSQL extends Driver_PostgreSQL  {

    /** Default SQL file from which this driver inherits base operations */
    protected static final String DEFAULT_SQL = "etc/postgresql.sql";
    
    /**
     * Constructor. Sets up all the interesting parameters.
     */
    public Driver_MsSQL() {
        super();

        String myPackageName = this.getClass().getPackage().getName();
        
        DATABASE_TYPE = "MsSQL";
        DRIVER_NAME = "com.microsoft.sqlserver.jdbc.SQLServerDriver";
        
        ID_SQL_TYPE = "INTEGER";
        URI_COMPRESS = false;
        INDEX_KEY_LENGTH_MAX = INDEX_KEY_LENGTH = 225;
        LONG_OBJECT_LENGTH_MAX = LONG_OBJECT_LENGTH = 225;
        TABLE_NAME_LENGTH_MAX = 128;
        IS_XACT_DB = true;
        PRE_ALLOCATE_ID = false;
        SKIP_DUPLICATE_CHECK = false;
        SQL_FILE = "etc/mssql.sql";
        QUOTE_CHAR = '\'';
        DB_NAMES_TO_UPPER = false;
        setTableNames(TABLE_NAME_PREFIX);
        
        m_psetClassName = myPackageName + ".PSet_TripleStore_RDB";
        m_psetReifierClassName = myPackageName + ".PSet_ReifStore_RDB";
        
        m_lsetClassName = myPackageName + ".SpecializedGraph_TripleStore_RDB";                      
        m_lsetReifierClassName = myPackageName + ".SpecializedGraphReifier_RDB";
    }
    
    /**
     * Set the database connection
     */
    public void setConnection( IDBConnection dbcon ) {
        m_dbcon = dbcon;
        
        try {           
             Properties defaultSQL = SQLCache.loadSQLFile(DEFAULT_SQL, null, ID_SQL_TYPE);
             m_sql = new SQLCache(SQL_FILE, defaultSQL, dbcon, ID_SQL_TYPE);
        } catch (Exception e) {
            e.printStackTrace( System.err );
            logger.error("Unable to set connection for Driver:", e);
        }
    }

    /**
     * Insert a long object into the database.
     * This assumes the object is not already in the database.
     * Almost a clone of the standard code in Driver_RDB but
     * returns the ID from the insert instead of from a separate call.
     *
     * @return the db index of the added literal
     */
    public DBIDInt addRDBLongObject(RDBLongObject lobj, String table)  throws RDFRDBException {
        DBIDInt result = null;
        try {
            int argi = 1;
            String opname = "insertLongObject";
            PreparedStatement ps = m_sql.getPreparedSQLStatement(opname, table);

            ps.setString(argi++, lobj.head);
            if (lobj.tail.length() > 0) {
                ps.setLong(argi++, lobj.hash);
                ps.setString(argi++, lobj.tail);
            } else {
                ps.setNull(argi++, java.sql.Types.BIGINT);
                ps.setNull(argi++, java.sql.Types.LONGVARCHAR);
            }

            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                result = wrapDBID(rs.getObject(1));
            } else {
                throw new RDFRDBException("No insert ID");
            }

            return result;
            
        } catch (Exception e1) {
            throw new RDFRDBException("Failed to add long object ", e1);
        }
    }
    
    /**
     * Allocate an identifier for a new graph.
     *
     */
    public int graphIdAlloc(String graphName) {
        DBIDInt result = null;
        try {
            PreparedStatement ps =
                m_sql.getPreparedSQLStatement("insertGraph", GRAPH_TABLE);
            ps.setString(1, graphName);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                result = wrapDBID(rs.getObject(1));
            } else {
                throw new RDFRDBException("No insert ID");
            }
        } catch (SQLException e) {
            throw new RDFRDBException("Failed to get last inserted ID: " + e);
        }
        return result.getIntID();
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

        spoColType = "NVARCHAR(" + LONG_OBJECT_LENGTH + ")";
        STRINGS_TRIMMED = false;
        param[0] = spoColType;
        headColType = "NVARCHAR(" + INDEX_KEY_LENGTH + ")";
        STRINGS_TRIMMED = false;
        param[1] = headColType;
        param[2] = TABLE_NAME_PREFIX;
    }

    // Suppressed. This was an attempt to force use of row level locking to
    // get round concurrency problems. Code level in comment form for future reference.
//    public String createTable( int graphId, boolean isReif) {
//        String tableName = super.createTable(graphId, isReif);
//        try {
//            xactOp(xactCommit);
//            m_sql.runSQLGroup("setLockLevel", tableName);
//            xactOp(xactBegin);
//        } catch (SQLException e) {
//            logger.error("Problem creating table", e);
//            throw new RDFRDBException("Failed to set lock level in statement table", e);
//        }
//        return tableName;
//    }
    
    // TODO: Review string match op codes
    // TODO: Check case sensitivity of URI retrieval
    // Case sensitive search is a problem (SQL Server 2000 defaults to case insensitive and
    // you only change it by defining collation order for the column/table, earlier
    // SQL Servers required a database reinstallation to change the collation order).
    
}


/*
    (c) Copyright 2005, 2006, 2007, 2008 Hewlett-Packard Development Company, LP
    All rights reserved.

    Redistribution and use in source and binary forms, with or without
    modification, are permitted provided that the following conditions
    are met:

    1. Redistributions of source code must retain the above copyright
       notice, this list of conditions and the following disclaimer.

    2. Redistributions in binary form must reproduce the above copyright
       notice, this list of conditions and the following disclaimer in the
       documentation and/or other materials provided with the distribution.

    3. The name of the author may not be used to endorse or promote products
       derived from this software without specific prior written permission.

    THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
    IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
    OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
    IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
    INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
    NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
    DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
    THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
    (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
    THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/
