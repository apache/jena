/*****************************************************************************
 * Source code information
 * -----------------------
 * Original author    Ian Dickinson, HP Labs Bristol
 * Author email       Ian_Dickinson@hp.com
 * Package            Jena
 * Created            29 Jan 2001
 * Filename           $RCSfile: DAMLClassExpression.java,v $
 * Revision           $Revision: 1.1.1.1 $
 * Release status     Preview-release $State: Exp $
 *
 * Last modified on   $Date: 2002-12-19 19:14:49 $
 *               by   $Author: bwm $
 *
 * (c) Copyright Hewlett-Packard Company 2001
 * All rights reserved.
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
 *****************************************************************************/

// Package
///////////////
package com.hp.hpl.jena.ontology.daml;


// Imports
///////////////


/**
 * Unifies different forms of class expression as one type.
 * According to the DAML specification, a class expression can be a named class,
 * a boolean expression, an enumeration or a Restriction.
 *
 * @author Ian Dickinson, HP Labs (<a href="mailto:Ian_Dickinson@hp.com">email</a>)
 * @version CVS info: $Id: DAMLClassExpression.java,v 1.1.1.1 2002-12-19 19:14:49 bwm Exp $
 * @see DAMLClass
 * @see DAMLRestriction
 */
public interface DAMLClassExpression
{
    // External signature methods
    //////////////////////////////////

    /**
     * Answer true if this class expression is an enumeration (i.e. has a property
     * 'oneOf' with a list of values).  This is not an exclusive property, a class
     * expression can be an enumeration at the same time as one of the other kinds
     * of class expression, though the conjunction of these may produce the Nothing
     * class.
     *
     * @return true if this class expression is an enumeration.
     */
    public boolean isEnumeration();


    /**
     * Answer true if this class expression is an named class (i.e. is not an anonymous
     * class expression).  This is not an exclusive property, a class
     * expression can be named at the same time as one of the other kinds
     * of class expression, though the conjunction of these may produce the Nothing
     * class.
     *
     * @return true if this class expression is a named class.
     */
    public boolean isNamedClass();


    /**
     * Answer true if this class expression is an property restriction (i.e. is a
     * Restriction value).  This is not an exclusive property, a class
     * expression can be a property restriction at the same time as one of the other kinds
     * of class expression, though the conjunction of these may produce the Nothing
     * class.
     *
     * @return true if this class expression is a property restriction.
     */
    public boolean isRestriction();


    /**
     * Answer true if this class expression is an boolean intersection of a list
     * of class expressions.  This is not an exclusive property, a class
     * expression can be an intersection at the same time as one of the other kinds
     * of class expression, though the conjunction of these may produce the Nothing
     * class.
     *
     * @return true if this class expression is an intersection.
     */
    public boolean isIntersection();


    /**
     * Answer true if this class expression is a boolean union of a list
     * of class expressions.  This is not an exclusive property, a class
     * expression can be an union at the same time as one of the other kinds
     * of class expression, though the conjunction of these may produce the Nothing
     * class.
     *
     * @return true if this class expression is a union.
     */
    public boolean isUnion();


    /**
     * Answer true if this class expression is a disjoint union of a list
     * of class expressions.  This is not an exclusive property, a class
     * expression can be a disjoint union at the same time as one of the other kinds
     * of class expression, though the conjunction of these may produce the Nothing
     * class.
     *
     * @return true if this class expression is a disjoint union.
     */
    public boolean isDisjointUnion();


    /**
     * Answer true if this class expression is an boolean complement of a list
     * of class expressions.  This is not an exclusive property, a class
     * expression can be an complement at the same time as one of the other kinds
     * of class expression, though the conjunction of these may produce the Nothing
     * class.
     *
     * @return true if this class expression is a complement.
     */
    public boolean isComplement();


}
