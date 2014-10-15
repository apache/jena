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

package com.hp.hpl.jena.sdb.store;

import java.io.StringReader;
import java.io.StringWriter;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.util.FileUtils;

import com.hp.hpl.jena.sparql.util.graph.GraphUtils;

import com.hp.hpl.jena.sdb.sql.*;

// Refactor
//   Name->BlobBytes class
//   Need to make (more) DB independent
//   Prepared statements

/**
 * A table that stores small models for small configuration information.
 * Stores an N-TRIPLES (robust to charsets) graph.
 * 
 * The objective here is not efficiency - it's stability of design because 
 * this may record version and layout configuration details so it needs to
 * be a design that will not change. 
 */


public class StoreConfig extends SDBConnectionHolder
{
    private static Logger log = LoggerFactory.getLogger(StoreConfig.class) ;
    private static final String serializationFormat = FileUtils.langNTriple ;
    
    public static final String defaultTag = "config" ;
    
    private boolean initialized = false ;
    private Map<String, Model> cache = new HashMap<String, Model>() ;
    private boolean caching = true ;
    TaggedString storage = null ;
    
    
    
    private Resource rootType = ResourceFactory.createResource() ;
    
    public StoreConfig(SDBConnection sdb)
    {   
        super(sdb) ;
        storage = new TaggedString(connection()) ;
    }

    /** Get the real tables in the database, not what any configuration information may think */
    public List<String> tables()
    {
        return TableUtils.getTableNames(connection().getSqlConnection()) ;
    }
    
    private Resource getRoot()
    {
        Model model = getModel() ;
        return GraphUtils.getResourceByType(model, ConfigVocab.typeConfig) ;
    }
    
    
    public void removeModel() { removeModel(defaultTag) ; }
    public void removeModel(String tag)
    { 
        init() ;
        log.trace(".removeModel: "+tag) ;
        storage.remove(tag) ;
    }
    
    public Model getModel() { return getModel(defaultTag) ; }

    public Model getModel(String tag)
    {
        init() ;
        log.trace(".getModel: "+tag) ;
        Model m = null ;
        
        if ( caching && cache.containsKey(tag) )
        {
            log.trace(".getModel: cache hit for "+tag) ;
            return cache.get(tag) ;
        }
        log.trace(".getModel: cache miss for "+tag) ;
        
        m = readModel(tag) ;
        if ( m == null )
            return null ;
        
        if ( caching )
            cache.put(tag, m) ;
        return m ;
    }
    
    public void setModel(Model m) { setModel(defaultTag, m) ; }
    public void setModel(String tag, Model m)
    {
        init() ;
        log.trace(".setModel: "+tag) ;
        // Write before caching.
        writeModel(tag, m) ;
        if ( caching )
        {
            log.trace(".setModel: cache model for "+tag) ;
            cache.put(tag, m) ;
        }
    }
    
    public List<String> getTags()
    {
        init() ;
        return storage.tags() ;
    }
    
    public void reset() { storage.reset() ; }
    
    //public void flush() { writeConfigModel() ; }
    
    private void init()
    {
        if ( initialized )
            return ;
        initialized = true ;
        
        if ( storage != null )
            return ;
        
//        LoggerFactory.getLogger(this.getClass()).warn("TESTING: config storage reset") ;
//        storage.reset() ;
    }
    
    private Model readModel(String tag)
    {
        log.trace(".readModel: "+tag) ;
        String s = storage.get(tag) ;
        
        if ( s == null )
            return null ;
        
        Model m = ModelFactory.createDefaultModel() ;
        StringReader r =  new StringReader(s) ; 
        m.read(r, null, serializationFormat) ;
        return m ;
    }
    
    private void writeModel(String tag, Model model)
    {
        log.trace(".writeModel: "+tag) ;
        StringWriter x = new StringWriter() ;
        model.write(x, serializationFormat) ;
        storage.set(tag, x.toString());
    }
}

class TaggedString extends SDBConnectionHolder
{
    
    static final String stringTableName  = "Strings" ;
    static final String columnName       = "tag" ;
    static final String columnData       = "data" ;
    
    private boolean initialized = false ;
    
    TaggedString(SDBConnection sdb) { super(sdb) ; }    

    void reset()
    {
        // MySQL.
        //final String sqlStmt1 = "DROP TABLE IF EXISTS "+stringTableName+" ;" ;

        final String sqlStmt1 = SQLUtils.sqlStr("DROP TABLE "+stringTableName) ;

        final String sqlStmt2 = SQLUtils.sqlStr(
                                                // Should be good for all databases
                                                // TODO Use TEXT? Moderately universal.
                                                "CREATE TABLE "+stringTableName,
                                                "( "+columnName+" VARCHAR(200) NOT NULL,",
                                                "  "+columnData+" VARCHAR(20000) NOT NULL ,",
                                                "  PRIMARY KEY("+columnName+")",
                                                ")") ;
        try
        { 
            if ( TableUtils.hasTable(connection().getSqlConnection(), stringTableName) )
                connection().execUpdate(sqlStmt1);
            connection().execUpdate(sqlStmt2);
        } catch (SQLException ex)
        { throw new SDBExceptionSQL("NamedString.reset", ex) ; }
    }

    private void init()
    {
        if ( initialized )
            return ;
        // TODO prepare statements
    }
    
    List<String> tags()
    {
        ResultSetJDBC rs = null ;
        try {
            final String sqlStmt = SQLUtils.sqlStr(
               "SELECT "+columnName,
               "FROM "+stringTableName) ;
            rs = connection().execQuery(sqlStmt) ;
            List<String> tags = new ArrayList<String>() ;
            while ( rs.get().next() )
            {
                String x = rs.get().getString(columnName) ;
                tags.add(x) ;
            }
            return tags ;
        }
        catch (SQLException ex)
        { throw new SDBExceptionSQL("getString", ex) ; }
        finally { RS.close(rs) ; }
    }
    
    void remove(String tag)
    {
        try {
            connection().exec("DELETE FROM "+stringTableName+" WHERE "+columnName+"="+SQLUtils.quoteStr(tag)) ;
        }
        catch (SQLException ex)
        { throw new SDBExceptionSQL(ex) ; }
    }
    
    void set(String tag, String value)
    {
        remove(tag) ;
        value = encode(value) ;
        try {
            connection().exec("INSERT INTO "+stringTableName+" VALUES ("+SQLUtils.quoteStr(tag)+", "+SQLUtils.quoteStr(value)+")") ;
        }
        catch (SQLException ex)
        { throw new SDBExceptionSQL("set", ex) ; }
    }

    String get(String tag)
    {
        boolean b = connection().loggingSQLExceptions() ;
        connection().setLogSQLExceptions(false) ;
        ResultSetJDBC rs = null ;
        try {
            final String sqlStmt = SQLUtils.sqlStr(
               "SELECT "+columnData,
               "FROM "+stringTableName,
               "WHERE "+columnName+" = "+SQLUtils.quoteStr(tag)) ;
            rs = connection().execQuery(sqlStmt) ;
            if ( ! rs.get().next() )
                //  No row.
                return null ;
                
            String x = rs.get().getString(columnData) ;
            return decode(x) ;
        }
        catch (SQLException ex)
        { 
            //throw new SDBExceptionSQL("getString", ex) ;
            return null ;
        }
        finally
        {
            connection().setLogSQLExceptions(b) ;
            RS.close(rs) ;
        }
    }
    
    // Escape non-7bit bytes. e.g. \ u stuff.
    // Not needed when using N-TRIPLES
    
    private String encode(String s) { return s ; }
        
    private String decode(String s) { return s ; }
    
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
