/*****************************************************************************
 * Source code information
 * -----------------------
 * Original author    Ian Dickinson, HP Labs Bristol
 * Author email       Ian.Dickinson@hp.com
 * Package            Jena 2
 * Web                http://sourceforge.net/projects/jena/
 * Created            14-Mar-2003
 * Filename           $RCSfile: PathFactory.java,v $
 * Revision           $Revision: 1.1 $
 * Release status     $State: Exp $
 *
 * Last modified on   $Date: 2003-03-25 10:11:39 $
 *               by   $Author: ian_dickinson $
 *
 * (c) Copyright 2002-2003, Hewlett-Packard Company, all rights reserved.
 * (see footer for full conditions)
 *****************************************************************************/

// Package
///////////////
package com.hp.hpl.jena.ontology.path;


// Imports
///////////////
import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.ontology.Profile;
import com.hp.hpl.jena.ontology.impl.*;
import com.hp.hpl.jena.ontology.path.impl.*;


/**
 * <p>
 * Factory class for creating path expressions
 * </p>
 *
 * @author Ian Dickinson, HP Labs
 *         (<a  href="mailto:Ian.Dickinson@hp.com" >email</a>)
 * @version CVS $Id: PathFactory.java,v 1.1 2003-03-25 10:11:39 ian_dickinson Exp $
 */
public class PathFactory {
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

    /**
     * <p>
     * Create a path expression that traverses any unit path from a given node.
     * </p>
     * 
     * @return The unit path expression over any edge.
     */
    public static PathExpr unit() {
        return new AnyUnitPathExpr();
    }
    
    
    /**
     * <p>
     * Create a path expression that traverses any unit path labelled with
     * the given named property.
     * </p>
     * 
     * @param p A property
     * @return A PathExpr for the unit path labelled <code>p</code>
     */
    public static PathExpr unit( Property p ) {
        return new NamedUnitPathExpr( p );
    }
    
    
    /**
     * <p>
     * Create a path expression that traverses the path <code>left</code>
     * followed by the path <code>right</code>.
     * </p>
     * 
     * @param left A path expression
     * @param right A path expression
     * @return The path expression <code>left o right</code>
     */
    public static PathExpr compose( PathExpr left, PathExpr right ) {
        return new ComposePathExpr( left, right );
    }
    
    
    /**
     * <p>
     * Create a path expression that traverses the unit path over property 
     * <code>pLeft</code>
     * followed by the unit path over property <code>pRight</code>.
     * </p>
     * 
     * @param pLeft A property
     * @param pRight A property
     * @return The path expression <code>unit(pLeft) o unit(pRight)</code>
     */
    public static PathExpr compose( Property pLeft, Property pRight ) {
        return compose( unit( pLeft ), unit( pRight ) );
    }
    
    
    /**
     * <p>
     * Create a path expression that traverses the path 
     * <code>pLeft</code>
     * followed by the unit path over property <code>p</code>.
     * </p>
     * 
     * @param pLeft A path
     * @param p A property
     * @return The path expression <code>pLeft o unit(p)</code>
     */
    public static PathExpr compose( PathExpr pLeft, Property p ) {
        return compose( pLeft, unit( p ) );
    }
    
    
    /**
     * <p>
     * Create a path expression for the transitive closure of property <code>p</code>.
     * </p>
     * 
     * @param p A transitive property
     * @param occurs If true, an occurs check is performed to ensure that no loops
     * are generated, that would make the transitive closure of a property infinite.
     * @return A PathExpr for the transitive closure of <code>p</code>
     */
    public static PathExpr closure( Property p, boolean occurs ) {
        return new ClosurePathExpr( p, occurs );
    }
    
    
    /**
     * <p>
     * Create a path expression for the transitive closure of property <code>p</code>.
     * Includes occurs check to prevent loops.
     * </p>
     * 
     * @param p A transitive property
     * @return A PathExpr for the transitive closure of <code>p</code>
     * @see #closure(Property, boolean)
     */
    public static PathExpr closure( Property p ) {
        return closure( p, true );
    }
    
    
    /**
     * <p>
     * Create a path expression for any property defined as an annotation under the
     * given language profile.
     * </p>
     * 
     * @param lang An ontology language profile
     * @return A PathExpr for unit paths over any annotation property in <code>lang</code> 
     */
    public static PathExpr annotations( Profile lang ) {
        return null;
    }
    
    
    /**
     * <p>
     * Create a path expression for a unit path over the given property, with the resriction
     * that only directly asserted and not inferred edges will be considered.
     * </p>
     * 
     * @param p A property
     * @return A PathExpr for unit paths p that are asserted and not inferred.
     */
    public static PathExpr direct( Property p ) {
        return null;
    }
    
    
    
    // Internal implementation methods
    //////////////////////////////////

    //==============================================================================
    // Inner class definitions
    //==============================================================================

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
