/*****************************************************************************
 * Source code information
 * -----------------------
 * Original author    Ian Dickinson, HP Labs Bristol
 * Author email       ian.dickinson@hp.com
 * Package            Jena 2
 * Web                http://sourceforge.net/projects/jena/
 * Created            22-Aug-2003
 * Filename           $RCSfile: Main.java,v $
 * Revision           $Revision: 1.1 $
 * Release status     $State: Exp $
 *
 * Last modified on   $Date: 2003-08-23 00:10:40 $
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
 * @version CVS $Id: Main.java,v 1.1 2003-08-23 00:10:40 ian_dickinson Exp $
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
        OntModel m = ModelFactory.createOntologyModel( OntModelSpec.OWL_MEM, null );
        
        // we have a local copy of the wine ontology
        m.getDocumentManager().addAltEntry( "http://www.w3.org/2001/sw/WebOnt/guide-src/wine", 
                                            "file:testing/reasoners/bugs/wine.owl" );
        m.getDocumentManager().addAltEntry( "http://www.w3.org/2001/sw/WebOnt/guide-src/food", 
                                            "file:testing/reasoners/bugs/food.owl" );
        m.getDocumentManager().addAltEntry( "http://www.w3.org/2001/sw/WebOnt/guide-src/food.owl", 
                                            "file:testing/reasoners/bugs/food.owl" );

        m.read( "http://www.w3.org/2001/sw/WebOnt/guide-src/wine" );
        
        // now list the classes
        DescribeClass dc = new DescribeClass();
        for (Iterator i = m.listClasses();  i.hasNext(); ) {
            dc.describeClass( System.out, (OntClass) i.next() );
        }
    }
    
    
    // Internal implementation methods
    //////////////////////////////////

    //==============================================================================
    // Inner class definitions
    //==============================================================================

}

