/*****************************************************************************
 * Source code information
 * -----------------------
 * Original author    Ian Dickinson, HP Labs Bristol
 * Author email       ian.dickinson@hp.com
 * Package            Jena 2
 * Web                http://sourceforge.net/projects/jena/
 * Created            04-Dec-2003
 * Filename           $RCSfile: SimpleXMLPath.java,v $
 * Revision           $Revision: 1.3 $
 * Release status     $State: Exp $
 *
 * Last modified on   $Date: 2005-02-21 12:20:33 $
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

import com.hp.hpl.jena.util.iterator.*;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;


/**
 * <p>
 * A simple path evaluator for traversing XML DOM trees.  The simplicity arises from
 * handling only a few types of XML nodes: document, element and attribute.  Support
 * for XML namespaces is currently missing.
 * </p>
 *
 * @author Ian Dickinson, HP Labs (<a  href="mailto:Ian.Dickinson@hp.com" >email</a>)
 * @version CVS $Id: SimpleXMLPath.java,v 1.3 2005-02-21 12:20:33 andy_seaborne Exp $
 */
public class SimpleXMLPath 
{
    // Constants
    //////////////////////////////////

    // Static variables
    //////////////////////////////////

    // Instance variables
    //////////////////////////////////

    /** The list of path components that comprises this path */
    protected List m_path = new ArrayList();
    
    // Constructors
    //////////////////////////////////

    /**
     * <p>Construct a simple XML path.  Additional traversals
     * may be added with {@link #append}.
     */
    public SimpleXMLPath() {
        this( false );
    }
    
    
    /**
     * <p>Construct a simple XML path, optionally starting with a 
     * default document path.  Additional traversals
     * may be added with {@link #append}.
     * @param documentRoot If true, the first traversal on the path
     * is to extract the document element from a DOM document node.
     * If false, no initial traversal is created.
     */
    public SimpleXMLPath( boolean documentRoot ) {
        if (documentRoot) {
            appendDocumentPath();
        }
    }
    
    
    // External signature methods
    //////////////////////////////////
    
    /**
     * <p>Append the given traversal to the end of this path, and return
     * the path object so that further traversals can be appended.</p>
     * @param path The path component to add to the end of the path
     * @return The extended path itself
     */
    public SimpleXMLPath append( SimpleXMLPathComponent path ) {
        m_path.add( path );
        return this;
    }
    
    
    /**
     * <p>Convenience method for appending to this path the path component that
     * selects the document element.</p>
     * @return This path itself
     */
    public SimpleXMLPath appendDocumentPath() {
        return append( new SimpleXMLPathDocument() );
    }
    
    
    /**
     * <p>Convenience method for appending to this path the path component that
     * selects the given element.</p>
     * @param elemName The name of the element to select
     * @return This path itself
     */
    public SimpleXMLPath appendElementPath( String elemName ) {
        return append( new SimpleXMLPathElement( elemName ) );
    }
    
    
    /**
     * <p>Convenience method for appending to this path the path component that
     * selects the given attribute.</p>
     * @param attrName The name of the attribute to select
     * @return This path itself
     */
    public SimpleXMLPath appendAttrPath( String attrName ) {
        return append( new SimpleXMLPathAttr( attrName ) );
    }
    
    /**
     * <p>Answer the list of components of this path</p>
     * @return The path components list
     */
    public List getPathComponents() {
        return m_path;
    }
    
    
    /**
     * <p>Answer the i'th component of this path</p>
     * @param i An index, starting from zero
     * @return The simple path component at that index
     */
    public SimpleXMLPathComponent getPathComponent( int i ) {
        return (SimpleXMLPathComponent) m_path.get( i );
    }
    
    
    /**
     * <p>Answer an iterator that traverses this path from the given document 
     * node, and answers all possible values.  This is thus a search evaluation
     * of the path wrt the given document as a starting point.</p>
     * 
     * @param doc The XML document to begin evaluating the path from
     * @return An iterator over all of the leaf values that match this path, starting
     * at doc
     */
    public ExtendedIterator getAll( Document doc ) {
        return WrappedIterator.create( new SimpleXMLPathIterator( this, doc ) );
    }

    
    /**
     * <p>Answer an iterator that traverses this path from the given document 
     * node, and answers all possible values.  This is thus a search evaluation
     * of the path wrt the given document as a starting point.</p>
     * 
     * @param elem The XML document to begin evaluating the path from
     * @return An iterator over all of the leaf values that match this path, starting
     * at elem
     */
    public ExtendedIterator getAll( Element elem ) {
        return WrappedIterator.create( new SimpleXMLPathIterator( this, elem ) );
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
