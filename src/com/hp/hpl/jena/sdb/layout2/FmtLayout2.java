/*
 * (c) Copyright 2006, 2007, 2008 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
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
    
    public void format()
    { 
        formatTablePrefixes() ;
        formatTableNodes() ;
        formatTableTriples() ;
        formatTableQuads() ;
    }
    
    public void truncate()
    {
        truncateTablePrefixes() ;
        truncateTableNodes() ;
        truncateTableTriples() ;
        truncateTableQuads() ;
    }
    
    public void addIndexes()
    {
    	addIndexesTableTriples() ;
    	addIndexesTableQuads() ;
    }
    
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
            for ( int i = 0 ; i < triplesIndexNames.length ; i++)
                connection().exec(syntaxDropIndex(triplesIndexNames[i], TableDescTriples.name())) ;
        } catch (SQLException ex)
        { throw new SDBExceptionSQL("SQLException dropping indexes for table '"+TableDescTriples.name()+"'",ex) ; }
    }

    // Excludes primary index.
    protected static String[] quadIndexCols = { "(o, s, g)", "(p, o, g)", "(g, s, p)", "(g, p, o)", "(g, o, s)" } ;
    protected static String[] quadIndexNames = {"ObjSubjGra",  "PredObjGra", "GraSubjPred", "GraPredObj", "GraObjSubj"} ;
    
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
            for ( int i = 0 ; i < quadIndexNames.length ; i++)
                connection().exec(syntaxDropIndex(quadIndexNames[i],  TableDescQuads.name())) ;
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

/*
 * (c) Copyright 2006, 2007, 2008 Hewlett-Packard Development Company, LP
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