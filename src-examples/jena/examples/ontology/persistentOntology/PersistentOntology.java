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
 * Last modified on   $Date: 2005-10-06 17:49:07 $
 *               by   $Author: andy_seaborne $
 *
 * (c) Copyright 2002, 2003, 2004, 2005 Hewlett-Packard Development Company, LP
 * (see footer for full conditions)
 *****************************************************************************/

// Package
///////////////
package jena.examples.ontology.persistentOntology;


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
 * @version CVS $Id: PersistentOntology.java,v 1.1 2005-10-06 17:49:07 andy_seaborne Exp $
 */
public class PersistentOntology {
    // Constants
    //////////////////////////////////


    // Static variables
    //////////////////////////////////

    // Instance variables
    //////////////////////////////////

    // Constructors
    //////////////////////////////////

    // External signature methods
    //////////////////////////////////

    public void loadDB( ModelMaker maker, String source ) {
        // use the model maker to get the base model as a persistent model
        // strict=false, so we get an existing model by that name if it exists
        // or create a new one
        Model base = maker.createModel( source, false );

        // now we plug that base model into an ontology model that also uses
        // the given model maker to create storage for imported models
        OntModel m = ModelFactory.createOntologyModel( getModelSpec( maker ), base );

        // now load the source document, which will also load any imports
        m.read( source );
    }

    public void listClasses( ModelMaker maker, String modelID ) {
        // use the model maker to get the base model as a persistent model
        // strict=false, so we get an existing model by that name if it exists
        // or create a new one
        Model base = maker.createModel( modelID, false );

        // create an ontology model using the persistent model as base
        OntModel m = ModelFactory.createOntologyModel( getModelSpec( maker ), base );

        for (Iterator i = m.listClasses(); i.hasNext(); ) {
            OntClass c = (OntClass) i.next();
            System.out.println( "Class " + c.getURI() );
        }
    }


    public ModelMaker getRDBMaker( String dbURL, String dbUser, String dbPw, String dbType, boolean cleanDB ) {
        try {
            // Create database connection
            IDBConnection conn  = new DBConnection( dbURL, dbUser, dbPw, dbType );

            // do we need to clean the database?
            if (cleanDB) {
                conn.cleanDB();
            }

            // Create a model maker object
            return ModelFactory.createModelRDBMaker( conn );
        }
        catch (Exception e) {
            e.printStackTrace();
            System.exit( 1 );
        }

        return null;
    }

    public OntModelSpec getModelSpec( ModelMaker maker ) {
        // create a spec for the new ont model that will use no inference over models
        // made by the given maker (which is where we get the persistent models from)
        OntModelSpec spec = new OntModelSpec( OntModelSpec.OWL_MEM );
        spec.setImportModelMaker( maker );

        return spec;
    }


    // Internal implementation methods
    //////////////////////////////////

    //==============================================================================
    // Inner class definitions
    //==============================================================================

}


/*
    (c) Copyright 2002, 2003, 2004, 2005 Hewlett-Packard Development Company, LP
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

