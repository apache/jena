/*****************************************************************************
 * Source code information
 * -----------------------
 * Original author    Ian Dickinson, HP Labs Bristol
 * Author email       ian.dickinson@hp.com
 * Package            Jena 2
 * Web                http://sourceforge.net/projects/jena/
 * Created            25-Jul-2003
 * Filename           $RCSfile: PersistentOntology.java,v $
 * Revision           $Revision: 1.1 $
 * Release status     $State: Exp $
 *
 * Last modified on   $Date: 2003-07-25 22:41:19 $
 *               by   $Author: ian_dickinson $
 *
 * (c) Copyright 2002-2003, Hewlett-Packard Company, all rights reserved.
 * (see footer for full conditions)
 *****************************************************************************/

// Package
///////////////

// Imports
///////////////
import java.util.*;

import com.hp.hpl.jena.db.*;
import com.hp.hpl.jena.ontology.*;
import com.hp.hpl.jena.rdf.model.*;


/**
 * <p>
 * Simple example of using the persistent db layer with ontology models.  Assumes
 * that a PostgreSQL database called 'jenatest' has been set up, for a user named ijd.
 * </p>
 *
 * @author Ian Dickinson, HP Labs
 *         (<a  href="mailto:Ian.Dickinson@hp.com" >email</a>)
 * @version CVS $Id: PersistentOntology.java,v 1.1 2003-07-25 22:41:19 ian_dickinson Exp $
 */
public class PersistentOntology {
    // Constants
    //////////////////////////////////

    public static final String ONT1 = "urn:x-hp-jena:test1";
    public static final String ONT2 = "urn:x-hp-jena:test2";

    public static final String DB_URL = "jdbc:postgresql://localhost/jenatest";
    public static final String DB_USER = "ijd";
    public static final String DB_PASSWD = "";
    public static final String DB = "PostgreSQL";


    // Static variables
    //////////////////////////////////

    // Instance variables
    //////////////////////////////////

    // Constructors
    //////////////////////////////////

    // External signature methods
    //////////////////////////////////

    public static void main( String[] args ) {
        new PersistentOntology().go( args );
    }


    protected void go( String[] args ) {
        if (args.length > 0 && args[0].equals( "reload" )) {
            reloadDB();
        }
        listClasses();
    }


    protected void reloadDB() {
        ModelMaker maker = getMaker();

        // clear out the old
        if (maker.hasModel( ONT1 )) maker.removeModel( ONT1 );
        if (maker.hasModel( ONT2 )) maker.removeModel( ONT2 );

        // create a spec for the new ont model
        OntModelSpec spec = new OntModelSpec( OntModelSpec.OWL_MEM_RULE_INF );
        spec.setModelMaker( maker );

        // create the base model as a persistent model
        Model base = maker.createModel( ONT1 );
        OntModel m = ModelFactory.createOntologyModel( spec, base );

        // tell m where to find the content for ont2
        m.getDocumentManager().addAltEntry( ONT2, "file:test2.owl" );

        // now load the document for test1, which will import ONT2
        m.read( "file:test.owl" );
    }

    protected void listClasses() {
        OntModelSpec spec = new OntModelSpec( OntModelSpec.OWL_MEM_RULE_INF );
        spec.setModelMaker( getMaker() );

        OntModel m = ModelFactory.createOntologyModel( spec, getMaker().createModel( ONT1 ) );

        for (Iterator i = m.listClasses(); i.hasNext(); ) {
            OntClass c = (OntClass) i.next();
            System.out.println( "Class " + c.getURI() );
        }
    }


    protected ModelMaker getMaker() {
        try {
            // Load the Driver
            String className = "org.postgresql.Driver";
            Class.forName(className);

            // Create database connection
            IDBConnection conn  = new DBConnection ( DB_URL, DB_USER, DB_PASSWD, DB );

            // Create a model maker object
            return ModelFactory.createModelRDBMaker(conn);
        }
        catch (Exception e) {
            e.printStackTrace();
            System.exit( 1 );
        }

        return null;
    }




    // Internal implementation methods
    //////////////////////////////////

    //==============================================================================
    // Inner class definitions
    //==============================================================================

}


/*
    (c) Copyright Hewlett-Packard Company 2002-2003
    All rights reserved.

    Redistribution and use in source and binary forms, with or without
    modification, are permitted provided that the following conditions
    are met:

    1. Redistributions of source code must retain the above copyright
       notice, this list of conditions and the following disclaimer.

    2. Redistributions in binary form must reproduce the above copyright
       notice, this list of conditions and the following disclaimer in the
       documentation and/or other materials provided with the distribution.

    3. The name of the author may not be used to endorse or promote products
       derived from this software without specific prior written permission.

    THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
    IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
    OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
    IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
    INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
    NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
    DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
    THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
    (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
    THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/

