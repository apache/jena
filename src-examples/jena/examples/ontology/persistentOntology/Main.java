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

// Package
///////////////
package jena.examples.ontology.persistentOntology;


// Imports
///////////////
import com.hp.hpl.jena.ontology.*;
import com.hp.hpl.jena.rdf.model.ModelMaker;


/**
 * <p>
 * Simple execution wrapper for persistent ontology example.
 * </p>
 * <p>
 * Usage:
 * <pre>
 * java jena.examples.ontology.persistentOntology.Main
 *                  [--dbUser string]     e.g: --dbUser ijd
 *                  [--dbURL string]      e.g: --dbURL jdbc:postgresql://localhost/jenatest
 *                  [--dbPw string]       e.g: --dbPw nosecrets
 *                  [--dbType string]     e.g: --dbType PostgreSQL
 *                  [--dbDriver string]   e.g: --dbDriver org.postgresql.Driver
 *                  [--reload]            if true will reload the source data
 *                  [sourceURL]           optional source URL for the data to persist
 * </pre>
 * If no db parameters or source URL is given, defaults will be used.
 * </p>
 *
 * @author Ian Dickinson, HP Labs
 *         (<a  href="mailto:ian_dickinson@users.sourceforge.net" >email</a>)
 * @version CVS $Id: Main.java,v 1.3 2009-10-06 13:04:42 ian_dickinson Exp $
 */
public class Main {
    // Constants
    //////////////////////////////////

    public static final String ONT1 = "urn:x-hp-jena:test1";
    public static final String ONT2 = "urn:x-hp-jena:test2";

    public static final String DB_URL = "jdbc:postgresql://localhost/jenatest";
    public static final String DB_USER = "ijd";
    public static final String DB_PASSWD = "";
    public static final String DB = "PostgreSQL";
    public static final String DB_DRIVER = "org.postgresql.Driver";

    // Static variables
    //////////////////////////////////

    // database connection parameters, with defaults
    private static String s_dbURL = DB_URL;
    private static String s_dbUser = DB_USER;
    private static String s_dbPw = DB_PASSWD;
    private static String s_dbType = DB;
    private static String s_dbDriver = DB_DRIVER;

    // if true, reload the data
    private static boolean s_reload = false;

    // source URL to load data from; if null, use default
    private static String s_source;


    // Instance variables
    //////////////////////////////////


    // Constructors
    //////////////////////////////////

    // External signature methods
    //////////////////////////////////

    public static void main( String[] args ) {
        processArgs( args );

        // check for default sources
        if (s_source == null) {
            s_source = getDefaultSource();
        }

        // create the helper class we use to handle the persistent ontologies
        PersistentOntology po = new PersistentOntology();

        // ensure the JDBC driver class is loaded
        try {
            Class.forName( s_dbDriver );
        }
        catch (Exception e) {
            System.err.println( "Failed to load the driver for the database: " + e.getMessage() );
            System.err.println( "Have you got the CLASSPATH set correctly?" );
        }

        // are we re-loading the data this time?
        if (s_reload) {

            // we pass cleanDB=true to clear out existing models
            // NOTE: this will remove ALL Jena models from the named persistent store, so
            // use with care if you have existing data stored
            ModelMaker maker = po.getRDBMaker( s_dbURL, s_dbUser, s_dbPw, s_dbType, true );

            // now load the source data into the newly cleaned db
            po.loadDB( maker, s_source );
        }

        // now we list the classes in the database, to show that the persistence worked
        ModelMaker maker = po.getRDBMaker( s_dbURL, s_dbUser, s_dbPw, s_dbType, false );
        po.listClasses( maker, s_source );
    }


    // Internal implementation methods
    //////////////////////////////////

    /**
     * Process any command line arguments
     */
    private static void processArgs( String[] args ) {
        int i = 0;
        while (i < args.length) {
            String arg = args[i++];

            if      (arg.equals( "--dbUser" ))   {s_dbURL = args[i++];}
            else if (arg.equals( "--dbURL" ))    {s_dbURL = args[i++];}
            else if (arg.equals( "--dbPasswd" )) {s_dbPw = args[i++];}
            else if (arg.equals( "--dbType" ))   {s_dbType = args[i++];}
            else if (arg.equals( "--reload" ))   {s_reload = true;}
            else if (arg.equals( "--dbDriver" )) {s_dbDriver = args[i++];}
            else {
                // assume this is a URL to load data from
                s_source = arg;
            }
        }
    }

    /**
     * Answer the default source document, and set up the document manager
     * so that we can find it on the file system
     *
     * @return The URI of the default source document
     */
    private static String getDefaultSource() {
        // use the ont doc mgr to map from a generic URN to a local source file
        OntDocumentManager.getInstance().addAltEntry( ONT1, "file:src-examples/data/test1.owl" );
        OntDocumentManager.getInstance().addAltEntry( ONT2, "file:src-examples/data/test2.owl" );

        return ONT1;
    }


    //==============================================================================
    // Inner class definitions
    //==============================================================================

}
