/*****************************************************************************
 * Source code information
 * -----------------------
 * Original author    Ian Dickinson, HP Labs Bristol
 * Author email       ian.dickinson@hp.com
 * Package            Jena 2
 * Web                http://sourceforge.net/projects/jena/
 * Created            22-Aug-2003
 * Filename           $RCSfile: Main.java,v $
 * Revision           $Revision: 1.3 $
 * Release status     $State: Exp $
 *
 * Last modified on   $Date: 2003-11-19 12:13:38 $
 *               by   $Author: ian_dickinson $
 *
 * (c) Copyright 2002-2003, Hewlett-Packard Company, all rights reserved.
 * (see footer for full conditions)
 *****************************************************************************/

// Package
///////////////

// Imports
///////////////
import java.util.Iterator;

import com.hp.hpl.jena.ontology.*;
import com.hp.hpl.jena.rdf.model.ModelFactory;


/**
 * <p>
 * Execution wrapper for describe-class example
 * </p>
 *
 * @author Ian Dickinson, HP Labs
 *         (<a  href="mailto:Ian.Dickinson@hp.com" >email</a>)
 * @version CVS $Id: Main.java,v 1.3 2003-11-19 12:13:38 ian_dickinson Exp $
 */
public class Main {
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

    public static void main( String[] args ) {
        // read the argument file, or the default
        String source = (args.length == 0) ? "http://www.w3.org/TR/2003/CR-owl-guide-20030818/wine" : args[0];
        
        // guess if we're using a daml source
        boolean isDAML = source.endsWith( ".daml" );
        
        OntModel m = ModelFactory.createOntologyModel( 
                        isDAML ? OntModelSpec.DAML_MEM : OntModelSpec.OWL_MEM, null
                     );

        // we have a local copy of the wine ontology
        m.getDocumentManager().addAltEntry( "http://www.w3.org/TR/2003/CR-owl-guide-20030818/wine",
                                            "file:wine.owl" );
        m.getDocumentManager().addAltEntry( "http://www.w3.org/TR/2003/CR-owl-guide-20030818/food",
                                            "file:food.owl" );

        // read the source document
        m.read( source );

        DescribeClass dc = new DescribeClass();

        if (args.length >= 2) {
            // we have a named class to describe
            OntClass c = m.getOntClass( args[1] );
            dc.describeClass( System.out, c );
        }
        else {
            for (Iterator i = m.listClasses();  i.hasNext(); ) {
                // now list the classes
                dc.describeClass( System.out, (OntClass) i.next() );
            }
        }
    }


    // Internal implementation methods
    //////////////////////////////////

    //==============================================================================
    // Inner class definitions
    //==============================================================================

}

