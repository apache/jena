/*
 	(c) Copyright 2005 Hewlett-Packard Development Company, LP
 	All rights reserved - see end of file.
 	$Id: DriverMap.java,v 1.1 2005-07-29 09:11:28 chris-dollin Exp $
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
    
    private final static Map mapped = new HashMap();
    
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
        { return (String) mapped.get( name.toLowerCase() ); }
    
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

/*
 * (c) Copyright 2005 Hewlett-Packard Development Company, LP
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