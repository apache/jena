/*****************************************************************************
 * Source code information
 * -----------------------
 * Original author    Ian Dickinson, HP Labs Bristol
 * Author email       ian.dickinson@hp.com
 * Package            Jena 2
 * Web                http://sourceforge.net/projects/jena/
 * Created            08-Sep-2003
 * Filename           $RCSfile: MinCardinalityQRestriction.java,v $
 * Revision           $Revision: 1.3 $
 * Release status     $State: Exp $
 *
 * Last modified on   $Date: 2004-12-06 13:50:10 $
 *               by   $Author: andy_seaborne $
 *
 * (c) Copyright 2001, 2002, 2003, 2004 Hewlett-Packard Development Company, LP
 * [See end of file]
 *****************************************************************************/

// Package
///////////////
package com.hp.hpl.jena.ontology;


// Imports
///////////////

/**
 * <p>
 * Interface representing the ontology abstraction for a qualified minimum cardinality
 * restriction.   A qualified restriction is a DAML+OIL term for a restriction
 * with a cardinality constraint <em>and</em> a constraint that the values of
 * the restricted property must all belong to the given class.  At the current 
 * time, qualified restrictions are part of DAML+OIL, but not part of OWL.
 * </p>
 *
 * @author Ian Dickinson, HP Labs
 *         (<a  href="mailto:Ian.Dickinson@hp.com" >email</a>)
 * @version CVS $Id: MinCardinalityQRestriction.java,v 1.3 2004-12-06 13:50:10 andy_seaborne Exp $
 */
public interface MinCardinalityQRestriction 
    extends QualifiedRestriction
{
    // Constants
    //////////////////////////////////

    // External signature methods
    //////////////////////////////////

    /**
     * <p>Assert that this restriction restricts the property to have the given
     * minimum cardinality. Any existing statements for <code>minCardinalityQ</code>
     * will be removed.</p>
     * @param minCardinality The minimum cardinality of the restricted property
     * @exception OntProfileException If the {@link Profile#MIN_CARDINALITY_Q()} property is not supported in the current language profile.   
     */ 
    public void setMinCardinalityQ( int minCardinality );

    /**
     * <p>Answer the min qualified cardinality of the restricted property.</p>
     * @return The cardinality of the restricted property
     * @exception OntProfileException If the {@link Profile#MIN_CARDINALITY_Q()} property is not supported in the current language profile.   
     */ 
    public int getMinCardinalityQ();

    /**
     * <p>Answer true if this property restriction has the given minimum qualifed cardinality.</p>
     * @param minCardinality The cardinality to test against 
     * @return True if the given cardinality is the minimum qualified cardinality of the restricted property in this restriction
     * @exception OntProfileException If the {@link Profile#MIN_CARDINALITY_Q()} property is not supported in the current language profile.   
     */
    public boolean hasMinCardinalityQ( int minCardinality );
    
    /**
     * <p>Remove the statement that this restriction has the given minimum qualified cardinality 
     * for the restricted property.  If this statement
     * is not true of the current model, nothing happens.</p>
     * @param minCardinality A cardinality value to be removed from this restriction
     * @exception OntProfileException If the {@link Profile#MIN_CARDINALITY_Q()} property is not supported in the current language profile.   
     */
    public void removeMinCardinalityQ( int minCardinality );
    


}

/*
 *  (c) Copyright 2001, 2002, 2003, 2004 Hewlett-Packard Development Company, LP
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
