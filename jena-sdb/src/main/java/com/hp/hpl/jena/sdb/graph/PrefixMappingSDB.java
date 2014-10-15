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

package com.hp.hpl.jena.sdb.graph;

import static com.hp.hpl.jena.sdb.sql.SQLUtils.quoteStr;
import static com.hp.hpl.jena.sdb.sql.SQLUtils.sqlStr;
import static java.lang.String.format;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.sdb.sql.RS;
import com.hp.hpl.jena.sdb.sql.ResultSetJDBC;
import com.hp.hpl.jena.sdb.sql.SDBConnection;
import com.hp.hpl.jena.sdb.sql.SDBExceptionSQL;
import com.hp.hpl.jena.shared.PrefixMapping;
import com.hp.hpl.jena.shared.impl.PrefixMappingImpl;

/** A prefix mapping that is backed by a database table
 */

public class PrefixMappingSDB extends PrefixMappingImpl
{
    // Use DatasetPrefixStorage as move to Per-graph prefix mappings. (schema change)
    
    // See PrefixMappingPersistent
    static private Logger log = LoggerFactory.getLogger(PrefixMappingSDB.class) ;
    
    static public final String prefixTableName  = "Prefixes" ;
    static public final int    prefixColWidth   = 50 ;  // Minimum
    static public final int    uriColWidth      = 500 ; // Minimum
    
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
        readPrefixMapping() ;
    }
    
    @Override
    protected void set(String prefix, String uri)
    {
        // Delete old one if present.
        String x = get(prefix) ;
        if ( x != null )
        {
            if(x.equals(uri))
                // Already there - no-op (thanks to Eric Diaz for pointing this out)
                return;
            removeFromPrefixMap(prefix, x) ;
        }

        // Persist
        insertIntoPrefixMap(prefix, uri) ;
        // Set caches
        super.set(prefix, uri) ;
    }

    @Override
    protected String get(String prefix)
    {
        String x = super.get(prefix) ;
        if ( x != null )
            return x ;
        x = readFromPrefixMap(prefix) ;
        return x ;
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
        ResultSetJDBC rsx = null ;
        try {
            String sqlStmt = "SELECT prefix, uri FROM "+prefixTableName ;
            rsx = connection.execSilent(sqlStmt) ;
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
        } catch (SQLException ex)
        { throw new SDBExceptionSQL("Failed to get prefixes", ex) ; }
        finally
        {
            RS.close(rsx) ;
        }
    }

    private String readFromPrefixMap(String prefix)
    {
        ResultSetJDBC rsx = null ;
        try {
            String sqlStmt = sqlStr(
                "SELECT uri FROM "+prefixTableName,
                "   WHERE prefix = "+quoteStr(prefix)
                ) ;
            rsx = connection.execQuery(sqlStmt) ;
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
            return uri ;
        } catch (SQLException ex)
        { throw new SDBExceptionSQL(format("Failed to read prefix (%s)", prefix), ex) ; }
        finally { RS.close(rsx) ; }
    }
    
    private void insertIntoPrefixMap(String prefix, String uri)
    {
        // Only called from set()
        // Assumes not present in the persistent table.
        try {
            prefix = encode(prefix) ;
            String sqlStmt = sqlStr(
                "INSERT INTO "+prefixTableName,
                "   VALUES ("+quoteStr(prefix)+", "+quoteStr(uri)+")"
                ) ;
            connection.execUpdate(sqlStmt) ;
        } catch (SQLException ex)
        { 
            throw new SDBExceptionSQL(format("Failed to set prefix (%s,%s)", prefix, uri), ex) ; 
        }
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
