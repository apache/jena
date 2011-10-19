/*****************************************************************************
 * Source code information
 * -----------------------
 * Original author    Ian Dickinson, HP Labs Bristol
 * Author email       ian_dickinson@users.sourceforge.net
 * Package            Jena 2
 * Web                http://sourceforge.net/projects/jena/
 * Created            07-May-2003
 * Filename           $RCSfile: AllValuesFromRestrictionImpl.java,v $
 * Revision           $Revision: 1.2 $
 * Release status     $State: Exp $
 *
 * Last modified on   $Date: 2009-10-06 13:04:42 $
 *               by   $Author: ian_dickinson $
 *
 * (c) Copyright 2002, 2003, 2004, 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * (see footer for full conditions)
 *****************************************************************************/

// Package
///////////////
package com.hp.hpl.jena.ontology.impl;



// Imports
///////////////
import com.hp.hpl.jena.enhanced.*;
import com.hp.hpl.jena.graph.*;
import com.hp.hpl.jena.ontology.*;
import com.hp.hpl.jena.rdf.model.Resource;


/**
 * <p>
 * Implementation of the allValuesFrom restriction abstraction.
 * </p>
 *
 * @author Ian Dickinson, HP Labs
 *         (<a  href="mailto:ian_dickinson@users.sourceforge.net" >email</a>)
 * @version CVS $Id: AllValuesFromRestrictionImpl.java,v 1.2 2009-10-06 13:04:42 ian_dickinson Exp $
 */
public class AllValuesFromRestrictionImpl
    extends RestrictionImpl
    implements AllValuesFromRestriction
{
    // Constants
    //////////////////////////////////

    // Static variables
    //////////////////////////////////

    /**
     * A factory for generating AllValuesFromRestriction facets from nodes in enhanced graphs.
     * Note: should not be invoked directly by user code: use
     * {@link com.hp.hpl.jena.rdf.model.RDFNode#as as()} instead.
     */
    @SuppressWarnings("hiding")
    public static Implementation factory = new Implementation() {
        @Override
        public EnhNode wrap( Node n, EnhGraph eg ) {
            if (canWrap( n, eg )) {
                return new AllValuesFromRestrictionImpl( n, eg );
            }
            else {
                throw new ConversionException( "Cannot convert node " + n + " to AllValuesFromRestriction");
            }
        }

        @Override
        public boolean canWrap( Node node, EnhGraph eg ) {
            // node will support being a AllValuesFromRestriction facet if it has rdf:type owl:Restriction or equivalent
            // and the combination of owl:onProperty and owl:allValuesFrom (or equivalents)
            Profile profile = (eg instanceof OntModel) ? ((OntModel) eg).getProfile() : null;
            return (profile != null)  &&  profile.isSupported( node, eg, AllValuesFromRestriction.class );
        }
    };


    // Instance variables
    //////////////////////////////////

    // Constructors
    //////////////////////////////////

    /**
     * <p>
     * Construct a hasValue restriction node represented by the given node in the given graph.
     * </p>
     *
     * @param n The node that represents the resource
     * @param g The enh graph that contains n
     */
    public AllValuesFromRestrictionImpl( Node n, EnhGraph g ) {
        super( n, g );
    }


    // External signature methods
    //////////////////////////////////

    // allValuesFrom

    /**
     * <p>Assert that this restriction restricts the property to have all values
     * be members of the given class. Any existing statements for <code>allValuesFrom</code>
     * will be removed.</p>
     * @param cls The class that all values of the property must belong to
     * @exception OntProfileException If the {@link Profile#ALL_VALUES_FROM()} property is not supported in the current language profile.
     */
    @Override
    public void setAllValuesFrom( Resource cls ) {
        setPropertyValue( getProfile().ALL_VALUES_FROM(), "ALL_VALUES_FROM", cls );
    }

    /**
     * <p>Answer the resource characterising the constraint on all values of the restricted property. This may be
     * a class, the URI of a concrete datatype, a DataRange object or the URI rdfs:Literal.</p>
     * @return A resource, which will have been pre-converted to the appropriate Java value type
     *        ({@link OntClass} or {@link DataRange}) if appropriate.
     * @exception OntProfileException If the {@link Profile#ALL_VALUES_FROM()} property is not supported in the current language profile.
     */
    @Override
    public Resource getAllValuesFrom() {
        checkProfile( getProfile().ALL_VALUES_FROM(), "ALL_VALUES_FROM" );
        Resource r = (Resource) getRequiredProperty( getProfile().ALL_VALUES_FROM() ).getObject();

        boolean currentStrict = ((OntModel) getModel()).strictMode();
        ((OntModel) getModel()).setStrictMode( true );

        try {
            if (r.canAs( OntClass.class )) {
                // all values from a class
                return r.as( OntClass.class );
            }
            else if (r.canAs( DataRange.class )) {
                // all values from a given data range
                return r.as( DataRange.class );
            }
            else {
                // must be a datatype ID or rdfs:Literal
                return r;
            }
        }
        finally {
            ((OntModel) getModel()).setStrictMode( currentStrict );
        }
    }

    /**
     * <p>Answer true if this property restriction has the given class as the class to which all
     * values of the restricted property must belong.</p>
     * @param cls A class to test
     * @return True if the given class is the class to which all values must belong
     * @exception OntProfileException If the {@link Profile#ALL_VALUES_FROM()} property is not supported in the current language profile.
     */
    @Override
    public boolean hasAllValuesFrom( Resource cls ) {
        return hasPropertyValue( getProfile().ALL_VALUES_FROM(), "ALL_VALUES_FROM", cls );
    }

    /**
     * <p>Remove the statement that this restriction has all values from the given class among
     * the values for the restricted property.  If this statement
     * is not true of the current model, nothing happens.</p>
     * @param cls A Resource the denotes the class to be removed from this restriction
     */
    @Override
    public void removeAllValuesFrom( Resource cls ) {
        removePropertyValue( getProfile().ALL_VALUES_FROM(), "ALL_VALUES_FROM", cls );
    }


    // Internal implementation methods
    //////////////////////////////////

    //==============================================================================
    // Inner class definitions
    //==============================================================================

}


/*
    (c) Copyright 2002, 2003, 2004, 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
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


