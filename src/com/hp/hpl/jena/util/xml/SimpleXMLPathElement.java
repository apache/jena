/*****************************************************************************
 * Source code information
 * -----------------------
 * Original author    Ian Dickinson, HP Labs Bristol
 * Author email       ian.dickinson@hp.com
 * Package            Jena 2
 * Web                http://sourceforge.net/projects/jena/
 * Created            04-Dec-2003
 * Filename           $RCSfile: SimpleXMLPathElement.java,v $
 * Revision           $Revision: 1.5 $
 * Release status     $State: Exp $
 *
 * Last modified on   $Date: 2005-02-21 12:20:40 $
 *               by   $Author: andy_seaborne $
 *
 * (c) Copyright 2001, 2002, 2003, 2004, 2005 Hewlett-Packard Development Company, LP
 * [See end of file]
 *****************************************************************************/

// Package
///////////////
package com.hp.hpl.jena.util.xml;


// Imports
///////////////
import java.util.*;

import org.w3c.dom.*;


/**
 * <p>
 * An implementation of a simple XML path component that handles named elements.
 * </p>
 *
 * @author Ian Dickinson, HP Labs (<a  href="mailto:Ian.Dickinson@hp.com" >email</a>)
 * @version CVS $Id: SimpleXMLPathElement.java,v 1.5 2005-02-21 12:20:40 andy_seaborne Exp $
 */
public class SimpleXMLPathElement 
    implements SimpleXMLPathComponent
{
    // Constants
    //////////////////////////////////

    /** Constant to select all children of a node */
    public static final String ALL_CHILDREN = "*";
    
    
    // Static variables
    //////////////////////////////////

    // Instance variables
    //////////////////////////////////

    /** The element name we are evaluating */
    protected String m_elemName;
    
    
    // Constructors
    //////////////////////////////////

    /**
     * <p>Construct a simple XML path component that selects a named
     * element from the parent.</p>
     * @param elemName The name of the element to extract
     */
    public SimpleXMLPathElement( String elemName ) {
        m_elemName = elemName;
    }
    
    
    /**
     * <p>Construct a simple XML path component that selects all child
     * elements of the parent node.</p>
     */
    public SimpleXMLPathElement() {
        m_elemName = ALL_CHILDREN;
    }
    
    
    // External signature methods
    //////////////////////////////////

    /**
     * <p>Answer an iterator over all of the values of this path component when
     * evaluated with respect to the given node.</p>
     * @param node The parent node to evaluate against
     * @return An iterator over all of the objects that correspond to evaluating
     * this path against the given node.
     */
    public Iterator getAll( Node node ) {
        // elements should occur within elements
        if (!(node instanceof Element)) {
            throw new IllegalArgumentException( "Tried to get element " + m_elemName + " from a parent node of type " + node.getClass().getName() );
        } 
        
        return new NodeListIterator( ((Element) node).getElementsByTagName( m_elemName ) );
    }
    
    /**
     * <p>Answer the first value for this path expression against the given node.</p>
     * @param node The parent node to evalauate against
     * @return The first object that corresponds to evaluating
     * this path against the given node, or null if there is no such value
     */
    public Object getFirst( Node node ) {
        return getAll( node ).next();
    }


    // Internal implementation methods
    //////////////////////////////////

    //==============================================================================
    // Inner class definitions
    //==============================================================================

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
