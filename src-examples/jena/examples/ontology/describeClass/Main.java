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
 * Last modified on   $Date: 2005-10-06 17:49:06 $
 *               by   $Author: andy_seaborne $
 *
 * (c) Copyright 2002, 2003, 2004, 2005 Hewlett-Packard Development Company, LP
 * (see footer for full conditions)
 *****************************************************************************/

// Package
///////////////
package jena.examples.ontology.describeClass;


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
 * @version CVS $Id: Main.java,v 1.1 2005-10-06 17:49:06 andy_seaborne Exp $
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
                                            "file:./testing/reasoners/bugs/wine.owl" );
        m.getDocumentManager().addAltEntry( "http://www.w3.org/TR/2003/CR-owl-guide-20030818/food",
                                            "file:./testing/reasoners/bugs/food.owl" );

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

