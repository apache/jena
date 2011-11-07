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

import java.io.InputStream;
import java.util.*;

import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.util.FileManager;

/**
    The DriverMap statics maintain a map from short database type names to
    database driver classes. The map contains some built-in items (for MySQL
    and Postgres) and can be extended by files in the etc directory.
    
    @author kers
*/
public class DriverMap
    {
    public static final String uri = "http://db.jena.hpl.hp.com/vocabulary#";

    public static final Property driverClass = property( "driverClass" );
    public static final Property driverName = property( "driverName" );
    
    private final static Map<String, String> mapped = new HashMap<String, String>();
    
    static
        {
        add( "mysql", "com.mysql.jdbc.Driver" );
        add( "postgres", "org.postgresql.Driver" );
        add( "postgresql", "org.postgresql.Driver" );
        addIfPresent( "etc/db-default-drivers.n3" );
        addIfPresent( "etc/db-extra-drivers.n3" );
        }
    
    /**
        Answer the class name associated with the driver name in the map.
        <code>name</code> is lower-cased before lookup.
    */
    
    public static String get( String name )
        { return mapped.get( name.toLowerCase() ); }
    
    /**
        Add a mapping from a driver name to its class name. The driver name
        is lower-cased before adding.
    */
    public static void add( String name, String className )
        { mapped.put( name.toLowerCase(), className ); }

    /**
        Add the mappings specified in the file named by <code>fileName</code>,
        if it exists; that file must be an N3 file. 
    */
    public static void addIfPresent( String fileName )
        {
        InputStream in = FileManager.get().open( fileName );
        if (in != null) add( in );
        }
    
    static void add( InputStream in )
        {
        Property ANY = null;
        Model m = ModelFactory.createDefaultModel();
        m.read( in, "", "N3" );
        StmtIterator A = m.listStatements( ANY, DriverMap.driverClass, ANY );
        while (A.hasNext())
            {
            Statement st = A.nextStatement();
            Resource S = st.getSubject();
            String className = st.getString();
            StmtIterator B = m.listStatements( S, DriverMap.driverName, ANY );
            while (B.hasNext()) add( B.nextStatement().getString(), className );
            }
        }
    
    static Property property( String s )
        { return ResourceFactory.createProperty( uri + s ); }
    }
