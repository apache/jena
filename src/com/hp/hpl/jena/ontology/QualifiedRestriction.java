/*****************************************************************************
 * Source code information
 * -----------------------
 * Original author    Ian Dickinson, HP Labs Bristol
 * Author email       ian.dickinson@hp.com
 * Package            Jena 2
 * Web                http://sourceforge.net/projects/jena/
 * Created            08-Sep-2003
 * Filename           $RCSfile: QualifiedRestriction.java,v $
 * Revision           $Revision: 1.4 $
 * Release status     $State: Exp $
 *
 * Last modified on   $Date: 2005-02-21 12:04:42 $
 *               by   $Author: andy_seaborne $
 *
 * (c) Copyright 2001, 2002, 2003, 2004, 2005 Hewlett-Packard Development Company, LP
 * [See end of file]
 *****************************************************************************/

// Package
///////////////
package com.hp.hpl.jena.ontology;


// Imports
///////////////

/**
 * <p>
 * Represents a qualified restriction, in which all values of the restricted property
 * are required to be members of a given class.  At present, this capability is only
 * part of DAML+OIL, not OWL.
 * </p>
 *
 * @author Ian Dickinson, HP Labs
 *         (<a  href="mailto:Ian.Dickinson@hp.com" >email</a>)
 * @version CVS $Id: QualifiedRestriction.java,v 1.4 2005-02-21 12:04:42 andy_seaborne Exp $
 */
public interface QualifiedRestriction 
    extends Restriction
{
    // Constants
    //////////////////////////////////

    // External signature methods
    //////////////////////////////////

    /**
     * <p>Assert that this qualified restriction restricts the property to have a given
     * cardinality and to have values belonging to the class denoted by <code>hasClassQ</code>. 
     * Any existing statements for <code>hasClassQ</code>
     * will be removed.</p>
     * @param cls The class to which all of the value of the restricted property must belong
     * @exception OntProfileException If the {@link Profile#HAS_CLASS_Q()} property is not supported in the current language profile.   
     */ 
    public void setHasClassQ( OntClass cls );

    /**
     * <p>Answer the class to which all values of the restricted property belong.</p>
     * @return The ontology class of the restricted property values 
     * @exception OntProfileException If the {@link Profile#HAS_CLASS_Q()} property is not supported in the current language profile.   
     */ 
    public OntClass getHasClassQ();

    /**
     * <p>Answer true if this qualified property restriction has the given class as
     * the class to which all of the property values must belong.</p>
     * @param cls The class to test against 
     * @return True if the given class is the class to which all members of this restriction must belong
     * @exception OntProfileException If the {@link Profile#HAS_CLASS_Q()} property is not supported in the current language profile.   
     */
    public boolean hasHasClassQ( OntClass cls );
    
    /**
     * <p>Remove the statement that this restriction has the given class 
     * as the class to which all values must belong.  If this statement
     * is not true of the current model, nothing happens.</p>
     * @param cls The ont class that is the object of the <code>hasClassQ</code> property. 
     */
    public void removeHasClassQ( OntClass cls );
    

}

/*
 *  (c) Copyright 2001, 2002, 2003, 2004, 2005 Hewlett-Packard Development Company, LP
 *  All rights reserved.
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
