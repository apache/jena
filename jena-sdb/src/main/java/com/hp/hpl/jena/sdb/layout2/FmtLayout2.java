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

package com.hp.hpl.jena.sdb.layout2;

import java.sql.SQLException;

import com.hp.hpl.jena.sdb.sql.SDBConnection;
import com.hp.hpl.jena.sdb.sql.SDBExceptionSQL;
import com.hp.hpl.jena.sdb.sql.TableUtils;
import com.hp.hpl.jena.sdb.store.StoreFormatterBase;


public abstract class FmtLayout2
    extends StoreFormatterBase
{
    public FmtLayout2(SDBConnection connection) { super(connection) ; }
    
    @Override
    public void format()
    { 
        formatTablePrefixes() ;
        formatTableNodes() ;
        formatTableTriples() ;
        formatTableQuads() ;
    }
    
    @Override
    public void truncate()
    {
        truncateTablePrefixes() ;
        truncateTableNodes() ;
        truncateTableTriples() ;
        truncateTableQuads() ;
    }
    
    @Override
    public void addIndexes()
    {
    	addIndexesTableTriples() ;
    	addIndexesTableQuads() ;
    }
    
    @Override
    public void dropIndexes()
    {
    	dropIndexesTableTriples() ;
    	dropIndexesTableQuads() ;
    }

    // Cols should be a list as in (o, s)
    protected String syntaxCreateIndex(String indexName, String cols, String table)
    {
        return String.format("CREATE INDEX %s ON %s %s", indexName, cols, table) ;
    }
    
    // Cols should be a list as in (o, s)
    protected String syntaxDropIndex(String indexName, String table)
    {
        return String.format("DROP INDEX %s", indexName) ;
    }
    
    // Excludes primary index.
    protected static String[] triplesIndexCols = { "(o, s)", "(p, o)" } ;
    protected static String[] triplesIndexNames = { "ObjSubj",  "PredObj" } ;
    
    protected void addIndexesTableTriples()
    {
        try {
            for ( int i = 0 ; i < triplesIndexNames.length ; i++)
                connection().exec(syntaxCreateIndex(triplesIndexNames[i],  TableDescTriples.name(), triplesIndexCols[i])) ;
        } catch (SQLException ex)
        { throw new SDBExceptionSQL("SQLException indexing table '"+TableDescTriples.name()+"'",ex) ; }
    }
    
    protected void dropIndexesTableTriples()
    {
        try {
            for ( String triplesIndexName : triplesIndexNames )
            {
                connection().exec( syntaxDropIndex( triplesIndexName, TableDescTriples.name() ) );
            }
        } catch (SQLException ex)
        { throw new SDBExceptionSQL("SQLException dropping indexes for table '"+TableDescTriples.name()+"'",ex) ; }
    }

    // Excludes primary index.
    protected static String[] quadIndexCols = { "(s, p, o)", "(p, o, s)", "(o, s, p)", "(g, p, o)", "(g, o, s)" } ;
    protected static String[] quadIndexNames = {"SubjPredObj",  "PredObjSubj", "ObjSubjPred", "GraPredObj", "GraObjSubj"} ;
    
    // Override this if the syntax is a bit different 
    protected void addIndexesTableQuads()
    {
        try {
            for ( int i = 0 ; i < quadIndexNames.length ; i++)
                connection().exec(syntaxCreateIndex(quadIndexNames[i],  TableDescQuads.name(), quadIndexCols[i])) ;
        } catch (SQLException ex)
        { throw new SDBExceptionSQL("SQLException indexing table '"+TableDescQuads.name()+"'",ex) ; }
    }
    
    // Override this if the syntax is a bit different (many are for DROP INDEX)
    protected void dropIndexesTableQuads()
    {
        try {
            for ( String quadIndexName : quadIndexNames )
            {
                connection().exec( syntaxDropIndex( quadIndexName, TableDescQuads.name() ) );
            }
        } catch (SQLException ex)
        { throw new SDBExceptionSQL("SQLException dropping indexes for table '"+TableDescQuads.name()+"'",ex) ; }
    }
    
    abstract protected void formatTableTriples() ;
    abstract protected void formatTableNodes() ;
    abstract protected void formatTableQuads() ;
    abstract protected void formatTablePrefixes() ;
    
    protected void truncateTableTriples()   { truncateTable(TableDescTriples.name()) ; }
    protected void truncateTableQuads()     { truncateTable(TableDescQuads.name()) ; }
    protected void truncateTableNodes()     { truncateTable(TableDescNodes.name()) ; }
    protected void truncateTablePrefixes()  { truncateTable(TablePrefixes.name()) ; }
    
    protected void truncateTable(String tableName)
    {
        try { 
            connection().exec("DELETE FROM "+tableName) ;
        } catch (SQLException ex)
        { throw new SDBExceptionSQL("SQLException truncating table: "+tableName,ex) ; }
    }
    
    protected void dropTable(String tableName)
    {
        TableUtils.dropTable(connection(), tableName) ;
    }
}
