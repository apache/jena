/*
 * (c) Copyright 2006 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sdb.graph;

import static com.hp.hpl.jena.sdb.sql.SQLUtils.quote;
import static com.hp.hpl.jena.sdb.sql.SQLUtils.sqlStr;
import static java.lang.String.format;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hp.hpl.jena.sdb.sql.RS;
import com.hp.hpl.jena.sdb.sql.SDBConnection;
import com.hp.hpl.jena.sdb.sql.SDBExceptionSQL;
import com.hp.hpl.jena.shared.PrefixMapping;
import com.hp.hpl.jena.shared.impl.PrefixMappingImpl;

/** A prefix mapping that is backed by a database table
 * 
 * @author Andy Seaborne
 * @version $Id: PrefixMappingSDB.java,v 1.1 2006/04/23 18:40:12 andy_seaborne Exp $
 */

public class PrefixMappingSDB extends PrefixMappingImpl
{
    static private Log log = LogFactory.getLog(PrefixMappingSDB.class) ;
    
    static public final String prefixTableName  = "Prefixes" ;
    static public final int    prefixColWidth   = 50 ;  // Minimum
    static public final int    uriColWidth      = 500 ; // Minimum
    
    // TODO Currently, two of these sharing a connection don't work completely.
    // Some PrefixMappingImpl operations directly iterate over the internal maps.
    // Would work if they all called a minimal interface and leave the checking to
    // general code.
    
    // new design needed : safe failures of other updates. 
    
    // We are a an in-memory prefix mapping except the update operations
    // are also applied to the table.
    /* Roughly:
          CREATE TABLE Prefixes (,
              -- Each column should be:
              -- Case sensitive, UTF8 characters, not NULL
              -- MySQL: BINARY CHARACTER SET utf8 NOT NULL default '',",
              prefix VARCHAR , 
              uri TEXT or VARCHAR ,
              PRIMARY KEY (prefix) ,
          )
     */
    
    SDBConnection connection = null ; 
    
    public PrefixMappingSDB(SDBConnection sdb)
    {
        super() ;
        connection = sdb  ;
        readPrefixMapping() ;
    }
    
    @Override
    protected void set(String prefix, String uri)
    {
        super.set(prefix, uri) ;
        insertIntoPrefixMap(prefix, uri) ;
    }

    @Override
    protected String get(String prefix)
    {
        String x = super.get(prefix) ;
        if ( x !=  null )
            return x ;
        // In case it has been updated.
        return readFromPrefixMap(prefix) ;
    }
    
    @Override
    public PrefixMapping removeNsPrefix(String prefix)
    {
        String uri = super.getNsPrefixURI(prefix) ;
        if ( uri != null )
            removeFromPrefixMap(prefix, uri) ;
        super.removeNsPrefix(prefix) ;
        return this ; 
    }
    
    // Super class implementation loops and calls setNsPrefix calls set() for each entry.
    //@Override
    //public PrefixMapping setNsPrefixes(PrefixMapping arg0)

    // Super class implementation will suffice.
    // It (creates a map copy and) loops on each entry.
    //@Override
    //public PrefixMapping setNsPrefixes(Map arg0)
    
    // -------- Worker implementations
    
    private void readPrefixMapping()
    {
        try {
            String sqlStmt = "SELECT prefix, uri FROM "+prefixTableName ;
            ResultSet rs = connection.execQuery(sqlStmt) ;
            while(rs.next())
            {
                String p = rs.getString("prefix") ;
                String v = rs.getString("uri") ;
                // Load in-memory copy.
                super.set(p, v) ;
            }
            RS.close(rs) ;
        } catch (SQLException ex)
        { throw new SDBExceptionSQL("Failed to get prefixes", ex) ; }
    }

    private String readFromPrefixMap(String prefix)
    {
        try {
            String sqlStmt = sqlStr(
                "SELECT uri FROM "+prefixTableName,
                "   WHERE prefix = "+quote(prefix)
                ) ;
            ResultSet rs = connection.execQuery(sqlStmt) ;
            String uri = null ;
            while(rs.next())
            {
                String v = rs.getString("uri") ;
                uri = v ;
                if ( rs.next() )
                    log.warn("Multiple prefix mappings for '"+prefix+"'") ;
                break ;
            }
            RS.close(rs) ;
            return uri ;
        } catch (SQLException ex)
        { throw new SDBExceptionSQL(format("Failed to read prefix (%s)", prefix), ex) ; }
    }
    
    private void insertIntoPrefixMap(String prefix, String uri)
    {
        try {
            // Delete old one.
            String x = get(prefix) ;
            if ( x != null )
                removeFromPrefixMap(prefix, x) ;

            String sqlStmt = sqlStr(
                "INSERT INTO "+prefixTableName,
                "   VALUES ("+quote(prefix)+", "+quote(uri)+")"
                ) ;
            connection.execUpdate(sqlStmt) ;
        } catch (SQLException ex)
        { throw new SDBExceptionSQL(format("Failed to set prefix (%s,%s)", prefix, uri), ex) ; }
    }
    
    private void removeFromPrefixMap(String prefix, String uri)
    {
        try {
            String sqlStmt = sqlStr(
                 "DELETE FROM "+prefixTableName+" WHERE",
                 "   prefix = "+quote(prefix) //+" AND uri = "+quote(uri)
                 ) ;
            connection.execUpdate(sqlStmt) ;
        } catch (SQLException ex)
        { throw new SDBExceptionSQL(format("Failed to remove prefix (%s,%s)", prefix, uri), ex) ; }
    }
}

/*
 * (c) Copyright 2006 Hewlett-Packard Development Company, LP
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