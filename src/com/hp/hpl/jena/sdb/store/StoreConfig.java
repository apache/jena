/*
 * (c) Copyright 2006 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sdb.store;

import java.io.StringReader;
import java.io.StringWriter;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.sdb.sql.*;
import com.hp.hpl.jena.util.FileUtils;

// Refactor
//   Name->BlobBytes class
//   Need to make DB independent
//   Prepared statements
//   Delayed initialization though to make connect to DB fast still.

/**
 * A table that stores small models for small configuration information.
 * Stores an N-TRIPLES (robust to charsets) graph.
 * 
 * The objective here is not efficiency - it's stability of design because 
 * this may record version and layout configuration details so it needs to
 * be a design that will not change. 
 * 
 * @author Andy Seaborne
 * @version $Id: StoreConfig.java,v 1.3 2006/04/23 21:24:41 andy_seaborne Exp $
 */


public class StoreConfig extends SDBConnectionHolder
{
    private static Log log = LogFactory.getLog(StoreConfig.class) ;
    private static final String serializationFormat = FileUtils.langNTriple ;
    
    public static final String defaultName = "config" ;
    
    private boolean initialized = false ;
    private Map<String, Model> cache = new HashMap<String, Model>() ;
    NamedString storage = null ;
    
    public StoreConfig(SDBConnection sdb)
    {   
        super(sdb) ;
        storage = new NamedString(connection()) ;
        // Turn off for testing
        cache = null ;
    }

    public void removeModel() { removeModel(defaultName) ; }
    public void removeModel(String name)
    { 
        init() ;
        log.trace(".removeModel: "+name) ;
        storage.remove(name) ;
    }
    
    public Model getModel() { return getModel(defaultName) ; }

    public Model getModel(String name)
    {
        init() ;
        log.trace(".getModel: "+name) ;
        Model m = null ;
        
        if ( cache != null && cache.containsKey(name) )
        {
            log.trace(".getModel: cache hit for "+name) ;
            return cache.get(name) ;
        }
        log.trace(".getModel: cache miss for "+name) ;
        
        m = readModel(name) ;
        if ( m == null )
            return null ;
        
        if ( cache == null )
            return m ;
        cache.put(name, m) ;
        return cache.get(name) ;
    }
    
    public void setModel(Model m) { setModel(defaultName, m) ; }
    public void setModel(String name, Model m)
    {
        init() ;
        log.trace(".setModel: "+name) ;
        // Write before caching.
        writeModel(name, m) ;
        if ( cache != null )
        {
            log.trace(".setModel: cache model for "+name) ;
            cache.put(name, m) ;
        }
    }
    
    public List<String> getNames()
    {
        init() ;
        return storage.names() ;
    }
    
    //public void flush() { writeConfigModel() ; }
    
    private void init()
    {
        if ( initialized )
            return ;
        initialized = true ;
        
        if ( storage != null )
            return ;
        
//        LogFactory.getLog(this.getClass()).warn("TESTING: config storage reset") ;
//        storage.reset() ;
    }
    
    private Model readModel(String name)
    {
        log.trace(".readModel: "+name) ;
        String s = storage.get(name) ;
        
        if ( s == null )
            return null ;
        
        Model m = ModelFactory.createDefaultModel() ;
        StringReader r =  new StringReader(s) ; 
        m.read(r, null, serializationFormat) ;
        return m ;
    }
    
    private void writeModel(String name, Model model)
    {
        log.trace(".writeModel: "+name) ;
        StringWriter x = new StringWriter() ;
        model.write(x, serializationFormat) ;
        storage.set(name, x.toString());
    }
}

class NamedString extends SDBConnectionHolder
{
    
    static final String stringTableName  = "Strings" ;
    static final String columnName       = "name" ;
    static final String columnData       = "data" ;
    
    private boolean initialized = false ;
    
    public NamedString(SDBConnection sdb) { super(sdb) ; }    

    public void reset()
    {
        // MySQL.
        //final String sqlStmt1 = "DROP TABLE IF EXISTS "+stringTableName+" ;" ;

        final String sqlStmt2 = SQLUtils.sqlStr(
                                                "CREATE TABLE "+stringTableName,
                                                "( "+columnName +" VARCHAR NOT NULL,",
                                                // TODO Make TEXT (or binary blob)
                                                "  "+columnData+" VARCHAR NOT NULL ,",
                                                "  PRIMARY KEY("+columnName+")",
                                                ")") ;
        try
        { connection().execUpdate(sqlStmt2);
        } catch (SQLException ex)
        { throw new SDBExceptionSQL("NamedString.reset", ex) ; }
    }

    private void init()
    {
        if ( initialized )
            return ;
        // TODO prepare statements
    }
    
    public List<String> names()
    {
        try {
            final String sqlStmt = SQLUtils.sqlStr(
               "SELECT "+columnName,
               "FROM "+stringTableName) ;
            ResultSet rs = connection().execQuery(sqlStmt) ;
            List<String> names = new ArrayList<String>() ;
            while ( rs.next() )
            {
                String x = rs.getString(columnName) ;
                names.add(x) ;
            }
            RS.close(rs) ;
            return names ;
        }
        catch (SQLException ex)
        { throw new SDBExceptionSQL("getString", ex) ; }
    }
    
    public void remove(String name)
    {
        try {
            connection().exec("DELETE FROM "+stringTableName+" WHERE "+columnName+"="+SQLUtils.quote(name)) ;
        }
        catch (SQLException ex)
        { throw new SDBExceptionSQL("setString", ex) ; }
        
    }
    
    public void set(String name, String value)
    {
        value = encode(value) ;
        try {
            // Delete any old values.
            connection().exec("DELETE FROM "+stringTableName+" WHERE "+columnName+"="+SQLUtils.quote(name)) ;
            connection().exec("INSERT INTO "+stringTableName+" VALUES ("+SQLUtils.quote(name)+", "+SQLUtils.quote(value)+")") ;
        }
        catch (SQLException ex)
        { throw new SDBExceptionSQL("setString", ex) ; }
    }

    public String get(String name)
    {
        try {
            final String sqlStmt = SQLUtils.sqlStr(
               "SELECT "+columnData,
               "FROM "+stringTableName,
               "WHERE "+columnName+" = "+SQLUtils.quote(name)) ;
            ResultSet rs = connection().execQuery(sqlStmt) ;
            if ( rs.next() )
            {
                String x = rs.getString(columnData) ;
                RS.close(rs) ;
                return decode(x) ;
            }
            //  No row.
            RS.close(rs) ;
            return null ;
            
        }
        catch (SQLException ex)
        { throw new SDBExceptionSQL("getString", ex) ; }
    }
    
    // Escape non-7bit bytes. e.g. \ u stuff.
    
    private String encode(String s) { return s ; }
        
    private String decode(String s) { return s ; }
    
    // rs.updateBytes() ;
    // rs.updateRow() ;
    
//  private byte[] encode(String str)
//  {
//      // CharsetEncoder
//      // return byte[]
//      if ( str.indexOf('"') != -1 )
//          str = str.replace("\"", "\\\"") ;
//      Charset cs = Charset.forName("utf8") ;
//      byte[] b = cs.encode(str).array() ;
//      return b ;
//  }
//  
//  private String decode(byte[] b)
//  {
//      Charset cs = Charset.forName("utf8") ;
//      ByteBuffer bb = ByteBuffer.wrap(b) ;
//      String str = new String(cs.decode(bb).array()) ;
//      if ( str.indexOf('\\') == -1 )
//          return str ;
//      return str.replace("\\\"", "\"") ;
//  }
    
    
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