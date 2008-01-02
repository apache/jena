/*
 * (c) Copyright 2006, 2007, 2008 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sdb.graph;

import static com.hp.hpl.jena.sdb.sql.SQLUtils.quoteStr;
import static com.hp.hpl.jena.sdb.sql.SQLUtils.sqlStr;
import static java.lang.String.format;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hp.hpl.jena.sdb.sql.ResultSetJDBC;
import com.hp.hpl.jena.sdb.sql.SDBConnection;
import com.hp.hpl.jena.sdb.sql.SDBExceptionSQL;
import com.hp.hpl.jena.shared.PrefixMapping;
import com.hp.hpl.jena.shared.impl.PrefixMappingImpl;

/** A prefix mapping that is backed by a database table
 * 
 * @author Andy Seaborne
 */

@SuppressWarnings("deprecation")
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
    
    // TODO Per-graph prefix mappings.
    
    // TODO: Encode capital letters in prefix names.
    
    // new design needed : safe failures of other updates.
    
    
    // We are an in-memory prefix mapping except the update operations
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
    
    private SDBConnection connection = null ;
    private String graphName ; 
    
    public PrefixMappingSDB(String graphURI, SDBConnection sdb)
    {
        super() ;
        graphName = graphURI ;
        connection = sdb  ;
        // ModelRDB does not support prefixes in the same way as models/graphs over SDB stores. 
        try { readPrefixMapping() ; }
        catch (Throwable th) { }
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
            
            ResultSetJDBC rsx = connection.execSilent(sqlStmt) ;
            if ( rsx == null || rsx.get() == null )
                return ;
            ResultSet rs = rsx.get() ;
            while(rs.next())
            {
                String p = rs.getString("prefix") ;
                p = decode(p) ;
                String v = rs.getString("uri") ;
                // Load in-memory copy.
                super.set(p, v) ;
            }
            rsx.close() ;
        } catch (SQLException ex)
        { throw new SDBExceptionSQL("Failed to get prefixes", ex) ; }
    }

    private String readFromPrefixMap(String prefix)
    {
        try {
            String sqlStmt = sqlStr(
                "SELECT uri FROM "+prefixTableName,
                "   WHERE prefix = "+quoteStr(prefix)
                ) ;
            ResultSetJDBC rsx = connection.execQuery(sqlStmt) ;
            ResultSet rs = rsx.get() ;
            String uri = null ;
            while(rs.next())
            {
                String v = rs.getString("uri") ;
                uri = v ;
                if ( rs.next() )
                    log.warn("Multiple prefix mappings for '"+prefix+"'") ;
                break ;
            }
            rsx.close() ;
            return uri ;
        } catch (SQLException ex)
        { throw new SDBExceptionSQL(format("Failed to read prefix (%s)", prefix), ex) ; }
    }
    
    private void insertIntoPrefixMap(String prefix, String uri)
    {
        // Only called from set() and set() has already updated the superclass
        // but get() defers to superclass so is never null.  Err.
        try {
            // Delete old one.
            String x = get(prefix) ;
            if ( x != null )
                removeFromPrefixMap(prefix, x) ;
            prefix = encode(prefix) ;
            String sqlStmt = sqlStr(
                "INSERT INTO "+prefixTableName,
                "   VALUES ("+quoteStr(prefix)+", "+quoteStr(uri)+")"
                ) ;
            connection.execUpdate(sqlStmt) ;
        } catch (SQLException ex)
        { throw new SDBExceptionSQL(format("Failed to set prefix (%s,%s)", prefix, uri), ex) ; }
    }
    
    private void removeFromPrefixMap(String prefix, String uri)
    {
        try {
            prefix = encode(prefix) ;
            String sqlStmt = sqlStr(
                 "DELETE FROM "+prefixTableName+" WHERE",
                 "   prefix = "+quoteStr(prefix) //+" AND uri = "+quote(uri)
                 ) ;
            connection.execUpdate(sqlStmt) ;
        } catch (SQLException ex)
        { throw new SDBExceptionSQL(format("Failed to remove prefix (%s,%s)", prefix, uri), ex) ; }
    }
    
    // Always put in a trailing ":" so the prefix is never the empty string
    // which is null on Oracle but the prefix is a primary key and can't be null. 
    
    private String encode(String prefix)
    { return prefix+":" ; }

    private String decode(String prefix)
    { return prefix.substring(0, prefix.length()-1) ; }

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