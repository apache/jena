/*****************************************************************************
 * Source code information
 * -----------------------
 * Original author    Ian Dickinson, HP Labs Bristol
 * Author email       Ian.Dickinson@hp.com
 * Package            Jena 2
 * Web                http://sourceforge.net/projects/jena/
 * Created            14-Mar-2003
 * Filename           $RCSfile: NamedUnitPathExpr.java,v $
 * Revision           $Revision: 1.4 $
 * Release status     $State: Exp $
 *
 * Last modified on   $Date: 2004-12-06 13:49:41 $
 *               by   $Author: andy_seaborne $
 *
 * (c) Copyright 2002, 2003, 2004 Hewlett-Packard Development Company, LP
 * (see footer for full conditions)
 *****************************************************************************/

// Package
///////////////
package com.hp.hpl.jena.ontology.path.impl;


// Imports
///////////////
import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.ontology.path.*;



/**
 * <p>
 * Path expression for unit (length one) named paths
 * </p>
 *
 * @author Ian Dickinson, HP Labs
 *         (<a  href="mailto:Ian.Dickinson@hp.com" >email</a>)
 * @version CVS $Id: NamedUnitPathExpr.java,v 1.4 2004-12-06 13:49:41 andy_seaborne Exp $
 */
public class NamedUnitPathExpr
    extends AnyUnitPathExpr 
{
    // Constants
    //////////////////////////////////

    // Static variables
    //////////////////////////////////

    // Instance variables
    //////////////////////////////////

    /** The named property that forms this unit path */
    protected Property m_predicate;
    
    
    // Constructors
    //////////////////////////////////

    /**
     * <p>
     * Construct an expression for the unit path with the named predicate as the edge.
     * </p>
     */
    public NamedUnitPathExpr( Property predicate ) {
        m_predicate = predicate;
    }
    
    
    // External signature methods
    //////////////////////////////////

    /**
     * <p>
     * Add the given value to the given root, using this path (if possible).  Not
     * all paths evaluate to a form that makes add possible; in this case an
     * exception is thrown.
     * </p>
     * 
     * @param root The resource that the path is to start from
     * @param value The value to add to the root
     * @exception PathException if this path expression cannot perform an add operation
     */
    public void add( Resource root, RDFNode value ) {
        root.addProperty( m_predicate, value );
    }
    

    /**
     * <p>
     * Evaluate the path expression against the given root, which will result in a set of paths
     * that can be iterated through
     * </p>
     * 
     * @param root
     * @return StmtIterator
     */
    public PathIterator evaluate( Resource root ) {
        return new UnitPathIterator( root.listProperties( m_predicate ) );
    }


    /**
     * <p>
     * Answer the named predicate in this path
     * </p>
     * 
     * @return The property that labels this unit path expression
     */
    public Property getProperty() {
        return m_predicate;
    }
    
    
    // Internal implementation methods
    //////////////////////////////////

    //==============================================================================
    // Inner class definitions
    //==============================================================================


}


/*
    (c) Copyright 2002, 2003, 2004 Hewlett-Packard Development Company, LP
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
