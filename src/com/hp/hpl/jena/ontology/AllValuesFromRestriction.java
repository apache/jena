/*****************************************************************************
 * Source code information
 * -----------------------
 * Original author    Ian Dickinson, HP Labs Bristol
 * Author email       Ian.Dickinson@hp.com
 * Package            Jena 2
 * Web                http://sourceforge.net/projects/jena/
 * Created            07-May-2003
 * Filename           $RCSfile: AllValuesFromRestriction.java,v $
 * Revision           $Revision: 1.4 $
 * Release status     $State: Exp $
 *
 * Last modified on   $Date: 2003-08-19 16:50:41 $
 *               by   $Author: ian_dickinson $
 *
 * (c) Copyright 2002-2003, Hewlett-Packard Company, all rights reserved.
 * (see footer for full conditions)
 *****************************************************************************/

// Package
///////////////
package com.hp.hpl.jena.ontology;


// Imports
///////////////
import com.hp.hpl.jena.rdf.model.Resource;


/**
 * <p>
 * A property restriction that requires the named property to have have any range
 * instances to be members of a given class.
 * </p>
 *
 * @author Ian Dickinson, HP Labs
 *         (<a  href="mailto:Ian.Dickinson@hp.com" >email</a>)
 * @version CVS $Id: AllValuesFromRestriction.java,v 1.4 2003-08-19 16:50:41 ian_dickinson Exp $
 */
public interface AllValuesFromRestriction
    extends Restriction 
{
    // Constants
    //////////////////////////////////


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
    public void setAllValuesFrom( Resource cls );

    /**
     * <p>Answer the resource characterising the constraint on all values of the restricted property. This may be
     * a class, the URI of a concrete datatype, a DataRange object or the URI rdfs:Literal.</p>
     * @return A resource, which will have been pre-converted to the appropriate Java value type
     *        ({@link OntClass} or {@link DataRange}) if appropriate.
     * @exception OntProfileException If the {@link Profile#ALL_VALUES_FROM()} property is not supported in the current language profile.   
     */ 
    public Resource getAllValuesFrom();

    /**
     * <p>Answer true if this property restriction has the given class as the class to which all 
     * values of the restricted property must belong.</p>
     * @param cls A class to test 
     * @return True if the given class is the class to which all values must belong
     * @exception OntProfileException If the {@link Profile#ALL_VALUES_FROM()} property is not supported in the current language profile.   
     */
    public boolean hasAllValuesFrom( Resource cls );
    
    /**
     * <p>Remove the statement that this restriction has all values from the given class among
     * the values for the restricted property.  If this statement
     * is not true of the current model, nothing happens.</p>
     * @param cls A Resource the denotes the class to be removed from this restriction
     */
    public void removeAllValuesFrom( Resource cls );
    

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

