/*****************************************************************************
 * Source code information
 * -----------------------
 * Original author    Ian Dickinson, HP Labs Bristol
 * Author email       Ian.Dickinson@hp.com
 * Package            Jena 2
 * Web                http://sourceforge.net/projects/jena/
 * Created            14-Mar-2003
 * Filename           $RCSfile: Path.java,v $
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

import java.util.List;



/**
 * <p>
 * A path encodes a series of edges in the underlying graph as an ordered
 * list of statements.
 * </p>
 *
 * @author Ian Dickinson, HP Labs
 *         (<a  href="mailto:Ian.Dickinson@hp.com" >email</a>)
 * @version CVS $Id: Path.java,v 1.1 2003-03-25 10:11:39 ian_dickinson Exp $
 */
public interface Path {
    // Constants
    //////////////////////////////////


    // External signature methods
    //////////////////////////////////

    /**
     * <p>
     * Answer the value of the path, interpreted as the object of the last
     * statement.
     * </p>
     * 
     * @return The object of the last statement in the path
     */
    public RDFNode getValue();
    
    
    /**
     * <p>
     * Answer an iterator over the statements in the path, starting with the 
     * root node.
     * </p>
     * 
     * @return A statement iterator over the statements in the path, in order
     */
    public StmtIterator iterator();
    
    
    /**
     * <p>
     * Answer the length of the path.
     * </p>
     * 
     * @return The number of statements in the path
     */
    public int length();
    
    
    /**
     * <p>
     * Answer true if the path is empty (has length zero).
     * </p>
     * 
     * @return True if this path is empty
     */
    public boolean isEmpty();
    
    
    /**
     * <p>
     * Answer the path as a list of statements
     * </p>
     * 
     * @return A List whose values are the statements in the path
     */
    public List asList();
    
    
    /**
     * <p>
     * Answer true if any of the statements on the path have <code>s</code>
     * as a subject.
     * </p>
     * 
     * @param s A resource
     * @return True if s is an existing subject on the path.
     */
    public boolean containsSubject( Resource s );
    
    
    /**
     * <p>
     * Answer the i'th statement from the path, where the first statement in
     * the path (closest to the root node) has index 0.
     * </p>
     * 
     * @param i An integer index into the statements on the path
     * @return The i'th statement along the path, from zero.
     * @exception IndexOutOfBoundsException if i is less than zero, or there are not
     * <code>i - 1</code> statements in the path.
     */
    public Statement getStatement( int i );
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

