/*****************************************************************************
 * Source code information
 * -----------------------
 * Original author    Ian Dickinson, HP Labs Bristol
 * Author email       Ian.Dickinson@hp.com
 * Package            Jena 2
 * Web                http://sourceforge.net/projects/jena/
 * Created            14-Mar-2003
 * Filename           $RCSfile: AbstractPathExpr.java,v $
 * Revision           $Revision: 1.1 $
 * Release status     $State: Exp $
 *
 * Last modified on   $Date: 2003-03-25 10:11:42 $
 *               by   $Author: ian_dickinson $
 *
 * (c) Copyright 2002-2003, Hewlett-Packard Company, all rights reserved.
 * (see footer for full conditions)
 *****************************************************************************/

// Package
///////////////
package com.hp.hpl.jena.ontology.path.impl;


// Imports
///////////////
import com.hp.hpl.jena.ontology.*;
import com.hp.hpl.jena.ontology.path.*;
import com.hp.hpl.jena.rdf.model.*;

import java.util.*;


/**
 * <p>
 * Base implementation for path expressions
 * </p>
 *
 * @author Ian Dickinson, HP Labs
 *         (<a  href="mailto:Ian.Dickinson@hp.com" >email</a>)
 * @version CVS $Id: AbstractPathExpr.java,v 1.1 2003-03-25 10:11:42 ian_dickinson Exp $
 */
public abstract class AbstractPathExpr
    implements PathExpr
{
    // Constants
    //////////////////////////////////

    // Static variables
    //////////////////////////////////

    // Instance variables
    //////////////////////////////////

    /** Stack of subjects seen so far, to implement occurs check to prevent looping */
    protected List m_occurs = new ArrayList();
    
    
    // Constructor
    //////////////////////////////////

    // External signature methods
    //////////////////////////////////

    /**
     * <p>
     * Default action is to throw an exception ruling out the add operation
     * </p>
     * 
     * @param root The resource that the path is to start from
     * @param value The value to add to the root
     * @exception PathException if this path expression cannot perform an add operation
     */
    public void add( Resource root, RDFNode value ) {
        throw new PathException( "Cannot add to the " + getClass().getName() + " path expression" );
    }

    
    /**
     * <p>
     * For convenience in accessing the contents of a path expression, answer a path set
     * of the paths defined from the given root.  The {@link PathSet} is really just a 
     * set of wrapper functions for {@link #evaluate()}. 
     * </p>
     * 
     * @return A set of the paths this path expression defines from the given root.
     */
    public PathSet asPathSet( Resource root ) {
        return new PathSet( root, this );
    }
    

    // Internal implementation methods
    //////////////////////////////////

    protected void push( List stack, Resource subj ) {
        stack.add( subj );
    }
    
    protected Resource pop( List stack ) {
        return (Resource) stack.remove( stack.size() - 1 );
    }
    
    protected boolean empty( List stack ) {
        return stack.isEmpty();
    }
    
    
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


