/*****************************************************************************
 * Source code information
 * -----------------------
 * Original author    Ian Dickinson, HP Labs Bristol
 * Author email       Ian.Dickinson@hp.com
 * Package            Jena 2
 * Web                http://sourceforge.net/projects/jena/
 * Created            07-May-2003
 * Filename           $RCSfile: HasValueRestriction.java,v $
 * Revision           $Revision: 1.5 $
 * Release status     $State: Exp $
 *
 * Last modified on   $Date: 2003-06-13 19:32:01 $
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
import com.hp.hpl.jena.rdf.model.*;


/**
 * <p>
 * A property restriction that requires the named property to have a given individual as
 * its value. 
 * </p>
 *
 * @author Ian Dickinson, HP Labs
 *         (<a  href="mailto:Ian.Dickinson@hp.com" >email</a>)
 * @version CVS $Id: HasValueRestriction.java,v 1.5 2003-06-13 19:32:01 ian_dickinson Exp $
 */
public interface HasValueRestriction
    extends Restriction 
{
    // Constants
    //////////////////////////////////

    // External signature methods
    //////////////////////////////////

    // hasValue
    
    /**
     * <p>Assert that this restriction restricts the property to have the given
     * value. Any existing statements for <code>hasValue</code>
     * will be removed.</p>
     * @param value The RDF value (an individual or a literal) 
     * that is the value that the restricted property must have to be a member of the
     * class defined by this restriction.
     * @exception OntProfileException If the {@link Profile#HAS_VALUE()} property is not supported in the current language profile.   
     */ 
    public void setHasValue( RDFNode value );

    /**
     * <p>Answer the RDF value that all values of the restricted property must be equal to.</p>
     * @return An RDFNode that is the value of the restricted property
     * @exception OntProfileException If the {@link Profile#HAS_VALUE()} property is not supported in the current language profile.   
     */ 
    public RDFNode getHasValue();

    /**
     * <p>Answer true if this property restriction has the given RDF value as the value which all 
     * values of the restricted property must equal.</p>
     * @param value An RDF value to test 
     * @return True if the given value is the value of the restricted property in this restriction
     * @exception OntProfileException If the {@link Profile#HAS_VALUE()} property is not supported in the current language profile.   
     */
    public boolean hasValue( RDFNode value );
    
    /**
     * <p>Remove the statement that this restriction requires the restricted property to have
     * the given value.  If this statement
     * is not true of the current model, nothing happens.</p>
     * @param value An RDF value that is to be removed as the required value for the restricted property
     */
    public void removeHasValue( RDFNode value );
    

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

