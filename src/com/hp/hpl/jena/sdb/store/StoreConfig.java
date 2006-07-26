/*
 * (c) Copyright 2006 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sdb.store;

import java.io.*;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

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
    public static final String configName = "config" ;
    private static final String serializationFormat = FileUtils.langNTriple ;
    
    private boolean initialized = false ;
    private Map<String, Model> cache = new HashMap<String, Model>() ;
    NamedString storage ;
    
    public StoreConfig(SDBConnection sdb)
    { super(sdb) ; }
    
    public Model getModel() { return getModel(configName) ; }

    public Model getModel(String name)
    {
        init() ;
        if ( ! cache.containsKey(name) )
        {
            Model m = readModel(name) ;
            cache.put(name, m) ;
            return m ;
        }
        return cache.get(name) ;
    }
    
    public void setModel(Model m) { setModel(configName, m) ; }
    public void setModel(String name, Model m)
    {
        init() ;
        cache.put(name, m) ;
        writeModel(name, m) ;
    }
    
    //public void flush() { writeConfigModel() ; }
    
    private void init()
    {
        if ( initialized )
            return ;
        initialized = true ;
        
        if ( storage != null )
            return ;
        
        storage = new NamedString(connection()) ;
        
        LogFactory.getLog(this.getClass()).warn("TESTING: config storage reset") ;
        storage.reset() ;
    }
    
    private Model readModel(String name)
    {
        String s = storage.get(name) ;
        
        if ( s == null )
            return null ;
        
        Model m = ModelFactory.createDefaultModel() ;
        StringReader r =  new StringReader(s) ; 
        m.read(s, serializationFormat) ;
        return m ;
    }
    
    private void writeModel(String name, Model model)
    {
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
        final String sqlStmt1 = "DROP TABLE IF EXISTS "+stringTableName+" ;" ;

        final String sqlStmt2 = SQLUtils.sqlStr(
                                                "CREATE TABLE "+stringTableName,
                                                "( "+columnName +" VARCHAR NOT NULL,",
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
    
    public void set(String name, String value)
    {
        value = encode(value) ;
        try {
            // Delete any old values.
            connection().execAny("DELETE FROM "+stringTableName+" WHERE "+columnName+"="+SQLUtils.quote(name)) ;
            connection().execAny("INSERT INTO "+stringTableName+" VALUES ("+SQLUtils.quote(name)+", "+SQLUtils.quote("")+")") ;
            // Hmm - how to include bytes at this point.
            // resultsSets have .updateBytes but how to get the ResultSet?
            Statement s = connection().getSqlConnection().createStatement() ;
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